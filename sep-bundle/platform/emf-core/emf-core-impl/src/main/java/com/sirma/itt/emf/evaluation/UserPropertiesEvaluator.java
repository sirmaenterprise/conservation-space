package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.User;

/**
 * Evaluator expression that handles user information extraction
 *
 * @author BBonev
 */
public class UserPropertiesEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6339048031811617361L;

	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{user\\(([\\w\\.]+|\\[[\\w]+\\])\\)\\.?([\\w]+)?" + FROM_PATTERN + "\\}");

	@Inject
	private ResourceService resourceService;

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
		String userId = matcher.group(1);
		String property = matcher.group(2);
		if (isPropertyKey(userId)) {
			userId = extractProperty(userId);
			Serializable serializable = getPropertyFrom(userId, matcher, context, values);
			if (serializable != null) {
				userId = serializable.toString();
			}
		}
		Resource resource = null;
		if (StringUtils.isNotNullOrEmpty(userId) && !"null".equals(userId)) {
			resource = resourceService.getResource(userId, ResourceType.USER);
		}
		if (resource == null) {
			return null;
		}
		return getUserData(resource, property);
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
	protected Serializable getUserData(Resource user, String property) {

		if (StringUtils.isNotNullOrEmpty(property)) {
			if ("id".equals(property)) {
				return user.getIdentifier();
			} else if ("tenant".equals(property) && (user instanceof User)) {
				return ((User) user).getTenantId();
			} else {
				Serializable serializable = user.getProperties().get(property);
				if (serializable != null) {
					return serializable;
				}
			}
		}
		return user.getDisplayName();
	}

}
