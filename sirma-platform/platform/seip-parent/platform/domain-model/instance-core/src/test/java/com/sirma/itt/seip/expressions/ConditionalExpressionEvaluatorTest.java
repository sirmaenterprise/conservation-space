package com.sirma.itt.seip.expressions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionEvaluator;

/**
 * The Class ConditionalExpressionEvaluatorTest.
 *
 * @author BBonev
 */
@Test
public class ConditionalExpressionEvaluatorTest extends BaseEvaluatorTest {

	/** The exp. */
	String exp = "${eval(${if(${extract(private,official[AL210031].type)} <> null).then(${extractFromStructured(official.type[AL210031],taxPaidOfVehiclesCertificateApplication/lotNumberOfTheVehicle)}).else(${if(${extract(private,official[AL210032].type)} <> null).then(${extractFromStructured(official.type[AL210032],dueToPatentTaxServiceApplication/lotNumber)}).else(${if(${extract(private,official[AL210033].type)} <> null).then(${extractFromStructured(official.type[AL210033],taxPaidOnPropertyAndGarbageApplication/propertyLotNumber)}).else(${if(${extract(private,official[AL210034].type)} <> null).then(${extractFromStructured(official.type[AL210034],PayInheritanceTaxServiceApplication/lotNumber)}).else(${if(${extract(private,official[AL210030].type)} <> null).then(${extractFromStructured(official.type[AL210030],taxAssesmentOfPropertyCertificateApplication/realEstateBatchNumber)}).else(Unclaimed)})})})})})}";

	/** The extract evaluator. */
	@Mock
	ExpressionEvaluator extractEvaluator;

	/** The extract from structured evaluator. */
	@Mock
	ExpressionEvaluator extractFromStructuredEvaluator;

	/**
	 * Before method.
	 */
	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		when(extractEvaluator.getExpressionId()).thenReturn("extract");
		when(extractEvaluator.canHandle("${extract(private,official[AL210031].type)}")).thenReturn(true);
		when(extractEvaluator.canHandle("${extract(private,official[AL210032].type)}")).thenReturn(true);
		when(extractEvaluator.canHandle("${extract(private,official[AL210033].type)}")).thenReturn(true);
		when(extractEvaluator.canHandle("${extract(private,official[AL210034].type)}")).thenReturn(true);
		when(extractEvaluator.canHandle("${extract(private,official[AL210030].type)}")).thenReturn(true);

		when(extractFromStructuredEvaluator.getExpressionId()).thenReturn("extractFromStructured");
		when(extractFromStructuredEvaluator.canHandle(
				"${extractFromStructured(official.type[AL210031],taxPaidOfVehiclesCertificateApplication/lotNumberOfTheVehicle)}"))
		.thenReturn(true);
		when(extractFromStructuredEvaluator.canHandle(
				"${extractFromStructured(official.type[AL210030],taxAssesmentOfPropertyCertificateApplication/realEstateBatchNumber)}"))
		.thenReturn(true);
		when(extractFromStructuredEvaluator.canHandle(
				"${extractFromStructured(official.type[AL210032],dueToPatentTaxServiceApplication/lotNumber)}"))
		.thenReturn(true);
		when(extractFromStructuredEvaluator.canHandle(
				"${extractFromStructured(official.type[AL210033],taxPaidOnPropertyAndGarbageApplication/propertyLotNumber)}"))
		.thenReturn(true);
		when(extractFromStructuredEvaluator.canHandle(
				"${extractFromStructured(official.type[AL210034],PayInheritanceTaxServiceApplication/lotNumber)}"))
		.thenReturn(true);
	}

	/**
	 * Complex expression_level_ n.
	 *
	 * @param level
	 *            the level
	 * @param ifExpression
	 *            the if expression
	 * @param thenExpression
	 *            the then expression
	 */
	@Test(dataProvider = "expressionProvider")
	public void complexExpression_level_N(Integer level, String ifExpression, String thenExpression) {
		when(extractEvaluator.evaluate(eq(ifExpression), any(ExpressionContext.class))).thenReturn("someValue");
		String expectedResult = "level " + level + " extractedValue";
		when(extractFromStructuredEvaluator.evaluate(eq(thenExpression), any(ExpressionContext.class)))
		.thenReturn(expectedResult);

		ExpressionContext expressionContext = new ExpressionContext();
		String rule = createManager().evaluateRule(exp, String.class, expressionContext);
		assertEquals(rule, expectedResult);
	}

	/**
	 * Expression provider.
	 *
	 * @return the object[][]
	 */
	@DataProvider(name = "expressionProvider")
	public Object[][] expressionProvider() {
		return new Object[][] {
			{ 1, "${extract(private,official[AL210031].type)}",
			"${extractFromStructured(official.type[AL210031],taxPaidOfVehiclesCertificateApplication/lotNumberOfTheVehicle)}" },
			{ 2, "${extract(private,official[AL210030].type)}",
			"${extractFromStructured(official.type[AL210030],taxAssesmentOfPropertyCertificateApplication/realEstateBatchNumber)}" },
			{ 3, "${extract(private,official[AL210032].type)}",
			"${extractFromStructured(official.type[AL210032],dueToPatentTaxServiceApplication/lotNumber)}" },
			{ 4, "${extract(private,official[AL210033].type)}",
			"${extractFromStructured(official.type[AL210033],taxPaidOnPropertyAndGarbageApplication/propertyLotNumber)}" },
			{ 5, "${extract(private,official[AL210034].type)}",
			"${extractFromStructured(official.type[AL210034],PayInheritanceTaxServiceApplication/lotNumber)}" } };
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
	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionsManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> evaluators = super.initializeEvaluators(manager, converter);
		evaluators.add(extractEvaluator);
		evaluators.add(extractFromStructuredEvaluator);
		return evaluators;
	}

	/**
	 * Test evaluation.
	 */
	public void testEvaluation() {
		ConditionalExpressionEvaluator evaluator = new ConditionalExpressionEvaluator();
		String expression = "3==5";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"), "else");
		expression = "3==3";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"), "then");
		expression = "3<5";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"), "then");
		expression = "3>5";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"), "else");
		expression = "3<=3";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"), "then");
		expression = "3<=5";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"), "then");
		expression = "3>=5";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"), "else");
		expression = "3>=3";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"), "then");

		expression = "test==";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"), "else");
		expression = "  == ";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"), "then");
		expression = "==";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"), "then");

		Assert.assertEquals(evaluator.evaluate("${if(true).then(then).else(else)}"), "then");
		Assert.assertEquals(evaluator.evaluate("${if(false).then(then).else(else)}"), "else");

		Assert.assertEquals(evaluator.evaluate("${if((null).matches(test)).then(then).else(else)}"), "else");
		Assert.assertEquals(evaluator.evaluate("${if((null).matches(test)).then(then)}"), "");

		Assert.assertEquals(evaluator.evaluate("${if(().matches(\\s*)).then(then).else(else)}"), "then");
		Assert.assertEquals(evaluator.evaluate("${if((test).matches(\\s*)).then(then).else(else)}"), "else");

		Assert.assertEquals(
				evaluator.evaluate("${if((test).matches(\\s*)).then(then\nsdsd  \n).else(else\non second line)}"),
				"else\non second line");

		Assert.assertEquals(evaluator.evaluate("${if(3!@3).then(then).else(else)}"), "else");

		Assert.assertEquals(evaluator.evaluate("${if().then(then).else(else)}"), "else");
	}

	public void testEvaluate_emptyBlocks() {
		ConditionalExpressionEvaluator evaluator = new ConditionalExpressionEvaluator();

		Assert.assertEquals(evaluator.evaluate("${if(==).then(then).else()}"), "then");
		Assert.assertEquals(evaluator.evaluate("${if(null==).then().else(else)}"), "else");
		Assert.assertEquals(evaluator.evaluate("${if(true).then().else(else)}"), "");
		Assert.assertEquals(evaluator.evaluate("${if(==).then().else()}"), "");
	}

	public void testNonEmptyCondition() {
		ExpressionsManager manager = createManager();
		String expression = "${eval(${var.test=${if(false).then(true)}}${if(${var.test}).then(in_then).else(in_else)})}";
		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		ExpressionContext context = manager.createDefaultContext(target, null, null);
		String result = manager.evaluateRule(expression, String.class, context);
		Assert.assertNotNull(result);
		Assert.assertEquals(result, "in_else");
	}
}
