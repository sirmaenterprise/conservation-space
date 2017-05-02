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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.rest.client.HTTPClient;
import com.sirma.itt.seip.testutil.io.RandomPortGenerator;
import com.sirma.seip.semantic.management.RepositoryConfiguration;
import com.sirma.seip.semantic.management.RepositoryInfo;
import com.sirma.seip.semantic.management.SolrConnectorConfiguration;

/**
 * The Class HTTPRepositoryManagerTest.
 */
public class HTTPRepositoryManagerTest {

	private static final String REPO_NAME = "repoName";

	private static int PORT = RandomPortGenerator.generatePort(8900, 9000);

	private static final URI REPO_ADDRESS = URI.create("http://localhost:" + PORT + "/graphdb-workbench");

	private static final String CONNECTOR_CHECK_URI = "/graphdb-workbench/repositories/repoName?query=PREFIX+solr-inst%3A+%3Chttp%3A%2F%2Fwww.ontotext.com%2Fconnectors%2Fsolr%2Finstance%23%3E%0APREFIX+solr%3A+%3Chttp%3A%2F%2Fwww.ontotext.com%2Fconnectors%2Fsolr%23%3E%0ASELECT+%3FcntUri+%3FcntStatus+%7B%0A++solr-inst%3Afts_connector++solr%3AconnectorStatus+%3FcntStatus+.%0A%7D";
	private static final String REPO_CHECK_URI = "/graphdb-workbench/repositories/repoName?query=select+%3Ftmp+where+%7B+BIND%281+as+%3Ftmp%29.%7D";

	private static final String REPO_EXISTS_RESPONSE = "{\n" + "  \"head\" : {\n" + "    \"vars\" : [ \"tmp\" ]\n"
			+ "  },\n" + "  \"results\" : {\n" + "    \"bindings\" : [ {\n" + "      \"tmp\" : {\n"
			+ "        \"datatype\" : \"http://www.w3.org/2001/XMLSchema#integer\",\n"
			+ "        \"type\" : \"literal\",\n" + "        \"value\" : \"1\"\n" + "      }\n" + "    } ]\n" + "  }\n"
			+ "}";

	private static final String CONNECTOR_EXISTS_RESPONSE = "{\n" + "  \"head\" : {\n"
			+ "   \"vars\" : [ \"cntUri\", \"cntStatus\" ]\n" + "  },\n" + " \"results\" : {\n"
			+ "  \"bindings\" : [ {\n" + "   \"cntStatus\" : {\n" + "      \"type\" : \"literal\",\n"
			+ "      \"value\" : \"OK\"\n" + "   }\n" + "  } ]\n" + " }\n" + "}";

	private static final String CONNECTOR_NOT_EXISTS_RESPONSE = "{\n" + "  \"head\" : {\n"
			+ "   \"vars\" : [ \"cntUri\", \"cntStatus\" ]\n" + "  },\n" + " \"results\" : {\n"
			+ "  \"bindings\" : [ ]\n" + " }\n" + "}";

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
	public void isConnectorExists_yes() throws Exception {
		mockConnectorExists(CONNECTOR_EXISTS_RESPONSE);

		RepositoryInfo configuration = getRepoInfo();
		assertTrue(manager.isSolrConnectorPresent(configuration, "fts_connector"));
	}

	@Test
	public void isConnectorExists_no() throws Exception {
		mockConnectorExists(CONNECTOR_NOT_EXISTS_RESPONSE);

		RepositoryInfo configuration = getRepoInfo();
		assertFalse(manager.isSolrConnectorPresent(configuration, "fts_connector"));
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

	@Test
	public void createSolrConnector() throws Exception {
		mockRepoExists(REPO_EXISTS_RESPONSE);

		stubFor(post(urlEqualTo("/graphdb-workbench" + HTTPRepositoryManager.REPOSITORIES + REPO_NAME
				+ HTTPRepositoryManager.STATEMENTS)).willReturn(aResponse().withStatus(204)));

		SolrConnectorConfiguration configuration = new SolrConnectorConfiguration(getRepoInfo(), "fts_connector",
				"solrConnector", null);
		manager.createSolrConnector(configuration);
	}

	@Test(expected = IllegalStateException.class)
	public void createSolrConnector_notRepo() throws Exception {
		mockRepoExists(REPO_NOT_EXISTS_RESPONSE);

		stubFor(post(urlEqualTo("/graphdb-workbench" + HTTPRepositoryManager.REST_REPOSITORIES + REPO_NAME
				+ HTTPRepositoryManager.STATEMENTS)).willReturn(aResponse().withStatus(204)));

		SolrConnectorConfiguration configuration = new SolrConnectorConfiguration(getRepoInfo(), "fts_connector",
				"solrConnector", null);
		manager.createSolrConnector(configuration);
	}

	@Test
	public void createSolrConnector_testData() throws Exception {
		mockRepoExists(REPO_EXISTS_RESPONSE);

		stubFor(post(urlEqualTo("/graphdb-workbench" + HTTPRepositoryManager.REPOSITORIES + REPO_NAME
				+ HTTPRepositoryManager.STATEMENTS)).willReturn(aResponse().withStatus(204)));

		SolrConnectorConfiguration configuration = new SolrConnectorConfiguration(getRepoInfo(), "fts_connector",
				"@prefix solrConnector",
				IOUtils.toString(this.getClass().getResourceAsStream("init_solr_schema.trig")));
		manager.createSolrConnector(configuration);
	}

	@Test
	public void deleteSolrConnector() throws Exception {
		mockRepoExists(REPO_EXISTS_RESPONSE);
		mockConnectorExists(CONNECTOR_EXISTS_RESPONSE);

		stubFor(post(urlEqualTo("/graphdb-workbench" + HTTPRepositoryManager.REPOSITORIES + REPO_NAME
				+ HTTPRepositoryManager.STATEMENTS)).willReturn(aResponse().withStatus(204)));

		manager.deleteSolrConnector(getRepoInfo(), "fts_connector");
	}

	@Test(expected = IllegalStateException.class)
	public void deleteSolrConnector_noRepo() throws Exception {
		mockRepoExists(REPO_NOT_EXISTS_RESPONSE);

		manager.deleteSolrConnector(getRepoInfo(), "fts_connector");
	}

	@Test
	public void deleteSolrConnector_noConnector() throws Exception {
		mockRepoExists(REPO_EXISTS_RESPONSE);
		mockConnectorExists(CONNECTOR_NOT_EXISTS_RESPONSE);

		manager.deleteSolrConnector(getRepoInfo(), "fts_connector");
	}

	@Test
	public void resetSolrConnector() throws Exception {
		mockRepoExists(REPO_EXISTS_RESPONSE);
		mockConnectorExists(CONNECTOR_EXISTS_RESPONSE);

		stubFor(post(urlEqualTo("/graphdb-workbench" + HTTPRepositoryManager.REPOSITORIES + REPO_NAME
				+ HTTPRepositoryManager.STATEMENTS)).willReturn(aResponse().withStatus(204)));

		SolrConnectorConfiguration configuration = new SolrConnectorConfiguration(getRepoInfo(), "fts_connector",
				"solrConnector", null);

		manager.resetSolrConnector(configuration);
	}

	@Test
	public void resetSolrConnector_noConnector() throws Exception {
		mockRepoExists(REPO_EXISTS_RESPONSE);
		mockConnectorExists(CONNECTOR_NOT_EXISTS_RESPONSE);

		stubFor(post(urlEqualTo("/graphdb-workbench" + HTTPRepositoryManager.REPOSITORIES + REPO_NAME
				+ HTTPRepositoryManager.STATEMENTS)).willReturn(aResponse().withStatus(204)));

		SolrConnectorConfiguration configuration = new SolrConnectorConfiguration(getRepoInfo(), "fts_connector",
				"solrConnector", null);

		manager.resetSolrConnector(configuration);
	}

	private static RepositoryInfo getRepoInfo() {
		return new RepositoryInfo(REPO_NAME, "user", "pass", REPO_ADDRESS);
	}

	private static void mockConnectorExists(String data) {
		stubFor(get(urlEqualTo(CONNECTOR_CHECK_URI))
				.withHeader(HttpHeaders.ACCEPT, equalTo(HTTPRepositoryManager.SPARQL_JSON_CONTENT_TYPE))
					.willReturn(aResponse()
							.withStatus(200)
								.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
								.withBody(data)));
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
