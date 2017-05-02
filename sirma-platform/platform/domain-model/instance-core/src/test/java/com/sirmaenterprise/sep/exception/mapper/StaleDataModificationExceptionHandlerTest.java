package com.sirmaenterprise.sep.exception.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Date;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.exceptions.StaleDataModificationException;
import com.sirma.itt.seip.resources.ResourceService;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link StaleDataModificationExceptionHandler}
 *
 * @author BBonev
 */
public class StaleDataModificationExceptionHandlerTest {

	@InjectMocks
	private StaleDataModificationExceptionHandler exceptionHandler;

	@Mock
	private LabelProvider labelProvider;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private ResourceService resourceService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(typeConverter.convert(eq(String.class), any(Date.class))).thenReturn("2014-04-14T12:00:00.000Z");
		when(resourceService.getDisplayName("admin")).thenReturn("Admin");
		when(labelProvider.getValue("idoc.exception.staleDataModification.message")).thenReturn("Entity modified");
	}

	@Test
	public void testMap() throws Exception {
		Response response = exceptionHandler
				.toResponse(new StaleDataModificationException("modified message", "admin", new Date()));
		assertNotNull(response);
		assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
		assertNotNull(response.getEntity());
		JsonAssert.assertJsonEquals(
				"{\"messages\":{\"staleDataMessage\":\"Entity modified\"},\"modifiedOn\":\"2014-04-14T12:00:00.000Z\",\"modifiedBy\":\"Admin\",\"exception\":\"modified message\"}",
				response.getEntity());
	}

	@Test
	public void testMap_noAdditionalData() throws Exception {
		Response response = exceptionHandler.toResponse(new StaleDataModificationException("modified message"));
		assertNotNull(response);
		assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
		assertNotNull(response.getEntity());
		JsonAssert.assertJsonEquals(
				"{\"messages\":{\"staleDataMessage\":\"Entity modified\"},\"exception\":\"modified message\"}",
				response.getEntity());
	}
}
