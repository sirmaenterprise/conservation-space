package com.sirma.itt.seip.tasks;

/**
 * Event that defines the different schedule entry types.
 *
 * @author BBonev
 */
public enum SchedulerEntryType {

	/** The timed. */
	TIMED, /** The event. */
	EVENT, /** The cron. */
	CRON,
	/**
	 * The immediate entries will be run immediately on schedule and depending on the configuration will be converted to
	 * {@link #TIMED} or {@link #CRON} entry.
	 * <p>
	 * The action will be executed depending on other configuration parameters. For example:
	 * <table border="1" cellpadding="1" cellspacing="1" summary="">
	 * <tr>
	 * <th align="left" id="mode">{@link TransactionMode}</th>
	 * <th align="left" id="persistent">Persistent</th>
	 * <th align="left" id="removeOnSuccess">Remove On Success</th>
	 * <th align="left" id="nextSchedule">Next schedule date</th>
	 * <th align="left" id="cron">Cron expression</th>
	 * <th align="left" id="output">Output {@link SchedulerEntryType}</th>
	 * <th align="left" id="outcome">Outcome</th>
	 * </tr>
	 * <tr>
	 * <td>{@link TransactionMode#REQUIRED}</td>
	 * <td>true</td>
	 * <td>true</td>
	 * <td>In the future</td>
	 * <td>true</td>
	 * <td>{@link SchedulerEntryType#CRON}</td>
	 * <td>Executed and persisted</td>
	 * </tr>
	 * <tr>
	 * <td>{@link TransactionMode#REQUIRED}</td>
	 * <td>true</td>
	 * <td>true</td>
	 * <td>In the future</td>
	 * <td>false</td>
	 * <td>{@link SchedulerEntryType#TIMED}</td>
	 * <td>Executed and persisted</td>
	 * </tr>
	 * <tr>
	 * <td>{@link TransactionMode#REQUIRED}</td>
	 * <td>true</td>
	 * <td>true</td>
	 * <td>In the past</td>
	 * <td>false</td>
	 * <td>{@link SchedulerEntryType#TIMED}</td>
	 * <td>Executed and not persisted</td>
	 * </tr>
	 * <tr>
	 * <td>{@link TransactionMode#REQUIRED}</td>
	 * <td>false</td>
	 * <td>true</td>
	 * <td>In the future</td>
	 * <td>false</td>
	 * <td>{@link SchedulerEntryType#TIMED}</td>
	 * <td>Executed, scheduled and not persisted</td>
	 * </tr>
	 * <tr>
	 * <td>{@link TransactionMode#REQUIRED}</td>
	 * <td>false</td>
	 * <td>true</td>
	 * <td>In the past</td>
	 * <td>false</td>
	 * <td>{@link SchedulerEntryType#TIMED}</td>
	 * <td>Executed and not persisted</td>
	 * </tr>
	 * <tr>
	 * <td>{@link TransactionMode#REQUIRED}</td>
	 * <td>any</td>
	 * <td>true</td>
	 * <td>not set</td>
	 * <td>false</td>
	 * <td>{@link SchedulerEntryType#TIMED}</td>
	 * <td>Executed and not persisted</td>
	 * </tr>
	 * <tr>
	 * <td>{@link TransactionMode#REQUIRED}</td>
	 * <td>any</td>
	 * <td>false</td>
	 * <td>not set</td>
	 * <td>false</td>
	 * <td>{@link SchedulerEntryType#TIMED}</td>
	 * <td>Executed and persisted</td>
	 * </tr>
	 * </table>
	 */
	IMMEDIATE;
}