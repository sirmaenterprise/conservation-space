/**
 *
 */
package com.sirma.itt.seip.expressions;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionEvaluator;
import com.sirma.itt.seip.instance.script.ScriptNode;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.script.ScriptEvaluator;
import com.sirma.itt.seip.script.ScriptInstance;

/**
 * @author BBonev
 */
public class ScriptExpressionEvaluatorTest extends BaseEvaluatorTest {
	@Mock
	ScriptEvaluator scriptEvaluator;

	@InjectMocks
	ScriptExpressionEvaluator expressionEvaluator;

	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
	}

	@Test
	public void testScriptInvocation() {
		createManager().evaluate("${script($$2+2$$)}", Object.class);
		verify(scriptEvaluator).eval(eq("2+2"), anyMap());
	}

	@Test
	public void testScriptInvocationMultiLine() {
		createManager().evaluate("${script($$2\n+2$$)}", Object.class);
		verify(scriptEvaluator).eval(eq("2\n+2"), anyMap());
	}

	@Test
	public void test_scriptInVariable() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add("about", new LinkSourceId("emf:about", mock(DataTypeDefinition.class)));
		instance.add(DefaultProperties.IS_DELETED, Boolean.FALSE);

		ExpressionsManager manager = createManager();
		ExpressionContext context = manager.createDefaultContext(instance, null, null);

		when(scriptEvaluator.eval(eq(" get('about').getIdentifier() "), anyMap())).thenReturn("emf:about");

		Serializable result = manager
				.evaluateRule("${eval(\n ${var.zzz=${script($$ get('about').getIdentifier() $$)}}\n"
						+ " ${var.href= ${if(${get([emf:isDeleted])} == false).then(href=\"${var.zzz}\")} }\n"
						+ " ${var.href})}", Serializable.class, context);
		assertEquals(result.toString().trim(), "href=\"emf:about\"");
	}

	@Override
	protected void registerConverters(TypeConverter typeConverter) {
		super.registerConverters(typeConverter);
		typeConverter.addConverter(EmfInstance.class, ScriptInstance.class, inst -> new ScriptNode().setTarget(inst));
	}

	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionsManager manager,
			TypeConverter converter) {
		// TODO implement ScriptExpressionEvaluatorTest.initializeEvaluators!
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		list.add(initEvaluator(expressionEvaluator, manager, converter));
		list.add(initEvaluator(new PropertyExpressionEvaluator(), manager, converter));
		return list;
	}
}
