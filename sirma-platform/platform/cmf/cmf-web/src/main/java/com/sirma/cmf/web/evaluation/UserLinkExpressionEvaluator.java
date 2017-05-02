package com.sirma.cmf.web.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.web.application.ApplicationConfigurationProvider;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionContextProperties;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * Evaluates expressions for building bookmarkable links for user.
 *
 * @author svelikov
 */
@Singleton
public class UserLinkExpressionEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 838564568489566278L;

	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START + "\\{userLink\\((.+?|)\\)\\}");

	private static final String USER_LINK = "userLink";
	private static final String DEFAULT_LINK = "javascript:void(0)";
	private static final String NULL_VALUE = "null";
	private static final String SLASH = "/";

	@Inject
	private ResourceService resourceService;

	@Inject
	private ApplicationConfigurationProvider applicationConfigurations;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return USER_LINK;
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String userId = matcher.group(1);

		if (ExpressionContextProperties.CURRENT_INSTANCE.equals(userId)) {
			String resourceId = expressionManager.get().evaluateRule("${id.db}", String.class, context, values);
			return buildUserLink(resourceId);
		} else if (StringUtils.isNotNullOrEmpty(userId) && !NULL_VALUE.equals(userId)) {
			Resource resource = resourceService.findResource(userId);
			return buildUserLink(resource);
		}

		return DEFAULT_LINK;
	}

	private String buildUserLink(Resource resource) {
		if (resource == null) {
			return DEFAULT_LINK;
		}

		return buildUserLink(resource.getId().toString());
	}

	private String buildUserLink(String resourceId) {
		StringBuilder userLink = new StringBuilder(applicationConfigurations.getUi2EntityOpenUrl());
		userLink.append(SLASH).append(resourceId);

		return userLink.toString();
	}

}
