package com.sirma.itt.objects.services.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.state.BaseCaseTreeStateServiceExtension;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateServiceExtension;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Extension for state service for {@link DocumentInstance} that uses the states of the containing
 * case.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.DOCUMENT)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 14, priority = 5)
public class ObjectDocumentStateServiceExtension extends
		BaseCaseTreeStateServiceExtension<DocumentInstance> {

	/** The initial state. */
	@Inject
	@Config(name = CmfConfigurationProperties.DOCUMENT_STATE_INITIAL, defaultValue = PrimaryStateType.INITIAL)
	private String initialState;
	/** The approved state. */
	@Inject
	@Config(name = CmfConfigurationProperties.DOCUMENT_STATE_APPROVED, defaultValue = PrimaryStateType.APPROVED)
	private String approvedState;
	/** The submitted state. */
	@Inject
	@Config(name = CmfConfigurationProperties.DOCUMENT_STATE_SUBMITTED, defaultValue = PrimaryStateType.SUBMITTED)
	private String submittedState;
	/** The in progress state. */
	@Inject
	@Config(name = CmfConfigurationProperties.DOCUMENT_STATE_IN_PROGRESS, defaultValue = PrimaryStateType.IN_PROGRESS)
	private String inProgressState;
	/** The completed state. */
	@Inject
	@Config(name = CmfConfigurationProperties.DOCUMENT_STATE_COMPLETED, defaultValue = PrimaryStateType.COMPLETED)
	private String completedState;
	/** The deleted state. */
	@Inject
	@Config(name = CmfConfigurationProperties.DOCUMENT_STATE_DELETED, defaultValue = PrimaryStateType.DELETED)
	private String deletedState;
	/** The on hold state. */
	@Inject
	@Config(name = CmfConfigurationProperties.DOCUMENT_STATE_ON_HOLD, defaultValue = PrimaryStateType.ON_HOLD)
	private String onHoldState;
	/** The archived state. */
	@Inject
	@Config(name = CmfConfigurationProperties.DOCUMENT_STATE_ARCHIVED, defaultValue = PrimaryStateType.ARCHIVED)
	private String archivedState;
	/** The cancelled state. */
	@Inject
	@Config(name = CmfConfigurationProperties.DOCUMENT_STATE_STOPPED, defaultValue = PrimaryStateType.CANCELED)
	private String cancelledState;
	@Inject
	@Config(name = CmfConfigurationProperties.DOCUMENT_STATE_CODELIST, defaultValue = "106")
	private Integer stateCodelist;
	/** The state type mapping. */
	private Map<String, String> stateTypeMapping;

	/**
	 * Initialize.
	 */
	@Override
	@PostConstruct
	public void initialize() {
		// moved to external configuration
		stateTypeMapping = new LinkedHashMap<String, String>();
		stateTypeMapping.put(PrimaryStateType.INITIAL, initialState);
		stateTypeMapping.put(PrimaryStateType.ARCHIVED, archivedState);
		stateTypeMapping.put(PrimaryStateType.CANCELED, cancelledState);
		stateTypeMapping.put(PrimaryStateType.COMPLETED, completedState);
		stateTypeMapping.put(PrimaryStateType.DELETED, deletedState);
		stateTypeMapping.put(PrimaryStateType.ON_HOLD, onHoldState);
		stateTypeMapping.put(PrimaryStateType.IN_PROGRESS, inProgressState);
		stateTypeMapping.put(PrimaryStateType.SUBMITTED, submittedState);
		stateTypeMapping.put(PrimaryStateType.APPROVED, approvedState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeState(DocumentInstance instance, Operation operation) {

		if (operation != null) {
			// predefined state have a priority
			boolean isPrimaryUpdated = false;
			if (StringUtils.isNotNullOrEmpty(operation.getNextPrimaryState())) {
				changePrimaryState(instance, operation.getNextPrimaryState());
				isPrimaryUpdated = true;
			}
			if (StringUtils.isNotNullOrEmpty(operation.getNextSecondaryState())) {
				instance.getProperties().put(CaseProperties.SECONDARY_STATE,
						operation.getNextSecondaryState());
			}
			// if we have updated the primary state no need to continue
			// otherwise we still can update it
			if (isPrimaryUpdated) {
				return true;
			}
		}

		String operationType = null;
		if (operation != null) {
			operationType = operation.getOperation();
		}

		String nextStateAutomatically = getNextStateAutomatically(instance, operationType);
		if (StringUtils.isNotNullOrEmpty(nextStateAutomatically)) {
			String string = changePrimaryState(instance, nextStateAutomatically);
			return !EqualsHelper.nullSafeEquals(string, nextStateAutomatically);
		}

		return false;
	}

	@Override
	public String getPrimaryState(DocumentInstance instance) {
		// if we have a state use it or get the case state
		String state = super.getPrimaryState(instance);
		if (StringUtils.isNotNullOrEmpty(state)) {
			return state;
		}
		ObjectInstance objectInstance = InstanceUtil.getParent(ObjectInstance.class, instance);
		if ((objectInstance != null) && (objectInstance.getProperties() != null)) {
			return (String) objectInstance.getProperties().get(DefaultProperties.STATUS);
		}

		// check the state in the case if any
		CaseInstance caseInstance = InstanceUtil.getParent(CaseInstance.class, instance);
		if ((caseInstance != null) && (caseInstance.getProperties() != null)) {
			return (String) caseInstance.getProperties().get(getPrimaryStateProperty());
		}

		return super.getPrimaryState(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<DocumentInstance> getInstanceClass() {
		return DocumentInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPrimaryStateCodelist() {
		return stateCodelist;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPrimaryStateProperty() {
		return DefaultProperties.STATUS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getStateTypeMapping() {
		return stateTypeMapping;
	}
}
