package com.sirma.itt.seip.instance.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Abstract state service extension that defines the default generic state mappings and to provide the default change
 * state logic that uses the {@link com.sirma.itt.seip.instance.state.StateTransitionManager} for the next state
 *
 * @author BBonev
 */
public abstract class GenericInstanceStateServiceExtension extends BaseStateServiceExtension {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.initial", defaultValue = PrimaryStates.INITIAL_KEY, label = "The default initial state.")
	private ConfigurationProperty<String> genericInitialState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.approved", defaultValue = PrimaryStates.APPROVED_KEY, label = "The default state approved.")
	private ConfigurationProperty<String> genericApprovedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.submitted", defaultValue = PrimaryStates.SUBMITTED_KEY, label = "The default state submitted.")
	private ConfigurationProperty<String> genericSubmittedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.in_progress", defaultValue = PrimaryStates.IN_PROGRESS_KEY, label = "The default state in progress.")
	private ConfigurationProperty<String> genericInProgressState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.completed", defaultValue = PrimaryStates.COMPLETED_KEY, label = "The default state completed.")
	private ConfigurationProperty<String> genericCompletedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.deleted", defaultValue = PrimaryStates.DELETED_KEY, label = "The default state deleted.")
	private ConfigurationProperty<String> genericDeletedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.on_hold", defaultValue = PrimaryStates.ON_HOLD_KEY, label = "The default state on hold.")
	private ConfigurationProperty<String> genericOnHoldState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.archived", defaultValue = PrimaryStates.ARCHIVED_KEY, label = "The default state archived.")
	private ConfigurationProperty<String> genericArchivedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.stopped", defaultValue = PrimaryStates.CANCELED_KEY, label = "The default state stopped.")
	private ConfigurationProperty<String> genericCancelledState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.codelist", type = Integer.class, defaultValue = "11", label = "The default state codelist")
	private ConfigurationProperty<Integer> genericStateCodelist;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.active", type = Set.class, defaultValue = PrimaryStates.ACTIVE_STATES_CONFIG, label = "Set of active default states")
	private ConfigurationProperty<Set<String>> genericActiveStates;

	/** The state type mapping. */
	private Map<String, ConfigurationProperty<String>> stateTypeMapping;

	/**
	 * Initialize the generic state type mappings
	 */
	@PostConstruct
	public void initialize() {
		// moved to external configuration
		stateTypeMapping = new LinkedHashMap<>();
		stateTypeMapping.put(PrimaryStates.INITIAL_KEY, genericInitialState);
		stateTypeMapping.put(PrimaryStates.ARCHIVED_KEY, genericArchivedState);
		stateTypeMapping.put(PrimaryStates.CANCELED_KEY, genericCancelledState);
		stateTypeMapping.put(PrimaryStates.COMPLETED_KEY, genericCompletedState);
		stateTypeMapping.put(PrimaryStates.DELETED_KEY, genericDeletedState);
		stateTypeMapping.put(PrimaryStates.ON_HOLD_KEY, genericOnHoldState);
		stateTypeMapping.put(PrimaryStates.IN_PROGRESS_KEY, genericInProgressState);
		stateTypeMapping.put(PrimaryStates.SUBMITTED_KEY, genericSubmittedState);
		stateTypeMapping.put(PrimaryStates.APPROVED_KEY, genericApprovedState);
	}

	@Override
	public int getPrimaryStateCodelist() {
		return genericStateCodelist.get().intValue();
	}

	@Override
	protected Set<String> getActiveStates() {
		return genericActiveStates.get();
	}

	@Override
	public Map<String, ConfigurationProperty<String>> getStateTypeMapping() {
		return stateTypeMapping;
	}

}
