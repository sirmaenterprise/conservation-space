package com.sirma.sep.content.rendition;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.content.rendition.ThumbnailConfigurations;
import com.sirma.sep.content.rendition.ThumbnailDao;
import com.sirma.sep.content.rendition.ThumbnailSyncQueue;
import com.sirma.sep.content.rendition.ThumbnailSynchronizationDataProvider;

/**
 * Test for {@link ThumbnailSynchronizationDataProvider}
 *
 * @author BBonev
 */
public class ThumbnailSynchronizationDataProviderTest {

	@InjectMocks
	private ThumbnailSynchronizationDataProvider provider;

	@Mock
	private ThumbnailDao thumbnailDao;
	@Spy
	private ConfigurationPropertyMock<Boolean> thumbnailLoaderEnabled = new ConfigurationPropertyMock<>(Boolean.TRUE);
	@Mock
	private ThumbnailSyncQueue syncQueue;
	@Mock
	private ThumbnailConfigurations thumbnailConfigurations;
	@Spy
	private Contextual<AtomicLong> lastSuccessAdd = ContextualReference.create();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(thumbnailConfigurations.getMaxThumbnailRetryCount()).thenReturn(5);
		provider.initLoader();
	}

	@Test
	public void load_noData() throws Exception {
		when(thumbnailDao.getThumbnailsForSynchronization(anyInt())).thenReturn(Collections.emptyList());

		provider.synchThumbnails();

		verify(thumbnailDao).getThumbnailsForSynchronization(anyInt());
		verify(syncQueue, never()).addAll(anyCollection());
	}

	@Test
	public void load() throws Exception {
		when(thumbnailDao.getThumbnailsForSynchronization(anyInt()))
				.thenReturn(Arrays.<Object[]> asList(new Object[] {}));
		when(syncQueue.addAll(anyCollection())).thenReturn(2);

		provider.synchThumbnails();

		verify(thumbnailDao).getThumbnailsForSynchronization(anyInt());
		verify(syncQueue).addAll(anyCollection());
	}

	@Test
	public void load_noSpaceInQueue() throws Exception {
		when(thumbnailDao.getThumbnailsForSynchronization(anyInt()))
				.thenReturn(Arrays.<Object[]> asList(new Object[] {}));
		when(syncQueue.addAll(anyCollection())).thenReturn(0);

		provider.synchThumbnails();

		verify(thumbnailDao).getThumbnailsForSynchronization(anyInt());
		verify(syncQueue).addAll(anyCollection());
	}

	@Test
	public void onActivateDeactivate() throws Exception {
		thumbnailLoaderEnabled.setValue(Boolean.FALSE);
		verify(syncQueue).disable();

		thumbnailLoaderEnabled.setValue(Boolean.TRUE);
		verify(syncQueue).enable();

		thumbnailLoaderEnabled.destroy();
		verify(syncQueue, times(2)).disable();
	}

	@Test
	public void syncWhenDisabled() throws Exception {
		thumbnailLoaderEnabled.setValue(Boolean.FALSE);

		provider.synchThumbnails();

		verify(thumbnailDao, never()).getThumbnailsForSynchronization(anyInt());
	}
}
