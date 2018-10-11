package com.sirma.itt.seip.tasks;

import static com.sirma.itt.seip.tasks.SchedulerEntryStatus.CANCELED;
import static com.sirma.itt.seip.tasks.SchedulerEntryStatus.COMPLETED;
import static com.sirma.itt.seip.tasks.SchedulerEntryStatus.FAILED;
import static com.sirma.itt.seip.tasks.SchedulerEntryStatus.NOT_RUN;
import static com.sirma.itt.seip.tasks.SchedulerEntryStatus.PENDING;
import static com.sirma.itt.seip.tasks.SchedulerEntryStatus.RUNNING;
import static com.sirma.itt.seip.tasks.SchedulerEntryStatus.RUN_WITH_ERROR;

import java.lang.invoke.MethodHandles;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;

/**
 * Utility class for some common static functions and constants used in scheduler implementation
 *
 * @author BBonev
 */
class SchedulerUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** Set of scheduler entry statuses that are considered active */
	static final Set<SchedulerEntryStatus> ACTIVE_STASUS = EnumSet.of(NOT_RUN, RUNNING, RUN_WITH_ERROR, PENDING);
	/** Set of scheduler entry statuses that are considered completed or in final state */
	static final Set<SchedulerEntryStatus> COMPLETED_STASUS = EnumSet.of(COMPLETED, CANCELED, FAILED);

	private SchedulerUtil() {
		// nothing to do
	}

	/**
	 * Invokes the given executable and any runtime exceptions throw will only be logged
	 *
	 * @param executable
	 *            the executable
	 */
	static void executeSilently(Executable executable) {
		try {
			executable.execute();
		} catch (RuntimeException e) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(e.getMessage(), e);
			} else {
				LOGGER.warn(e.getMessage());
			}
		}
	}
	
	/**
	 * Invokes the given supplier whilst ignoring any thrown runtime exceptions and only logging
	 * them.
	 * 
	 * @param supplier
	 *            the supplier
	 */
	static <T> T supplySilently(Supplier<T> supplier) {
		try {
			return supplier.get();
		} catch (RuntimeException e) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(e.getMessage(), e);
			} else {
				LOGGER.warn(e.getMessage());
			}
		}
		return null;
	}
}
