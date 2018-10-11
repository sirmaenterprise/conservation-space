package com.sirma.itt.seip.instance.save.expression.evaluation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.BaseEvaluatorTest;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;

@Test
public class ObjectPropertyExpressionEvaluatorTest extends BaseEvaluatorTest {

	@Mock
	private InstanceTypeResolver instanceTypeResolve;

	@Mock
	private HeadersService headerService;

	@InjectMocks
	private ObjectPropertyExpressionEvaluator objectPropertyExpressionEvaluator;

	@Test
	public void testObjectPropertyEvaluator_withProperParams() {
		List<Instance> instances = new ArrayList<>();

		Instance instance = new EmfInstance();
		instances.add(instance);

		when(instanceTypeResolve.resolveInstances(anyCollectionOf(Serializable.class))).thenReturn(instances);
		when(headerService.generateInstanceHeader(any(Instance.class), anyString())).thenReturn("header");

		ExpressionContext context = createManager().createDefaultContext(instance, null, null);

		// single argument passed to the evaluator
		String script = ObjectPropertyExpressionEvaluatorTest.buildScript("emf:123");
		String result = objectPropertyExpressionEvaluator.evaluate(script, context).toString();
		assertEquals(result, "header");

		instances.add(new EmfInstance());
		when(instanceTypeResolve.resolveInstances(anyCollectionOf(Serializable.class))).thenReturn(instances);
		when(headerService.generateInstanceHeader(any(Instance.class), anyString())).thenReturn("header");

		// multiple arguments passed to the evaluator
		script = ObjectPropertyExpressionEvaluatorTest.buildScript("emf:123,emf:321");
		result = objectPropertyExpressionEvaluator.evaluate(script, context).toString();
		assertEquals(result, "header,header");
	}

	@Test
	public void testObjectPropertyEvaluator_notSupportedParams() {
		// wrong property
		String script = ObjectPropertyExpressionEvaluatorTest.buildScript("null");
		assertTrue(objectPropertyExpressionEvaluator.evaluate(script).toString().isEmpty());

		// empty property
		script = ObjectPropertyExpressionEvaluatorTest.buildScript("");
		assertTrue(objectPropertyExpressionEvaluator.evaluate(script).toString().isEmpty());

		List<Instance> instances = new ArrayList<>();
		Instance instance = new EmfInstance();
		instances.add(instance);

		when(instanceTypeResolve.resolveInstances(anyCollectionOf(Serializable.class))).thenReturn(instances);
		when(headerService.generateInstanceHeader(any(Instance.class), anyString())).thenReturn(null);

		// header not found
		script = ObjectPropertyExpressionEvaluatorTest.buildScript("emf:123");
		assertTrue(objectPropertyExpressionEvaluator.evaluate(script).toString().isEmpty());
	}

	/**
	 * Generate script with require data send from definition.
	 *
	 * @param ids
	 *            instance identifiers
	 * @return script
	 */
	private static String buildScript(String... ids) {
		StringBuilder evaluated = new StringBuilder("${objectProperty(");
		int i = 0, len = ids.length;
		for (i = 0; i < len; i++) {
			evaluated.append(ids[i]);
			if (i != len - 1) {
				evaluated.append(",");
			}
		}
		evaluated.append(")}");
		return evaluated.toString();
	}
}
