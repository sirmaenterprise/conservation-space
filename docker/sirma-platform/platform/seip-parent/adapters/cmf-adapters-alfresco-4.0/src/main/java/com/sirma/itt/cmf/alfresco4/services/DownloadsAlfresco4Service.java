/**
 *
 */
package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.downloads.DownloadsAdapterService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Contains the logic for creating and processing archives (zip files) for downloads functionality.
 *
 * @author A. Kunchev
 * @deprecated This should not depend on dms functionality but should be implemented internally to collect all content
 *             and build zip
 */
@ApplicationScoped
@Deprecated
public class DownloadsAlfresco4Service implements DownloadsAdapterService {

	private static final String NODE_REF = "nodeRef";

	private static final String DATE_FORMAT = "YYYY.MM.dd";

	private static final Pattern PREFIX_ID = Pattern.compile("(.*)(?<=:)");

	@Inject
	private RESTClient restClient;

	@Inject
	private InstanceContentService contentService;

	@Inject
	private SecurityContext securityContext;

	@Override
	public String createArchive(Collection<Serializable> instanceIds) {
		if (CollectionUtils.isEmpty(instanceIds)) {
			throw new IllegalArgumentException("The passed collection is empty.");
		}
		List<Serializable> list = new ArrayList<>(instanceIds);
		Collection<ContentInfo> instances = contentService.getContent(list, Content.PRIMARY_CONTENT);

		JSONArray requestParameter = buildRequestParameter(instances);

		if (JsonUtil.isNullOrEmpty(requestParameter)) {
			throw new IllegalArgumentException("The JSON is empty.");
		}

		try {
			HttpMethod method = restClient.createMethod(new PostMethod(), requestParameter.toString(), true);
			return restClient.request(ServiceURIRegistry.DOWNLOADS_CREATE_ZIP_URI, method);
		} catch (Exception e) {
			throw new RollbackedRuntimeException("Could not create http method.", e);
		}
	}

	/**
	 * Builds JSONArray object with the documents, which will be archived.
	 *
	 * @param instances
	 *            the instances from, which the DMS ids will be get
	 * @return JSONArray with the DMS id of the documents, which will be zipped
	 */
	private static JSONArray buildRequestParameter(Collection<ContentInfo> instances) {
		try {
			JSONArray requestParam = new JSONArray();
			for (ContentInfo instance : instances) {
				if (instance.exists() && instance.getRemoteSourceName().contains("alfresco")) {
					requestParam.put(new JSONObject().put(NODE_REF, instance.getRemoteId()));
				}
			}
			return requestParam;
		} catch (JSONException e) {
			throw new RollbackedRuntimeException("There is a problem with the JSON object building.", e);
		}
	}

	@Override
	public String getArchiveStatus(String archiveId) {
		if (StringUtils.isBlank(archiveId)) {
			throw new IllegalArgumentException("The passed parameter is null or empty.");
		}

		try {
			HttpMethod method = restClient.createMethod(new GetMethod(), "", true);
			return restClient.request(MessageFormat.format(ServiceURIRegistry.DOWNLOADS_ZIP_STATUS, archiveId), method);
		} catch (Exception e) {
			throw new RollbackedRuntimeException("Could not create http method.", e);
		}
	}

	@Override
	public String removeArchive(String archiveId) {
		if (StringUtils.isBlank(archiveId)) {
			throw new IllegalArgumentException("Passed parameter is null or empty.");
		}
		try {
			HttpMethod method = restClient.createMethod(new DeleteMethod(), "", true);
			String uri = MessageFormat.format(ServiceURIRegistry.DOWNLOADS_ZIP_URI, archiveId);
			return restClient.request(uri, method);
		} catch (Exception e) {
			throw new RollbackedRuntimeException("There is a problem with the http creation.", e);
		}
	}

	@Override
	public String getArchiveURL(String archiveId) {
		if (StringUtils.isBlank(archiveId)) {
			throw new IllegalArgumentException("The archive id is null or empty.");
		}
		return MessageFormat.format(ServiceURIRegistry.DOWNLOADS_ARCHIVE_URI, archiveId, buildArchiveName());
	}

	/**
	 * Builds specific name for the created archive.
	 * <p />
	 * <b>Name template:</b>
	 *
	 * <pre>
	 * [user]-[YYYY.MM.dd]-[time to ms].zip
	 * </pre>
	 *
	 * @return compiled archive name
	 */
	private String buildArchiveName() {
		final String dash = "-";
		StringBuilder name = new StringBuilder();
		Date date = new Date();

		String username = securityContext.getAuthenticated().getSystemId().toString();
		username = PREFIX_ID.matcher(username).replaceFirst("");
		String formatedDate = new SimpleDateFormat(DATE_FORMAT).format(date);

		name.append(username).append(dash);
		name.append(formatedDate).append(dash);
		name.append(date.getTime());
		name.append(".zip");

		return name.toString();
	}

}
