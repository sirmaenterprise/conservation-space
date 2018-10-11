package com.sirma.sep.content.idoc.extract;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.save.event.AfterInstanceSaveEvent;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Observer for {@link AfterInstanceSaveEvent}. When event occurred an action of view content
 * extraction will be scheduled by {@link SchedulerService}.
 *
 * @author Boyan Tonchev.
 */
@ApplicationScoped
public class ViewContentExtractor {

    private static final int MAX_RETRIES = 5;

    @Inject
    private SchedulerService schedulerService;

    /**
     * Schedules an action of view content extraction.
     *
     * @param event
     *         the AfterInstanceSaveEvent.
     */
    void afterInstanceSave(@Observes AfterInstanceSaveEvent event) {
        Instance instance = event.getInstanceToSave();
        Serializable instanceId = instance.getId();
        SchedulerContext context = ScheduleViewContentExtraction.createContext(instanceId);
        SchedulerConfiguration configuration = buildConfiguration();
        schedulerService.schedule(ScheduleViewContentExtraction.NAME, configuration, context);
    }

    /**
     * Build configuration for asynchronous action.
     *
     * @return builder action configuration.
     */
    private static SchedulerConfiguration buildConfiguration() {
        // add 5 seconds delay in order for the instance save transaction to finish properly and the
        // instance cache to be cleared from the stale instance data: Affected issue: CMF-27784
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 5);
        return new DefaultSchedulerConfiguration().setType(SchedulerEntryType.TIMED)
                .setScheduleTime(calendar.getTime())
                .setRemoveOnSuccess(true)
                .setPersistent(true)
                .setTransactionMode(TransactionMode.NOT_SUPPORTED)
                .setMaxRetryCount(MAX_RETRIES)
                .setIncrementalDelay(true)
                .setMaxActivePerGroup(ScheduleViewContentExtraction.NAME, 5);
    }
}
