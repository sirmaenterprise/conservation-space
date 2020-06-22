package com.sirmaenterprise.sep.bpm.camunda.bpmn.execution.listeners;

import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.TRANSITIONS_NONPERSISTED;
import static com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModel.SequenceFlowModelProperties.KEY_LEAVING_OBJECT_STATE;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.sep.bpm.camunda.bpmn.execution.listeners.BaseStatusChangeListener;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.ListenerExpressionUtil;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.listeners.delegate.CDIJavaDelegate;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModel;
import com.sirmaenterprise.sep.bpm.camunda.util.BPMInstanceUtil;

/**
 * Changes a state of a series of objects based on a parameters provided. Based on story INUC03-UI2-S08.
 *
 * @author bbanchev
 */
@Singleton
public class ChangeObjectState extends BaseStatusChangeListener implements CDIJavaDelegate<ChangeObjectState> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private FixedValue relation;
	private Expression source;
	private Expression status;

	@Override
	public void validateParameters() {
		validateParameters(source, status, relation);
	}

	@Override
	@RunAsSystem
	public void execute(DelegateExecution execution, ChangeObjectState changeObjectState) {
		LOGGER.info("Called {} listener for execution with id {} on process {}! Parameters: {}/{}/{}",
					ChangeObjectState.class, execution.getId(), execution.getProcessInstance().getId(),
					changeObjectState.source, changeObjectState.relation, changeObjectState.status);
		String businessId = ListenerExpressionUtil.extractBusinessIdBySourceExpression(execution,
																					   changeObjectState.source);
		String statusValue = (String) changeObjectState.status.getValue(execution);
		Objects.requireNonNull(statusValue, "Status value could not be resolved in context! Original value: "
				+ changeObjectState.status);
		// resolve instance since later would be needed check for object properties
		Instance sourceInstance = BPMInstanceUtil.resolveInstance(businessId, instanceTypeResolver);
		String relationId = (String) changeObjectState.relation.getValue(execution);
		Objects.requireNonNull(relationId, "Relation identifier could not be resolved in context! Original value: "
				+ changeObjectState.relation);
		// clean up wrong formatting
		relationId = relationId.trim();
		statusValue = statusValue.trim();
		doStatusChange(businessId, sourceInstance, relationId, statusValue);

	}

	@Override
	public void save(Instance instance, String actionId, String statusValue) {
		Map<String, Serializable> properties = Collections.singletonMap(KEY_LEAVING_OBJECT_STATE, statusValue);
		SequenceFlowModel buildManualTransition = new SequenceFlowModel(actionId, properties);
		instance.add(TRANSITIONS_NONPERSISTED, buildManualTransition.serialize());
		try {
			domainInstanceService.save(InstanceSaveContext.create(instance, new Operation(actionId)));
		} catch (LockException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CamundaIntegrationRuntimeException("Can not change status of locked document: " + headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_COMPACT));
		}
	}
}
