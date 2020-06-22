package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;

/**
 * Test for {@link ContentToolProviderRest}.
 *
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentToolProviderRestTest {

	@Mock
	private SecurityContext securityContext;

	@Mock
	private SecurityTokensManager tokensManager;

	@Mock
	private InstanceAccessEvaluator instanceAccessEvaluator;

	@Mock
	private SpreadsheetIntegrationConfiguration spreadsheetIntegrationConfiguration;

	@Mock
	private JwtConfiguration jwtConfiguration;

	@InjectMocks
	private ContentToolProviderRest contentToolProviderRest;

	@Before
	public void beforeEach() {
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser());
		when(tokensManager.generate(any(EmfUser.class))).thenReturn("jwt");
		when(jwtConfiguration.getJwtParameterName()).thenReturn("APIKey");
	}

	@Test
	public void testLoadTool() throws IOException {
		File xml = File.createTempFile("test-content-tool-jnlp.xml", "");
		xml.deleteOnExit();
		
		FileUtils.write(xml, "test");
		
		UriInfo uriInfo = mock(UriInfo.class);
		when(spreadsheetIntegrationConfiguration.getContentToolJNLP()).thenReturn(new ConfigurationPropertyMock<>(xml));
		Response loadedTool = contentToolProviderRest.loadTool(uriInfo, null);
		assertEquals(401, loadedTool.getStatus());

		when(instanceAccessEvaluator.canWrite(isNull(Serializable.class))).thenReturn(Boolean.FALSE);
		loadedTool = contentToolProviderRest.loadTool(uriInfo, null);
		assertEquals(401, loadedTool.getStatus());

		when(instanceAccessEvaluator.canWrite(eq("emf:401"))).thenReturn(Boolean.FALSE);
		loadedTool = contentToolProviderRest.loadTool(uriInfo, "emf:401");
		assertEquals(401, loadedTool.getStatus());

		when(instanceAccessEvaluator.canWrite(eq("emf:id"))).thenReturn(Boolean.TRUE);
		when(uriInfo.resolve(Matchers.any(URI.class))).thenReturn(URI.create("http://localhost/app/eai"));
		when(uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost/app/api"));
		loadedTool = contentToolProviderRest.loadTool(uriInfo, "emf:id");
		assertEquals("test", loadedTool.getEntity().toString());
	}

}
