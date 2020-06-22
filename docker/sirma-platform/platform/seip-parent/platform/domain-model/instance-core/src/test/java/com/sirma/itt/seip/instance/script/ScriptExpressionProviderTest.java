package com.sirma.itt.seip.instance.script;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.script.GlobalBindingsExtension;

/**
 * The Class ScriptExpressionProviderTest.
 *
 * @author BBonev
 */
@Test
public class ScriptExpressionProviderTest extends BaseInstanceScriptTest {

	/** The Constant EXPRESSION_VALUE. */
	private static final String EXPRESSION_VALUE = "expressionValue";

	/** The expressions manager. */
	@Mock
	private ExpressionsManager expressionsManager;
 
	/** The expression provider. */
	@InjectMocks
	ScriptExpressionProvider expressionProvider;

	/**
	 * Before method.
	 */
	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		when(expressionsManager.isExpression(anyString())).thenReturn(true);
		when(expressionsManager.createDefaultContext(any(Instance.class), any(PropertyDefinition.class), anyMap()))
				.thenReturn(new ExpressionContext());
		when(expressionsManager.evaluateRule(anyString(), eq(Serializable.class), any(ExpressionContext.class)))
				.thenReturn(EXPRESSION_VALUE);
	}

	/**
	 * Provide bindings.
	 *
	 * @param bindingsExtensions
	 *            the bindings extensions
	 */
	@Override
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		super.provideBindings(bindingsExtensions);
		bindingsExtensions.add(expressionProvider);
	}

	/**
	 * Test string expression_no expression.
	 */
	public void testStringExpression_noExpression() {
		Object object = eval("evalExp(null)");
		assertNull(object);
	}

	/**
	 * Test string expression_valid.
	 */
	public void testStringExpression_valid() {
		Object object = eval("evalExp('${today}')");
		assertEquals(object, EXPRESSION_VALUE);
	}

	/**
	 * Test string expression over node.
	 */
	public void testStringExpressionOverNode() {
		EmfInstance emfInstance = new EmfInstance();
		Object object = eval("evalExp('${today}', root)", emfInstance);
		assertEquals(object, EXPRESSION_VALUE);
	}

	public void testGet() {
		EmfInstance instance = new EmfInstance();
		instance.setId("inst1");
		instance.setProperties(new HashMap<>());
		instance.getProperties().put("test", "value");

		Object result = eval("get('test')", instance);
		assertEquals(result, "value");
	}

	public void testSet() {
		EmfInstance instance = new EmfInstance();
		instance.setId("inst1");
		instance.setProperties(new HashMap<>());

		eval("set('test', 'value')", instance);
		assertEquals(instance.getProperties().get("test"), "value");

		eval("set('numb', 2)", instance);
		assertEquals(instance.getProperties().get("numb"), 2);
	}

	public void testGetFromContext() {
		EmfInstance instance = new EmfInstance();
		instance.setId("inst1");
		EmfInstance context = new EmfInstance();
		context.setId("ctx");
		context.setProperties(new HashMap<>());
		context.getProperties().put("test", "value");
		contextService.bindContext(instance, context);
		Object result = eval("getFromContext('test')", instance);
		assertEquals(result, "value");
	}
}
