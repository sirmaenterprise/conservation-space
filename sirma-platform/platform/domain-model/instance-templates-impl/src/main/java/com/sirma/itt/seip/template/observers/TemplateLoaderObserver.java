package com.sirma.itt.seip.template.observers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.concurrent.locks.ContextualLock;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.template.LoadTemplates;
import com.sirma.itt.seip.template.TemplateService;

/**
 * Event observer for asynchronous template loading/reloading.
 *
 * @author BBonev
 */
@ApplicationScoped
public class TemplateLoaderObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateLoaderObserver.class);
	/** The template service. */
	@Inject
	private TemplateService templateService;

	@Inject
	private TaskExecutor taskExecutor;

	/** Used to restrict multiple simultaneous template loadings. */
	@Inject
	private ContextualLock lock;

	/**
	 * Listens for {@link LoadTemplates} event to initiate a template loading.
	 *
	 * @param event
	 *            the event
	 */
	@RunAsSystem
	void onApplicationLoaded(@Observes LoadTemplates event) {
		taskExecutor.executeAsync(() -> {
			// here we check if someone called a synchronization during
			// asynchronous call
			if (!lock.tryLock()) {
				LOGGER.warn("Template loading in progress: ignoring event request LoadTemplates");
				return null;
			}
			try {
				templateService.reload();
			} finally {
				lock.unlock();
			}
			return null;
		});
	}

	@RunAsAllTenantAdmins
	@Startup(async = true, phase = StartupPhase.BEFORE_APP_START, name = "TemplatesInitialization")
	void loadTemplatesOnStartup() {
		templateService.reload();
	}
}
