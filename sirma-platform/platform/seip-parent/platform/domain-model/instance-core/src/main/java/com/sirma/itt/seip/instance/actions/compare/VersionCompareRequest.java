package com.sirma.itt.seip.instance.actions.compare;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Contains required data for correct execution of compare versions operations.
 *
 * @author A. Kunchev
 */
public class VersionCompareRequest extends ActionRequest {

	public static final String COMPARE_VERSIONS = "compareVersions";

	private static final long serialVersionUID = 3446018182355540401L;

	private Serializable firstSourceId;

	private Serializable secondSourceId;

	private Map<String, String> authenticationHeaders;

	@Override
	public String getOperation() {
		return COMPARE_VERSIONS;
	}

	public Serializable getFirstSourceId() {
		return firstSourceId;
	}

	public void setFirstSourceId(Serializable firstSourceId) {
		this.firstSourceId = firstSourceId;
	}

	public Serializable getSecondSourceId() {
		return secondSourceId;
	}

	public void setSecondSourceId(Serializable secondSourceId) {
		this.secondSourceId = secondSourceId;
	}

	public Map<String, String> getAuthenticationHeaders() {
		return authenticationHeaders;
	}

	public void setAuthenticationHeaders(Map<String, String> authenticationHeaders) {
		this.authenticationHeaders = authenticationHeaders;
	}

}
