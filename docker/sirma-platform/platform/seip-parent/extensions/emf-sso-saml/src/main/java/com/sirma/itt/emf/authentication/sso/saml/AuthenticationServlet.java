package com.sirma.itt.emf.authentication.sso.saml;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;

/**
 * Authentication servlet. It is only responsible for redirecting to the IdP.
 *
 * @author yasko
 */
@WebServlet(AuthenticationServlet.SERLVET_PATH)
public class AuthenticationServlet extends HttpServlet {

	private static final long serialVersionUID = -8362554735879931954L;

	protected static final String SERLVET_PATH = "/auth";

	@Inject
	private SAMLRequestBuilder requestBuilder;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String redirect = req.getParameter("url");
		res.sendRedirect(requestBuilder.build(req, redirect));
	}

	/**
	 * Security exclusion for the Authentication Servlet.
	 *
	 * @author Adrian Mitev
	 */
	@Extension(target = SecurityExclusion.TARGET_NAME, order = 0.7)
	public static class AuthenticationServletSecurityExclusion implements SecurityExclusion {

		@Override
		public boolean isForExclusion(String path) {
			return path.startsWith(AuthenticationServlet.SERLVET_PATH);
		}
	}
}
