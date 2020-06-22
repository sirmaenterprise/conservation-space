package com.sirma.sep.content;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.sep.content.InstanceContentService;

/**
 * Used to schedule content delete after specific time period. Implements {@link SchedulerActionAdapter} and it is
 * scheduled by {@link #NAME}. The context should contain id of the instance, which content should be deleted and the
 * purpose of the content. This class contains methods for building {@link SchedulerConfiguration} and
 * {@link SchedulerContext} specific for this task.
 *
 * @author A. Kunchev
 * @see #buildConfiguration(int, TimeUnit)
 * @see #createContext(Serializable, String)
 * @deprecated No longer needed. Implemented via JMS queue operation. Left here for backward compatibility. Will be
 * removed at later time. Deprecated at 22.12.2017
 */
@Deprecated
@ApplicationScoped
@Named(ScheduleContentDelete.NAME)
public class ScheduleContentDelete extends SchedulerActionAdapter {

	public static final String NAME = "scheduleContentDelete";

	private static final int MAX_RETRIES = 2;

	private static final String TARGET_ID = "target";

	private static final String CONTENT_PURPOSE = "purpose";

	private static final List<Pair<String, Class<?>>> ARGUMENTS_VALIDATION = Arrays
			.asList(new Pair<>(TARGET_ID, Serializable.class), new Pair<>(CONTENT_PURPOSE, String.class));

	@Inject
	private InstanceContentService instanceContentService;

	/**
	 * Creates specific {@link SchedulerContext} for this task. It contains id of the instance, which content should be
	 * deleted and the purpose of the content.
	 *
	 * @param id
	 *            the id of the instance which content will be deleted
	 * @param purpose
	 *            the purpose of the content that will be deleted
	 * @return new {@link SchedulerContext} specific for this task
	 */
	public static SchedulerContext createContext(Serializable id, String purpose) {
		if (id == null || StringUtils.isBlank(purpose)) {
			throw new IllegalArgumentException("The input arguments are required!");
		}

		SchedulerContext context = new SchedulerContext();
		context.put(TARGET_ID, id);
		context.put(CONTENT_PURPOSE, purpose);
		return context;
	}

	/**
	 * Builds new {@link SchedulerConfiguration} with specific delay after which the task will be executed. The
	 * configuration is asynchronous, with max retries <code>2</code> and it is of {@link SchedulerEntryType#TIMED}.
	 * Also the entry is persistent and it will be removed on success.
	 *
	 * @param delay
	 *            the delay before task execution
	 * @param timeUnit
	 *            the {@link TimeUnit} for the delay
	 * @return new {@link SchedulerConfiguration} with specified execution delay
	 */
	public static SchedulerConfiguration buildConfiguration(int delay, TimeUnit timeUnit) {
		return new DefaultSchedulerConfiguration()
				.setType(SchedulerEntryType.TIMED)
					.setRemoveOnSuccess(true)
					.setPersistent(true)
					.setTransactionMode(TransactionMode.REQUIRED)
					.setMaxRetryCount(MAX_RETRIES)
					.setScheduleTime(getExecutionTime(delay, timeUnit));
	}

	private static Date getExecutionTime(int delay, TimeUnit unit) {
		long delayInMillis = unit.toMillis(delay);
		return new Date(Calendar.getInstance().getTimeInMillis() + delayInMillis);
	}

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return ARGUMENTS_VALIDATION;
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		Serializable id = context.getIfSameType(TARGET_ID, Serializable.class);
		String purpose = context.getIfSameType(CONTENT_PURPOSE, String.class);
		instanceContentService.deleteContent(id, purpose);
	}

}
