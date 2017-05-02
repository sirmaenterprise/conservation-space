package com.sirma.itt.seip.content.rendition;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.json.JsonUtil;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Tests for {@link ThumbnailSyncQueueRest}
 *
 * @author BBonev
 */
public class ThumbnailSyncQueueRestTest {

	@InjectMocks
	private ThumbnailSyncQueueRest queueRest;
	@Mock
	private ThumbnailSyncQueue syncQueue;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getStatus() throws Exception {
		when(syncQueue.size()).thenReturn(0);
		when(syncQueue.getWorkerInfo()).thenReturn(Collections.emptyList());

		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "name", "workerName");
		when(syncQueue.getWorkerInfo()).thenReturn(Collections.singletonList(object));

		String status = queueRest.getStatus();
		assertNotNull(status);
		JsonAssert.assertJsonEquals("{\"active\":0, \"workers\":[{\"name\":\"workerName\"}]}", status);
	}

	@Test
	public void getActive() throws Exception {
		when(syncQueue.size()).thenReturn(0, 5);
		String activeCount = queueRest.getActiveCount();
		assertNotNull(activeCount);
		JsonAssert.assertJsonEquals("{\"active\":0}", activeCount);
		activeCount = queueRest.getActiveCount();
		assertNotNull(activeCount);
		JsonAssert.assertJsonEquals("{\"active\":5}", activeCount);
	}

	@Test
	public void reset() throws Exception {
		queueRest.reset();
		verify(syncQueue).clear();
	}

	@Test
	public void enableWorkers() throws Exception {
		queueRest.start();
		verify(syncQueue).enable();
	}

	@Test
	public void disableWorkers() throws Exception {
		queueRest.stop();
		verify(syncQueue).disable();
	}

	@Test
	public void resetWorkers() throws Exception {
		queueRest.restartWorkers();
		verify(syncQueue).disable();
		verify(syncQueue).enable();
	}

}
