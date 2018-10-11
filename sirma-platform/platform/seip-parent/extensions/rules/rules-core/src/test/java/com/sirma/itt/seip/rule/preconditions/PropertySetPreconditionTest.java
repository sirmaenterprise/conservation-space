package com.sirma.itt.seip.rule.preconditions;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;

import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rule.model.PropertyMapping;
import com.sirma.itt.seip.rule.preconditions.PropertySetPrecondition;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * @author BBonev
 */
@Test
public class PropertySetPreconditionTest extends EmfTest {

	@Spy
	TypeConverter typeConverter = createTypeConverter();

	@InjectMocks
	PropertySetPrecondition setPrecondition;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
	}

	public void testContainsProperty() {
		Context<String, Object> context = new Context<>();
		context.put(PropertyMapping.NAME,
				Collections.singletonList(PropertyMapping.toMap(new PropertyMapping("property", (String) null))));
		assertTrue(setPrecondition.configure(context));

		Instance instance = new EmfInstance();
		instance.setId("emf:id");
		instance.setProperties(new HashMap<String, Serializable>());

		assertFalse(setPrecondition.checkPreconditions(RuleContext.create(instance, instance, null)));

		instance.getProperties().put("property", "value");

		assertTrue(setPrecondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}

	public void testSetAndValueMatch() {
		Context<String, Object> context = new Context<>();
		context.put(PropertyMapping.NAME,
				Collections.singletonList(PropertyMapping.toMap(new PropertyMapping("property", "test"))));
		assertTrue(setPrecondition.configure(context));

		Instance instance = new EmfInstance();
		instance.setId("emf:id");
		instance.setProperties(new HashMap<String, Serializable>());

		instance.getProperties().put("property", "value");

		assertFalse(setPrecondition.checkPreconditions(RuleContext.create(instance, instance, null)));

		instance.getProperties().put("property", "test");

		assertTrue(setPrecondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}

	public void testSetAndValueMatch_propertyConvert() {
		Context<String, Object> context = new Context<>();
		context.put(PropertyMapping.NAME,
				Collections.singletonList(PropertyMapping.toMap(new PropertyMapping("property", "2"))));
		assertTrue(setPrecondition.configure(context));

		Instance instance = new EmfInstance();
		instance.setId("emf:id");
		instance.setProperties(new HashMap<String, Serializable>());

		instance.getProperties().put("property", 3);

		assertFalse(setPrecondition.checkPreconditions(RuleContext.create(instance, instance, null)));

		instance.getProperties().put("property", 2);

		assertTrue(setPrecondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}
}
