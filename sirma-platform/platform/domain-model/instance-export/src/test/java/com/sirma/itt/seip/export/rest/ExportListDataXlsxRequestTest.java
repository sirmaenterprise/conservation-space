package com.sirma.itt.seip.export.rest;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for {@link ExportListDataXlsxRequest}
 *
 * @author gshevkedov
 */
public class ExportListDataXlsxRequestTest {

	/**
	 * Test method for {@link com.sirma.itt.seip.export.rest.ExportListDataXlsxRequest#getOperation()}.
	 */
	@Test
	public void testGetOperation() {
		ExportListDataXlsxRequest request = new ExportListDataXlsxRequest();
		assertEquals(ExportListDataXlsxRequest.EXPORT_XLSX, request.getOperation());
	}

	/**
	 * Test method for {@link com.sirma.itt.seip.export.rest.ExportListDataXlsxRequest#getHeaderType()}.
	 */
	@Test
	public void testHeaderType() {
		ExportListDataXlsxRequest request = new ExportListDataXlsxRequest();
		request.setHeaderType("someHeaderType");
		assertEquals("someHeaderType", request.getHeaderType());
	}

	/**
	 * Test method for {@link com.sirma.itt.seip.export.rest.ExportListDataXlsxRequest#getSelectedInstances()}.
	 */
	@Test
	public void testSelectedInstances() {
		ExportListDataXlsxRequest request = new ExportListDataXlsxRequest();
		List<String> instances = Arrays.asList("someInstance");
		request.setSelectedInstances(instances);
		assertEquals(instances, request.getSelectedInstances());
	}

	/**
	 * Test method for {@link com.sirma.itt.seip.export.rest.ExportListDataXlsxRequest#getSelectedProperties()}.
	 */
	@Test
	public void testSelectedProperties() {
		ExportListDataXlsxRequest request = new ExportListDataXlsxRequest();
		Set<String> properties = new HashSet<>(1);
		properties.add("someProperty");
		request.setSelectedProperties(properties);
		assertEquals(properties, request.getSelectedProperties());
	}
}
