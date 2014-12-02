package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;

/**
 * Expression evaluator that fetches instances relative to the current instance.
 * 
 * @author BBonev
 */
public class FromEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6124103474961197919L;
	private static final Pattern PATTERN = Pattern.compile(EXPRESSION_START + "\\{" + FROM_PATTERN
			+ "\\}");

	@Inject
	private LinkService linkService;

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
		String from = matcher.group(FROM_GROUP);
		String link = matcher.group("link");

		return evaluateFromInternal(from, link, context, values);
	}

	/**
	 * Evaluate from internal.
	 * 
	 * @param key
	 *            the key
	 * @param linkId
	 *            the link
	 * @param context
	 *            the context
	 * @param values
	 *            the values
	 * @return the serializable
	 */
	protected Serializable evaluateFromInternal(String key, String linkId,
			ExpressionContext context, Serializable... values) {
		Instance current = null;
		Serializable serializable = getCurrentInstance(context, values);
		if (serializable instanceof Instance) {
			current = (Instance) serializable;
		}
		if (StringUtils.isNullOrEmpty(key) || "current".equals(key)) {
			return current;
		} else if ("context".equals(key)) {
			return InstanceUtil.getParentContext(current, true);
		} else if ("rootContext".equals(key)) {
			return InstanceUtil.getRootInstance(current, true);
		} else if ("owningInstance".equals(key) && (current instanceof OwnedModel)) {
			return InstanceUtil.getDirectParent(current, true);
		} else if ("parent".equals(key)) {
			return (Serializable) current.getParentElement();
		} else if (key.startsWith("link") && StringUtils.isNotNullOrEmpty(linkId)
				&& (current != null)) {
			List<LinkReference> link = linkService.getLinks(current.toReference(), linkId);
			if (!link.isEmpty()) {
				return linkService.convertToLinkInstance(link.get(0));
			}
			// no such link
			return null;
		}
		throw new EmfConfigurationException("Expression key " + key + " not supported!");
	}
}
