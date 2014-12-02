package com.sirma.itt.emf.scheduler;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;

/**
 * Abstract adapter implementation for the {@link SchedulerAction} interface.
 * 
 * @author BBonev
 */
public abstract class SchedulerActionAdapter implements SchedulerAction {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void beforeExecute(SchedulerContext context) throws Exception {
		List<Pair<String, Class<?>>> input = validateInput();
		if ((context == null) && !input.isEmpty()) {
			throw new EmfConfigurationException("SchedulerContext is required for action "
					+ this.getClass());
		}
		for (Pair<String, Class<?>> pair : input) {
			if ((pair != null) && (pair.getFirst() != null)) {
				Serializable serializable = context.get(pair.getFirst());
				if (serializable == null) {
					throw new EmfConfigurationException("Missing required action argument: "
							+ pair.getFirst());
				}
				if ((pair.getSecond() != null) && !pair.getSecond().isInstance(serializable)) {
					throw new EmfConfigurationException(
							"Incompatible argument types: expected type " + pair.getSecond()
									+ " but got " + serializable.getClass());
				}
			}
		}
	}

	/**
	 * Validate input. If the method returns non empty list then contents will be validated against
	 * the {@link SchedulerContext} in {@link #beforeExecute(SchedulerContext)} method. The context
	 * should contain a entry identified by {@link Pair#getFirst()} and type as
	 * {@link Pair#getSecond()} otherwise exception will be thrown and action will not be executed.
	 * 
	 * @return the list of properties to check
	 */
	protected List<Pair<String, Class<?>>> validateInput() {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(SchedulerContext context) throws Exception {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void afterExecute(SchedulerContext context) throws Exception {
		// nothing to do
	}

}
