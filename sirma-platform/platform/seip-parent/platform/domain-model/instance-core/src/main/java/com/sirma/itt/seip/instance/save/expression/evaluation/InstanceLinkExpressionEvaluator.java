package com.sirma.itt.seip.instance.save.expression.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.instance.util.LinkProviderService;

/**
 * Evaluates expressions for building bookmarkable links for instance.
 *
 * @author svelikov
 */
@Singleton
public class InstanceLinkExpressionEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 838564568489566278L;

	private static final String EMPTY_LINK = "javascript:void(0)";
	private static final String LINK = "link";

	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START + "\\{link\\(([\\w:\\\\.-]+)\\)\\}");

	@Inject
	private LinkProviderService linkProviderService;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return LINK;
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String link = EMPTY_LINK;
		String instanceKey = matcher.group(1);
		if (StringUtils.isNotBlank(instanceKey)) {
			if (context.containsKey(instanceKey)) {
				Instance instance = (Instance) context.get(instanceKey);
				if (!instance.isDeleted()) {
					link = buildInstanceLink(instance);
				}
			} else if (instanceKey.indexOf(':') > 0) {
				// if uri use it directly we will resolve the type later
				link = linkProviderService.buildLink(instanceKey);
			}
		}
		return link;
	}

	/**
	 * Build link path based on specific instance.
	 *
	 * @param instance
	 *            current instance
	 * @return link path
	 */
	protected String buildInstanceLink(Instance instance) {
		return linkProviderService.buildLink(instance);
	}
}
