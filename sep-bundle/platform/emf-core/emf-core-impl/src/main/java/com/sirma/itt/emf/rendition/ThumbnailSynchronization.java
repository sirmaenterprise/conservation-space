package com.sirma.itt.emf.rendition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.sirma.itt.emf.concurrent.NonTxAsyncCallableEvent;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.patch.PatchDbService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;

/**
 * Single timer service to work synchronize/download thumbnails from providers.
 * 
 * @author BBonev
 */
@Singleton
@Startup
@DependsOn(PatchDbService.SERVICE_NAME)
@Lock(LockType.READ)
public class ThumbnailSynchronization {

	/** The providers. */
	@Inject
	private javax.enterprise.inject.Instance<ThumbnailProvider> providers;
	/** The timer service. */
	@Resource
	private TimerService timerService;

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The provider mapping. */
	private Map<String, ThumbnailProvider> providerMapping;

	/** The minutes. */
	@Inject
	@Config(name = EmfConfigurationProperties.THUMBNAIL_SCHEDULE_EXPRESSION_MINUTES, defaultValue = "*")
	private String minutes;

	/** The seconds. */
	@Inject
	@Config(name = EmfConfigurationProperties.THUMBNAIL_SCHEDULE_EXPRESSION_SECONDS, defaultValue = "*/30")
	private String seconds;

	/** The max thumbnail retry count. */
	@Inject
	@Config(name = EmfConfigurationProperties.THUMBNAIL_MAX_DOWNLOAD_RETRIES, defaultValue = "5")
	private Integer maxThumbnailRetryCount = 5;
	/** The thumbnail loader parallelism. */
	@Inject
	@Config(name = EmfConfigurationProperties.THUMBNAIL_LOADER_THREADS, defaultValue = "5")
	private Integer thumbnailLoaderParalelism = 5;
	/** The thumbnail loader parallelism threshold. */
	@Inject
	@Config(name = EmfConfigurationProperties.THUMBNAIL_LOADER_PARALLEL_THRESHOLD, defaultValue = "10")
	private Integer thumbnailLoaderParalelismThreshold = 10;
	/** The max page size. */
	@Inject
	@Config(name = EmfConfigurationProperties.THUMBNAIL_LOADER_MAX_PARALLEL_DATA_SIZE, defaultValue = "50")
	private Integer maxPageSize = 50;

	/**
	 * Initializes service instance variables.
	 */
	@PostConstruct
	public void init() {
		providerMapping = new HashMap<String, ThumbnailProvider>(5);
		for (ThumbnailProvider provider : providers) {
			if (!providerMapping.containsKey(provider.getName())) {
				providerMapping.put(provider.getName(), provider);
			} else {
				// duplicate provider name
			}
		}

		ScheduleExpression schedule = new ScheduleExpression();
		schedule.hour("*");
		schedule.minute(minutes);
		schedule.second(seconds);

		// create non-persistent timer configuration
		// and schedule the re-initialization of the registry
		final TimerConfig timerConfig = new TimerConfig(this.getClass().getName(), false);
		timerService.createCalendarTimer(schedule, timerConfig);
	}

	/**
	 * Synch thumbnails.
	 * 
	 * @param timer
	 *            the timer
	 */
	@Timeout
	@Secure(runAsSystem = true)
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void synchThumbnails(Timer timer) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("retries", maxThumbnailRetryCount));
		List<Object[]> list = dbDao.fetchWithNamed(EmfQueries.QUERY_THUMBNAILS_FOR_SYNC_KEY, args);
		if (!list.isEmpty()) {
			if (list.size() < thumbnailLoaderParalelismThreshold) {
				fileLoadingEvent(list);
			} else {
				int page = list.size() / thumbnailLoaderParalelism;
				if ((maxPageSize > 0) && (page > maxPageSize)) {
					page = maxPageSize;
				}
				// fire events for synchronization for the first full pages
				for (int i = 0; i < (thumbnailLoaderParalelism - 1); i++) {
					fileLoadingEvent(list.subList(i * page, (i + 1) * page));
				}
				// fire the remaining elements in the last page
				int lastPageIndex = (thumbnailLoaderParalelism - 1) * page;
				if (list.size() > lastPageIndex) {
					fileLoadingEvent(list.subList(lastPageIndex,
							Math.min(list.size(), lastPageIndex + page)));
				}
			}
		}
	}

	/**
	 * File loading event for the given data
	 * 
	 * @param list
	 *            the list
	 */
	private void fileLoadingEvent(List<Object[]> list) {
		eventService.fire(new NonTxAsyncCallableEvent(new ThumbnailLoader(dbDao,
				providerMapping, maxThumbnailRetryCount, list), SecurityContextManager
				.getCurrentSecurityContext()));
	}
}
