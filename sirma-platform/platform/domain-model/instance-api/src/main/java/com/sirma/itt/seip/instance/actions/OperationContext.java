package com.sirma.itt.seip.instance.actions;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.context.Context;

/**
 * Context class for scheduler actions. It's used to pass arguments to the executing action.
 *
 * @author BBonev
 */
public class OperationContext extends Context<Object, Serializable> {

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
	public OperationContext() {
		super();
	}

	/**
	 * Instantiates a new scheduler context.
	 *
	 * @param <M>
	 *            the generic type
	 * @param preferredSize
	 *            the preferred size
	 * @param source
	 *            the source
	 */
	public <M extends Map<Object, Serializable>> OperationContext(int preferredSize, M source) {
		super(preferredSize, source);
	}

	/**
	 * Instantiates a new scheduler context.
	 *
	 * @param preferredSize
	 *            the preferred size
	 */
	public OperationContext(int preferredSize) {
		super(preferredSize);
	}

	/**
	 * Instantiates a new scheduler context.
	 *
	 * @param <M>
	 *            the generic type
	 * @param source
	 *            the source
	 */
	public <M extends Map<Object, Serializable>> OperationContext(M source) {
		super(source);
	}

}
