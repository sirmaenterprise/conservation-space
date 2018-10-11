package com.sirma.itt.seip.instance.version.compare;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.instance.version.InstanceVersionService.getIdFromVersionId;
import static com.sirma.itt.seip.instance.version.InstanceVersionService.isVersion;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentNotImportedException;
import com.sirma.sep.content.InstanceContentService;

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

	private static final String COMPARE_VERSIONS_CONTENT_PURPOSE = "comparedVersions";

	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private VersionCompareConfigurations compareVersionConfiguration;

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public String compareVersionsContent(VersionCompareContext context) {
		validateInput(context);
		String contentLink = getIfAvailable(context);
		if (contentLink != null) {
			return contentLink;
		}

		return callExternalCompare(context);
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

		if (isEmpty(context.getAuthenticationHeaders())) {
			throw new IllegalArgumentException("Original http headers are null and cannot be used to "
					+ "download the version required for the compare!");
		}

		Serializable originalInstanceId = context.getOriginalInstanceId();
		if (!getIdFromVersionId(firstIdentifier).equals(originalInstanceId)
				|| !getIdFromVersionId(secondIdentifier).equals(originalInstanceId)) {
			throw new IllegalArgumentException("Both version should originate from the same object!");
		}
	}

	/**
	 * Checks if there is stored result for the requested versions and returns link to it, if available. This means that
	 * compare is already executed for the requested versions and new compare is not required at the moment, the result
	 * could be returned directly.<br>
	 * For the content check is used identifier, which is based on the requested for compare version identifiers.
	 */
	private String getIfAvailable(VersionCompareContext context) {
		String id = buildContentIdentifier(context);
		ContentInfo content = instanceContentService.getContent(id, COMPARE_VERSIONS_CONTENT_PURPOSE);
		if (!content.exists()) {
			return null;
		}

		// as optimization - reschedule delete task (requires changes in content delete scheduling)
		return buildResultLink(content.getContentId());
	}

	private static String buildContentIdentifier(VersionCompareContext context) {
		return context.getFirstIdentifier() + "-" + context.getSecondIdentifier();
	}

	/**
	 * Builds link to the result content for the versions compare operation. This link could be used to download or
	 * display the result from the operation.
	 */
	private static String buildResultLink(String contentId) {
		return "/content/" + contentId + "?download=true";
	}

	/**
	 * Creates and executes compare request to external service. As request parameters are set links to the content that
	 * should be compared. If the request is successful the method will handle content import for the newly generated
	 * content, which represents the result from the compare of the two contents.
	 */
	private String callExternalCompare(VersionCompareContext context) {
		String firstLink = buildLinkForContentPreview(context.getFirstIdentifier());
		String secondLink = buildLinkForContentPreview(context.getSecondIdentifier());
		HttpPost post = buildPost(context.getAuthenticationHeaders(), firstLink, secondLink);

		try (CloseableHttpClient client = HttpClientBuilder.create().build();
				CloseableHttpResponse response = client.execute(post)) {
			StatusLine statusLine = response.getStatusLine();
			if (Status.OK.getStatusCode() != statusLine.getStatusCode()) {
				throw new VersionCompareException("Compare operation failed! Reason: " + statusLine.getReasonPhrase());
			}

			return importContentRecord(response.getEntity().getContent(), buildContentIdentifier(context));
		} catch (IOException e) {
			throw new VersionCompareException("There was a problem with external service used in compare operation!",
					e);
		}
	}

	private String buildLinkForContentPreview(Serializable versionId) {
		return new StringBuilder(systemConfiguration.getRESTRemoteAccessUrl().get().toString())
				.append(BASE_INSTANCES_PATH)
				.append(versionId)
				.append("/content/preview")
				.toString();
	}

	private HttpPost buildPost(Map<String, String> headers, String firstLink, String secondLink) {
		HttpPost post = new HttpPost(compareVersionConfiguration.getServiceBaseUrl().get().toString());
		addPayload(firstLink, secondLink, post);
		copyRequestHeaders(headers, post);
		return post;
	}

	private static void addPayload(String firstLink, String secondLink, HttpPost post) {
		JsonObject requestBody = Json
				.createObjectBuilder()
				.add("firstURL", firstLink)
				.add("secondURL", secondLink)
				.build();

		post.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));
	}

	/**
	 * Transferring the headers from the original request to the request with which will be executed the compare by the
	 * external service. This is required for the authentication, when the contents for the versions are requested by
	 * the external service.
	 */
	private static void copyRequestHeaders(Map<String, String> headers, HttpPost post) {
		post.addHeader(HttpHeaders.CONTENT_TYPE, Versions.V2_JSON);
		post.addHeader(HttpHeaders.COOKIE, headers.get(HttpHeaders.COOKIE));
		post.addHeader(HttpHeaders.AUTHORIZATION, headers.get(HttpHeaders.AUTHORIZATION));
	}

	/**
	 * Creates new record in the DB from the generated and stored content. This content represents the result file from
	 * the version compare. Also schedules delete of this content after configurable time period in order to keep the
	 * store cleaned up.
	 */
	private String importContentRecord(InputStream stream, String contentIdentifier) {
		String contentId = instanceContentService.importContent(JSON.readObject(stream, toContent(contentIdentifier)));
		if (StringUtils.isBlank(contentId)) {
			throw new ContentNotImportedException("Failed to import content from version compare!");
		}

		scheduleDelete(contentId);
		return buildResultLink(contentId);
	}

	/**
	 * Extracts the data for the generated result file after compare and builds {@link ContentImport} object, which is
	 * used to create record for the stored content. The data retrieved from the response is the file name, mime type
	 * and the content length of generated file. <br>
	 * The content identifier is pre-build from the ids of the versions that are compared and it is used to extract the
	 * result file, if such is available, instead of generating new one every time.
	 */
	private static Function<JsonObject, ContentImport> toContent(String contentIdentifier) {
		return json -> {
			String name = json.getString("fileName", null);
			if (StringUtils.isBlank(name)) {
				throw new VersionCompareException(
						"Missing required parameter for file name in the external compare service response!");
			}

			String mimeType = json.getString("mimeType", "application/pdf");
			long contentLength = json.getJsonNumber(JsonKeys.FILE_SIZE).longValue();
			return ContentImport
					.createEmpty()
					.setName(name)
					.setPurpose(COMPARE_VERSIONS_CONTENT_PURPOSE)
					.setRemoteId(name)
					.setRemoteSourceName(VersionCompareContentStore.STORE_NAME)
					.setMimeType(mimeType)
					.setContentLength(contentLength)
					.setInstanceId(contentIdentifier);
		};
	}

	/**
	 * Schedules delete of the generated result file after comparing the versions. Done so we could clean up and keep
	 * the store optimized.
	 */
	private void scheduleDelete(String contentId) {
		instanceContentService.deleteContent(contentId, COMPARE_VERSIONS_CONTENT_PURPOSE,
				compareVersionConfiguration.getExpirationTime(), TimeUnit.HOURS);
	}

}
