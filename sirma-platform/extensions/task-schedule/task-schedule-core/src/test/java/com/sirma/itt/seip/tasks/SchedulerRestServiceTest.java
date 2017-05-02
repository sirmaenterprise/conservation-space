package com.sirma.itt.seip.tasks;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.tasks.entity.SchedulerEntity;

/**
 * Test class for {@link SchedulerRestService}
 *
 * @author BBonev
 */
@Test
public class SchedulerRestServiceTest {

	/** The scheduler service. */
	@Mock
	private SchedulerService schedulerService;

	/** The type converter. */
	@Mock
	private TypeConverter typeConverter;

	/** The db dao. */
	@Mock
	private DbDao dbDao;

	/** The rest service. */
	@InjectMocks
	private SchedulerRestService restService;

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(typeConverter.convert(eq(JSONObject.class), any(SchedulerEntry.class))).then(invocation -> ((SchedulerEntry) invocation.getArguments()[1]).toJSONObject());
		when(typeConverter.convert(eq(JSONObject.class), anyList())).then(invocation -> {
			List<JSONObject> result = new LinkedList<>();
			for (SchedulerEntry entry : (Collection<SchedulerEntry>) invocation.getArguments()[1]) {
				result.add(entry.toJSONObject());
			}
			return result;
		});
	}
	
	/**
 	 * Test method stop() scenario not found.
	 */
	public void stop() {
		//setup test 
		String timerIdentifier = "timerIdentifier";
		
		DefaultSchedulerConfiguration configuration = Mockito.mock(DefaultSchedulerConfiguration.class);
		when(configuration.toJSONObject()).thenReturn(new JSONObject());
		
		SchedulerEntry active = new SchedulerEntry();
		active.setIdentifier(timerIdentifier);
		active.setStatus(SchedulerEntryStatus.RUNNING);
		active.setConfiguration(configuration);
		
		when(schedulerService.getScheduleEntry(timerIdentifier)).thenReturn(active);
		
		//execute tested method
		Response response = restService.stop("timerIdentifier");
		
		//verification
		Assert.assertEquals(active.getStatus(), SchedulerEntryStatus.COMPLETED);
		Mockito.verify(schedulerService).save(active);
		Mockito.verify(configuration).setRemoveOnSuccess(true);
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
	}

	/**
	 * Test method stop() scenario not found.
	 */
	@Test(expectedExceptions = ResourceNotFoundException.class)
	public void stopNotFound() {
		Response response = restService.stop("not_exist_identifier");
	}
	
	/**
	 * List all active.
	 */
	public void listAllActive() {
		when(dbDao.fetchWithNamed(eq(SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_STATUS_KEY),
				eq(Collections.singletonList(new Pair<String, Object>("status", SchedulerEntryStatus.ACTIVE_STATES)))))
						.thenReturn(Collections.<Object> singletonList(1L));

		SchedulerEntry active = new SchedulerEntry();
		active.setIdentifier("test1");
		active.setStatus(SchedulerEntryStatus.RUNNING);

		when(schedulerService.loadByDbId(anyList())).thenReturn(Collections.singletonList(active));

		Response response = restService.listAll(Boolean.TRUE);
		JSONArray data = readOkData(response, 1);
		JSONObject object = data.optJSONObject(0);
		assertEquals(JsonUtil.getStringValue(object, "status"), active.getStatus().toString());
		assertEquals(JsonUtil.getStringValue(object, "identifier"), active.getIdentifier());
	}

	/**
	 * List all.
	 */
	public void listAll() {
		when(dbDao.fetchWithNamed(SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_STATUS_KEY,
				Collections.singletonList(new Pair<String, Object>("status", SchedulerEntryStatus.ALL_STATES))))
						.thenReturn(Arrays.<Object> asList(1L, 2L));

		SchedulerEntry active = new SchedulerEntry();
		active.setIdentifier("test1");
		active.setStatus(SchedulerEntryStatus.RUNNING);
		SchedulerEntry inactive = new SchedulerEntry();
		inactive.setIdentifier("test2");
		inactive.setStatus(SchedulerEntryStatus.FAILED);

		when(schedulerService.loadByDbId(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(active, inactive));

		Response response = restService.listAll(Boolean.FALSE);
		JSONArray data = readOkData(response, 2);
		JSONObject object = data.optJSONObject(0);
		assertEquals(JsonUtil.getStringValue(object, "status"), active.getStatus().toString());
		assertEquals(JsonUtil.getStringValue(object, "identifier"), active.getIdentifier());

		object = data.optJSONObject(1);
		assertEquals(JsonUtil.getStringValue(object, "status"), inactive.getStatus().toString());
		assertEquals(JsonUtil.getStringValue(object, "identifier"), inactive.getIdentifier());
	}

	/**
	 * Test get.
	 */
	public void testGet() {
		SchedulerEntry active = new SchedulerEntry();
		active.setIdentifier("test1");
		active.setStatus(SchedulerEntryStatus.RUNNING);

		when(schedulerService.getScheduleEntry("test1")).thenReturn(active);

		Response response = restService.get("test1");
		JSONArray data = readOkData(response, 1);
		JSONObject object = data.optJSONObject(0);
		assertEquals(JsonUtil.getStringValue(object, "status"), active.getStatus().toString());
		assertEquals(JsonUtil.getStringValue(object, "identifier"), active.getIdentifier());
	}

	/**
	 * Test get_not found.
	 */
	public void testGet_notFound() {
		Response response = restService.get("test1");
		readErrorResponse(response, Status.NOT_FOUND);
	}

	/**
	 * Read ok data.
	 *
	 * @param response
	 *            the response
	 * @param expectedsize
	 *            the expected number of responses
	 * @return the JSON array
	 */
	protected JSONArray readOkData(Response response, int expectedsize) {
		assertNotNull(response);
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		assertNotNull(response.getEntity());
		Object data = RestUtil.readDataRequest(response.getEntity().toString());
		assertNotNull(data);
		assertTrue(data instanceof JSONArray);
		JSONArray arrayData = (JSONArray) data;
		assertEquals(arrayData.length(), expectedsize);
		return arrayData;
	}

	/**
	 * Read error response.
	 *
	 * @param response
	 *            the response
	 * @param status
	 *            the expected status code
	 */
	protected void readErrorResponse(Response response, Status status) {
		AssertJUnit.assertNotNull(response);
		AssertJUnit.assertEquals(response.getStatus(), status.getStatusCode());
	}

}
