package com.sirma.itt.seip.export.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;

/**
 * Tests for {@link ExportListDataBodyReader}
 *
 * @author gshevkedov
 */
public class ExportListDataBodyReaderTest {

	@InjectMocks
	private ExportListDataBodyReader reader;

	@Mock
	private RequestInfo request;

	@Mock
	private SearchService searchService;

	@Mock
	private JsonToConditionConverter convertor;

	@Before
	public void setUp() {
		reader = new ExportListDataBodyReader();
		MockitoAnnotations.initMocks(this);

		setupPathParamId();
	}

	private void setupPathParamId() {
		MultivaluedMap<String, String> paramsMap = mock(MultivaluedMap.class);
		when(paramsMap.get("id")).thenReturn(Arrays.asList("instanceId"));
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void isReadable_incorrectClass() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	@Test
	public void isReadable_correctClass() {
		assertTrue(reader.isReadable(ExportListDataXlsxRequest.class, null, null, null));
	}

	@Test(expected = JsonException.class)
	public void readFrom_notAObject() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("[]".getBytes())) {
			reader.readFrom(ExportListDataXlsxRequest.class, null, null, null, null, stream);
		}
	}

	@Test(expected = BadRequestException.class)
	public void readFrom_emptyJson() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("{}".getBytes())) {
			reader.readFrom(ExportListDataXlsxRequest.class, null, null, null, null, stream);
		}
	}

	@Test
	public void readFrom_successful() throws IOException {
		try (InputStream stream = ExportListDataBodyReaderTest.class
				.getResourceAsStream("/export-xlsx-request-test.json")) {
			ExportListDataXlsxRequest exportRequest = reader.readFrom(ExportListDataXlsxRequest.class, null, null, null,
					null, stream);
			assertEquals("Title", exportRequest.getFileName());
		}
	}

	@Test
	public void readFrom_successful_useCriteriaWhenSelectedObjectsIsEmpty() throws IOException {
		try (InputStream stream = ExportListDataBodyReaderTest.class
				.getResourceAsStream("/export-xlsx-request-test-criteria.json")) {
			Mockito.when(convertor.parseCondition(Mockito.any(JsonObject.class))).thenReturn(new Condition());
			UriInfo uriInfo = Mockito.mock(UriInfo.class);
			Mockito.when(request.getUriInfo()).thenReturn(uriInfo);
			MultivaluedMap<String, String> qparams = Mockito.mock(MultivaluedMap.class);
			Mockito.when(request.getUriInfo().getQueryParameters()).thenReturn(qparams);
			Mockito.when(request.getUriInfo().getPathParameters()).thenReturn(qparams);
			SearchArguments<Instance> searchArgs = Mockito.mock(SearchArguments.class);
			Mockito.when(searchService.parseRequest(Mockito.any(SearchRequest.class))).thenReturn(searchArgs);
			List<Instance> instances = new ArrayList<Instance>();
			Mockito.when(searchArgs.getResult()).thenReturn(instances);
			ExportListDataXlsxRequest exportRequest = reader.readFrom(ExportListDataXlsxRequest.class, null, null, null,
					null, stream);
			assertEquals("Title", exportRequest.getFileName());
		}
	}
}
