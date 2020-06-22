package com.sirma.itt.emf.solr.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The ModelUpdateAdminHandler is servlet to handle solr update requestss
 */
public class ModelUpdateAdminHandler extends HttpServlet {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6854362621743232252L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		System.out.println("ModelUpdateAdminHandler.doGet()");
		super.doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("ModelUpdateAdminHandler.doGet()");
		super.doPost(req, resp);
	}

}
