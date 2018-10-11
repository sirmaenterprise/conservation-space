/**
 *
 */
package com.sirma.itt.seip.expressions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.Serializable;

import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * @author BBonev
 */
public class FromEvaluatorTest extends BaseEvaluatorTest {

	@Test
	public void test_fromCurrent() {
		ExpressionsManager manager = createManager();
		EmfInstance target = new EmfInstance();
		target.setId("emf:id");
		ExpressionContext context = manager.createDefaultContext(target, null, null);

		Serializable result = manager.evaluateRule("${from(current)}", Serializable.class, context, target);
		assertNotNull(result);
		assertEquals(result, target);
	}

	@Test
	public void test_fromContext() {
		ExpressionsManager manager = createManager();
		EmfInstance target = new EmfInstance();
		target.setId("emf:id");
		EmfInstance parent = new EmfInstance();
		parent.setId("emf:parentId");
		contextService.bindContext(target, parent);

		ExpressionContext context = manager.createDefaultContext(target, null, null);

		Serializable result = manager.evaluateRule("${from(context)}", Serializable.class, context, target);
		assertNotNull(result);
		assertEquals(result, parent);
	}

	@Test
	public void test_fromOwningInstance() {
		ExpressionsManager manager = createManager();
		EmfInstance target = new EmfInstance();
		target.setId("emf:id");
		EmfInstance parent = new EmfInstance();
		parent.setId("emf:parentId");
		contextService.bindContext(target, parent);
		ExpressionContext context = manager.createDefaultContext(target, null, null);

		Serializable result = manager.evaluateRule("${from(owningInstance)}", Serializable.class, context, target);
		assertNotNull(result);
		assertEquals(result, parent);
	}

	@Test
	public void test_fromParent() {
		ExpressionsManager manager = createManager();
		EmfInstance target = new EmfInstance();
		target.setId("emf:id");
		EmfInstance parent = new EmfInstance();
		parent.setId("emf:parentId");
		contextService.bindContext(target, parent);
		ExpressionContext context = manager.createDefaultContext(target, null, null);

		Serializable result = manager.evaluateRule("${from(parent)}", Serializable.class, context, target);
		assertNotNull(result);
		assertEquals(result, parent);
	}

}
