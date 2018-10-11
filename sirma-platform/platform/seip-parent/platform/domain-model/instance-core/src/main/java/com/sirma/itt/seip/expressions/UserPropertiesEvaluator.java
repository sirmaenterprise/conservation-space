package com.sirma.itt.seip.expressions;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * Evaluator expression that handles user information extraction
 *
 * @author BBonev
 */
@Singleton
public class UserPropertiesEvaluator extends BaseEvaluator {
	private static final long serialVersionUID = 6339048031811617361L;

	private static final Pattern FIELD_PATTERN = Pattern.compile(
			EXPRESSION_START + "\\{user\\(([\\w\\-\\.@:,]+|\\[[\\w:]+\\]|)\\)\\.?([\\w]+)?" + FROM_PATTERN + "\\}");

	@Inject
	protected ResourceService resourceService;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "user";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String userId = matcher.group(1);
		String property = matcher.group(2);
		if (isPropertyKey(userId)) {
			userId = extractProperty(userId);
			Serializable serializable = getPropertyFrom(userId, matcher, context, values);
			userId = converter.convert(String.class, serializable);
		}
		List<Resource> resources = parseIdentifiers(userId)
				.map(resourceService::findResource)
					.filter(Objects::nonNull)
					.map(Resource.class::cast)
					.collect(Collectors.toList());

		return escape(getUserData(resources, property));
	}

	private static Stream<String> parseIdentifiers(String expressionValue) {
		if (StringUtils.isNotBlank(expressionValue) && !"null".equals(expressionValue)) {
			if (expressionValue.contains(",")) {
				return Stream.of(expressionValue.split(",")).map(String::trim);
			}
			return Stream.of(expressionValue);
		}
		return Stream.empty();
	}

	/**
	 * Gets the user info.
	 *
	 * @param user
	 *            the user id
	 * @param property
	 *            the property
	 * @return the user info
	 */
	@SuppressWarnings("static-method")
	protected Serializable getUserData(Resource user, String property) {
		if (user == null) {
			return null;
		}
		if (StringUtils.isNotBlank(property)) {
			if ("id".equals(property)) {
				return user.getId();
			}
			if ("name".equals(property)) {
				return user.getName();
			}
			Serializable serializable = user.get(property);
			if (serializable != null) {
				return serializable;
			}
		}
		return user.getDisplayName();
	}

	/**
	 * Gets the user info for collection of users. Users data is separated by a comma
	 *
	 * @param users
	 *            the user id
	 * @param property
	 *            the property
	 * @return the user info
	 */
	protected Serializable getUserData(Collection<Resource> users, String property) {
		if (isEmpty(users)) {
			return null;
		}
		return users
				.stream()
					.map(user -> getUserData(user, property))
					.filter(Objects::nonNull)
					.map(String::valueOf)
					.collect(Collectors.joining(", "));
	}

}
