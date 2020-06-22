package com.sirma.itt.seip.tasks;

import com.sirma.itt.seip.context.Context;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Context class for scheduler actions. It's used to pass arguments to the executing action.
 *
 * @author BBonev
 */
public class SchedulerContext extends Context<Object, Serializable> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 5145620535625537507L;
	/**
	 * When the context is passed to a action method for execution the given key will always point to the entry that is
	 * executed.
	 */
	public static final String SCHEDULER_ENTRY = "%current_scheduler_entry%";

	/**
	 * Instantiates a new scheduler context.
	 */
	public SchedulerContext() {
		super();
	}

	/**
	 * Instantiates a new scheduler context.
	 *
	 * @param <M>
	 * 		the generic type
	 * @param preferredSize
	 * 		the preferred size
	 * @param source
	 * 		the source
	 */
	public <M extends Map<Object, Serializable>> SchedulerContext(int preferredSize, M source) {
		super(preferredSize, source);
	}

	/**
	 * Instantiates a new scheduler context.
	 *
	 * @param preferredSize
	 * 		the preferred size
	 */
	public SchedulerContext(int preferredSize) {
		super(preferredSize);
	}

	/**
	 * Instantiates a new scheduler context.
	 *
	 * @param <M>
	 * 		the generic type
	 * @param source
	 * 		the source
	 */
	public <M extends Map<Object, Serializable>> SchedulerContext(M source) {
		super(source);
	}

	// the methods below are synchronized, sonar detects that there are the same as the super class and mark them as
	// critical issues, so we disable that rule

	@Override
	@SuppressWarnings("squid:S1185")
	public synchronized Serializable put(Object key, Serializable value) {
		return super.put(key, value);
	}

	@Override
	@SuppressWarnings("squid:S1185")
	public synchronized Serializable remove(Object key) {
		return super.remove(key);
	}

	@Override
	@SuppressWarnings("squid:S1185")
	public synchronized Set<Entry<Object, Serializable>> entrySet() {
		return super.entrySet();
	}

	/**
	 * Gets the current scheduler configuration when inside of the {@link com.sirma.itt.seip.tasks.SchedulerAction}
	 *
	 * @return the configuration that was used for triggering the current action
	 */
	public SchedulerConfiguration getConfiguration() {
		SchedulerEntry entry = getIfSameType(SCHEDULER_ENTRY, SchedulerEntry.class);
		if (entry == null) {
			throw new IllegalStateException("Cannot call this method outside of SchedulerAction");
		}
		return entry.getConfiguration();
	}
}
