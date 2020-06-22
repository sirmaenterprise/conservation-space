package com.sirma.sep.content.rendition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.content.rendition.ThumbnailLoader;
import com.sirma.sep.content.rendition.ThumbnailSyncQueue;

/**
 * Tests for {@link ThumbnailSyncQueue}
 *
 * @author BBonev
 */
public class ThumbnailSyncQueueTest {

	@InjectMocks
	private ThumbnailSyncQueue syncQueue;

	@Spy
	private ConfigurationPropertyMock<Integer> workerGroup = new ConfigurationPropertyMock<>();
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	private ThumbnailLoader thumbnailLoader;
	@Mock
	private GroupConverterContext converterContext;

	@BeforeMethod
	public void beforeMethod() throws Exception {
		MockitoAnnotations.initMocks(this);

		Method method = ThumbnailSyncQueue.class.getDeclaredMethod("buildWorkerGroup", GroupConverterContext.class,
				SecurityContextManager.class, ThumbnailLoader.class);
		when(converterContext.get(ThumbnailSyncQueue.STORE_CAPACITY)).thenReturn(100);
		when(converterContext.get(ThumbnailSyncQueue.WORKER_THREADS)).thenReturn(1);

		workerGroup.setSupplier(method, ThumbnailSyncQueue.class, converterContext, securityContextManager,
				thumbnailLoader);

		syncQueue.initQueue();
		syncQueue.disable();
		syncQueue.enable();
	}

	@Test
	public void shouldProcessSingleItem() throws Exception {
		assertTrue(syncQueue.add("id", "endPoint", "providerName", 1));

		verify(thumbnailLoader, timeout(2500)).load("id", "endPoint", "providerName", 1);
	}

	@Test
	public void shouldAutoEnableIfNoActiveWorkers() throws Exception {
		syncQueue.disable();
		assertEquals(3,
				syncQueue.addAll(Arrays.<Object[]> asList(new Object[] { null, "endPoint", "providerName", 1 },
						new Object[] { "id", null, "providerName", 1 }, new Object[] { "id", "endPoint", null, 1 },
						new Object[] { "id", "endPoint", "providerName", null },
						new Object[] { "id", "endPoint", "providerName", 1 },
						new Object[] { "id", "endPoint2", "providerName", 1 },
						new Object[] { "id", "endPoint3", "providerName", 1 })));

		verify(thumbnailLoader, timeout(2500).times(3)).load(anyString(), anyString(), anyString(), any(Integer.class));
	}

	@Test
	public void getWorkerStatus() throws Exception {
		assertTrue(syncQueue.add("id", "endPoint", "providerName", 1));

		verify(thumbnailLoader, timeout(2500)).load("id", "endPoint", "providerName", 1);
		Collection<JSONObject> info = syncQueue.getWorkerInfo();
		assertNotNull(info);
		assertFalse(info.isEmpty());
	}

	@AfterMethod
	public void cleanup() {
		syncQueue.disable();
	}
}
