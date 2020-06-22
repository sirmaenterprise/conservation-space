package com.sirma.itt.seip.instance.headers;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;

/**
 * Scheduler task that triggers instance reindexing by calling the {@link InstanceHeaderService#reindexDefinition(String)}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/11/2017
 */
@Named(InstanceHeaderReindexingAction.NAME)
public class InstanceHeaderReindexingAction extends SchedulerActionAdapter {
	public static final String NAME = "InstanceHeaderReindexing";

	private static final String DEFINITION_ID = "definitionId";

	@Inject
	private InstanceHeaderService headerService;

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return Collections.singletonList(new Pair<>(DEFINITION_ID, String.class));
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		String definitionId = context.getIfSameType(DEFINITION_ID, String.class);
		headerService.reindexDefinition(definitionId);
	}

	/**
	 * Build context instance that can be passed when scheduling the current task
	 *
	 * @param definitionId the affected definition identifier
	 * @return the build context that need to be passed to trigger the reindexing
	 */
	public static SchedulerContext buildContext(String definitionId) {
		SchedulerContext context = new SchedulerContext(1);
		context.put(DEFINITION_ID, Objects.requireNonNull(definitionId));
		return context;
	}
}
