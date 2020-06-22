package com.sirma.itt.imports;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.inject.Alternative;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.evaluation.BaseEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Evaluates expressions for building bookmarkable links for instance.
 * 
 * @author svelikov
 */
@Alternative
public class InstanceLinkExpressionEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 838564568489566278L;

	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{link\\(([\\w]+)\\)\\}");

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "link";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {

		String link = "#";
		String instanceKey = matcher.group(1);
		if (StringUtils.isNotBlank(instanceKey) && context.containsKey(instanceKey)) {
			Instance instance = (Instance) context.get(instanceKey);
			link = "/entity/open.jsf?instanceId=" + instance.getId().toString() + "&type="
					+ instance.getClass().getSimpleName().toLowerCase();
		}
		return link;
	}

}
