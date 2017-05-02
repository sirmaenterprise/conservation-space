package com.sirma.itt.seip.expressions;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Base test for Expression evaluators with logic to create and populate a {@link ExpressionEvaluatorManager}.
 *
 * @author BBonev
 */
public abstract class BaseEvaluatorTest extends EmfTest {
	/**
	 * Creates the manager.
	 *
	 * @return the expressions manager
	 */
	public ExpressionsManager createManager() {
		ExpressionEvaluatorManager manager = new ExpressionEvaluatorManager();
		TypeConverter converter = createTypeConverter();
		ReflectionUtils.setField(manager, "typeConverter", converter);

		List<ExpressionEvaluator> list = initializeEvaluators(manager, converter);

		InstanceProxyMock<ExpressionEvaluator> evaluators = new InstanceProxyMock<>(list.get(0),
				list.subList(1, list.size()).toArray(new ExpressionEvaluator[list.size() - 1]));

		ReflectionUtils.setField(manager, "evaluators", evaluators);
		ReflectionUtils.setField(manager, "securityContext", securityContext);
		return manager;
	}

	/**
	 * Initialize evaluators.
	 *
	 * @param manager
	 *            the manager
	 * @param converter
	 *            the converter
	 * @return the list
	 */
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionsManager manager, TypeConverter converter) {
		List<ExpressionEvaluator> list = new LinkedList<>();
		EvalEvaluator evalEvaluator = new EvalEvaluator();
		ReflectionUtils.setField(evalEvaluator, "manager", manager);

		list.addAll(Arrays.asList(initEvaluator(new PropertyExpressionEvaluator(), manager, converter),
				initEvaluator(new TodayDateEvaluator(), manager, converter),
				initEvaluator(new FromEvaluator(), manager, converter),
				initEvaluator(new ConditionalExpressionEvaluator(), manager, converter),
				initEvaluator(new VariableGetEvaluator(), manager, converter),
				initEvaluator(new VariableSetEvaluator(), manager, converter),
				initEvaluator(new OrExpressionEvaluator(), manager, converter),
				initEvaluator(new DateEvaluator(), manager, converter),
				initEvaluator(evalEvaluator, manager, converter)));
		return list;
	}

	/**
	 * Initializes the evaluator.
	 *
	 * @param evaluator
	 *            the evaluator
	 * @param manager
	 *            the manager
	 * @param converter
	 *            the converter
	 * @return the expression evaluator
	 */
	public ExpressionEvaluator initEvaluator(ExpressionEvaluator evaluator, ExpressionsManager manager,
			TypeConverter converter) {
		ReflectionUtils.setField(evaluator, "expressionManager", new InstanceProxyMock<>(manager));
		ReflectionUtils.setField(evaluator, "converter", converter);
		ReflectionUtils.setField(evaluator, "userPreferences", userPreferences);
		return evaluator;
	}
}
