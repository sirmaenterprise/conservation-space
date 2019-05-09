package com.sirma.sep.content.preview.remote;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.json.JSON;
import com.sirma.itt.seip.rest.client.HTTPClient;
import com.sirma.sep.content.preview.ContentPreviewConfigurations;
import com.sirma.sep.content.preview.remote.mimetype.MimeTypeSupport;
import org.apache.http.HttpHost;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

/**
 * Provides access to the remove preview service REST endpoints.
 *
 * @author Mihail Radkov
 */
@Singleton
public class ContentPreviewRemoteService {

	private static final MimeTypeSupport UNSUPPORTED = new UnsupportedMimeType();
	private static final URI MIME_TYPES_URL = URI.create("/content/mimetypes");
	private static final String FILTER_PARAM = "filter";

	@Inject
	private ContentPreviewConfigurations previewConfigurations;

	@Inject
	private HTTPClient httpClient;

	private HttpHost previewServiceHost;

	@PostConstruct
	void initialize() {
		ConfigurationProperty<String> addressConfig = previewConfigurations.getPreviewServiceAddress();
		createHost(addressConfig.get());
		addressConfig.addConfigurationChangeListener(address -> createHost(address.get()));
	}

	private void createHost(String address) {
		previewServiceHost = HttpHost.create(address);
	}

	/**
	 * Retrieve support for the given mimetype in {@link MimeTypeSupport} by calling the remove preview service.
	 *
	 * @param mimetype
	 * 		the mime type to retrieve support for
	 * @return a {@link MimeTypeSupport} with the support information for the given mime type
	 */
	public MimeTypeSupport getMimeTypeSupport(String mimetype) {
		HttpGet getRequest = buildGetMethod(mimetype);
		return httpClient.execute(getRequest, null, previewServiceHost, readResponse(), readError());
	}

	private static HttpGet buildGetMethod(String mimetype) {
		URIBuilder uriBuilder = new URIBuilder(MIME_TYPES_URL).addParameter(FILTER_PARAM, mimetype);
		try {
			return new HttpGet(uriBuilder.build());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Wrong URL address for remote preview service!", e);
		}
	}

	private static ResponseHandler<MimeTypeSupport> readResponse() {
		return response -> {
			if (response.getStatusLine().getStatusCode() != 200) {
				return UNSUPPORTED;
			}
			try {
				return JSON.readObject(response.getEntity().getContent(), readJson());
			} catch (IOException e) {
				throw new EmfRuntimeException("Couldn't read response object!", e);
			}
		};
	}

	private static Function<JsonObject, MimeTypeSupport> readJson() {
		return json -> {
			MimeTypeSupport mimeTypeSupport = new MimeTypeSupport();
			mimeTypeSupport.setSupportsPreview(json.getBoolean("supportsPreview", false));
			mimeTypeSupport.setSelfPreview(json.getBoolean("selfPreview", false));
			mimeTypeSupport.setSupportsThumbnail(json.getBoolean("supportsThumbnail", false));
			return mimeTypeSupport;
		};
	}

	private Function<IOException, MimeTypeSupport> readError() {
		return e -> {
			throw new EmfRuntimeException("Cannot reach remote preview service!", e);
		};
	}

	private static class UnsupportedMimeType extends MimeTypeSupport {
		@Override
		public boolean supportsPreview() {
			return false;
		}

		@Override
		public boolean isSelfPreview() {
			return false;
		}

		@Override
		public boolean supportsThumbnail() {
			return false;
		}
	}
}
