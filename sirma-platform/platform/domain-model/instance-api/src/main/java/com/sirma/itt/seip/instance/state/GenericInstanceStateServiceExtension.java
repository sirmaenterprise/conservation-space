package com.sirma.itt.seip.instance.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Abstract state service extension that defines the default generic state mappings and to provide the default change
 * state logic that uses the {@link com.sirma.itt.seip.instance.state.StateTransitionManager} for the next state
 *
 * @param <I>
 *            the generic type
 * @author BBonev
 */
public abstract class GenericInstanceStateServiceExtension<I extends Instance> extends BaseStateServiceExtension<I> {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.initial", defaultValue = PrimaryStates.INITIAL_KEY, label = "The default initial state.")
	private ConfigurationProperty<String> initialState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.approved", defaultValue = PrimaryStates.APPROVED_KEY, label = "The default state approved.")
	private ConfigurationProperty<String> approvedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.submitted", defaultValue = PrimaryStates.SUBMITTED_KEY, label = "The default state submitted.")
	private ConfigurationProperty<String> submittedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.in_progress", defaultValue = PrimaryStates.IN_PROGRESS_KEY, label = "The default state in progress.")
	private ConfigurationProperty<String> inProgressState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.completed", defaultValue = PrimaryStates.COMPLETED_KEY, label = "The default state completed.")
	private ConfigurationProperty<String> completedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.deleted", defaultValue = PrimaryStates.DELETED_KEY, label = "The default state deleted.")
	private ConfigurationProperty<String> deletedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.on_hold", defaultValue = PrimaryStates.ON_HOLD_KEY, label = "The default state on hold.")
	private ConfigurationProperty<String> onHoldState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.archived", defaultValue = PrimaryStates.ARCHIVED_KEY, label = "The default state archived.")
	private ConfigurationProperty<String> archivedState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.stopped", defaultValue = PrimaryStates.CANCELED_KEY, label = "The default state stopped.")
	private ConfigurationProperty<String> cancelledState;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.codelist", type = Integer.class, defaultValue = "11", label = "The default state codelist")
	private ConfigurationProperty<Integer> stateCodelist;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.state.active", type = Set.class, defaultValue = PrimaryStates.ACTIVE_STATES_CONFIG, label = "Set of active default states")
	private ConfigurationProperty<Set<String>> activeStates;

	/** The state type mapping. */
	private Map<String, ConfigurationProperty<String>> stateTypeMapping;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
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
	protected String getPrimaryStateProperty() {
		return DefaultProperties.STATUS;
	}

	@Override
	protected Set<String> getActiveStates() {
		return activeStates.get();
	}

}
