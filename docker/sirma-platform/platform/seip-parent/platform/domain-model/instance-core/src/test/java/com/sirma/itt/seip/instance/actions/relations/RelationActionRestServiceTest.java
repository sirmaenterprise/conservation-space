package com.sirma.itt.seip.instance.actions.relations;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Tests for {@link RelationActionRestService}.
 *
 * @author A. Kunchev
 */
public class RelationActionRestServiceTest {

	@Mock
	private Actions actions;

	@InjectMocks
	private RelationActionRestService service;

	@Before
	public void setup() {
		service = new RelationActionRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testAddRelation() {
		AddRelationRequest request = new AddRelationRequest();
		service.addRelation(request);
		verify(actions).callAction(request);
	}

	@Test
	public void testRemoveRelation() {
		RemoveRelationRequest request = new RemoveRelationRequest();
		service.removeRelation(request);
		verify(actions).callAction(request);
	}
	@Test
	public void should_CallAction_When_UpdateRelationsIsCalled() {
		UpdateRelationsRequest request = new UpdateRelationsRequest("");
		service.updateRelations(request);
		verify(actions).callAction(request);
	}
}
