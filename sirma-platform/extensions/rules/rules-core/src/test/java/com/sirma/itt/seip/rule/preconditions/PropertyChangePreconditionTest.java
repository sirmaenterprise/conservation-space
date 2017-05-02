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
import com.sirma.itt.seip.rule.model.PropertyMapping;
import com.sirma.itt.seip.rule.model.PropertyValueChange;
import com.sirma.itt.seip.rule.preconditions.PropertyChangePrecondition;

/**
 * @author BBonev
 */
@Test
public class PropertyChangePreconditionTest {

	PropertyChangePrecondition changePrecondition;

	public void checkForAdded() {
		changePrecondition = new PropertyChangePrecondition();
		Context<String, Object> context = new Context<>();
		context.put(PropertyMapping.NAME, Collections
				.singletonList(PropertyMapping.toMap(new PropertyMapping("property", PropertyValueChange.ADDED))));
		assertTrue(changePrecondition.configure(context));

		EmfInstance current = new EmfInstance();
		current.setProperties(Collections.<String, Serializable> singletonMap("property", "value"));

		EmfInstance oldVersion = new EmfInstance();
		oldVersion.setProperties(new HashMap<String, Serializable>());

		assertTrue(changePrecondition.checkPreconditions(RuleContext.create(current, oldVersion, null)));
	}

	public void checkForRemoved() {
		changePrecondition = new PropertyChangePrecondition();
		Context<String, Object> context = new Context<>();
		context.put(PropertyMapping.NAME, Collections
				.singletonList(PropertyMapping.toMap(new PropertyMapping("property", PropertyValueChange.REMOVED))));
		assertTrue(changePrecondition.configure(context));

		EmfInstance current = new EmfInstance();
		current.setProperties(new HashMap<String, Serializable>());

		EmfInstance oldVersion = new EmfInstance();
		oldVersion.setProperties(Collections.<String, Serializable> singletonMap("property", "value"));

		assertTrue(changePrecondition.checkPreconditions(RuleContext.create(current, oldVersion, null)));
	}

	public void checkForChanged_added() {
		changePrecondition = new PropertyChangePrecondition();
		Context<String, Object> context = new Context<>();
		context.put(PropertyMapping.NAME, Collections
				.singletonList(PropertyMapping.toMap(new PropertyMapping("property", PropertyValueChange.CHANGED))));
		assertTrue(changePrecondition.configure(context));

		EmfInstance current = new EmfInstance();
		current.setProperties(Collections.<String, Serializable> singletonMap("property", "value"));

		EmfInstance oldVersion = new EmfInstance();
		oldVersion.setProperties(new HashMap<String, Serializable>());

		assertTrue(changePrecondition.checkPreconditions(RuleContext.create(current, oldVersion, null)));
	}

	public void checkForChanged_changed() {
		changePrecondition = new PropertyChangePrecondition();
		Context<String, Object> context = new Context<>();
		context.put(PropertyMapping.NAME, Collections
				.singletonList(PropertyMapping.toMap(new PropertyMapping("property", PropertyValueChange.CHANGED))));
		assertTrue(changePrecondition.configure(context));

		EmfInstance current = new EmfInstance();
		current.setProperties(Collections.<String, Serializable> singletonMap("property", "newValue"));

		EmfInstance oldVersion = new EmfInstance();
		oldVersion.setProperties(Collections.<String, Serializable> singletonMap("property", "oldValue"));

		assertTrue(changePrecondition.checkPreconditions(RuleContext.create(current, oldVersion, null)));
	}

	public void checkForNotChanged() {
		changePrecondition = new PropertyChangePrecondition();
		Context<String, Object> context = new Context<>();
		context.put(PropertyMapping.NAME, Collections
				.singletonList(PropertyMapping.toMap(new PropertyMapping("property", PropertyValueChange.CHANGED))));
		assertTrue(changePrecondition.configure(context));

		EmfInstance current = new EmfInstance();
		current.setProperties(Collections.<String, Serializable> singletonMap("property", "value"));

		EmfInstance oldVersion = new EmfInstance();
		oldVersion.setProperties(Collections.<String, Serializable> singletonMap("property", "value"));

		assertFalse(changePrecondition.checkPreconditions(RuleContext.create(current, oldVersion, null)));
	}

}
