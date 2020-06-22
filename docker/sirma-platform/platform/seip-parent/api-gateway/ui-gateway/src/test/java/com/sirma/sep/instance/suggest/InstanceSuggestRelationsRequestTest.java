package com.sirma.sep.instance.suggest;

import org.junit.Test;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Tests for {@link InstanceSuggestRelationsRequest}.
 *
 * @author Boyan Tonchev.
 */
public class InstanceSuggestRelationsRequestTest {

	private InstanceSuggestRelationsRequest instanceSuggestRelationsRequest = new InstanceSuggestRelationsRequest();


	@Test(expected = BadRequestException.class)
	public void should_ThrowException_When_PropertyNameIsEmpty() {
		instanceSuggestRelationsRequest.setPropertyName(null);
	}

	@Test(expected = BadRequestException.class)
	public void should_ThrowException_When_PropertyNameIsNull() {
		instanceSuggestRelationsRequest.setPropertyName(null);
	}

	@Test(expected = BadRequestException.class)
	public void should_ThrowException_When_DefinitionIdIsEmpty() {
		instanceSuggestRelationsRequest.setDefinitionId(null);
	}

	@Test(expected = BadRequestException.class)
	public void should_ThrowException_When_DefinitionIdIsNull() {
		instanceSuggestRelationsRequest.setDefinitionId(null);
	}
}