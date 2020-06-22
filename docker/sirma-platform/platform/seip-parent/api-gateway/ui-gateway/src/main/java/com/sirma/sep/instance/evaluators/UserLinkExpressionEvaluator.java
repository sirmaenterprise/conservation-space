package com.sirma.sep.instance.evaluators;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

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
		} else if (StringUtils.isNotBlank(userId) && !NULL_VALUE.equals(userId)) {
			// we return link to the first user only, otherwise the link will not be valid
			return readUserIds(userId)
					.map(resourceService::findResource)
					.map(Resource.class::cast)
					.filter(Objects::nonNull)
					.map(this::buildResourceLink)
					.findFirst().orElse(DEFAULT_LINK);
		}

		return DEFAULT_LINK;
	}

	private Stream<String> readUserIds(String identifiers) {
		if (identifiers.contains(",")) {
			return Arrays.stream(identifiers.split(","));
		}
		return Stream.of(identifiers);
	}

	private String buildResourceLink(Resource resource) {
		if (resource == null) {
			return DEFAULT_LINK;
		}

		return buildUserLink(resource.getId().toString());
	}

	private String buildUserLink(String resourceId) {
		return applicationConfigurations.getUi2EntityOpenUrl() + SLASH + resourceId;
	}

}
