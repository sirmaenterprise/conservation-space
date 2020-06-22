package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.sequence.SequenceGeneratorService;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Evaluator class that generates a sequence ids based on templates.
 * <p>
 * The supported expressions are:
 * <ul>
 * <li>{sequenceId} - reference a value from a sequence - gets the current value and does not modify the sequence with
 * the given id.
 * <li>{+sequenceId} - reference a value from a sequence - increments the sequence with the given name and returns the
 * result.
 * <li>{sequenceId+} - reference a value from a sequence - returns the current value and increments the sequence
 * <li>[field_name] - gets the value of a property from the current object instance. If a property is referenced but not
 * found then the evaluator will throw {@link com.sirma.itt.seip.exception.EmfConfigurationException}.
 * <li>(date_pattern) - gets the value after evaluating the date pattern. If the pattern is not valid a
 * {@link com.sirma.itt.seip.exception.EmfConfigurationException} will be thrown.The pattern is based on
 * {@link java.text.SimpleDateFormat}
 * </ul>
 *
 * @see java.text.SimpleDateFormat
 * @see SequenceGeneratorService
 * @author BBonev
 */
@Singleton
public class GenerateIdEvaluator extends BaseEvaluator {
	private static final long serialVersionUID = 2329990157363300649L;

	/**
	 * The expression pattern. The pattern does not validate the internal structure due to non determined structure. The
	 * structure is validated on evaluation
	 */
	private static final Pattern GENERAL_EXPRESSION = Pattern.compile(EXPRESSION_START
			+ "\\{seq\\((.+?)\\)(?:.min\\((?<min>[\\d]+?)\\))?(?:.init\\((?<init>[\\d]+?)\\))?" + FROM_PATTERN + "}");
	private static final Pattern CLEAR_SEQUENCE = Pattern.compile("[{}+]");

	@Inject
	protected SequenceGeneratorService generatorService;

	@Override
	protected Pattern getPattern() {
		return GENERAL_EXPRESSION;
	}

	@Override
	public String getExpressionId() {
		return "seq";
	}

	@Override
	public boolean isCachingSupported() {
		return false;
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String internalExression = matcher.group(1);
		String minimalLenght = matcher.group("min");
		String initialValue = matcher.group("init");
		int startIndex = internalExression.indexOf('[');
		int endIndex = internalExression.lastIndexOf(']');
		if (startIndex >= 0 && endIndex > -1) {
			String propertyKey = CLEAR_SEQUENCE.matcher(internalExression).replaceAll("");
			Serializable serializable = getPropertyFrom(propertyKey, matcher, context, values);
			if (serializable != null) {
				internalExression = internalExression.substring(0, startIndex) + serializable
						+ internalExression.substring(endIndex + 1, internalExression.length());
			}
		}
		if (StringUtils.isNotBlank(initialValue)) {
			BigInteger bigInteger = new BigInteger(initialValue);
			String sequenceId = CLEAR_SEQUENCE.matcher(internalExression).replaceAll("");
			Long currentId = generatorService.getCurrentId(sequenceId);
			if (currentId.compareTo(0L) == 0) {
				generatorService.resetSequenceTo(sequenceId, bigInteger.longValue());
			}
		}
		String nextSequence = generatorService.getNextSequenceByTemplate(internalExression);
		if (StringUtils.isNotBlank(minimalLenght)) {
			BigInteger neededLenght = new BigInteger(minimalLenght);
			BigInteger currentLenght = BigInteger.valueOf(nextSequence.length());
			StringBuilder builder = new StringBuilder(neededLenght.intValue());
			int max = neededLenght.subtract(currentLenght).intValue();
			if (max > 0) {
				for (int i = 0; i < max; i++) {
					builder.append("0");
				}
				builder.append(nextSequence);
				nextSequence = builder.toString();
			}
		}
		return nextSequence;
	}
}
