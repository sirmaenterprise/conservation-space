package com.sirma.sep.export.xlsx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.junit.Test;

import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.sep.export.xlsx.XlsxExportRequest.JsonXlsxExportRequestBuilder;
import com.sirma.sep.export.xlsx.XlsxExportRequest.XlsxExportRequestBuilder;

/**
 * Test for {@link XlsxExportRequest}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class XlsxExportRequestTest {

	private static final String JSON_REQUEST_BUILDER_TEST_JSON = "xlsx-json-request-builder-test.json";
	private static final String JSON_REQUEST_BUILDER_WITHOUT_HEADER_TYPE_TEST_JSON = "xlsx-json-request-builder-without-header-type-test.json";
	private static final String JSON_REQUEST_PROPERTIES_MAP_BUILDER_TEST_JSON = "xlsx-json-request-properties-map-builder-test.json";
	private static final String JSON_REQUEST_SUBPROPERTIES_MAP_BUILDER_TEST_JSON = "xlsx-json-request-subproperties-map-builder-test.json";

	@Test
	public void getName() {
		assertEquals("excel", new XlsxExportRequestBuilder().buildRequest().getName());
	}

	@Test
	public void getObjectsData_returnNotNullResult() {
		XlsxExportRequest request = new XlsxExportRequestBuilder().buildRequest();
		assertNotNull(request.getObjectsData());
	}

	@Test
	public void getSearchData_returnNotNullResult() {
		XlsxExportRequest request = new XlsxExportRequestBuilder().buildRequest();
		assertNotNull(request.getSearchData());
	}

	@Test
	public void getTableConfiguration_returnNotNullResult() {
		XlsxExportRequest request = new XlsxExportRequestBuilder().buildRequest();
		assertNotNull(request.getTableConfiguration());
	}

	@Test
	public void jsonRequestBuilder() throws IOException {
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream(JSON_REQUEST_BUILDER_TEST_JSON)) {
			XlsxExportRequest request = new JsonXlsxExportRequestBuilder(JSON.readObject(stream, Function.identity()))
					.buildRequest();
			assertNotNull(request);
		}
	}

	@Test
	public void jsonRequestBuilderWithoutHeaderType() throws IOException {
		try (InputStream stream = getClass()
				.getClassLoader()
					.getResourceAsStream(JSON_REQUEST_BUILDER_WITHOUT_HEADER_TYPE_TEST_JSON)) {
			XlsxExportRequest request = new JsonXlsxExportRequestBuilder(JSON.readObject(stream, Function.identity()))
					.buildRequest();
			assertNotNull(request);
		}
	}
	
	@Test
	public void jsonRequestBuilderMapProperties() throws IOException {
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream(JSON_REQUEST_PROPERTIES_MAP_BUILDER_TEST_JSON)) {
			XlsxExportRequest request = new JsonXlsxExportRequestBuilder(JSON.readObject(stream, Function.identity()))
					.buildRequest();
			assertNotNull(request);
			
			Map<String, List<String>> expected = new HashMap<>();
			expected.put("ET120001", new LinkedList<>(Arrays.asList("title", "hasParent")));
			expected.put("GEC20001", new LinkedList<>(Arrays.asList("title", "identifier")));
			expected.put("COMMON_PROPERTIES", new LinkedList<>());
			expected.put("testImage", new LinkedList<>(Arrays.asList("hasParent")));
			expected.put("GEP10002", new LinkedList<>(Arrays.asList("identifier", "type", "emf:version", "hasParent")));

			assertEquals(expected, request.getObjectsData().getSelectedProperties());
		}
	}

	@Test
	public void jsonRequestBuilderMapSubProperties() throws IOException {
		try (InputStream stream = getClass().getClassLoader()
				.getResourceAsStream(JSON_REQUEST_SUBPROPERTIES_MAP_BUILDER_TEST_JSON)) {
			XlsxExportRequest request = new JsonXlsxExportRequestBuilder(JSON.readObject(stream, Function.identity()))
					.buildRequest();
			assertNotNull(request);

			Map<String, Set<String>> expected = new HashMap<>();
			Set<String> values = new HashSet<>();
			values.add("title");
			values.add("emf:department");
			expected.put("hasParent", values);

			assertEquals(expected, request.getObjectsData().getSelectedSubProperties());
		}
	}

}
