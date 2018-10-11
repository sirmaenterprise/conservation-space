package com.sirma.sep.content.rendition;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.tasks.Schedule;

/**
 * Single timer service to work synchronize/download thumbnails from providers.
 *
 * @author BBonev
 */
@ApplicationScoped
class ThumbnailSynchronizationDataProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long WARNING_TIMEOUT = TimeUnit.HOURS.toMillis(1);

	@Inject
	private ThumbnailDao thumbnailDao;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "thumbnail.loader.enabled", defaultValue = "true", sensitive = true, system = true, type = Boolean.class, label = "Property to enable/disabled the thumbnail loader")
	private ConfigurationProperty<Boolean> thumbnailLoaderEnabled;

	@Inject
	private ThumbnailSyncQueue syncQueue;

	@Inject
	private ThumbnailConfigurations thumbnailConfigurations;

	@Inject
	private Contextual<AtomicLong> lastSuccessAdd;

	/**
	 * Inits the.
	 */
	@PostConstruct
	void initLoader() {
		lastSuccessAdd.initializeWith(AtomicLong::new);
		thumbnailLoaderEnabled.addConfigurationChangeListener(c -> onActiveChange(c.get()));
		thumbnailLoaderEnabled.addValueDestroyListener(c -> onActiveChange(Boolean.FALSE));
	}

	private void onActiveChange(Boolean isActive) {
		if (Boolean.FALSE.equals(isActive)) {
			syncQueue.disable();
			return;
		}
		syncQueue.enable();
	}

	/**
	 * Synch thumbnails.
	 */
	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.AFTER_APP_START, async = true)
	@Schedule(identifier = "ThumbnailSynchronization", system = false)
	@ConfigurationPropertyDefinition(name = "thumbnail.schedule.expression", defaultValue = "0/30 * * ? * *", system = true, label = "Defines the expression when thumbnails should be checked for download. "
			+ "The service will run then to collect non synchronized thumbnails. By default will run every 30 seconds.")
	void synchThumbnails() {
		if (!thumbnailLoaderEnabled.get()) {
			return;
		}
		List<Object[]> list = thumbnailDao
				.getThumbnailsForSynchronization(thumbnailConfigurations.getMaxThumbnailRetryCount());
		if (!list.isEmpty()) {
			long last = lastSuccessAdd.getContextValue().get();
			int added = syncQueue.addAll(list);
			LOGGER.debug("Added {}/{} new items for thumbnail loading", added, list.size());

			if (added > 0) {
				lastSuccessAdd.getContextValue().compareAndSet(last, System.currentTimeMillis());
			} else if (last > 0 && System.currentTimeMillis() - last > WARNING_TIMEOUT) {
				LOGGER.warn(
						"No entries where successfully added for thumbnail loading in the last {} minutes. "
								+ "Probably processing queue is full because thumbnail resolving is really slow. "
								+ "No new thumbnails will be processed unless fixed!",
						TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - last));
			}
		}
	}

}
