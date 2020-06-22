package com.sirma.itt.seip.template.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.template.TemplateSearchCriteria;
import com.sirma.itt.seip.testutil.io.FileTestUtils;

public class TemplatesSearchCriteriaBodyReaderTest {

	@InjectMocks
	private TemplatesSearchCriteriaBodyReader reader;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Not_Read_Not_Applicable_Class() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	@Test
	public void should_Read_Applicable_Class() {
		assertTrue(reader.isReadable(TemplateSearchCriteria.class, null, null, null));
	}

	@Test(expected = BadRequestException.class)
	public void should_Throw_Exception_When_Empty_Request() throws WebApplicationException, IOException {
		BufferedInputStream stream = new BufferedInputStream(
				new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
		reader.readFrom(null, null, null, null, null, stream);
	}

	@Test(expected = BadRequestException.class)
	public void should_Throw_Exception_If_Group_Not_Provided() throws IOException {
		reader.readFrom(null, null, null, null, null, FileTestUtils
				.getResourceAsStream("/com/sirma/itt/seip/template/rest/templatesInvalidSearchCriteria.json"));
	}

	@Test
	public void should_Pass_Empty_Map_If_Filter_Not_Provided() throws IOException {
		TemplateSearchCriteria criteria = reader.readFrom(null, null, null, null, null, FileTestUtils
				.getResourceAsStream("/com/sirma/itt/seip/template/rest/templatesSearchCriteriaNullFilter.json"));

		assertNotNull(criteria);
		assertNotNull(criteria.getFilter());
		assertEquals(0, criteria.getFilter().size());
	}

	@Test
	public void should_Build_Correct_Criteria_From_Request_Json() throws IOException {
		TemplateSearchCriteria criteria = reader.readFrom(null, null, null, null, null, FileTestUtils
				.getResourceAsStream("/com/sirma/itt/seip/template/rest/templatesSearchCriteria.json"));
		assertNotNull(criteria);
		assertNotNull(criteria.getGroup());
		assertNotNull(criteria.getPurpose());
		assertNotNull(criteria.getFilter());

		assertEquals("ET220001", criteria.getGroup());
		assertEquals("creatable", criteria.getPurpose());

		Map<String, Serializable> expectedFilterMap = new HashMap<>();
		expectedFilterMap.put("department", "QAL");
		expectedFilterMap.put("functional", "QAS");
		expectedFilterMap.put("filterCodelist", "3");
		assertEquals(expectedFilterMap, criteria.getFilter());
	}
}
