package com.sirma.itt.seip.content.rest;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.rest.Range;

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
						.header("Accept-Ranges", Range.BYTES)
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
	 * @param response
	 *            the response to send to
	 */
	public void sendPreview(String contentId, String purpose, HttpServletResponse response) {
		if (StringUtils.isBlank(contentId)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		ContentInfo contentInfo = instanceContentService.getContentPreview(contentId, purpose);
		if (!contentInfo.exists()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		setPreviewContentType(contentInfo, response);
		sendContent(contentInfo, response, Range.ALL);
		response.setStatus(HttpServletResponse.SC_OK);
	}

	private static void setPreviewContentType(ContentInfo contentInfo, HttpServletResponse response) {
		String contentType = "application/pdf";
		if (contentInfo.getMimeType() != null && contentInfo.getMimeType().startsWith("image/")) {
			contentType = contentInfo.getMimeType();
		}
		response.setContentType(contentType);
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
			HttpServletResponse response,
			String fileName) {
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

		setContentType(contentInfo, response);
		addDispositionIfNeeded(contentInfo, response, forDownload, fileName);
		addContentLength(contentInfo, response, range);
		sendContent(contentInfo, response, range);
		response.setStatus(range.isAllRequested() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_PARTIAL_CONTENT);
	}

	private static void sendContent(ContentInfo contentInfo, HttpServletResponse response, Range range) {
		addHeaders(response);
		try (ServletOutputStream out = response.getOutputStream(); InputStream in = contentInfo.getInputStream()) {
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

	private static void addHeaders(HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		Collection<String> cacheControl = response.getHeaders(HttpHeaders.CACHE_CONTROL);
		// do not add cache control if already present. This way content caching can be enabled per service
		if (isEmpty(cacheControl)) {
			response.addHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
			response.addHeader("Pragma", "no-cache");
			response.addHeader(HttpHeaders.EXPIRES, "0");
		}
	}

	private static void addDispositionIfNeeded(ContentInfo contentInfo, HttpServletResponse response,
			boolean forDownload, String fileName) {
		if (forDownload) {
			String name = contentInfo.getName();
			if (StringUtils.isNotBlank(fileName)) {
				name = fileName;
			}
			StringBuilder disposition = new StringBuilder(256)
					.append("attachment; filename=\"")
						.append(name)
						.append("\"");
			try {
				disposition.append("; filename*=utf-8''").append(URIUtil.encodePath(name));
			} catch (URIException e) {
				LOGGER.warn("Fail encoding file name: {} with {}", name, e.getMessage());
				LOGGER.trace("Fail encoding file name {} with error", name, e);
			}
			response.setHeader("Content-Disposition", disposition.toString());
		}
	}

	private static void addContentLength(ContentInfo contentInfo, HttpServletResponse response, Range range) {
		long contentLength = contentInfo.getLength();
		if (contentLength > 0L) {
			response.setHeader("Accept-Ranges", Range.BYTES);

			if (!range.isAllRequested()) {
				if (range.isToTheEnd()) {
					contentLength = contentInfo.getLength() - range.getFrom();
				} else {
					contentLength = range.getRangeLength();
				}
				response.setHeader("Content-Range", range.asResponse(contentInfo.getLength()));
			}
			response.setContentLengthLong(contentLength);
		}
	}

	private static void setContentType(ContentInfo contentInfo, HttpServletResponse response) {
		response.setContentType(
				contentInfo.getMimeType() == null ? MediaType.APPLICATION_OCTET_STREAM : contentInfo.getMimeType());
	}
}
