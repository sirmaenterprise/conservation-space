package com.sirma.itt.seip.expressions;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.instance.save.expression.evaluation.DueDateExpressionEvaluator;
import com.sirma.itt.seip.instance.save.expression.evaluation.InstanceLinkExpressionEvaluator;
import com.sirma.itt.seip.instance.save.expression.evaluation.ObjectPropertyExpressionEvaluator;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * @author Boyan Tonchev.
 */
@RunWith(DataProviderRunner.class)
public class ExpressionEvaluatorTest extends BaseEvaluatorTest {

	private ExpressionsManager expressionsManager;

	@Before
	public void init() {
		expressionsManager = createManager();
	}

	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionsManager manager, TypeConverter converter) {
		List<ExpressionEvaluator> evaluators = super.initializeEvaluators(manager, converter);
		evaluators.add(initEvaluator(new UserPropertiesEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new CodelistEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new LabelExpressionEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new ByteConverterExpressionEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new OrExpressionEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new VariableGetEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new ObjectPropertyExpressionEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new PlainPropertyExpressionEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new InstanceLinkExpressionEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new DueDateExpressionEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new ShortUriEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new CurrentUserEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new FromDmsConverterEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new ToDmsConverterEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new IdEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new ConfigurationEvaluator(), manager, converter));
		evaluators.add(initEvaluator(new ArithmeticEvaluator(), manager, converter));
		return evaluators;
	}

	@Test
	@UseDataProvider("testData")
	public void findEvaluatorTest(String expression, Class<?> expectedExpressionEvaluator) {
		ExpressionEvaluator evaluator = expressionsManager.getEvaluator(expression);
		String errorMessage =
				"Expression evaluator error! Expected: " + getClassName(expectedExpressionEvaluator) + " but found: "
						+ getClassName(evaluator != null ? evaluator.getClass() : null);
		Assert.assertTrue(errorMessage, expectedExpressionEvaluator.isInstance(evaluator));
	}

	@DataProvider
	public static Object[][] testData() {
		return new Object[][] {

				// ToDmsConverterEvaluator
				{"${=2/60}", ArithmeticEvaluator.class},

				// ToDmsConverterEvaluator
				{"${config(help.support.email)}", ConfigurationEvaluator.class},

				// ToDmsConverterEvaluator
				{"${id.uid}", IdEvaluator.class},

				// ToDmsConverterEvaluator
				{"${to.dmsConvert(CL20,extra1,value)}", ToDmsConverterEvaluator.class},

				// FromDmsConverterEvaluator
				{"${from.dmsConvert(CL20,extra1,value)}", FromDmsConverterEvaluator.class},

				// PropertyExpressionEvaluator
				{ "${get([type])}", PropertyExpressionEvaluator.class},
				{ "${get([emf:type])}", PropertyExpressionEvaluator.class},

				// DateEvaluator
				{ "${date([plannedStartDate]).format(dd.MM.yyyy, HH:mm)}", DateEvaluator.class},
				{ "${date([emf:plannedStartDate]).format(dd.MM.yyyy, HH:mm)}", DateEvaluator.class},

				// UserPropertiesEvaluator
				{ "${user([instanceProperty])}", UserPropertiesEvaluator.class},
				{ "${user([emf:instanceProperty])}", UserPropertiesEvaluator.class},

				// CodelistEvaluator
				{ "${CL6(DT0002).bg}", CodelistEvaluator.class},
				{ "${CL4(CS0001).comment}", CodelistEvaluator.class},
				{ "${CL([type])}", CodelistEvaluator.class},
				{ "${CL([emf:type])}", CodelistEvaluator.class},

				// LabelExpressionEvaluator
				{ "${label(lockedBy)}", LabelExpressionEvaluator.class},
				{ "${label(default.favouries.icon)}", LabelExpressionEvaluator.class},
				{ "${label([emf:lockedBy])}", LabelExpressionEvaluator.class},

				// ByteConverterExpressionEvaluator
				{ "${getReadableFormat(some)}", ByteConverterExpressionEvaluator.class},

				// ConditionalExpressionEvaluator
				{ "${if(condition).then(href=\"${link(currentInstance)}\")}", ConditionalExpressionEvaluator.class},
				{ "${if(condition).then(statements).else(statements)}", ConditionalExpressionEvaluator.class},

				// CurrentUserEvaluator
				{ "${currentUser.id}", CurrentUserEvaluator.class},
				{ "${currentUser.name}", CurrentUserEvaluator.class},
				{ "${currentUser.emf:name}", CurrentUserEvaluator.class},

				// ShortUriEvaluator
				{ "${shortUri(instance)}", ShortUriEvaluator.class},

				// DueDateExpressionEvaluator
				{ "${duedate(currentInstance)}", DueDateExpressionEvaluator.class},

				// InstanceLinkExpressionEvaluator
				{ "${link(currentInstance)}", InstanceLinkExpressionEvaluator.class},

				// PlainPropertyExpressionEvaluator
				{ "${getPlain([content])}", PlainPropertyExpressionEvaluator.class},
				{ "${getPlain([emf:content])}", PlainPropertyExpressionEvaluator.class},

				// TodayDateEvaluator
				{ "${today}", TodayDateEvaluator.class},

				// EvalEvaluator
				{ "${eval(some script)}", EvalEvaluator.class},

				// ObjectPropertyExpressionEvaluator
				{ "${objectProperty(some property)}", ObjectPropertyExpressionEvaluator.class},

				// VariableGetEvaluator
				{ "${var.isVersion}", VariableGetEvaluator.class},

				// VariableSetEvaluator
				{ "${var.isVersion= value}", VariableSetEvaluator.class},

				// OrExpressionEvaluator
				{ "${or(true or false)}", OrExpressionEvaluator.class}
		};
	}

	private String getClassName(Class<?> clazz) {
		return clazz != null ? clazz.getSimpleName() : "null";
	}
}
