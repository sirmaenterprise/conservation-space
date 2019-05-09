package com.sirma.itt.seip.definition.validator;

import static com.sirma.itt.seip.definition.ValidationMessageUtils.hasError;
import static com.sirma.itt.seip.definition.validator.ConditionValidator.ConditionValidatorMessageBuilder.*;
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
import com.sirma.itt.seip.domain.validation.ValidationMessage;

public class ConditionValidatorTest {

	private ConditionValidator validator = new ConditionValidator();

	@Test
	public void should_Not_AllowEmptyExpression() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");
		definition.setExpression("");

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, EMPTY_EXPRESSION, "d1"));
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

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, MISSING_FIELDS_FOR_EXPRESSION));
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

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, MISSING_FIELDS_FOR_EXPRESSION));
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

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, MISSING_FIELDS_FOR_EXPRESSION));
	}

}
