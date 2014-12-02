package com.sirma.itt.objects.services.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.state.BaseStateServiceExtension;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateServiceExtension;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.objects.constants.ObjectsConfigProperties;
import com.sirma.itt.objects.domain.ObjectTypesObject;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Extension point for state service to support project states management
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesObject.OBJECT)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 150)
public class ObjectStateServiceExtension extends BaseStateServiceExtension<ObjectInstance> {

	@Inject
	@Config(name = ObjectsConfigProperties.OBJECT_STATE_CODELIST, defaultValue = "11")
	private Integer projectPrimaryState;

	/** The initial state. */
	@Inject
	@Config(name = ObjectsConfigProperties.OBJECT_STATE_INITIAL, defaultValue = PrimaryStateType.INITIAL)
	private String initialState;
	/** The approved state. */
	@Inject
	@Config(name = ObjectsConfigProperties.OBJECT_STATE_APPROVED, defaultValue = PrimaryStateType.APPROVED)
	private String approvedState;
	/** The submitted state. */
	@Inject
	@Config(name = ObjectsConfigProperties.OBJECT_STATE_SUBMITTED, defaultValue = PrimaryStateType.SUBMITTED)
	private String submittedState;
	/** The in progress state. */
	@Inject
	@Config(name = ObjectsConfigProperties.OBJECT_STATE_IN_PROGRESS, defaultValue = PrimaryStateType.IN_PROGRESS)
	private String inProgressState;
	/** The completed state. */
	@Inject
	@Config(name = ObjectsConfigProperties.OBJECT_STATE_COMPLETED, defaultValue = PrimaryStateType.COMPLETED)
	private String completedState;
	/** The deleted state. */
	@Inject
	@Config(name = ObjectsConfigProperties.OBJECT_STATE_DELETED, defaultValue = PrimaryStateType.DELETED)
	private String deletedState;
	/** The on hold state. */
	@Inject
	@Config(name = ObjectsConfigProperties.OBJECT_STATE_ON_HOLD, defaultValue = PrimaryStateType.ON_HOLD)
	private String onHoldState;
	/** The archived state. */
	@Inject
	@Config(name = ObjectsConfigProperties.OBJECT_STATE_ARCHIVED, defaultValue = PrimaryStateType.ARCHIVED)
	private String archivedState;
	/** The cancelled state. */
	@Inject
	@Config(name = ObjectsConfigProperties.OBJECT_STATE_STOPPED, defaultValue = PrimaryStateType.CANCELED)
	private String cancelledState;

	/** The state type mapping. */
	private Map<String, String> stateTypeMapping;

	/**
	 * Initialize.
	 */
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

	@Override
	public boolean changeState(ObjectInstance instance, Operation operation) {
		String nextStateAutomatically = getNextStateAutomatically(instance,
				operation.getOperation());
		String oldState = changePrimaryState(instance, nextStateAutomatically);
		return !EqualsHelper.nullSafeEquals(nextStateAutomatically, oldState);
	}

	@Override
	public int getPrimaryStateCodelist() {
		return projectPrimaryState;
	}

	@Override
	protected String getPrimaryStateProperty() {
		return DefaultProperties.STATUS;
	}

	@Override
	protected Class<ObjectInstance> getInstanceClass() {
		return ObjectInstance.class;
	}

	@Override
	public Map<String, String> getStateTypeMapping() {
		return stateTypeMapping;
	}

}
