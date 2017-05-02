package com.sirma.itt.seip.rule.preconditions;

import javax.inject.Named;

import com.sirma.itt.emf.rule.RuleContext;

/**
 * Check for existing relations of simple or complex to objects of type identified by uri or instance type, in direction
 * of outgoing or incoming.
 *
 * @author BBonev
 */
@Named(RelationNotExistsPrecondition.NO_EXIST_NAME)
public class RelationNotExistsPrecondition extends RelationExistsPrecondition {

	public static final String NO_EXIST_NAME = "relationDoesNotExists";

	@Override
	public String getName() {
		return NO_EXIST_NAME;
	}

	@Override
	public String getPrimaryOperation() {
		return NO_EXIST_NAME;
	}

	@Override
	public boolean isAsyncSupported() {
		return true;
	}

	@Override
	public boolean checkPreconditions(RuleContext processingContext) {
		return !super.checkPreconditions(processingContext);
	}
}
