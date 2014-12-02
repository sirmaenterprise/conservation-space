package com.sirma.itt.cmf.services.ws.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.emf.security.AuthenticationService;

/**
 * The provides access to content of documents, by proxing access to dms.
 */
@WebServlet(urlPatterns = { "/content/*" }, name = "Content Access Proxy")
public class ContentAccessServlet extends HttpServlet {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ContentAccessServlet.class);

	/** The Constant debug. */
	private static final boolean debug = LOGGER.isDebugEnabled();

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The document service. */
	@Inject
	private DocumentService documentService;

	/** The authentication service. */
	@Inject
	private AuthenticationService authenticationService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		processGet(req, resp);
	}

	/**
	 * Process the request of getting the content of file. Content is write in the response stream
	 * of servlet. The id of doc is its db id and it is provided in the last url segment
	 * <code> .../content/10</code>.
	 *
	 * @param req
	 *            HttpServletRequest.
	 * @param resp
	 *            HttpServletResponse.
	 * @throws ServletException
	 *             the servlet exception
	 */
	public void processGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		DocumentInstance documentInstance = loadDocumentInstance(req.getRequestURI());

		try (BufferedOutputStream out = new BufferedOutputStream(resp.getOutputStream())) {
			resp.setCharacterEncoding("UTF-8");
			resp.reset();
			resp.setContentType((String) documentInstance.getProperties().get(
					DocumentProperties.MIMETYPE));
			documentService.getContent(documentInstance, out);
			out.flush();
		} catch (IOException e) {
			throw new ServletException(e);
		}

	}

	/**
	 * Load document instance.
	 * 
	 * @param requestURI
	 *            the request uri
	 * @return the document instance
	 * @throws ServletException
	 *             the servlet exception
	 */
	private DocumentInstance loadDocumentInstance(String requestURI) throws ServletException {
		int idTokenStart = requestURI.lastIndexOf('/');
		if (idTokenStart == -1) {
			throw new ServletException("Invalid request uri");
		}
		DocumentInstance documentInstance = null;
		try {
			String parameter = requestURI.substring(idTokenStart + 1);
			if (debug) {
				LOGGER.debug("Accessing document with id:" + parameter + " by user "
						+ authenticationService.getCurrentUserId());
			}
			documentInstance = documentService.loadByDbId(parameter);
		} catch (Exception e) {
			throw new ServletException(e);
		}
		if (documentInstance == null) {
			throw new ServletException("Invalid document is provided or not found!");
		}
		return documentInstance;
	}
}
