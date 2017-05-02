package com.sirma.itt.seip.content.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.rest.Range;

/**
 * Servlet that provides a instance content for download. The servlet supports HTTP HEAD and HTTP Range header to
 * provide partial content.
 * <p>
 * Note that when user accesses this servlet if it's not logged in it will be be redirected by the security module for
 * authentication before landing here.
 *
 * @author BBonev
 */
@WebServlet(urlPatterns = { "/share/content/*" }, name = "Content download access servlet")
public class ContentDownloadServlet extends HttpServlet {

	private static final long serialVersionUID = -202595861419265591L;

	@Inject
	private ContentDownloadService downloadService;

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String contentId = getContentId(req);
		if (StringUtils.isBlank(contentId)) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		Response response = downloadService.getContentHeadResponse(contentId, getContentPurpose(req));
		copyHeaders(response, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String contentId = getContentId(req);
		if (StringUtils.isBlank(contentId)) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		Range range = Range.fromString(req.getHeader("Range"));
		downloadService.sendContent(contentId, getContentPurpose(req), range, true, resp, null);
	}

	private static void copyHeaders(Response response, HttpServletResponse resp) {
		resp.setStatus(response.getStatus());
		response.getStringHeaders().forEach((key, values) -> {
			for (String value : values) {
				resp.addHeader(key, value);
			}
		});
	}

	private static String getContentId(HttpServletRequest req) {
		String requestURI = req.getRequestURI();
		int lastIndexOf = requestURI.lastIndexOf('/');
		if (lastIndexOf >= 0) {
			return requestURI.substring(lastIndexOf + 1);
		}
		return null;
	}

	private static String getContentPurpose(HttpServletRequest req) {
		String purpose = req.getParameter("purpose");
		if (StringUtils.isBlank(purpose)) {
			purpose = Content.PRIMARY_CONTENT;
		}
		return purpose;
	}
}
