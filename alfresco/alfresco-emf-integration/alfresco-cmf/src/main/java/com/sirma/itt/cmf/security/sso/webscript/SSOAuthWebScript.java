package com.sirma.itt.cmf.security.sso.webscript;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.web.scripts.bean.Login;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * The Class OpenSSOAuthWebScript.
 */
public class SSOAuthWebScript extends Login {

	/** The authentication service. */
	private AuthenticationService authenticationService;

	/** The authentication component. */
	private AuthenticationComponent authenticationComponent;

	/**
	 * Gets the authentication component.
	 * 
	 * @return the authentication component
	 */
	public AuthenticationComponent getAuthenticationComponent() {
		return this.authenticationComponent;
	}

	/**
	 * Sets the authentication component.
	 * 
	 * @param authenticationComponent
	 *            the new authentication component
	 */
	public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
		this.authenticationComponent = authenticationComponent;
	}

	/**
	 * Gets the authentication service.
	 * 
	 * @return the authentication service
	 */
	public AuthenticationService getAuthenticationService() {
		return this.authenticationService;
	}

	@Override
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status) {

		try {
			Map<String, Object> model = new HashMap<String, Object>();
			String currentTicket = this.authenticationService.getCurrentTicket();
			model.put("ticket", currentTicket);
			return model;
		} finally {
			this.authenticationService.clearCurrentSecurityContext();
		}
	}
}