package com.sirma.itt.emf.web.config;

import javax.annotation.PostConstruct;
import javax.faces.FactoryFinder;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

import com.sirma.itt.emf.web.util.PhaseTracker;

/**
 * Conditionally installs a PhaseTracker phase listener.
 * 
 * @author Adrian Mitev
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class PhaseTrackerInstaller {

	/**
	 * Called when the JSF container has started. Conditionally installs a PhaseTracker phase
	 * listener if {@link EmfWebConfigurationProperties#UI_JSF_PHASETRACKER} is set to true.
	 */
	@PostConstruct
	public void init() {
		Boolean installPhaseTracker = (Boolean) FacesContext.getCurrentInstance()
				.getExternalContext().getApplicationMap()
				.get(EmfWebConfigurationProperties.UI_JSF_PHASETRACKER);
		if (installPhaseTracker) {
			LifecycleFactory factory = (LifecycleFactory) FactoryFinder
					.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
			Lifecycle lifecycle = factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
			lifecycle.addPhaseListener(new PhaseTracker());
		}
	}

}
