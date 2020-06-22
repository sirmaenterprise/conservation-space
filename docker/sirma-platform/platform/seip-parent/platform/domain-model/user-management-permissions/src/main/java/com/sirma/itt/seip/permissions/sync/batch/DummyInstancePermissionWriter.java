package com.sirma.itt.seip.permissions.sync.batch;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.event.EventService;

/**
 * Synchronization step that writes a chunk of permissions changes to the semantic database. The implementation
 * handles changes in parent inheritance, library inheritance, permission changes and adds missing permission role
 * definitions. The implementation handles list of {@link PermissionsDiff} instances
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/06/2017
 */
@Named
public class DummyInstancePermissionWriter extends AbstractItemWriter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private EventService eventService;
	@Inject
	private JobContext jobContext;

	private List<String> lastProcessed = new LinkedList<>();
	private List<String> dataStore = new LinkedList<>();

	@Override
	public void writeItems(List<Object> items) throws Exception {
		lastProcessed.clear();

		items.stream()
				.map(PermissionsDiff.class::cast)
				.filter(PermissionsDiff::hasChanges)
				.map(PermissionsDiff::getTargetId)
				.forEach(lastProcessed::add);

		dataStore.addAll(lastProcessed);

		if (!lastProcessed.isEmpty()) {
			LOGGER.info("Found changes in total of {}, current batch:({}):{}", dataStore.size(), lastProcessed.size(),
					lastProcessed);
		}
	}

	@Override
	public Serializable checkpointInfo() throws Exception {
		if (!lastProcessed.isEmpty()) {
			eventService.fire(new CompletedDryRunJobProcessingEvent(jobContext.getExecutionId(), lastProcessed, false));
			lastProcessed.clear();
		}
		return null;
	}

	@Override
	public void close() throws Exception {
		eventService.fire(new CompletedDryRunJobProcessingEvent(jobContext.getExecutionId(), dataStore, true));
	}
}
