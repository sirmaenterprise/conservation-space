package com.sirma.itt.emf.definition.model;

import com.sirma.itt.emf.domain.model.Identity;

/**
 * Defines a single condition definitions.
 * 
 * @author BBonev
 */
public interface Condition extends Identity {

	/**
	 * If the expression does not evaluate to <code>true</code> how to render the field.
	 * 
	 * @return the render as
	 */
	String getRenderAs();

	/**
	 * Gets the expression that need to be evaluated to <code>true</code> or <code>false</code>.
	 * 
	 * @return the expression to evaluate
	 */
	String getExpression();
}
