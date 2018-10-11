package com.sirma.itt.seip.template.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Tests the functionality of {@link EditTemplateRuleActionRequestReader}.
 * 
 * @author Vilizar Tsonev
 */
public class EditTemplateRuleActionRequestReaderTest {

	@InjectMocks
	private EditTemplateRuleActionRequestReader reader;

	@Mock
	private RequestInfo info;

	@Mock
	private UriInfo uriInfo;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(info.getUriInfo()).thenReturn(uriInfo);
		when(uriInfo.getPathParameters()).thenReturn(new MultivaluedHashMap<>());

	}

	@Test
	public void should_Parse_EditTemplateRule_Request_Correctly() throws Exception {
		uriInfo.getPathParameters().add(RequestParams.KEY_ID, "emf:targetInstanceId");
		EditTemplateRuleActionRequest request = reader.readFrom(null, null, null, null, null,
				getRequestData("/edit-template-rule-request.json"));
		assertNotNull(request);
		assertEquals("emf:targetInstanceId", request.getTargetId());
		assertEquals("editTemplateRule", request.getUserOperation());
		assertEquals("primary == true && (department == \"DEV\" || department == \"QA\")", request.getRule());
	}

	@Test
	public void should_Parse_Null_Rule_Correctly() throws Exception {
		uriInfo.getPathParameters().add(RequestParams.KEY_ID, "emf:targetInstanceId");
		EditTemplateRuleActionRequest request = reader.readFrom(null, null, null, null, null,
				getRequestData("/edit-template-null-rule-request.json"));
		assertNotNull(request);
		assertEquals("emf:targetInstanceId", request.getTargetId());
		assertEquals("editTemplateRule", request.getUserOperation());
		assertNull(request.getRule());
	}

	@Test(expected = BadRequestException.class)
	public void should_Throw_BadRequestException_When_Missing_TargetId() throws IOException {
		reader.readFrom(null, null, null, null, null,
				getRequestData("/edit-template-rule-request.json"));
	}

	private static InputStream getRequestData(String fileName) {
		return EditTemplateRuleActionRequestReaderTest.class.getClassLoader().getResourceAsStream(
				EditTemplateRuleActionRequestReaderTest.class.getPackage().getName().replace('.', '/')
						+ fileName);
	}

}
