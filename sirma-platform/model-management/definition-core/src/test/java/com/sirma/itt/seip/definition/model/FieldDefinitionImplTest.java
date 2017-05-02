package com.sirma.itt.seip.definition.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

/**
 * Test for {@link FieldDefinitionImpl}
 *
 * @author BBonev
 */
public class FieldDefinitionImplTest {

	@Test
	public void getDependentFieldsShouldIgnoreInternalExpressions_1() throws Exception {
		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setRnc("${get([propertyName])}");
		Set<String> dependentFields = field.getDependentFields();
		assertNotNull(dependentFields);
		assertTrue(dependentFields.isEmpty());
	}

	@Test
	public void getDependentFieldsShouldIgnoreInternalExpressions_2() throws Exception {
		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setRnc("#{get([propertyName])}");
		Set<String> dependentFields = field.getDependentFields();
		assertNotNull(dependentFields);
		assertTrue(dependentFields.isEmpty());
	}

	@Test
	public void getDependentFieldsShouldCollectFieldsFromRncAndConditions() throws Exception {
		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setRnc("+[field1] AND -[field2]");
		ConditionDefinitionImpl condition = new ConditionDefinitionImpl();
		condition.setExpression("+[field2] AND -[field3] AND [field4] in ('value1', 'value2')");
		field.setConditions(Arrays.asList(condition));

		Set<String> dependentFields = field.getDependentFields();
		assertNotNull(dependentFields);
		assertEquals(4, dependentFields.size());
		assertTrue(dependentFields.contains("field1"));
		assertTrue(dependentFields.contains("field2"));
		assertTrue(dependentFields.contains("field3"));
		assertTrue(dependentFields.contains("field4"));
	}

	@Test
	public void getDependentFieldsShouldBeCached() throws Exception {
		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setRnc("+[field1] AND -[field2]");
		ConditionDefinitionImpl condition = new ConditionDefinitionImpl();
		condition.setExpression("+[field2] AND -[field3] AND [field4] in ('value1', 'value2')");
		field.setConditions(Arrays.asList(condition));

		Set<String> dependentFields1 = field.getDependentFields();

		field.setRnc(null);
		field.setConditions(null);

		Set<String> dependentFields2 = field.getDependentFields();

		assertEquals(dependentFields1, dependentFields2);
	}
}
