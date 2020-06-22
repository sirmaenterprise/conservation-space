package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.util.file.FileUtil;

/**
 * Converts bytes to human readable format. For the transformation is used {@link FileUtil#humanReadableByteCount(long)}
 * The format of the expression should be:
 * <p>
 * <b> {@code getReadableFormat(<digits>)}</b>
 * </p>
 *
 * @author A. Kunchev
 */
@Singleton
public class ByteConverterExpressionEvaluator extends BaseEvaluator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long serialVersionUID = -6132150932125053478L;

	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{getReadableFormat.?([-\\w.,]*).?\\}");

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "getReadableFormat";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String value = matcher.group(1);
		if (StringUtils.isBlank(value)) {
			return null;
		}

		try {
			return FileUtil.humanReadableByteCount(Long.valueOf(value));
		} catch (NumberFormatException e) {
			LOGGER.debug("Wrong format, can't convert [{}] to number.", value);
			LOGGER.trace("", e);
			return value;
		}
	}

}
