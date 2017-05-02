package com.sirma.itt.seip.time.schedule.script;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.time.schedule.DeadlineCalculator;

/**
 * Time scheduler script provider
 *
 * @author Valeri Tishev
 *
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 42)
public class TimeSchedulerScriptProvider implements GlobalBindingsExtension {

	private static final String TIME_SCHEDULER_SCRIPT = "time-scheduler-actions.js";

	@Inject
	private DeadlineCalculator deadlineCalculator;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("timeScheduler", this);
	}

	@Override
	public Collection<String> getScripts() {
		return ResourceLoadUtil.loadResources(getClass(), TIME_SCHEDULER_SCRIPT);
	}

	/**
	 * Calculate the deadline by given start date and duration [days], considering or not workday exclusions.
	 *
	 * @param startDate
	 *            the start date of the time period to be calculated
	 * @param duration
	 *            duration [days] of the time period to be calculated
	 * @param mindWorkdayExclusions
	 *            consider or not workday exclusions
	 * @param startOnWorkdayExclusion
	 *            start or not on a workday exclusion
	 * @param endOnWorkdayExclusion
	 *            end or not on a workday exclusion
	 * @return the deadline date
	 */
	public Date calculateDeadLine(Date startDate, int duration, boolean mindWorkdayExclusions,
			boolean startOnWorkdayExclusion, boolean endOnWorkdayExclusion) {
		return deadlineCalculator.calculateDeadLine(startDate, duration, mindWorkdayExclusions, startOnWorkdayExclusion,
				endOnWorkdayExclusion);
	}

}
