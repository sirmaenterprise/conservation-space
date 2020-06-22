package com.sirma.itt.seip.tasks;

import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.DestinationType;

/**
 * Defines the names and definitions of the queues and topics used in the scheduler service
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 15/12/2017
 */
final class SchedulerQueues {

	/**
	 * Queue used to notify for newly added tasks. These tasks will be executed immediately when processed if the
	 * required delay has elapsed
	 */
	@DestinationDef(type = DestinationType.TOPIC)
	static final String TASK_QUEUE = "java:/jms.queue.SchedulerTasksQueue";

	private SchedulerQueues() {
		// constants class
	}
}
