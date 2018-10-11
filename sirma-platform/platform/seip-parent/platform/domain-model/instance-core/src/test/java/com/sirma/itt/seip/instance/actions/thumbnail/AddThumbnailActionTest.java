package com.sirma.itt.seip.instance.actions.thumbnail;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Tests for {@link AddThumbnailAction}.
 *
 * @author A. Kunchev
 */
public class AddThumbnailActionTest {

	@InjectMocks
	private AddThumbnailAction action;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private ThumbnailService thumbnailService;

	@Mock
	private EventService eventService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private LabelProvider labelProvider;

	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Before
	public void setup() {
		action = new AddThumbnailAction();
		MockitoAnnotations.initMocks(this);
		LinkConstants.init(new SecurityContextManagerFake(), ContextualMap.create());
	}

	@Test(expected = BadRequestException.class)
	public void should_NotException_When_InstanceDefinitionDoNotContainsFieldWithUriHasThumbnail() {
		InstanceReferenceMock target = new InstanceReferenceMock(mock(Instance.class));
		setupValidateTest(target, Optional.empty());

		action.validate(buildRequest(target, "targetId", "thumbnailId"));
	}

	@Test
	public void should_NotThrowException_When_InstanceDefinitionContainsFieldWithUriHasThumbnail() {
		InstanceReferenceMock target = new InstanceReferenceMock(mock(Instance.class));
		setupValidateTest(target, Optional.of(mock(PropertyDefinition.class)));

		action.validate(buildRequest(target,"targetId", "thumbnailId"));
	}

	private void setupValidateTest(InstanceReference instanceReference, Optional<PropertyDefinition> propertyDefinition) {
		when(instanceTypeResolver.resolveReference("targetId")).thenReturn(Optional.of(instanceReference));
		DefinitionModel instanceDefinitionModel = mock(DefinitionModel.class);
		when(definitionService.getInstanceDefinition(instanceReference.toInstance())).thenReturn(instanceDefinitionModel);
		when(instanceDefinitionModel.findField(any(Predicate.class))).thenReturn(propertyDefinition);
		when(labelProvider.getLabel("field.has.thumbnail.not.existing")).thenReturn("Error message");
	}

	@Test
	public void getName() {
		assertEquals(AddThumbnailRequest.OPERATION_NAME, action.getName());
	}

	@Test(expected = EmfRuntimeException.class)
	public void perform_nullRequest() {
		action.validate(null);
	}

	@Test(expected = NullPointerException.class)
	public void perform_nullTargetId() {
		action.validate(buildRequest(null, null, "thumbnailId"));
	}

	@Test(expected = NullPointerException.class)
	public void perform_nullThumbnailObjectId() {
		action.validate(buildRequest(null, "targetId", null));
	}

	@Test(expected = InstanceNotFoundException.class)
	public void perform_noTargetInstanceFound() {
		when(instanceTypeResolver.resolveReference("targetId")).thenReturn(Optional.empty());
		action.validate(buildRequest(null, "targetId", "thumbnailId"));
	}

	@Test(expected = InstanceNotFoundException.class)
	public void perform_noThumbnailInstance() {
		InstanceReferenceMock target = new InstanceReferenceMock();
		when(instanceTypeResolver.resolveReference("targetId")).thenReturn(Optional.of(target));
		when(instanceTypeResolver.resolveReference("thumbnailId")).thenReturn(Optional.empty());
		action.perform(buildRequest(target, "targetId", "thumbnailId"));
	}

	@Test
	public void perform_withOldThumbnail_addedNewOne() {
		InstanceReferenceMock target = new InstanceReferenceMock(new EmfInstance("targetId"));
		when(instanceTypeResolver.resolveReference("targetId")).thenReturn(Optional.of(target));

		InstanceReferenceMock thumbnail = new InstanceReferenceMock();
		when(instanceTypeResolver.resolveReference("thumbnailId")).thenReturn(Optional.of(thumbnail));

		action.perform(buildRequest(target, "targetId", "thumbnailId"));
		verify(thumbnailService).register(eq(target), any(Instance.class), eq(null));
		verify(domainInstanceService).save(any());
	}

	private static AddThumbnailRequest buildRequest(InstanceReference targetReference, String targetId, String thumbnailObjectId) {
		AddThumbnailRequest request = new AddThumbnailRequest();
		request.setTargetId(targetId);
		request.setThumbnailObjectId(thumbnailObjectId);
		request.setTargetReference(targetReference);
		return request;
	}

}
