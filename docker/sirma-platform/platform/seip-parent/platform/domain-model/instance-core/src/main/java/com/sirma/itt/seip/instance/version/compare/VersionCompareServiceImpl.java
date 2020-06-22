package com.sirma.itt.seip.instance.version.compare;

import static com.sirma.itt.seip.instance.version.InstanceVersionService.getIdFromVersionId;
import static com.sirma.itt.seip.instance.version.InstanceVersionService.isVersion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Contains logic that compares instance versions. The service provides a way of calling external service that compares
 * the primary content of two versions. The content is provided by links that are used to download the file, then the
 * files are compared. The result from the external service is also file, where the differences between the provided are
 * highlighted. This file is stored directly in {@link VersionCompareContentStore} from where it could be requested. The
 * service will schedule task which will delete the generated result file after configurable expiration time in order to
 * clean up and optimize space usage.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class VersionCompareServiceImpl implements VersionCompareService {

	private static final String BASE_INSTANCES_PATH = "/instances/";

	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private VersionCompareConfigurations compareVersionConfiguration;

	@Inject
	private TempFileProvider fileProvider;

	@Inject
	private SecurityContext securityContext;

	@Override
	public File compareVersionsContent(VersionCompareContext context) {
		validateInput(context);

		String first = buildLinkForContentPreview(context.getFirstIdentifier());
		String second = buildLinkForContentPreview(context.getSecondIdentifier());

		HttpPost post = buildPost(context.getAuthentication(), first, second);

		File file = fileProvider.createTempFile("compare-" + securityContext.getRequestId(), "");
		try (CloseableHttpClient client = HttpClientBuilder.create().build();
				CloseableHttpResponse response = client.execute(post);
				OutputStream out = new FileOutputStream(file)) {

			StatusLine statusLine = response.getStatusLine();
			if (Status.OK.getStatusCode() != statusLine.getStatusCode()) {
				throw new VersionCompareException("Compare operation failed! Reason: " + statusLine.getReasonPhrase());
			}

			IOUtils.copy(response.getEntity().getContent(), out);
			return file;
		} catch (IOException e) {
			throw new VersionCompareException("There was a problem with external service used in compare operation!",
					e);
		}
	}

	/**
	 * Checks and throws {@link IllegalArgumentException}, if:
	 * <ul>
	 * <li>system access url or compare service base url is not set</li>
	 * <li>any of the passed identifiers is blank</li>
	 * <li>the identifiers are equal</li>
	 * <li>any of the passed identifiers is not a version identifier</li>
	 * <li>any of the passed identifiers is not equal (without version suffix) to the original instance identifier</li>
	 * </ul>
	 */
	private void validateInput(VersionCompareContext context) {
		if (systemConfiguration.getRESTRemoteAccessUrl().isNotSet()
				|| compareVersionConfiguration.getServiceBaseUrl().isNotSet()) {
			throw new IllegalArgumentException("Missing configurations requeired for compare process!");
		}

		Serializable firstIdentifier = context.getFirstIdentifier();
		Serializable secondIdentifier = context.getSecondIdentifier();
		String firstAsString = Objects.toString(firstIdentifier, null);
		String secondAsString = Objects.toString(secondIdentifier, null);

		if (StringUtils.isBlank(firstAsString) || StringUtils.isBlank(secondAsString)) {
			throw new IllegalArgumentException("Both input arguments are required!");
		}

		if (firstAsString.equals(secondAsString)) {
			throw new IllegalArgumentException("Both identifiers point to the same object!");
		}

		if (!isVersion(firstIdentifier) || !isVersion(secondIdentifier)) {
			throw new IllegalArgumentException("Both identifiers should point to versions!");
		}

		if (StringUtils.isBlank(context.getAuthentication())) {
			throw new IllegalArgumentException("Original http headers are null and cannot be used to "
					+ "download the version required for the compare!");
		}

		Serializable originalInstanceId = context.getOriginalInstanceId();
		if (!getIdFromVersionId(firstIdentifier).equals(originalInstanceId)
				|| !getIdFromVersionId(secondIdentifier).equals(originalInstanceId)) {
			throw new IllegalArgumentException("Both version should originate from the same object!");
		}
	}

	private String buildLinkForContentPreview(Serializable versionId) {
		return new StringBuilder(systemConfiguration.getRESTRemoteAccessUrl().get().toString())
				.append(BASE_INSTANCES_PATH)
				.append(versionId)
				.append("/content/preview")
				.toString();
	}

	private HttpPost buildPost(String auth, String firstLink, String secondLink) {
		HttpPost post = new HttpPost(compareVersionConfiguration.getServiceBaseUrl().get().toString());
		post.addHeader(HttpHeaders.CONTENT_TYPE, Versions.V2_JSON);
		addPayload(auth, firstLink, secondLink, post);
		return post;
	}

	private static void addPayload(String jwt, String firstLink, String secondLink, HttpPost post) {
		String linkFormat = "%s?jwt=%s";
		JsonObject requestBody = Json
				.createObjectBuilder()
				.add("first", String.format(linkFormat, firstLink, jwt))
				.add("second",String.format(linkFormat, secondLink, jwt))
				.build();

		post.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));
	}
}
