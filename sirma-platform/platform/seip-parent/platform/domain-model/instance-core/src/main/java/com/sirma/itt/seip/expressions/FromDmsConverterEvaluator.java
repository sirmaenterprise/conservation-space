package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The Converter class from DMS properties.
 *
 * @author BBonev
 */
@Singleton
public class FromDmsConverterEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = -5023175698574284729L;

	private static final Pattern VALUE_EXTRACTION = Pattern.compile(EXPRESSION_START
			+ "\\{from\\.dmsConvert\\(CL(\\d{1,4})\\s*?,\\s*?([\\w]+?)\\s*?,\\s*?(\\w+?)\\s*?\\)\\s*?\\}");

	@Inject
	private CodelistService codelistService;

	@Override
	protected Pattern getPattern() {
		return VALUE_EXTRACTION;
	}

	@Override
	public String getExpressionId() {
		return "from";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		if (values == null || values.length == 0) {
			return null;
		}
		Serializable value = values[0];
		Integer cl = Integer.valueOf(matcher.group(1));
		// the source field
		String from = matcher.group(2);
		// codelist field
		String to = matcher.group(3);
		Map<String, CodeValue> codeValues = codelistService.getCodeValues(cl);
		if ("value".equals(from)) {
			CodeValue codeValue = codeValues.get(value.toString());
			if (codeValue != null) {
				return codeValue.getProperties().get(to);
			}
		} else {
			for (Entry<String, CodeValue> entry : codeValues.entrySet()) {
				if (EqualsHelper.nullSafeEquals((String) entry.getValue().getProperties().get(from), value.toString(),
						true)) {
					if ("value".equals(to)) {
						return entry.getKey();
					}
					return entry.getValue().getProperties().get(to);
				}
			}
		}
		return value;
	}

}
