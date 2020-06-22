package com.sirma.itt.seip.instance.revision;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.sep.model.ModelImportCompleted;

/**
 * Observes for {@link ModelImportCompleted}, which is  fired when models are loaded.
 * Schedules operation to create contexts of revisions.
 *
 * @author Boyan Tonchev.
 */
@ApplicationScoped
@Named(RevisionContextsCreator.NAME)
public class RevisionContextsCreator extends SchedulerActionAdapter {

	static final String NAME = "revisionContextsCreator";

	@Inject
	private RevisionService revisionService;

	@Inject
	private SchedulerService schedulerService;

	/**
	 * Observes for {@link ModelImportCompleted}. When event occurred schedules operation to create contexts of revisions.
	 *
	 * @param event - the {@link ModelImportCompleted} event.
	 */
	public void onModelsImportComplete(@Observes ModelImportCompleted event) {
		schedulerService.schedule(NAME, buildConfiguration());
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		revisionService.createRevisionsContexts();
	}

	/**
	 * Builds scheduler configuration for asynchronous action.
	 *
	 * @return builder action configuration.
	 */
	private static SchedulerConfiguration buildConfiguration() {
		return new DefaultSchedulerConfiguration().setType(SchedulerEntryType.TIMED)
				.setScheduleTime(new Date())
				.setRemoveOnSuccess(true)
				.setTransactionMode(TransactionMode.NOT_SUPPORTED)
				.setSynchronous(false)
				.setMaxRetryCount(5);
	}
}
