package com.sirma.itt.emf.semantic.evaluator;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.sirma.itt.emf.evaluation.BaseEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Evaluator to convert it's arguments to full or short uri depending on the expression name.
 * 
 * @author BBonev
 */
public class NamespaceEvaluator extends BaseEvaluator {

	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{(toFullUri|toShortUri)\\((.+?)\\)\\}");

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		String mode = matcher.group(1);
		String uri = matcher.group(2);
		if ("toFullUri".equals(mode)) {
			return namespaceRegistryService.buildFullUri(uri);
		}
		return namespaceRegistryService.getShortUri(uri);
	}

}
