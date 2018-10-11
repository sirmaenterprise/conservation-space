package com.sirmaenterprise.sep.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Test for {@link RecentActivitiesRequestReader}.
 *
 * @author A. Kunchev
 */
public class RecentActivitiesRequestReaderTest {

	private static final String REQUEST_PAYLOAD_TEST_FILE = "recent-activities-request-payload-test.json";

	@InjectMocks
	private RecentActivitiesRequestReader reader;

	@Mock
	private MultivaluedMap<String, String> quertyParamsMap;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private RequestInfo request;

	@Before
	public void setup() {
		reader = new RecentActivitiesRequestReader();
		MockitoAnnotations.initMocks(this);

		when(uriInfo.getQueryParameters()).thenReturn(quertyParamsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void isReadable_incorrectType() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	@Test
	public void isReadable_correctType() {
		assertTrue(reader.isReadable(RecentActivitiesRequest.class, null, null, null));
	}

	@Test
	public void readFrom_withEmptyPayload() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("{}".getBytes())) {
			RecentActivitiesRequest activityRequest = reader.readFrom(null, null, null, null, null, stream);
			assertFalse(activityRequest.getDateRange().isPresent());
			assertEquals(0, activityRequest.getLimit());
			assertEquals(0, activityRequest.getOffset());
			assertNull(activityRequest.getIds());
		}
	}

	@Test
	public void readFrom_withPayload() throws IOException {
		try (InputStream stream = RecentActivitiesRequestReader.class
				.getClassLoader()
					.getResourceAsStream(REQUEST_PAYLOAD_TEST_FILE)) {
			RecentActivitiesRequest activityRequest = reader.readFrom(null, null, null, null, null, stream);
			assertTrue(activityRequest.getDateRange().isPresent());
			assertEquals(10, activityRequest.getLimit());
			assertEquals(0, activityRequest.getOffset());
			assertFalse(activityRequest.getIds().isEmpty());
		}
	}

}
