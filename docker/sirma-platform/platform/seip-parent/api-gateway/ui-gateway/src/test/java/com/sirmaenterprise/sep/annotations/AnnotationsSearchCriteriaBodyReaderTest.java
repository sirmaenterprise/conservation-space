package com.sirmaenterprise.sep.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.testutil.io.FileTestUtils;

/**
 * Tests the functionality of {@link AnnotationsSearchCriteriaBodyReader}.
 *
 * @author Vilizar Tsonev
 */
public class AnnotationsSearchCriteriaBodyReaderTest {

	@InjectMocks
	private AnnotationsSearchCriteriaBodyReader reader;

	@Mock
	private DateConverter dateConverter;

	@Mock
	private JsonToConditionConverter jsonToConditionConverter;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(dateConverter.parseDate(anyString())).thenReturn(new Date());
		when(jsonToConditionConverter.parseCondition(any(JsonObject.class))).thenReturn(
				SearchCriteriaBuilder.createConditionBuilder().build());
	}

	/**
	 * Tests {@link AnnotationsSearchCriteriaBodyReader#isReadable()} with an invalid class
	 */
	@Test
	public void testStringClassNotReadable() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	/**
	 * Tests {@link AnnotationsSearchCriteriaBodyReader#isReadable()} with an valid class
	 */
	@Test
	public void testIsReadable() {
		assertTrue(reader.isReadable(AnnotationsSearchCriteria.class, null, null, null));
	}

	/**
	 * Tests if {@link BadRequestException} is thrown when an empty JSON is passed as a payload.
	 */
	@Test(expected = BadRequestException.class)
	public void testReadEmptyJson() throws WebApplicationException, IOException {
		BufferedInputStream stream = new BufferedInputStream(
				new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
		reader.readFrom(null, null, null, null, null, stream);
	}

	/**
	 * Verifies that all JSON parameters are read and the {@link AnnotationsSearchCriteria} is properly built.
	 */
	@Test
	public void testCriteriaIsProperlyBuilt() throws IOException {
		AnnotationsSearchCriteria criteria = reader.readFrom(null, null, null, null, null, FileTestUtils
				.getResourceAsStream("/com/sirmaenterprise/sep/annotations/annotationsSearchCriteria.json"));
		assertNotNull(criteria);
		assertNotNull(criteria.getSearchTree());
		assertNotNull(criteria.getCreatedFrom());
		assertNotNull(criteria.getCreatedTo());
		assertEquals("object1", criteria.getManuallySelectedObjects().get(0));
		assertEquals("object2", criteria.getManuallySelectedObjects().get(1));
		assertEquals("user1", criteria.getUserIds().get(0));
		assertEquals("user2", criteria.getUserIds().get(1));
		assertEquals(Integer.valueOf(1), criteria.getOffset());
		assertEquals(Integer.valueOf(30), criteria.getLimit());
	}

}
