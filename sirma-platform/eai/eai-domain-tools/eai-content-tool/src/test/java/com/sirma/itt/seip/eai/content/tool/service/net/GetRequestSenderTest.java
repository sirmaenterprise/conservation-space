package com.sirma.itt.seip.eai.content.tool.service.net;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.sirma.itt.seip.eai.content.tool.exception.EAIException;

public class GetRequestSenderTest {
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().port(11999));

	@Before
	public void setUp() throws Exception {
		wireMockRule.stubFor(get(urlEqualTo("/content/"))
				.willReturn(aResponse().withHeader("Content-Type", "application/vnd.seip.v2+json").withBody(
						"{\"emf:contentId\":\"emf:contentId\",\"name\":\"emfuri.xlsx\",\"mimetype\":\"any\",\"size\":0}")));
	}

	@Test
	public void testSendGet() throws IOException, EAIException {
		String apiUrl = "http://0.0.0.0:11999";
		URI requestUrl = new URIBuilder(URI.create(apiUrl)).append("/content/").build();
		GetRequestSender sender = new GetRequestSender(requestUrl);
		Map<String, String> headers = new HashMap<>();
		headers.put("key", "value");
		sender.init(headers);
		sender.send();
		Set<String> requestUrls = getRequestUrls();
		assertTrue(requestUrls.contains("http://0.0.0.0:11999/content/"));
	}

	private Set<String> getRequestUrls() {
		Set<String> urls = new HashSet<>();
		for (ServeEvent event : wireMockRule.getAllServeEvents()) {
			urls.add(event.getRequest().getAbsoluteUrl());
		}
		return urls;
	}
}
