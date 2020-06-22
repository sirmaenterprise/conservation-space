package com.sirma.itt.seip.content.processing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.rest.client.HTTPClient;
import com.sirma.itt.seip.util.file.FileUtil;

/**
 * Helper class that handles external image download during image embedding process
 *
 * @author BBonev
 */
@Singleton
public class ImageDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Pair<String, byte[]> NO_DATA = new Pair<>(null, new byte[0]);

	/**
	 * The max allowed download size of files. Current value is 10 MB
	 */
	static final long MAX_DOWNLOADS_SIZE = 10L * 1024L * 1024L * 1024L;
	private static final String HR_MAX_DOWNLOADS_SIZE = FileUtil.humanReadableByteCount(MAX_DOWNLOADS_SIZE);

	@Inject
	private HTTPClient httpClient;

	/**
	 * Perform the download and calls one of the functions depending on the outcome. It won't download images bigger
	 * than {@link #MAX_DOWNLOADS_SIZE}={@value #MAX_DOWNLOADS_SIZE} bytes.<br>
	 * Note that the method will not throw exception if failed to download the provided address
	 *
	 * @param <R>
	 *            the response type
	 * @param address
	 *            the image address to access and retrieve
	 * @param onSuccess
	 *            on success will call this function with the content mimetype and the content itself read as byte data.
	 * @param onFail
	 *            on fail will call the supplier to provide a default value to be returned
	 * @return a value produced from processing the read content or the default value on failure
	 */
	public <R> R download(URI address, BiFunction<String, byte[], R> onSuccess, Supplier<R> onFail) {
		Objects.requireNonNull(onSuccess, "On success function is required");
		Objects.requireNonNull(onFail, "On fail supplier is required");
		if (address == null) {
			return onFail.get();
		}
		Pair<String, byte[]> download = download(address);
		if (download.getSecond().length > 0) {
			return onSuccess.apply(download.getFirst(), download.getSecond());
		}
		return onFail.get();
	}

	private Pair<String, byte[]> download(URI targetURI) {
		LOGGER.info("Downloading image: {}", targetURI);
		HttpClientContext context = HttpClientContext.create();
		context.setRequestConfig(RequestConfig.custom()
				.setSocketTimeout((int) TimeUnit.SECONDS.toMillis(30))
				.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(10))
				.setConnectionRequestTimeout((int) TimeUnit.MINUTES.toMillis(1))
				.build());
		HttpGet get = new HttpGet(targetURI);
		HttpHost httpHost = new HttpHost(targetURI.getHost(), targetURI.getPort(), targetURI.getScheme());
		return httpClient.execute(get, context, httpHost, readResponse(), logError(targetURI));
	}

	private static Function<IOException, Pair<String, byte[]>> logError(URI targetURI) {
		return error -> {
			LOGGER.warn("Could not download: {} due to error: {}", targetURI, error.getMessage());
			LOGGER.debug("", error);
			return NO_DATA;
		};
	}

	private static ResponseHandler<Pair<String, byte[]>> readResponse() {
		return response -> {
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				HttpEntity entity = response.getEntity();
				long length = entity.getContentLength();
				if (entity.getContentLength() <= MAX_DOWNLOADS_SIZE) {
					return doDownload(entity);
				}
				String humanReadableSize = FileUtil.humanReadableByteCount(length);
				LOGGER.warn("Skipped downloading an image of size {} as it's exceeds the max allowed size of {}",
						humanReadableSize, HR_MAX_DOWNLOADS_SIZE);
			} else {
				LOGGER.warn("Received response code {} different than 200 on image download", code);
			}
			return NO_DATA;
		};
	}

	private static Pair<String, byte[]> doDownload(HttpEntity entity) {
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream((int) entity.getContentLength())) {
			entity.writeTo(stream);
			return new Pair<>(getContentType(entity), stream.toByteArray());
		} catch (IOException e) {
			LOGGER.warn("Could not download image to be embedded", e);
		}
		return NO_DATA;
	}

	private static String getContentType(HttpEntity entity) {
		Header contentType = entity.getContentType();
		if (contentType == null) {
			return null;
		}
		MediaType mediaType = MediaType.valueOf(contentType.getValue());
		return mediaType.getType() + "/" + mediaType.getSubtype();
	}
}
