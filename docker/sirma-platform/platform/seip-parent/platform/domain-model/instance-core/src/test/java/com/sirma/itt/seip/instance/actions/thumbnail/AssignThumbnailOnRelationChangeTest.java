package com.sirma.itt.seip.instance.actions.thumbnail;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyRemoveEvent;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Test for {@link AssignThumbnailOnRelationChange}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 10/09/2018
 */
public class AssignThumbnailOnRelationChangeTest {
	@InjectMocks
	private AssignThumbnailOnRelationChange relationChange;

	@Mock
	private ThumbnailService thumbnailService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onRelationRemoved_doNothingIfNotHasThumbnail() throws Exception {
		relationChange.onRelationRemoved(
				new RemoveRelationEvent("emf:target-instance", LinkConstants.HAS_TEMPLATE, "emf:thumbnail-instance"));
		verify(thumbnailService, never()).removeAssignedThumbnail("emf:target-instance");
	}

	@Test
	public void onRelationRemoved_shouldRemoveOldThumbnail() {
		relationChange.onRelationRemoved(
				new RemoveRelationEvent("emf:target-instance", LinkConstants.HAS_THUMBNAIL, "emf:thumbnail-instance"));

		verify(thumbnailService).removeAssignedThumbnail("emf:target-instance");
	}

	@Test
	public void onRelationAdded_shouldAssignTheNewThumbnail() {
		relationChange.onRelationAdded(
				new AddRelationEvent("emf:target-instance", LinkConstants.HAS_THUMBNAIL, "emf:thumbnail-instance"));

		verify(thumbnailService).register(any(Serializable.class), any(Serializable.class));
	}

	private static class AddRelationEvent implements ObjectPropertyAddEvent {

		private Serializable sourceId;
		private String propertyName;
		private Serializable targetId;

		private AddRelationEvent(Serializable sourceId, String propertyName, Serializable targetId) {
			this.sourceId = sourceId;
			this.propertyName = propertyName;
			this.targetId = targetId;
		}

		@Override
		public Serializable getSourceId() {
			return sourceId;
		}

		@Override
		public String getObjectPropertyName() {
			return propertyName;
		}

		@Override
		public Serializable getTargetId() {
			return targetId;
		}
	}

	private static class RemoveRelationEvent implements ObjectPropertyRemoveEvent {

		private Serializable sourceId;
		private String propertyName;
		private Serializable targetId;

		private RemoveRelationEvent(Serializable sourceId, String propertyName, Serializable targetId) {
			this.sourceId = sourceId;
			this.propertyName = propertyName;
			this.targetId = targetId;
		}

		@Override
		public Serializable getSourceId() {
			return sourceId;
		}

		@Override
		public String getObjectPropertyName() {
			return propertyName;
		}

		@Override
		public Serializable getTargetId() {
			return targetId;
		}
	}
}
