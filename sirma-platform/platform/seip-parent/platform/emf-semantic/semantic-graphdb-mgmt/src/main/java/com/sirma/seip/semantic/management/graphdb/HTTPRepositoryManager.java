package com.sirma.seip.semantic.management.graphdb;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rest.client.HTTPClient;
import com.sirma.seip.semantic.management.RepositoryConfiguration;
import com.sirma.seip.semantic.management.RepositoryInfo;
import com.sirma.seip.semantic.management.RepositoryManagement;

/**
 * Repository creator that creates semantic repository using HTTP requests
 *
 * @author kirq4e
 */
@Singleton
public class HTTPRepositoryManager implements RepositoryManagement {

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPRepositoryManager.class);

	public static final String TURTLE_CONTENT_TYPE = "application/x-turtle";
	public static final String SPARQL_JSON_CONTENT_TYPE = "application/sparql-results+json";

	private static final String ENTITY_INDEX_SIZE_PARAM = "{entity-index-size}";
	private static final String LABEL_PARAM = "{label}";
	private static final String REPOSITORY_ID_PARAM = "{repositoryId}";

	public static final String REST_REPOSITORIES = "/rest/repositories/";
	public static final String REPOSITORIES = "/repositories/";

	public static final String CONFIGURATION_FILE = "worker.ttl";

	private static final String CHECK_QUERY;

	static {
		try {
			CHECK_QUERY = URLEncoder.encode("select ?tmp where { BIND(1 as ?tmp).}", "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private final HTTPClient httpClient;

	/**
	 * Constructs repository manager to uses the given client
	 *
	 * @param httpClient
	 *            the client to use
	 */
	@Inject
	public HTTPRepositoryManager(HTTPClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Creates repository by the given configuration parameters
	 *
	 * @param configuration
	 *            Repository configuration that contains the title, label and server address for the new repository
	 */
	@Override
	public void createRepository(RepositoryConfiguration configuration) {
		String serverAddress = configuration.getAddress().toString();
		HttpPost post = new HttpPost(serverAddress + REST_REPOSITORIES);

		HttpEntity configurationEntity = MultipartEntityBuilder
				.create()
					.addBinaryBody("config", createConfiguration(configuration).getBytes(StandardCharsets.UTF_8),
							ContentType.create(TURTLE_CONTENT_TYPE, StandardCharsets.UTF_8), CONFIGURATION_FILE)
					.build();
		post.setHeader(configurationEntity.getContentType());
		post.setEntity(configurationEntity);

		HttpClientContext context = HttpClientContext.create();
		HttpHost targetHost = buildHost(configuration.getInfo());

		setSecurity(configuration.getInfo(), context, targetHost);

		executeHttpRequest(post, context, targetHost, errorIfNot(201));
	}

	private static String createConfiguration(RepositoryConfiguration configuration) {
		String fileContent = loadConfigurationFile();

		// update repository configuration
		fileContent = fileContent.replace(REPOSITORY_ID_PARAM, configuration.getRepositoryName());
		fileContent = fileContent.replace(LABEL_PARAM, configuration.getLabel());
		fileContent = fileContent.replace(ENTITY_INDEX_SIZE_PARAM, String.valueOf(configuration.getEntityIndexSize()));
		return fileContent;
	}

	protected static HttpHost buildHost(RepositoryInfo configuration) {
		return new HttpHost(configuration.getAddress().getHost(), configuration.getAddress().getPort(),
				configuration.getAddress().getScheme());
	}

	protected static BiFunction<Integer, HttpResponse, HttpResponse> errorIfNot(int... code) {
		return (i, r) -> {
			boolean matchFound = false;
			for (int j : code) {
				if (i.intValue() == j) {
					matchFound = true;
					break;
				}
			}
			if (!matchFound) {
				throw new EmfRuntimeException("Could not contact semantic repository! Response: " + r);
			}
			return r;
		};
	}

	@SuppressWarnings("boxing")
	protected <T> T executeHttpRequest(HttpUriRequest post, HttpClientContext context, HttpHost targetHost,
			BiFunction<Integer, HttpResponse, T> statusCodeConsumer) {
		return httpClient.execute(post, context, targetHost, statusCodeConsumer);
	}

	private static void setSecurity(RepositoryInfo configuration, HttpClientContext context, HttpHost targetHost) {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(configuration.getUserName(), configuration.getPassword()));

		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local auth cache
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(targetHost, basicAuth);

		// Add AuthCache to the execution context
		context.setCredentialsProvider(credsProvider);
		context.setAuthCache(authCache);
	}

	/**
	 * Load configuration file located in the same package
	 *
	 * @return the loaded configuration file
	 */
	protected static String loadConfigurationFile() {
		try (InputStream inputStream = HTTPRepositoryManager.class.getResourceAsStream(CONFIGURATION_FILE)) {
			Objects.requireNonNull(inputStream, "Configuration file " + CONFIGURATION_FILE + " not found!");

			return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Could not load default configuration", e);
		}
	}

	@Override
	public void deleteRepository(RepositoryInfo configuration) {
		String serverAddress = configuration.getAddress().toString();
		HttpDelete post = new HttpDelete(serverAddress + REST_REPOSITORIES + configuration.getRepositoryName());

		HttpClientContext context = HttpClientContext.create();
		HttpHost targetHost = buildHost(configuration);

		setSecurity(configuration, context, targetHost);

		executeHttpRequest(post, context, targetHost, errorIfNot(200));
	}

	@Override
	public void createAccessUserForRepo(RepositoryInfo configuration, String userName, String password) {
		// TODO: implement createAccessUserForRepo!
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRepositoryExists(RepositoryInfo configuration) {
		String repositoryName = configuration.getRepositoryName();
		String serverAddress = configuration.getAddress().toString();

		HttpClientContext context = HttpClientContext.create();
		HttpHost targetHost = buildHost(configuration);

		setSecurity(configuration, context, targetHost);

		HttpGet get = new HttpGet(serverAddress + REPOSITORIES + repositoryName + "?query=" + CHECK_QUERY);
		get.setHeader(HttpHeaders.ACCEPT, SPARQL_JSON_CONTENT_TYPE);

		return executeHttpRequest(get, context, targetHost, parseResponse(
				HTTPRepositoryManager::readResponseIsRepoExists, Boolean.FALSE,
				() -> String.format("Could not check if repository %s at %s exists", repositoryName, serverAddress)));
	}

	/**
	 * Method that returns a function for reading a response from {@link HttpResponse} object. The method accepts a
	 * transformer function for the result conversion if any.
	 *
	 * @param <T>
	 *            the generic type
	 * @param parser
	 *            the parser function to apply to the read string content. It will be called with non null text
	 * @param defaultValue
	 *            the default value to return if the response is not valid or no content is found
	 * @param errorMessage
	 *            the error message to print along with the error
	 * @return the bi function
	 */
	@SuppressWarnings("boxing")
	private static <T> BiFunction<Integer, HttpResponse, T> parseResponse(Function<String, T> parser, T defaultValue,
			Supplier<String> errorMessage) {
		return (Integer statusCode, HttpResponse response) -> {
			if (statusCode == 404 || statusCode != 200) {
				return defaultValue;
			}
			try {
				String content = readContent(response);
				LOGGER.trace("Parsing response content \n{}", content);
				return parser.apply(content);
			} catch (IOException e) {
				LOGGER.warn(errorMessage.get(), e);
			}
			return defaultValue;
		};
	}

	private static String readContent(HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();
		try (InputStream local = entity.getContent()) {
			return IOUtils.toString(local);
		}
	}

	@SuppressWarnings("boxing")
	private static Boolean readResponseIsRepoExists(String string) {
		List<Map<String, Object>> response = parseQueryResponse(string);
		if (response.isEmpty()) {
			return Boolean.FALSE;
		}
		return nullSafeEquals("1", response.get(0).get("tmp"));
	}

	private static List<Map<String, Object>> parseQueryResponse(String response) {
		JSONObject object = JsonUtil.createObjectFromString(response);
		Set<String> bindingNames = readBindings(object);

		JSONObject results = JsonUtil.getJsonObject(object, "results");
		JSONArray bindings = JsonUtil.getJsonArray(results, "bindings");
		return JsonUtil.toJsonObjectStream(bindings).map(item -> readResponseItem(item, bindingNames)).collect(
				Collectors.toList());
	}

	private static Map<String, Object> readResponseItem(JSONObject item, Set<String> bindingNames) {
		Map<String, Object> mapping = CollectionUtils.createHashMap(bindingNames.size());
		for (String name : bindingNames) {
			JSONObject itemValue = JsonUtil.getJsonObject(item, name);
			mapping.put(name, JsonUtil.getValueOrNull(itemValue, "value"));
		}
		return mapping;
	}

	private static Set<String> readBindings(JSONObject object) {
		JSONObject head = JsonUtil.getJsonObject(object, "head");
		JSONArray vars = JsonUtil.getJsonArray(head, "vars");
		return JsonUtil.toStringStream(vars).collect(Collectors.toSet());
	}

}