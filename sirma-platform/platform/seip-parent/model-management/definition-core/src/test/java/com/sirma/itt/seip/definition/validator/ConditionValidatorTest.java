package com.sirma.itt.seip.definition.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sirma.itt.seip.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;

public class ConditionValidatorTest {

	private ConditionValidator validator = new ConditionValidator();

	@Test
	public void should_Not_AllowEmptyExpression() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setExpression("");

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(errors.get(0).contains("Expression should not be empty"));
	}

	@Test
	public void should_NotAllowInvalidExpressionInRootFieldCondition() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();

		ConditionDefinitionImpl condition = new ConditionDefinitionImpl();
		condition.setExpression("[status] IN ('DRAFT')");

		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setConditions(new ArrayList<>());
		field.getConditions().add(condition);

		definition.getFields().add(field);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(errors.get(0).contains("No fields in the target model "));
	}

	@Test
	public void should_NotAllowInvalidExpressionInRegionFieldCondition() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();

		ConditionDefinitionImpl condition = new ConditionDefinitionImpl();
		condition.setExpression("[status] IN ('DRAFT')");

		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setConditions(new ArrayList<>());
		field.getConditions().add(condition);

		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.getFields().add(field);

		definition.getRegions().add(region);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(errors.get(0).contains("No fields in the target model "));
	}

	@Test
	public void should_NotAllowInvalidExpressionInTransitionCondition() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();

		ConditionDefinitionImpl condition = new ConditionDefinitionImpl();
		condition.setExpression("[status] IN ('DRAFT')");

		TransitionDefinitionImpl transition = new TransitionDefinitionImpl();
		transition.setConditions(new ArrayList<>());
		transition.getConditions().add(condition);

		definition.getTransitions().add(transition);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(errors.get(0).contains("No fields in the target model "));
	}

}
