/**
 *
 */
package com.sirma.itt.seip.instance.revision;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.sep.content.rendition.ThumbnailService;

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

	@BeforeClass
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

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
}