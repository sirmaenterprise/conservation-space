package com.sirma.itt.seip.tasks;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.exception.EmfConfigurationException;

/**
 * Abstract adapter implementation for the {@link SchedulerAction} interface.
 *
 * @author BBonev
 */
public abstract class SchedulerActionAdapter implements SchedulerAction {

	@Override
	public void beforeExecute(SchedulerContext context) throws Exception {
		if (context == null) {
			throw new EmfConfigurationException("SchedulerContext is required for action " + this.getClass());
		}
		List<Pair<String, Class<?>>> input = validateInput();
		for (Pair<String, Class<?>> pair : input) {
			if (pair != null && pair.getFirst() != null) {
				Serializable serializable = context.get(pair.getFirst());
				if (serializable == null) {
					throw new EmfConfigurationException("Missing required action argument: " + pair.getFirst());
				}
				if (pair.getSecond() != null && !pair.getSecond().isInstance(serializable)) {
					throw new EmfConfigurationException("Incompatible argument types: expected type " + pair.getSecond()
							+ " but got " + serializable.getClass());
				}
			}
		}
	}

	/**
	 * Validate input. If the method returns non empty list then contents will be validated against the
	 * {@link SchedulerContext} in {@link #beforeExecute(SchedulerContext)} method. The context should contain a entry
	 * identified by {@link Pair#getFirst()} and type as {@link Pair#getSecond()} otherwise exception will be thrown and
	 * action will not be executed.
	 *
	 * @return the list of properties to check
	 */
	protected List<Pair<String, Class<?>>> validateInput() {
		return Collections.emptyList();
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		// nothing to do
	}

	@Override
	public void afterExecute(SchedulerContext context) throws Exception {
		// nothing to do
	}

}
