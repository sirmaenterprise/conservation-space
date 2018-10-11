package com.sirma.itt.imports;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.evaluation.BaseEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionContext;

/**
 * Evaluates expressions for building bookmarkable links for user.
 * 
 * @author svelikov
 */
@ApplicationScoped
public class UserLinkExpressionEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 838564568489566278L;

	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{userLink\\((.+?)\\)\\}");

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "userLink";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		String link = "javascript:void(0)";
		// String userId = matcher.group(1);
		// if (StringUtils.isNotBlank(userId) && !"null".equals(userId)) {
		// EmfUser user = peopleService.findUser(userId);
		// }
		return link;
	}

}
