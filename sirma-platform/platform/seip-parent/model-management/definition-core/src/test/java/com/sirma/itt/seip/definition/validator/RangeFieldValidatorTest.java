package com.sirma.itt.seip.definition.validator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.definition.DefinitionModel;

/**
 * Tests for {@link RangeFieldValidator}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class RangeFieldValidatorTest {

	@Spy
	private RangeFieldValidator rangeFieldValidator;

	@Test
	public void should_NotReturnErrorMessage_When_PropertyDefinitionIsNotGenericDefinition() {
		Assert.assertTrue(rangeFieldValidator.validate(Mockito.mock(DefinitionModel.class)).isEmpty());
	}
}