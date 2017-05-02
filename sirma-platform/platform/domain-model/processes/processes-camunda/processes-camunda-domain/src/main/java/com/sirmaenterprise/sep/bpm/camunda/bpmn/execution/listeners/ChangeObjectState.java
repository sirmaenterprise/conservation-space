package com.sirmaenterprise.sep.bpm.camunda.bpmn.execution.listeners;

import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.TRANSITIONS_NONPERSISTED;
import static com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModel.SequenceFlowModelProperties.KEY_LEAVING_OBJECT_STATE;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.ListenerExpressionUtil;
import com.sirmaenterprise.sep.bpm.camunda.listeners.delegate.CDIJavaDelegate;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModel;
import com.sirmaenterprise.sep.bpm.camunda.util.BPMInstanceUtil;

/**
 * Changes a state of a series of objects based on a parameters provided. Based on story INUC03-UI2-S08.
 *
 * @author bbanchev
 */
@Singleton
public class ChangeObjectState implements CDIJavaDelegate<ChangeObjectState> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private FixedValue relation;
	private Expression source;
	private Expression status;

	@Inject
	private LinkService linkService;
	@Inject
	private InstanceTypeResolver instanceTypeResolver;
	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	public void validateParameters() {
		Objects.requireNonNull(source, "Source of relation is a required argument!");
		Objects.requireNonNull(status, "Target status for related objects is a required argument!");
		Objects.requireNonNull(relation, "Type of relation (relation identifier) is a required argument!");
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	@RunAsSystem
	public void execute(DelegateExecution execution, ChangeObjectState sourceEvent) {
		LOGGER.info("{} Executes listener for execution with id {} on process {}! Paremeters: {}/{}/{}",
				ChangeObjectState.class, execution.getId(), execution.getProcessInstance().getId(), sourceEvent.source,
				sourceEvent.relation, sourceEvent.status);
		String businessId = ListenerExpressionUtil.extractBusinessIdBySourceExpression(execution, sourceEvent.source);
		String statusValue = (String) sourceEvent.status.getValue(execution);
		Objects.requireNonNull(statusValue,
				"Status value cound not be resolved in context! Original value: " + sourceEvent.status);
		// resolve instance since later would be needed check for object properties
		Instance sourceInstance = BPMInstanceUtil.resolveInstance(businessId, instanceTypeResolver);
		String relationId = (String) sourceEvent.relation.getValue(execution);
		Objects.requireNonNull(relationId,
				"Relation identifier cound not be resolved in context! Original value: " + sourceEvent.relation);
		// clean up wrong formatting
		relationId = relationId.trim();
		statusValue = statusValue.trim();

		doStatusChange(businessId, sourceInstance, relationId, statusValue);
	}

	private void doStatusChange(String buisnessId, Instance sourceObject, String relationId, String statusValue) {
		List<LinkReference> links = linkService.getLinks(sourceObject.toReference(), relationId);
		List<String> related = new ArrayList<>(links.size());
		for (LinkReference linkReference : links) {
			InstanceReference relatedTo = linkReference.getTo();
			// check the direction of relation
			if (!buisnessId.equals(relatedTo.getIdentifier())) {
				related.add(relatedTo.getIdentifier());
			} else {
				related.add(linkReference.getFrom().getIdentifier());
			}
		}
		if (related.isEmpty()) {
			LOGGER.warn("No instances are selected for automatic status change! Check your relation: {} to object: {}",
					relationId, sourceObject.getId());
			return;
		}
		// batch load all instances - needed with their loaded properties
		Collection<Instance> resolvedInstances = instanceTypeResolver.resolveInstances(related);
		for (Instance resolved : resolvedInstances) {
			// add uid operation and remove _ as splitter for business id.
			String actionId = "dynamicBPMNStatusChange-" + relationId.replace("_", "-");
			Map<String, Serializable> properties = Collections.singletonMap(KEY_LEAVING_OBJECT_STATE, statusValue);
			SequenceFlowModel buildManualTransition = new SequenceFlowModel(actionId, properties);
			resolved.add(TRANSITIONS_NONPERSISTED, buildManualTransition.serialize());
			domainInstanceService.save(InstanceSaveContext.create(resolved, new Operation(actionId)));
		}
	}

}