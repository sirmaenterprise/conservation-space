package com.sirma.seip.semantic.management.graphdb;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpHeaders;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.rest.client.HTTPClient;
import com.sirma.itt.seip.testutil.rest.RandomPortGenerator;
import com.sirma.seip.semantic.management.RepositoryConfiguration;
import com.sirma.seip.semantic.management.RepositoryInfo;

/**
 * The Class HTTPRepositoryManagerTest.
 */
public class HTTPRepositoryManagerTest {

	private static final String REPO_NAME = "repoName";

	private static int PORT = RandomPortGenerator.generatePort(8900, 9000);

	private static final URI REPO_ADDRESS = URI.create("http://localhost:" + PORT + "/graphdb-workbench");

	private static final String REPO_CHECK_URI = "/graphdb-workbench/repositories/repoName?query=select+%3Ftmp+where+%7B+BIND%281+as+%3Ftmp%29.%7D";

	private static final String REPO_EXISTS_RESPONSE = "{\n" + "  \"head\" : {\n" + "    \"vars\" : [ \"tmp\" ]\n"
			+ "  },\n" + "  \"results\" : {\n" + "    \"bindings\" : [ {\n" + "      \"tmp\" : {\n"
			+ "        \"datatype\" : \"http://www.w3.org/2001/XMLSchema#integer\",\n"
			+ "        \"type\" : \"literal\",\n" + "        \"value\" : \"1\"\n" + "      }\n" + "    } ]\n" + "  }\n"
			+ "}";

	private static final String REPO_NOT_EXISTS_RESPONSE = "{\n" + "  \"head\" : {\n" + "    \"vars\" : [ \"tmp\" ]\n"
			+ "  },\n" + "  \"results\" : {\n" + "    \"bindings\" : [ ]\n" + "  }\n" + "}";

	private HTTPRepositoryManager manager = new HTTPRepositoryManager(new HTTPClient());

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(PORT);

	@Test
	public void isRepositoryExits_yes() throws Exception {
		mockRepoExists(REPO_EXISTS_RESPONSE);

		RepositoryInfo configuration = getRepoInfo();
		assertTrue(manager.isRepositoryExists(configuration));
	}

	@Test
	public void isRepositoryExits_no() throws Exception {
		mockRepoExists(REPO_NOT_EXISTS_RESPONSE);

		RepositoryInfo configuration = getRepoInfo();
		assertFalse(manager.isRepositoryExists(configuration));
	}

	@Test
	public void isRepositoryExits_notFound() throws Exception {
		stubFor(get(urlEqualTo(REPO_CHECK_URI))
				.withHeader(HttpHeaders.ACCEPT, equalTo(HTTPRepositoryManager.SPARQL_JSON_CONTENT_TYPE))
					.willReturn(aResponse().withStatus(404).withHeader(HttpHeaders.CONTENT_TYPE,
							MediaType.APPLICATION_JSON)));

		RepositoryInfo configuration = getRepoInfo();
		assertFalse(manager.isRepositoryExists(configuration));
	}

	@Test
	public void createRepo() throws Exception {
		stubFor(post(urlEqualTo("/graphdb-workbench" + HTTPRepositoryManager.REST_REPOSITORIES))
				.willReturn(aResponse().withStatus(201)));

		RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
		repositoryConfiguration.setAddress(REPO_ADDRESS);
		repositoryConfiguration.setPassword("pass");
		repositoryConfiguration.setRepositoryName(REPO_NAME);
		repositoryConfiguration.setUserName("user");
		repositoryConfiguration.setLabel("Repo name");
		manager.createRepository(repositoryConfiguration);
	}

	@Test(expected = EmfRuntimeException.class)
	public void createRepo_fail() throws Exception {

		RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
		repositoryConfiguration.setAddress(REPO_ADDRESS);
		repositoryConfiguration.setPassword("pass");
		repositoryConfiguration.setRepositoryName(REPO_NAME);
		repositoryConfiguration.setUserName("user");
		repositoryConfiguration.setLabel("Repo name");
		manager.createRepository(repositoryConfiguration);
	}

	@Test
	public void deleteRepo() throws Exception {
		stubFor(delete(urlEqualTo("/graphdb-workbench" + HTTPRepositoryManager.REST_REPOSITORIES + REPO_NAME))
				.willReturn(aResponse().withStatus(200)));

		RepositoryInfo configuration = getRepoInfo();
		manager.deleteRepository(configuration);
	}

	private static RepositoryInfo getRepoInfo() {
		return new RepositoryInfo(REPO_NAME, "user", "pass", REPO_ADDRESS);
	}

	private static void mockRepoExists(String data) {
		stubFor(get(urlEqualTo(REPO_CHECK_URI))
				.withHeader(HttpHeaders.ACCEPT, equalTo(HTTPRepositoryManager.SPARQL_JSON_CONTENT_TYPE))
					.willReturn(aResponse()
							.withStatus(200)
								.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
								.withBody(data)));
	}
}
