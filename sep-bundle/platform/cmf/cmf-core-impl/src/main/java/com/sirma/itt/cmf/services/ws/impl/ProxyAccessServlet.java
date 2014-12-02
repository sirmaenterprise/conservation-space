package com.sirma.itt.cmf.services.ws.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
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
 * The provides access to content of any dms script, by proxing access to dms. Access is done with
 * current authenticated user. Only GET requests are supported
 */
@WebServlet(urlPatterns = { "/service/dms/proxy/*" }, name = "DMS Access Proxy")
public class ProxyAccessServlet extends HttpServlet {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(ProxyAccessServlet.class);
	/** The Constant debug. */
	private static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Inject
	private RESTClient restClient;

	@Inject
	private AuthenticationService authenticationService;

	/**
	 * Default constructor.
	 */
	public ProxyAccessServlet() {
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
	 * Process the request of getting the content of uri. Content is write in the response stream of
	 * servlet.
	 *
	 * @param req
	 *            HttpServletRequest.
	 * @param resp
	 *            HttpServletResponse.
	 * @throws ServletException
	 *             the servlet exception
	 */
	public void processGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException {
		// create local dms proxy address
		final String requestURI = req.getRequestURI().replaceAll("/.+/service/dms/proxy", "");
		// EmfUser executer = new EmfUser(user.getIdentifier());
		// executer.setTicket(((UserWithCredentials) user).getTicket());
		SecurityContextManager.callAs(authenticationService.getCurrentUser(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				BufferedOutputStream out = null;
				try {
					resp.setCharacterEncoding("UTF-8");
					resp.reset();
					out = new BufferedOutputStream(resp.getOutputStream());
					if (TRACE_ENABLED) {
						LOGGER.trace("Initiatiating request " + requestURI);
					}
					HttpMethod requestGet = restClient.rawRequest(
							restClient.createMethod(new GetMethod(), "", true), requestURI);
					if (requestGet == null) {
						throw new ServletException();
					}

					Header contentType = requestGet.getResponseHeader("Content-Type");
					if (contentType != null && contentType.getValue() != null) {
						String value = contentType.getValue();
						resp.setContentType(value);
					}
					IOUtils.copyLarge(requestGet.getResponseBodyAsStream(), out);
					resp.flushBuffer();
				} catch (ServletException e) {
					throw e;
				} catch (Exception e) {
					throw new ServletException(e);
				} finally {
					IOUtils.closeQuietly(out);
				}
				return null;
			}
		});
	}
}
