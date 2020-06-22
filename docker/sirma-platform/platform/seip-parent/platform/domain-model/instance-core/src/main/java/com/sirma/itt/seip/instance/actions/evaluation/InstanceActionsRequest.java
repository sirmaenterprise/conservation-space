package com.sirma.itt.seip.instance.actions.evaluation;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Used as request object for the actions listing for the instances. Instances of this class are build, when there are
 * requests for the instance actions.
 *
 * @author A. Kunchev
 */
public class InstanceActionsRequest {

	private final Instance target;

	private String placeholder;

	public InstanceActionsRequest(final Instance target) {
		this.target = target;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public InstanceActionsRequest setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
		return this;
	}

	public Instance getTargetInstance() {
		return target;
	}
}