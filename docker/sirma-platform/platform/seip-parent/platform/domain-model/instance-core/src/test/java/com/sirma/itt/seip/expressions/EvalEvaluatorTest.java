package com.sirma.itt.seip.expressions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;

import java.io.Serializable;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.UriConverterProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.util.ReflectionUtils;

public class EvalEvaluatorTest extends BaseEvaluatorTest {

	@InjectMocks
	private EvalEvaluator evalEvaluator = new EvalEvaluator();

	@Mock
	private javax.enterprise.inject.Instance<ExpressionsManager> expressionManagerInjector;

	@Spy
	private TypeConverter typeConverter = createTypeConverter();

	private ExpressionsManager expressionManager;

	@Before
	public void init() {
		this.beforeMethod();

		expressionManager = createManager();

		ReflectionUtils.setFieldValue(evalEvaluator, "manager", expressionManager);

		when(expressionManagerInjector.get()).thenReturn(expressionManager);
		when(userPreferences.getLanguage()).thenReturn("en");
	}

	@Test
	public void should_EvaluateNestedExpressions() {
		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put("test", "testValue");
		target.getProperties().put("test2", "testValue2");
		ExpressionContext context = expressionManager.createDefaultContext(target, null, null);

		// simple evaluation
		Serializable result = evalEvaluator.evaluate("${eval(#{get([test])})}", context);
		assertEquals("#{get([test])}", result);

		// first pass parsing and evalution
		result = evalEvaluator.evaluate("${eval(Some text #{get([test])} some other text ${get([test])} and ${if(2==2).then(#{get([test2])})})}", context);
		assertEquals("Some text #{get([test])} some other text testValue and #{get([test2])}", result);

		// second pass
		result = evalEvaluator.evaluate("#{eval(" + result + ")}", context);
		assertEquals("Some text testValue some other text testValue and testValue2", result);
	}

	@Test
	public void should_ReturnNull_WhenExpressionCannotBeEvaluated() {
		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put("test", "testValue");
		target.getProperties().put("test2", "testValue2");
		ExpressionContext context = expressionManager.createDefaultContext(target, null, null);

		// simple evaluation
		Serializable result = evalEvaluator.evaluate("#{get([test])}", context);
		assertNull(result);
	}

	@Override
	public TypeConverter createTypeConverter() {
		TypeConverter converter = super.createTypeConverter();
		UriConverterProvider provider = new UriConverterProvider();
		provider.register(converter);
		return converter;
	}

}
