package com.sirma.itt.seip.eai.content.tool.service.net;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URI;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;

/**
 * Tests for {@link MultipartSender}
 * 
 * @author gshevkedov
 */
public class MultipartSenderTest {
	private File file = null;
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().port(11999));

	@Before
	public void setUp() throws Exception {
		wireMockRule.stubFor(post(urlEqualTo("/content/"))
				.willReturn(aResponse().withHeader("Content-Type", "application/vnd.seip.v2+json").withBody(
						"{\"emf:contentId\":\"emf:contentId\",\"name\":\"emfuri.xlsx\",\"mimetype\":\"any\",\"size\":0}")));
		file = File.createTempFile("name", ".txt");
	}

	@Test
	public void testMultipartUtility() throws Exception {
		String apiUrl = "http://0.0.0.0:11999";
		URI requestUrl = new URIBuilder(URI.create(apiUrl)).append("/content/").build();
		MultipartSender utility = (MultipartSender) new MultipartSender(requestUrl).init(Collections.emptyMap());
		utility.addFilePart("fieldName", file);
		ContentInfo result = utility.send();
		assertNotNull(result);
	}

	@After
	public void tearDown() throws Exception {
		if (!file.delete()) {
			file.deleteOnExit();
		}
	}
}
