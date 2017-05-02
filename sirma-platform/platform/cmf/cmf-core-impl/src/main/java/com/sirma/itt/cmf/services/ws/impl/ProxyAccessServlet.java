package com.sirma.itt.cmf.services.ws.impl;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The provides access to content of any dms script, by proxing access to dms. Access is done with current authenticated
 * user. Only GET requests are supported
 */
@WebServlet(urlPatterns = { "/dms/proxy/*" }, name = "DMS Access Proxy")
public class ProxyAccessServlet extends BaseContentAccessServlet {
	private static final long serialVersionUID = -1449036375457371926L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyAccessServlet.class);

	private static final Pattern FIELD_PATTERN = Pattern.compile("/.+/dms/proxy");

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		sendContentToResponse(req, resp, false);
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected String parseRequest(HttpServletRequest req) {
		return FIELD_PATTERN.matcher(req.getRequestURI()).replaceAll("");
	}
}
