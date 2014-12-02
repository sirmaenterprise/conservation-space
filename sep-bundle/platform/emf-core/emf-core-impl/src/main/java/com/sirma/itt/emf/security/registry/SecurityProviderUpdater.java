package com.sirma.itt.emf.security.registry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.definition.event.AllDefinitionsLoaded;
import com.sirma.itt.emf.definition.event.TopLevelDefinitionsLoaded;
import com.sirma.itt.emf.security.model.ActionRegistry;
import com.sirma.itt.emf.security.model.RoleRegistry;

/**
 * A listener class for changes that reflect the contents of the {@link ActionRegistry} and
 * {@link RoleRegistry} and upon detection triggers a reload of both.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class SecurityProviderUpdater {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityProviderUpdater.class);

	/** The action registry. */
	@Inject
	private ActionRegistry actionRegistry;

	/** The role registry. */
	@Inject
	private RoleRegistry roleRegistry;

	/**
	 * Reloads manually actions and roles.
	 */
	public void reload() {
		// first updates actions because the roles depend on the actions to be up ot date
		actionRegistry.reload();

		roleRegistry.reload();
	}

	/**
	 * Post startup event.
	 * 
	 * @param event
	 *            the event
	 */
	public void postStartupEvent(@Observes AllDefinitionsLoaded event) {
		LOGGER.info("Triggered update due to completed definition update/reload");
		reload();
	}

	/**
	 * Post definition loading event.
	 * 
	 * @param event
	 *            the event
	 */
	public void postDefinitionLoadingEvent(@Observes TopLevelDefinitionsLoaded event) {
		LOGGER.info("Triggered update due to completed update/reload of top level definitions");
		reload();
	}

}
