package com.sirma.itt.cmf.evaluators;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.evaluation.BaseEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionContext;

/**
 * Evaluator class for outjecting values to case instances from a task instance.
 * <p>
 * <code><pre> ${outject(sourseField, destinationField)} - copy task field to case
 * ${outject([sourceValue], destinationField)} - sets the given value to case field</pre></code>>
 *
 * @author BBonev
 */
public class CaseOutjectEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -140540828543092459L;

	/** The Constant VALUE_EXTRACTION. */
	private static final Pattern VALUE_EXTRACTION = Pattern.compile(EXPRESSION_START
			+ "\\{outject\\((?:(\\w+)|\\[(\\w+)\\])\\s*,\\s*(\\w+)\\)\\}");

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getPattern() {
		return VALUE_EXTRACTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		if ((values == null) || (values.length < 2)) {
			return null;
		}
		AbstractTaskInstance taskInstance = getTypedParameter(values, AbstractTaskInstance.class);
		CaseInstance caseInstance = getTypedParameter(values, CaseInstance.class);
		// we need that argument so if not present then we a done
		if ((caseInstance == null) || (taskInstance == null)) {
			// we could also throw an exception
			return null;
		}

		String sourceField = matcher.group(1);
		String sourceValue = matcher.group(2);
		String destinationField = matcher.group(3);

		Serializable serializable;
		if (StringUtils.isNullOrEmpty(sourceValue)) {
			serializable = taskInstance.getProperties().get(sourceField);
		} else {
			serializable = sourceValue;
		}
		caseInstance.getProperties().put(destinationField, serializable);

		return serializable;
	}

}
