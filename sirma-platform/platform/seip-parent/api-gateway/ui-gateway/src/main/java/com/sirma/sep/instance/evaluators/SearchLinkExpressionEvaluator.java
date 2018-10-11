package com.sirma.sep.instance.evaluators;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.web.application.ApplicationConfigurationProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Expression evaluator for bookmarkable instance links. The generated link will point to main search location. As
 * content will holds instance identifier.
 *
 * @author cdimitrov
 */
@Singleton
public class SearchLinkExpressionEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 1L;
	private static final String SLASH = "/";
	private static final String EMPTY_LINK = "javascript:void(0)";
	private static final String SEARCH_LINK = "searchLink";
	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{searchLink\\(([\\w:\\\\.-]+)\\)\\}");

	@Inject
	private ApplicationConfigurationProvider applicationConfigurations;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return SEARCH_LINK;
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String id = matcher.group(1);
		if (StringUtils.isNotBlank(id) && context.containsKey(id)) {
			Instance instance = (Instance) context.get(id);
			return buildSearchLink(instance);
		}
		return EMPTY_LINK;
	}

	protected String buildSearchLink(Instance instance) {
		StringBuilder searchLink = new StringBuilder(applicationConfigurations.getUi2SearchOpenUrl());
		searchLink.append(SLASH).append(instance.getId());
		return searchLink.toString();
	}
}