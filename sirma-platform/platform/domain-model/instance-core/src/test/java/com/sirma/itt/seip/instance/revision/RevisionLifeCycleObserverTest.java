/**
 *
 */
package com.sirma.itt.seip.instance.revision;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.seip.content.rendition.ThumbnailService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.revision.CreatedRevisionEvent;
import com.sirma.itt.seip.instance.revision.PublishApprovedRevisionEvent;
import com.sirma.itt.seip.instance.revision.RevisionLifeCycleObserver;

/**
 * RevisionLifeCycleObserver test.
 *
 * @author A. Kunchev
 */
@Test
public class RevisionLifeCycleObserverTest {

	@InjectMocks
	private RevisionLifeCycleObserver observer = new RevisionLifeCycleObserver();

	@Mock
	private ThumbnailService thumbnailService;

	/**
	 * Setups mocks.
	 */
	@BeforeClass
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	// ----------------------------- onNewRevision(CreatedRevisionEvent) -----------------------------

	/**
	 * Successful.
	 */
	public void onNewRevision_thumbnailServiceCalled() {
		Instance instance = mock(Instance.class);
		Instance revision = mock(Instance.class);
		InstanceReference sourceReference = mock(InstanceReference.class);
		InstanceReference revisionReference = mock(InstanceReference.class);
		when(instance.toReference()).thenReturn(sourceReference);
		when(revision.toReference()).thenReturn(revisionReference);
		CreatedRevisionEvent event = new CreatedRevisionEvent(instance, revision);
		observer.onNewRevision(event);
		verify(thumbnailService, atLeastOnce()).copyThumbnailFromSource(revisionReference, sourceReference);
	}

	// ----------------------------- onNewApprovedRevsion(PublishApprovedRevisionEvent) ---------------

	/**
	 * Successful.
	 */
	public void onNewApprovedRevsion_uidCopied() {
		String uid = "someUID";
		Instance instance = new EmfInstance();
		Instance latestRevisionInstance = new EmfInstance();
		HashMap<String, Serializable> properties = new HashMap<>(1);
		latestRevisionInstance.setProperties(properties);
		properties.put(DefaultProperties.UNIQUE_IDENTIFIER, uid);
		instance.setProperties(properties);
		PublishApprovedRevisionEvent event = new PublishApprovedRevisionEvent(instance, null, null,
				latestRevisionInstance);
		observer.onNewApprovedRevsion(event);
		assertEquals(latestRevisionInstance.getProperties().get(DefaultProperties.UNIQUE_IDENTIFIER), uid);
	}
}
