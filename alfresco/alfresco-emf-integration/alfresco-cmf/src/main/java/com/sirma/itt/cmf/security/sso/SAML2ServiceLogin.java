/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.sirma.itt.cmf.security.sso;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class SAML2ConsumerServlet.
 */
public class SAML2ServiceLogin extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(SAML2ServiceLogin.class);
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7143296520344698617L;

	/** The alfresco facade. */
	private AlfrescoFacade alfrescoFacade;

	/** The client. */
	private WSO2SAMLClient client;

	/** The sso enabled. */
	private boolean ssoEnabled;

	/**
	 * Servlet init.
	 * 
	 * @param config
	 *            the config
	 * @throws ServletException
	 *             the servlet exception
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		alfrescoFacade = new AlfrescoFacade(config.getServletContext());
		ssoEnabled = SAMLSSOConfigurations.isSSOEnabled();
	}

	/**
	 * Do get.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ServletException
	 *             the servlet exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * Do post.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ServletException
	 *             the servlet exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (ssoEnabled) {
			try {
				client = new WSO2SAMLClient(request);
				client.doPost(request, response, alfrescoFacade);
			} catch (Exception e) {
				LOGGER.error("Failed to process SAML request!", e);
			}
		}
	}
}
