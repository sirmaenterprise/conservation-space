package com.sirma.itt.emf.web.session;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Accepts requests from web component to check if the session of the user is still active or not.
 * 
 * @author svelikov
 */
@WebServlet(urlPatterns = { "/activeSession" })
public class CheckSessionServlet extends HttpServlet {

	private static final long serialVersionUID = -6837200289149569423L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		boolean isActive = false;
		HttpSession session = request.getSession(false);
		if ((session != null) && !session.isNew()) {
			isActive = true;
		}

		if (isActive) {
			pw.print(true);
		} else {
			pw.print(false);
		}
	}
}
