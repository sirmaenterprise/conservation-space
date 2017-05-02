package com.sirma.itt.cmf.services.ws.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet provides dms like access url to a document that is proxied with emf security interceptors upon request.
 */
@WebServlet(urlPatterns = { "/document/access/*" }, name = "Document Access Proxy")
public class ContentDirectAccessServlet extends BaseContentAccessServlet {
	private static final long serialVersionUID = -7543549781345370747L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyAccessServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		sendContentToResponse(req, resp, true);
	}

	@Override
	protected String parseRequest(final HttpServletRequest req) {
		return req.getRequestURI().replaceAll("/.+/document/access", "");
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
