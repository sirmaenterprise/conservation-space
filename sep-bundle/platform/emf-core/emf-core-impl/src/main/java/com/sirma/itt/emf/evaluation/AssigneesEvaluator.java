package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.ResourceService;

/**
 * Expression that returns the assignee users to the current instance.
 * 
 * @author BBonev
 */
public class AssigneesEvaluator extends BaseEvaluator {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 753070861787488965L;

	/** The Constant VALUE_EXTRACTION. */
	private static final Pattern PATTERN = Pattern.compile(EXPRESSION_START + "\\{assignees\\}");

	/** The resource service. */
	@Inject
	private ResourceService resourceService;
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getPattern() {
		return PATTERN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		// TODO: implement count method so not to create and return unused user information
		return resourceService.getResources((Instance) getCurrentInstance(context, values)).size();
	}

}
