package com.sirma.itt.cmf.services.ws.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;

/**
 * Servlet provides dms like access url to a document that is proxied with emf security interceptors
 * upon request.
 */
@WebServlet(urlPatterns = { "/document/access/*" }, name = "Document Access Proxy")
public class ContentDirectAccessServlet extends HttpServlet {

	/** The serialVersionUID. */
	private static final long serialVersionUID = -7766223945703741986L;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(ProxyAccessServlet.class);
	/** The Constant debug. */
	private static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();

	@Inject
	private RESTClient restClient;

	@Inject
	private AuthenticationService authenticationService;

	/**
	 * Default constructor.
	 */
	public ContentDirectAccessServlet() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest ,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		processGet(req, resp);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest ,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		throw new ServletException("Servlet does not support POST!");
	}

	/**
	 * Process the request of getting the content of uri. Content is written in the response stream
	 * of servlet with direct copy of request to dms
	 *
	 * @param req
	 *            HttpServletRequest.
	 * @param resp
	 *            HttpServletResponse.
	 * @throws ServletException
	 *             the {@link ServletException} or any other wrapped in {@link ServletException}
	 */
	public void processGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException {
		// create local dms proxy address
		final String requestURI = req.getRequestURI().replaceAll("/.+/document/access", "");
		SecurityContextManager.callAs(authenticationService.getCurrentUser(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				BufferedOutputStream out = null;
				InputStream in = null;
				try {
					out = new BufferedOutputStream(resp.getOutputStream());
					if (TRACE_ENABLED) {
						LOGGER.trace("Initiatiating request " + requestURI);
					}
					HttpMethod requestGet = restClient.rawRequest(
							restClient.createMethod(new GetMethod(), "", true), requestURI);
					if (requestGet == null) {
						throw new ServletException();
					}
					resp.setCharacterEncoding("UTF-8");
					resp.reset();
					Header contentType = requestGet.getResponseHeader("Content-Type");
					if (contentType != null && contentType.getValue() != null) {
						String value = contentType.getValue();
						resp.setContentType(value);
						resp.setHeader("Content-Disposition", "attachment");
					}
					in = requestGet.getResponseBodyAsStream();
					IOUtils.copyLarge(in, out);
					out.flush();
				} catch (ServletException e) {
					throw e;
				} catch (Exception e) {
					throw new ServletException(e);
				} finally {
					IOUtils.closeQuietly(in);
				}
				return null;
			}
		});
	}
}
