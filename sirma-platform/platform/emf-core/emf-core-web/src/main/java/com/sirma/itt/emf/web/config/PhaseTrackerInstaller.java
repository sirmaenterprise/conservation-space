package com.sirma.itt.emf.web.config;

import javax.annotation.PostConstruct;
import javax.faces.FactoryFinder;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.inject.Inject;

import com.sirma.itt.emf.web.application.ApplicationConfigurationProvider;
import com.sirma.itt.emf.web.util.PhaseTracker;
import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Conditionally installs a PhaseTracker phase listener.
 *
 * @author Adrian Mitev
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class PhaseTrackerInstaller {

	@Inject
	private ApplicationConfigurationProvider applicationConfiguration;

	private static PhaseTracker phaseTracker = new PhaseTracker();

	/**
	 * Called when the JSF container has started. Conditionally installs a PhaseTracker phase listener if
	 * {@link EmfWebConfigurationProperties#UI_JSF_PHASETRACKER} is set to true.
	 */
	@PostConstruct
	public void init() {
		ConfigurationProperty<Boolean> jsfPhaseTrackerConfiguration = applicationConfiguration
				.getJsfPhaseTrackerConfiguration();
		registerPhaseTracker(jsfPhaseTrackerConfiguration);
		jsfPhaseTrackerConfiguration.addConfigurationChangeListener(PhaseTrackerInstaller::registerPhaseTracker);
	}

	static void registerPhaseTracker(ConfigurationProperty<Boolean> jsfPhaseTrackerConfiguration) {
		LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
		Lifecycle lifecycle = factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
		// register or unregister phase tracker
		if (jsfPhaseTrackerConfiguration.get().booleanValue()) {
			lifecycle.addPhaseListener(phaseTracker);
		} else {
			lifecycle.removePhaseListener(phaseTracker);
		}
	}

}
