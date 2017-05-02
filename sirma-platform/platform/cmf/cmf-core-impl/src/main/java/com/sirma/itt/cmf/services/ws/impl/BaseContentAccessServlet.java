/*
 *
 */
package com.sirma.itt.cmf.services.ws.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.rmi.ServerException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Basic content access servlet via GET method. The implementation could add disposition header to the response if
 * needed.
 *
 * @author BBonev
 */
public abstract class BaseContentAccessServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final long serialVersionUID = -8711671019070536237L;
	@Inject
	private RESTClient restClient;
	@Inject
	protected SecurityContext securityContext;

	@Override
	protected abstract void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new ServletException("Servlet does not support POST!");
	}

	/**
	 * Process the request of getting the content of uri. Content is written in the response stream of servlet with
	 * direct copy of request to dms
	 *
	 * @param req
	 *            HttpServletRequest.
	 * @param resp
	 *            HttpServletResponse.
	 * @param addDisposition
	 *            the add disposition
	 * @throws ServletException
	 *             the {@link ServletException} or any other wrapped in {@link ServletException}
	 */
	public void sendContentToResponse(final HttpServletRequest req, final HttpServletResponse resp,
			boolean addDisposition) throws ServletException {
		// create local dms proxy address
		Serializable requestURI = parseRequest(req);
		if (requestURI == null) {
			throw new ServletException("Could not parse request!");
		}
		accessContent(requestURI, resp, addDisposition);
	}

	/**
	 * Gets the logger to use
	 *
	 * @return the logger
	 */
	protected abstract Logger getLogger();

	/**
	 * Parses the servlet request and returns some data that will be passed to the method
	 * {@link #getAccessStream(Serializable, HttpServletResponse)}. For default implementation the method should return
	 * an URL link to the resource to access. If not then the method
	 * {@link #getAccessStream(Serializable, HttpServletResponse)} should be overridden to handle the different format.
	 *
	 * @param req
	 *            the request to parse
	 * @return an object that represent the parsed request if null an exception will be thrown
	 * @throws ServletException
	 *             the servlet exception if could not parse the request to something is missing
	 */
	protected abstract Serializable parseRequest(final HttpServletRequest req) throws ServletException;

	/**
	 * Access content using the given request URI and send it to the given response object. The provided parameter is
	 * the one returned from the method {@link #parseRequest(HttpServletRequest)}
	 *
	 * @param requestURI
	 *            the request uri
	 * @param resp
	 *            the resp
	 * @param addDisposition
	 *            the add disposition
	 */
	protected void accessContent(final Serializable requestURI, final HttpServletResponse resp,
			final boolean addDisposition) {
		// access the content using the provided security context
		try {
			doPrivilegedGet(requestURI, resp, addDisposition);
		} catch (ServletException e) {
			LOGGER.warn("", e);
		}
	}

	/**
	 * Gets the access stream to the resource that is pointed by the given parsed request. The default implementation
	 * expects a URL link to get the content from and sets the content type of the response.
	 *
	 * @param requestURI
	 *            the request uri
	 * @param resp
	 *            the resp
	 * @return the access stream
	 * @throws Exception
	 *             the exception
	 */
	protected InputStream getAccessStream(Serializable requestURI, HttpServletResponse resp) throws ServletException {
		getLogger().trace("Initiatiating request " + requestURI);

		HttpMethod requestGet;
		try {
			requestGet = restClient.rawRequest(restClient.createMethod(new GetMethod(), "", true),
					requestURI.toString());
			if (requestGet == null) {
				throw new ServletException();
			}

			Header contentType = requestGet.getResponseHeader("Content-Type");
			if (contentType != null && contentType.getValue() != null) {
				resp.setContentType(contentType.getValue());
			}
			return requestGet.getResponseBodyAsStream();
		} catch (DMSClientException | IOException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Do privileged get.
	 *
	 * @param requestURI
	 *            the request uri
	 * @param resp
	 *            the resp
	 * @param addDisposition
	 *            the add disposition
	 * @throws ServletException
	 *             the servlet exception
	 */
	protected void doPrivilegedGet(Serializable requestURI, HttpServletResponse resp, boolean addDisposition)
			throws ServletException {
		try (BufferedOutputStream out = new BufferedOutputStream(resp.getOutputStream());
				InputStream in = getAccessStream(requestURI, resp)) {
			if (in == null) {
				throw new ServerException("Could not load resource");
			}
			setResponseHeaders(resp, addDisposition,requestURI);
			IOUtils.copyLarge(in, out);
			out.flush();
		} catch (ServletException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}


	/**
	 * Sets the response headers as character encoding and content disposition if requested.
	 *
	 * @param resp
	 *            the resp
	 * @param disposition
	 *            the new response headers
	 */
	@SuppressWarnings("static-method")
	protected void setResponseHeaders(HttpServletResponse resp, boolean disposition, Serializable requestData) {
		resp.setCharacterEncoding("UTF-8");
		if (disposition) {
			resp.setHeader("Content-Disposition", "attachment");
		}
	}
}
