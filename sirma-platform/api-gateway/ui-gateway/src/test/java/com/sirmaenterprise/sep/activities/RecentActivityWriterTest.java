package com.sirmaenterprise.sep.activities;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.audit.processor.RecentActivity;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.models.SearchResponseWrapper;
import com.sirma.itt.seip.testutil.io.FileTestUtils;

/**
 * Tests for {@link RecentActivityWriter}.
 *
 * @author yasko
 */
public class RecentActivityWriterTest {

	@Mock
	private Instance user;

	@Mock
	private ParameterizedType genericType;

	@Mock
	private TypeConverter typeConverter;

	@InjectMocks
	private RecentActivityWriter writer;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(user.getId()).thenReturn("1");
		when(user.getLabel()).thenReturn("test user");

		when(typeConverter.convert(eq(String.class), any(Date.class))).thenReturn("2016-10-17T17:44:24.613+03:00");
	}

	@Test
	public void testIsWritable() {
		when(genericType.getActualTypeArguments()).thenReturn(new Type[] { RecentActivity.class });

		Assert.assertTrue(writer.isWriteable(SearchResponseWrapper.class, genericType, null, null));
	}

	@Test
	public void testIsNotWritable() {
		Assert.assertFalse(writer.isWriteable(Integer.class, null, null, null));

		when(genericType.getActualTypeArguments()).thenReturn(new Type[] { String.class });
		Assert.assertFalse(writer.isWriteable(SearchResponseWrapper.class, genericType, null, null));

		Assert.assertFalse(writer.isWriteable(SearchResponseWrapper.class, null, null, null));
	}

	@Test
	public void testWriteEmptyList() throws Exception {
		SearchResponseWrapper<RecentActivity> list = new SearchResponseWrapper<>();
		list.setResults(Collections.emptyList());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeTo(list, null, null, null, null, null, out);

		JsonArray actual = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readArray();
		JsonArray expected = Json.createArrayBuilder().build();

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testWriteList() throws Exception {
		SearchResponseWrapper<RecentActivity> list = new SearchResponseWrapper<>();
		list.setResults(Arrays.asList(new RecentActivity(user, new Date(), "it's just a test bro")));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeTo(list, null, null, null, null, null, out);

		JsonArray actual = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readArray();

		try (InputStream stream = FileTestUtils
				.getResourceAsStream("/com/sirmaenterprise/sep/activities/history-rest-result.json")) {
			JsonArray expected = Json.createReader(stream).readArray();

			Assert.assertEquals(expected, actual);
		}
	}
}
