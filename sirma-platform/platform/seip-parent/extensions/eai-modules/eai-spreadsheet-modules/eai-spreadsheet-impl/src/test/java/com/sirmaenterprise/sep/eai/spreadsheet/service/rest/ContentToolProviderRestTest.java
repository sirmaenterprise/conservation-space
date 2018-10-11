package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test for {@link ContentToolProviderRest}.
 *
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentToolProviderRestTest {
	@Mock
	private InstanceAccessEvaluator instanceAccessEvaluator;

	@Mock
	private SpreadsheetIntegrationConfiguration spreadsheetIntegrationConfiguration;
	@Mock
	private JwtConfiguration jwtConfiguration;
	@InjectMocks
	private ContentToolProviderRest contentToolProviderRest;

	@Test
	public void testLoadTool() {
		when(jwtConfiguration.getJwtParameterName()).thenReturn("APIKey");
		UriInfo uriInfo = mock(UriInfo.class);
		when(spreadsheetIntegrationConfiguration.getContentToolJNLP()).thenReturn(new ConfigurationPropertyMock<>(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>%n<jnlp spec=\"1.0+\" xmlns:jfx=\"http://javafx.com\" codebase=\"%1s\">%n\t<information>%n\t\t<title>EAI content import tool</title>%n\t\t<vendor>Sirma Solutions</vendor>%n\t\t<description>SEP content import tool</description>%n\t</information>%n\t<security>%n\t\t<all-permissions/>%n\t</security>%n\t<update check=\"always\" policy=\"always\"/>%n\t<resources>%n\t\t<j2se version=\"1.8+\" href=\"http://java.sun.com/products/autodl/j2se\"/>%n\t\t<jar href=\"%2s\" download=\"eager\" />%n\t</resources>%n\t<jfx:javafx-desc main-class=\"com.sirma.itt.seip.eai.content.tool.Main\" name=\"eai-content-tool\">%n\t\t<fx:param name=\"apiUrl\" value=\"%3s\"/>%n\t\t<fx:param name=\"authorization\" value=\"%4s\"/>%n\t\t<fx:param name=\"uri\" value=\"%5s\"/>%n\t</jfx:javafx-desc>%n</jnlp>"));
		Response loadedTool = contentToolProviderRest.loadTool(uriInfo, null, null);
		assertEquals(401, loadedTool.getStatus());
		when(instanceAccessEvaluator.canWrite(isNull(Serializable.class))).thenReturn(Boolean.FALSE);
		loadedTool = contentToolProviderRest.loadTool(uriInfo, "jwt", null);
		assertEquals(401, loadedTool.getStatus());

		when(instanceAccessEvaluator.canWrite(eq("emf:401"))).thenReturn(Boolean.FALSE);
		loadedTool = contentToolProviderRest.loadTool(uriInfo, "jwt", "emf:401");
		assertEquals(401, loadedTool.getStatus());

		when(instanceAccessEvaluator.canWrite(eq("emf:id"))).thenReturn(Boolean.TRUE);
		when(uriInfo.resolve(Matchers.any(URI.class))).thenReturn(URI.create("http://localhost/app/eai"));
		when(uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost/app/api"));
		loadedTool = contentToolProviderRest.loadTool(uriInfo, "jwt", "emf:id");
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?><jnlp spec=\"1.0+\" xmlns:jfx=\"http://javafx.com\" codebase=\"http://localhost/app/eai\"><information><title>EAI content import tool</title><vendor>Sirma Solutions</vendor><description>SEP content import tool</description></information><security><all-permissions/></security><update check=\"always\" policy=\"always\"/><resources><j2se version=\"1.8+\" href=\"http://java.sun.com/products/autodl/j2se\"/><jar href=\"eai-content-tool-jfx.jar?APIKey=jwt\" download=\"eager\" /></resources><jfx:javafx-desc main-class=\"com.sirma.itt.seip.eai.content.tool.Main\" name=\"eai-content-tool\"><fx:param name=\"apiUrl\" value=\"http://localhost/app/api\"/><fx:param name=\"authorization\" value=\"Bearer jwt\"/><fx:param name=\"uri\" value=\"emf:id\"/></jfx:javafx-desc></jnlp>",
				loadedTool.getEntity().toString().replaceAll("[\r\n\t]", ""));
	}

}
