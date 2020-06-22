package com.sirma.sep.content.rest;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.Range;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.shared.ShareCodeUtils;
import com.sirma.itt.seip.shared.security.ShareCodeSecurityAuthenticator;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentConfigurations;

/**
 * Servlet that provides a instance content for download. The servlet supports HTTP HEAD and HTTP
 * Range header to provide partial content.
 *
 * @author BBonev
 */
@WebServlet(urlPatterns = { ContentDownloadServlet.SERLVET_PATH + "*" }, name = "Content download access servlet")
public class ContentDownloadServlet extends HttpServlet {

	protected static final String SERLVET_PATH = "/share/content/";

	private static final long serialVersionUID = -202595861419265591L;

	@Inject
	private ContentDownloadService downloadService;

	@Inject
	private ContentConfigurations contentConfigurations;

	@Inject
	private SecurityContextManager securityContextManager;

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

	/**
	 * Retrieve the content by given content id and shareCode. If the share code or contentId is
	 * missing or has been tampered with, the content info wont be retrieved. This is to prevent
	 * users from accessing resources that have not been shared by just changing the contentId in
	 * the url. Actual sharecode verification and validation is performed in
	 * {@link ShareCodeSecurityAuthenticator} which uses the {@link ShareCodeUtils}.
	 * 
	 * @param req
	 *            the http servlet request
	 * @param resp
	 *            the http servlet response
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String contentId = getContentId(req);
		if (StringUtils.isBlank(contentId)) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		Range range = Range.fromString(req.getHeader("Range"));
		String shareCode = req.getParameter("shareCode");
		Map<String, String> contextProperties = CollectionUtils.createHashMap(4);
		contextProperties.put(ShareCodeSecurityAuthenticator.SHARE_CODE, shareCode);
		contextProperties.put(ShareCodeSecurityAuthenticator.RESOURCE_ID, contentId);
		contextProperties.put(ShareCodeSecurityAuthenticator.SECRET_KEY,
				contentConfigurations.getShareCodeSecretKey().get());
		// This is added because we need to skip the session authentication. After all, if the user
		// + tenant info isn't provided in the share code, we don't want to try to access the
		// content with a user from another session that might not even be in the same tenant.
		contextProperties.put(Authenticator.FORCE_AUTHENTICATION, "true");
		if (securityContextManager.initializeExecution(AuthenticationContext.create(contextProperties))) {
			try {
				downloadService.sendContent(contentId, getContentPurpose(req), range, true, resp, null);
			} finally {
				securityContextManager.endContextExecution();
			}
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		}
		resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
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

	/**
	 * Security exclusion for the content download servlet.
	 *
	 * @author nvelkov
	 */
	@Extension(target = SecurityExclusion.TARGET_NAME, order = 0.8)
	public static class ContentDownloadServletSecurityExclusion implements SecurityExclusion {

		@Override
		public boolean isForExclusion(String path) {
			return path.startsWith(ContentDownloadServlet.SERLVET_PATH);
		}
	}
}
