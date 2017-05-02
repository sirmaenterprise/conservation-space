package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.context.Context;

/**
 * Context used for expression evaluation.
 *
 * @author BBonev
 */
public class ExpressionContext extends Context<String, Serializable> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1330789191490615274L;

	/**
	 * Instantiates a new expression context.
	 */
	public ExpressionContext() {
		super();
	}

	/**
	 * Instantiates a new expression context.
	 *
	 * @param <M>
	 *            the generic type
	 * @param preferredSize
	 *            the preferred size
	 * @param source
	 *            the source
	 */
	public <M extends Map<String, Serializable>> ExpressionContext(int preferredSize, M source) {
		super(preferredSize, source);
	}

	/**
	 * Instantiates a new expression context.
	 *
	 * @param preferredSize
	 *            the preferred size
	 */
	public ExpressionContext(int preferredSize) {
		super(preferredSize);
	}

	/**
	 * Instantiates a new expression context.
	 *
	 * @param <M>
	 *            the generic type
	 * @param source
	 *            the source
	 */
	public <M extends Map<String, Serializable>> ExpressionContext(M source) {
		super(source);
	}

}
