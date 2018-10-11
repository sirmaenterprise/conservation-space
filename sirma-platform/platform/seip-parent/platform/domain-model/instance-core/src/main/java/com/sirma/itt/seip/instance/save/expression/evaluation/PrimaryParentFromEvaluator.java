/**
 *
 */
package com.sirma.itt.seip.instance.save.expression.evaluation;

import static com.sirma.itt.seip.util.EqualsHelper.equalsTo;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.collections.ComputationChain;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.expressions.FromEvaluator;
import com.sirma.itt.seip.instance.dao.InstanceService;

/**
 * Expression evaluator that is extension of the {@link FromEvaluator} that fetches a primary parent of a given document
 * instance.
 *
 * @author BBonev
 */
@Singleton
public class PrimaryParentFromEvaluator extends FromEvaluator {

	private static final long serialVersionUID = 872957258004412826L;

	private static final String SUPPORTED_PATTERN = "primaryParent";
	private static final Pattern PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{(?:\\.?from\\((?<" + FROM_GROUP + ">" + SUPPORTED_PATTERN + ")\\))?\\}");

	@Inject
	private InstanceService instanceService;

	@Override
	protected Pattern getPattern() {
		return PATTERN;
	}

	@Override
	protected void buildComputationChain(ComputationChain<String, BiFunction<Instance, String, Serializable>> aChain) {
		// no need to call super because we are independent implementation and the only thing we need is this here
		aChain.addStep(equalsTo(SUPPORTED_PATTERN), (inst, link) -> getPrimaryParent(inst));
	}

	@Override
	protected String getAdditionalArg(Matcher matcher) {
		// we does not support second argument
		return null;
	}

	private Serializable getPrimaryParent(Instance inst) {
		InstanceReference primaryParent = instanceService.getPrimaryParent(inst.toReference());
		if (InstanceReference.isValid(primaryParent)) {
			return primaryParent.toInstance();
		}
		return null;
	}
}
