package com.sirma.itt.seip.permissions.sync.batch;

import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.seip.event.EmfEvent;

/**
 * Event fired to notify for completed chunk or batch job
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/07/2017
 */
public class CompletedDryRunJobProcessingEvent implements EmfEvent {
	private final long executionId;
	private final List<String> data;
	private final boolean isDone;

	/**
	 * Instantiate event
	 *
	 * @param executionId the executed job id
	 * @param data the processed and collected data
	 * @param isDone true if the job has completed and false if the data is intermediate
	 */
	public CompletedDryRunJobProcessingEvent(long executionId, List<String> data, boolean isDone) {
		this.executionId = executionId;
		this.data = new ArrayList<>(data);
		this.isDone = isDone;
	}

	public long getExecutionId() {
		return executionId;
	}

	public List<String> getData() {
		return data;
	}

	public boolean isDone() {
		return isDone;
	}
}
