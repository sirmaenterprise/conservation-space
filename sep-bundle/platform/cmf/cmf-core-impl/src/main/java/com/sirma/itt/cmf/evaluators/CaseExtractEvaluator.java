package com.sirma.itt.cmf.evaluators;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.evaluation.BaseEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Extractor class that can handle case extractions.
 * <p>
 * Possible format:<br>
 * <code><pre>${extract(field)}</pre></code> <br>
 * Extract a field from a case instance by name.
 * 
 * @author BBonev
 */
public class CaseExtractEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2212362984373431354L;

	/** The Constant VALUE_EXTRACTION. */
	private static final Pattern VALUE_EXTRACTION = Pattern.compile(EXPRESSION_START
			+ "\\{extract\\((\\w+)\\)\\}");

	@Override
	protected Pattern getPattern() {
		return VALUE_EXTRACTION;
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {

		CaseInstance caseInstance = getTypedParameter(values, CaseInstance.class);
		// we need that argument so if not present then we a done
		if (caseInstance == null) {
			Serializable instance = getCurrentInstance(context, values);
			if (instance instanceof CaseInstance) {
				caseInstance = (CaseInstance) instance;
			} else if (instance instanceof Instance) {
				caseInstance = InstanceUtil.getParent(CaseInstance.class, (Instance) instance);
			}
			if (caseInstance == null) {
				// we could also throw an exception
				return null;
			}
		}
		// the field to copy
		String fieldName = matcher.group(1);

		return caseInstance.getProperties().get(fieldName);
	}

}
