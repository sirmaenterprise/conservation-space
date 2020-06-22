package com.sirma.sep.bpm.camunda.bpmn.execution.listeners;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import javax.inject.Singleton;
import javax.transaction.Transactional;

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
import com.sirmaenterprise.sep.bpm.camunda.bpmn.ListenerExpressionUtil;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.listeners.delegate.CDIJavaDelegate;
import com.sirmaenterprise.sep.bpm.camunda.util.BPMInstanceUtil;

/**
 * Changes a state of a series of objects based on a parameters provided. Based on story BPM-MUT8.
 *
 * @author hlungov
 */
@Singleton
public class StopProcessListener extends BaseStatusChangeListener implements CDIJavaDelegate<StopProcessListener> {

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
	public void execute(DelegateExecution execution, StopProcessListener stopProcessListener) {
		LOGGER.info("Called {} listener for execution with id {} on process {}! Parameters: {}/{}/{}",
					StopProcessListener.class, execution.getId(), execution.getProcessInstance().getId(),
					stopProcessListener.source, stopProcessListener.relation, stopProcessListener.status);
		if (!execution.isCanceled()) {
			return;
		}
		String businessId = ListenerExpressionUtil.extractBusinessIdBySourceExpression(execution, stopProcessListener.source);
		String statusValue = (String) stopProcessListener.status.getValue(execution);
		Objects.requireNonNull(statusValue,
							   "Status value could not be resolved in context! Original value: " + stopProcessListener.status);
		Instance sourceInstance = BPMInstanceUtil.resolveInstance(businessId, instanceTypeResolver);
		String relationId = (String) stopProcessListener.relation.getValue(execution);
		Objects.requireNonNull(relationId,
							   "Relation identifier could not be resolved in context! Original value: " + stopProcessListener.relation);
		relationId = relationId.trim();
		statusValue = statusValue.trim();
		doStatusChange(businessId, sourceInstance, relationId, statusValue);
	}

	@Override
	public void save(Instance instance, String actionId, String statusValue) {
		try {
			domainInstanceService.save(InstanceSaveContext.create(instance, new Operation(actionId, statusValue, statusValue)));
		} catch (LockException e) {
			LOGGER.error(e.getMessage(), e);
			throw new CamundaIntegrationRuntimeException("Can not change status of locked document: " + headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_COMPACT));
		}
	}
}
