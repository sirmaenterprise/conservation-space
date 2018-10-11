package com.sirma.sep.content.rest;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.rest.Range;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Helper service to provide content streaming and download
 *
 * @author BBonev
 */
@Singleton
public class ContentDownloadService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long ONE_MB = 1024L * 1024L;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private TempFileProvider tempFileProvider;

	/**
	 * Gets the content head response for the requested content.
	 *
	 * @param contentId
	 *            the content id
	 * @param purpose
	 *            the purpose
	 * @return the content head response
	 */
	public Response getContentHeadResponse(String contentId, String purpose) {
		return getContentHeadResponse(contentId, purpose, Function.identity()).build();
	}

	/**
	 * Gets the content head response for the requested content.
	 *
	 * @param contentId
	 *            the content id
	 * @param purpose
	 *            the purpose
	 * @param onFound
	 *            function to apply before returning the response on successfully found content. This can be used to
	 *            enrich the response. Required argument. If not needed use
	 *            {@link #getContentHeadResponse(String, String)} method
	 * @return the content head response builder
	 */
	@SuppressWarnings("boxing")
	public ResponseBuilder getContentHeadResponse(String contentId, String purpose,
			Function<ResponseBuilder, ResponseBuilder> onFound) {
		if (StringUtils.isBlank(contentId)) {
			return Response.status(Status.BAD_REQUEST);
		}

		ContentInfo contentInfo = instanceContentService.getContent(contentId, purpose);
		if (!contentInfo.exists()) {
			return Response.status(Status.NOT_FOUND);
		}

		ResponseBuilder responseBuilder;
		if (contentInfo.getLength() > 0L) {
			// range is supported when we know the content length
			responseBuilder = Response
					.ok()
						.header(Range.HTTP_HEADER_ACCEPT_RANGES, Range.BYTES)
						.header(HttpHeaders.CONTENT_LENGTH, contentInfo.getLength())
						.header(HttpHeaders.CONTENT_TYPE, contentInfo.getMimeType());
		} else {
			responseBuilder = Response.ok().header(HttpHeaders.CONTENT_TYPE, contentInfo.getMimeType());
		}
		return onFound.apply(responseBuilder);
	}

	/**
	 * Sends the content preview to the specified {@link HttpServletResponse} if exists
	 *
	 * @param contentId
	 *            the content or instance id
	 * @param purpose
	 *            the purpose if instance id is passed
	 * @param range
	 *            the HTTP header Range that specifies the range of the content to return. The default value is all
	 *            content
	 * @param response
	 *            the response to send to
	 */
	public void sendPreview(String contentId, String purpose, HttpServletResponse response, Range range) {
		if (StringUtils.isBlank(contentId)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		ContentInfo contentInfo = instanceContentService.getContentPreview(contentId, purpose);
		if (!contentInfo.exists()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		File file = null;
		try {
			file = tempFileProvider.createTempFile(contentInfo.getName(), "");
			FileUtils.copyInputStreamToFile(contentInfo.getInputStream(), file);
			setPreviewContentType(contentInfo, response);
			addContentLength(file.length(), response, range);
			// status code always comes before any output in the data returned to the client
			response.setStatus(
					range.isAllRequested() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_PARTIAL_CONTENT);
			sendFile(file, response, range);
		} catch (IOException e) {
			LOGGER.trace("", e);
			LOGGER.warn("Failed to stream file for preview: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			tempFileProvider.deleteFile(file);
		}
	}

	private static void setPreviewContentType(ContentInfo contentInfo, HttpServletResponse response) {
		String contentType = "application/pdf";
		if (contentInfo.getMimeType() != null && contentInfo.getMimeType().startsWith("image/")) {
			contentType = contentInfo.getMimeType();
		}
		response.setContentType(contentType);
	}

	private static void addContentLength(long lenght, HttpServletResponse response, Range range) {
		long contentLength = lenght;
		if (contentLength > 0L) {
			response.setHeader(Range.HTTP_HEADER_ACCEPT_RANGES, Range.BYTES);

			if (!range.isAllRequested()) {
				if (range.isToTheEnd()) {
					contentLength = lenght - range.getFrom();
				} else {
					contentLength = range.getRangeLength();
				}
				response.setHeader(Range.HTTP_HEADER_CONTENT_RANGE, range.asResponse(lenght));
			}
			response.setContentLengthLong(contentLength);
		}
	}

	@SuppressWarnings("resource")
	private static void sendFile(File file, HttpServletResponse response, Range range) {
		addHeaders(response);
		try {
			streamData(response.getOutputStream(), new FileInputStream(file), range);
		} catch (IOException e) {
			LOGGER.trace("", e);
			LOGGER.warn("Client disconnected during content download: {}", e.getMessage());
		}
	}

	private static void streamData(ServletOutputStream outputStream, InputStream inputStream, Range range) {
		try (ServletOutputStream out = outputStream; InputStream in = inputStream) {
			// first send 1MB or the requested range if less
			long readLimit = Math.min(ONE_MB, range.isToTheEnd() ? ONE_MB : range.getRangeLength());
			long read = IOUtils.copyLarge(in, out, range.getFrom(), readLimit);
			// commit response
			out.flush();
			if (read < readLimit) {
				// the content is less then the first expected data so no more data
				return;
			}
			// send the rest if any
			readLimit = -1L;
			if (!range.isToTheEnd() && range.getRangeLength() > read) {
				readLimit = range.getRangeLength() - read;
			}
			IOUtils.copyLarge(in, out, 0, readLimit);
			out.flush();
		} catch (IOException e) {
			LOGGER.trace("", e);
			LOGGER.warn("Client disconnected during content download: {}", e.getMessage());
		}
	}

	/**
	 * Sends the content to the specified {@link HttpServletResponse} if found.
	 *
	 * @param contentId
	 *            the content or instance id
	 * @param purpose
	 *            the purpose if instance id is passed
	 * @param range
	 *            the range of the content to send
	 * @param forDownload
	 *            the for download or not. If it's for download a content disposition will be added to the response
	 *            headers
	 * @param response
	 *            the response to send to
	 * @param fileName
	 *            the custom file name
	 */
	public void sendContent(String contentId, String purpose, Range range, boolean forDownload,
			HttpServletResponse response, String fileName) {
		if (StringUtils.isBlank(contentId)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		ContentInfo contentInfo = instanceContentService.getContent(contentId, purpose);
		if (!contentInfo.exists()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		if (contentInfo.getLength() > 0L && range.getFrom() >= contentInfo.getLength()) {
			response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
			return;
		}

		setContentType(contentInfo.getMimeType(), response);
		addDispositionIfNeeded(contentInfo.getName(), response, forDownload, fileName);
		addContentLength(contentInfo.getLength(), response, range);
		// status code always comes before any output in the data returned to the client
		response.setStatus(range.isAllRequested() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_PARTIAL_CONTENT);
		sendContent(contentInfo, response, range);
	}

	private static void addDispositionIfNeeded(String persistedName, HttpServletResponse response, boolean forDownload,
			String fileName) {
		if (!forDownload) {
			return;
		}

		String name = StringUtils.isNotBlank(fileName) ? fileName : persistedName;
		StringBuilder disposition = new StringBuilder(256).append("attachment; filename=\"").append(name).append("\"");
		try {
			disposition.append("; filename*=utf-8''").append(URIUtil.encodePath(name));
		} catch (URIException e) {
			LOGGER.warn("Fail encoding file name: {} with {}", name, e.getMessage());
			LOGGER.trace("Fail encoding file name {} with error", name, e);
		}
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());
	}

	private static void sendContent(ContentInfo contentInfo, HttpServletResponse response, Range range) {
		addHeaders(response);
		try {
			streamData(response.getOutputStream(), contentInfo.getInputStream(), range);
		} catch (IOException e) {
			LOGGER.trace("", e);
			LOGGER.warn("Client disconnected during content download: {}", e.getMessage());
		}
	}

	private static void addHeaders(HttpServletResponse response) {
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		Collection<String> cacheControl = response.getHeaders(HttpHeaders.CACHE_CONTROL);
		// do not add cache control if already present. This way content caching can be enabled per service
		if (isEmpty(cacheControl)) {
			response.addHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
			response.addHeader("Pragma", "no-cache");
			response.addHeader(HttpHeaders.EXPIRES, "0");
		}
	}

	private static void setContentType(String mimeType, HttpServletResponse response) {
		response.setContentType(mimeType == null ? MediaType.APPLICATION_OCTET_STREAM : mimeType);
	}

	/**
	 * Sends file to the specified {@link HttpServletResponse} if exists and found.
	 *
	 * @param file
	 *            the file to be send
	 * @param range
	 *            the range of the content to send
	 * @param forDownload
	 *            the for download or not. If it's for download a content disposition will be added to the response
	 *            headers
	 * @param response
	 *            the response to send to
	 * @param fileName
	 *            the custom file name
	 * @param mimeType
	 *            the mimetype
	 */
	@SuppressWarnings("static-method")
	public void sendFile(File file, Range range, boolean forDownload, HttpServletResponse response, String fileName,
			String mimeType) {
		if (file == null || !file.exists()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			flushResponse(response);
			return;
		}

		setContentType(mimeType, response);
		addDispositionIfNeeded(file.getName(), response, forDownload, fileName);
		addContentLength(file.length(), response, range);
		setDownloadCookie(response);
		response.setStatus(range.isAllRequested() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_PARTIAL_CONTENT);
		sendFile(file, response, range);
	}

	/**
	 * Related to https://github.com/johnculviner/jquery.fileDownload - library used in the UI2 to indicate the success
	 * or fail of the file download. We write a cookie to indicate that a file download has been initiated properly
	 * (instead of an error page). The response from the web server will now look something like this:
	 *
	 * <pre>
	 * 		Content-Disposition: attachment; filename=Report0.docx
	 * 		Set-Cookie: fileDownload=true; path=/
	 * </pre>
	 *
	 * While we can’t directly tell if a file download has occurred we can check for the existence of a cookie which is
	 * exactly how jQuery File Download works. IMPORTANT!!! The cookie must set setSecure to <code>false</code> in order
	 * the UI library to work properly. Annotated with <code>@SuppressWarnings("squid:S2092")</code> in order to
	 * suppress SonarQube issue http://sonarqube.vpn.ittbg.com:8081/sonarqube/coding_rules#rule_key=squid%3AS2092
	 *
	 * @param response
	 *            the response
	 */
	@SuppressWarnings("squid:S2092")
	private static void setDownloadCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("fileDownload", "true");
		cookie.setSecure(false);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	private static void flushResponse(HttpServletResponse response) {
		try {
			response.flushBuffer();
		} catch (IOException e) {
			LOGGER.trace("", e);
			LOGGER.warn("Client disconnected during content download: {}", e.getMessage());
		}
	}

}
