package com.sirma.itt.seip.instance.actions.compare;

import java.io.Serializable;

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

	private String authentication;

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

	public String getAuthentication() {
		return authentication;
	}

	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}

}
