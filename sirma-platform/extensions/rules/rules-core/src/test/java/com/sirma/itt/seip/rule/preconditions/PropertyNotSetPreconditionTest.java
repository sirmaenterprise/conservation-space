package com.sirma.itt.seip.rule.preconditions;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;

import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rule.model.PropertyMapping;
import com.sirma.itt.seip.rule.preconditions.PropertyNotSetPrecondition;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * @author BBonev
 */
@Test
public class PropertyNotSetPreconditionTest extends EmfTest {

	public void testValueNotSet() {
		PropertyNotSetPrecondition precondition = new PropertyNotSetPrecondition();
		Context<String, Object> context = new Context<>();
		context.put(PropertyMapping.NAME,
				Collections.singletonList(PropertyMapping.toMap(new PropertyMapping("property", (String) null))));
		assertTrue(precondition.configure(context));

		Instance instance = new EmfInstance();
		instance.setId("emf:id");
		instance.setProperties(new HashMap<String, Serializable>());

		assertTrue(precondition.checkPreconditions(RuleContext.create(instance, instance, null)));

		instance.getProperties().put("property", "value");

		assertFalse(precondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}

}
