package com.sirma.itt.seip.instance.version.compare;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.rest.utils.HttpClientUtil;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreMissMatchException;
import com.sirma.sep.content.DeleteContentData;
import com.sirma.sep.content.StoreException;
import com.sirma.sep.content.StoreItemInfo;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Content store used to retrieve the results for the different versions compare. This store is remote and it is
 * accessed by services. Supports delete, that is used to clean up the result files after compare, when their "keep"
 * time expires or they are not needed anymore. <br>
 * <b>Add and update are not supported!</b>
 *
 * @author A. Kunchev
 */
// TODO authentication, when we implement security for the external services (future sprint)
@ApplicationScoped
public class VersionCompareContentStore implements ContentStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String STORE_NAME = "comparedVersionsStore";

	public static final String BASE_SERVICE_PATH = "/compare";

	private static final String ID_QUERY_PARAM = "?id=";

	private static final String REMOTE_ID = "remoteId";

	private static final String COMPARE_SERVICE_URL = "compareServiceUrl";

	@Inject
	private VersionCompareConfigurations compareVersionsConfiguration;

	/**
	 * Creates http request to the external store where the compared content is stored.
	 *
	 * @throws StoreException when the response code from the service is other then 200 or when the external service could not be
	 * reached
	 */
	@Override
	public FileDescriptor getReadChannel(StoreItemInfo info) {
		if (!isFromThisStore(info)) {
			throw new ContentStoreMissMatchException(getName(), info == null ? null : info.getProviderType());
		}
		String remoteId = info.getRemoteId();
		String url = getServiceUrl() + ID_QUERY_PARAM + remoteId;

		try {
			return HttpClientUtil.callRemoteService(new HttpGet(url));
		} catch (RuntimeException e) {
			throw new StoreException(
					"Could not contact remote store for id - " + remoteId + " due to - " + e.getMessage(), e);
		}
	}

	/**
	 * Creates http request for deleting specific content from the store.
	 */
	@Override
	public void delete(DeleteContentData deleteContentData) {
		Serializable remoteId = deleteContentData.getProperties().get(REMOTE_ID);
		String url = deleteContentData.getProperties().get(COMPARE_SERVICE_URL) + ID_QUERY_PARAM + remoteId;

		try (CloseableHttpClient client = HttpClientBuilder.create().build();
				CloseableHttpResponse response = client.execute(new HttpDelete(url))) {
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (Response.Status.OK.getStatusCode() == statusCode) {
				return;
			}

			LOGGER.warn("Content with id - {} was not deleted. Service response - status: {}, reason: {}", statusCode,
					remoteId, statusLine.getReasonPhrase());
		} catch (IOException e) {
			LOGGER.warn("Could not delete content [{}] from [{}] store. The store could be unreachable.", remoteId,
					getName(), e);
		}
	}

	@Override
	public String getName() {
		return STORE_NAME;
	}

	@Override
	public Optional<DeleteContentData> prepareForDelete(StoreItemInfo itemInfo) {
		if (!isFromThisStore(itemInfo)) {
			throw new ContentStoreMissMatchException(getName(), itemInfo == null ? null : itemInfo.getProviderType());
		}

		DeleteContentData data = new DeleteContentData().setStoreName(STORE_NAME)
				.addProperty(REMOTE_ID, itemInfo.getRemoteId())
				// The configuration is added to the delete message, because when actual delete is executed it is
				// from the scope of the system tenant, but if we want to take the current tenant's configuration we
				// can do it here.
				.addProperty(COMPARE_SERVICE_URL, getServiceUrl());
		return Optional.of(data);
	}

	private String getServiceUrl() {
		return compareVersionsConfiguration.getServiceBaseUrl().get().toString();
	}

	@Override
	public boolean isTwoPhaseDeleteSupported() {
		return true;
	}

	@Override
	public StoreItemInfo add(Serializable instance, Content descriptor) {
		throw new UnsupportedOperationException("[add] is not supported for store - " + getName());
	}

	@Override
	public StoreItemInfo update(Serializable instance, Content descriptor, StoreItemInfo previousVersion) {
		throw new UnsupportedOperationException("[update] is not supported for store - " + getName());
	}

	@Override
	public boolean delete(StoreItemInfo info) {
		throw new UnsupportedOperationException("Immediate [delete] is not supported for store - " + getName());
	}
}