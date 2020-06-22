package com.sirma.itt.objects.services.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.instance.state.BaseStateServiceExtension;
import com.sirma.itt.seip.instance.state.PrimaryStates;

/**
 * Extension point for state service to support project states management. Abstract impl for object subclasses
 *
 * @author BBonev
 * @author bbanchev
 */

abstract class AbstractObjectStateServiceExtension extends BaseStateServiceExtension {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.initial", defaultValue = PrimaryStates.INITIAL_KEY, label = "The object initial state.")
	private ConfigurationProperty<String> initialState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.approved", defaultValue = PrimaryStates.APPROVED_KEY, label = "The object state approved.")
	private ConfigurationProperty<String> approvedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.submitted", defaultValue = PrimaryStates.SUBMITTED_KEY, label = "The object state submitted.")
	private ConfigurationProperty<String> submittedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.in_progress", defaultValue = PrimaryStates.IN_PROGRESS_KEY, label = "The object state in progress.")
	private ConfigurationProperty<String> inProgressState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.completed", defaultValue = PrimaryStates.COMPLETED_KEY, label = "The object state completed.")
	private ConfigurationProperty<String> completedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.deleted", defaultValue = PrimaryStates.DELETED_KEY, label = "The object state deleted.")
	private ConfigurationProperty<String> deletedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.on_hold", defaultValue = PrimaryStates.ON_HOLD_KEY, label = "The object state on hold.")
	private ConfigurationProperty<String> onHoldState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.archived", defaultValue = PrimaryStates.ARCHIVED_KEY, label = "The object state archived.")
	private ConfigurationProperty<String> archivedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.stopped", defaultValue = PrimaryStates.CANCELED_KEY, label = "The object state stopped.")
	private ConfigurationProperty<String> cancelledState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.rejected", defaultValue = PrimaryStates.REJECTED_KEY, label = "The object rejected state. Used for revisions.")
	private ConfigurationProperty<String> rejectedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.obsolete", defaultValue = PrimaryStates.OBSOLETE_KEY, label = "The object obsolete state. Used for revisions.")
	private ConfigurationProperty<String> obsoleteState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.codelist", type = Integer.class, defaultValue = "11", label = "The object state codelist")
	private ConfigurationProperty<Integer> stateCodelist;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.state.active", type = Set.class, defaultValue = PrimaryStates.ACTIVE_STATES_CONFIG, label = "Set of active object states")
	private ConfigurationProperty<Set<String>> activeStates;

	/** The state type mapping. */
	private Map<String, ConfigurationProperty<String>> stateTypeMapping;

	/**
	 * Initialize state type mappings
	 */
	@PostConstruct
	protected void initialize() {
		// moved to external configuration
		stateTypeMapping = new LinkedHashMap<>();
		stateTypeMapping.put(PrimaryStates.INITIAL_KEY, initialState);
		stateTypeMapping.put(PrimaryStates.ARCHIVED_KEY, archivedState);
		stateTypeMapping.put(PrimaryStates.CANCELED_KEY, cancelledState);
		stateTypeMapping.put(PrimaryStates.COMPLETED_KEY, completedState);
		stateTypeMapping.put(PrimaryStates.DELETED_KEY, deletedState);
		stateTypeMapping.put(PrimaryStates.ON_HOLD_KEY, onHoldState);
		stateTypeMapping.put(PrimaryStates.IN_PROGRESS_KEY, inProgressState);
		stateTypeMapping.put(PrimaryStates.SUBMITTED_KEY, submittedState);
		stateTypeMapping.put(PrimaryStates.APPROVED_KEY, approvedState);
		stateTypeMapping.put(PrimaryStates.REJECTED_KEY, rejectedState);
		stateTypeMapping.put(PrimaryStates.OBSOLETE_KEY, obsoleteState);
	}

	@Override
	public int getPrimaryStateCodelist() {
		return stateCodelist.get().intValue();
	}

	@Override
	public Map<String, ConfigurationProperty<String>> getStateTypeMapping() {
		return stateTypeMapping;
	}

	@Override
	protected Set<String> getActiveStates() {
		return activeStates.get();
	}

}
