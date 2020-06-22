package com.sirma.sep.content.rendition;

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
import com.sirma.sep.content.rendition.ThumbnailSyncQueue;
import com.sirma.sep.content.rendition.ThumbnailSyncQueueRest;

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
		queueRest.reset(null);
		verify(syncQueue).clear();

		queueRest.reset("someProvider");
		verify(syncQueue).clear("someProvider");
	}

	@Test
	public void resetWorkers() throws Exception {
		queueRest.restartWorkers();
		verify(syncQueue).disable();
		verify(syncQueue).enable();
	}

}
