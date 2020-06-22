package com.sirma.itt.seip.instance.editoffline.actions;

import javax.servlet.http.HttpServletResponse;

import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.itt.seip.rest.Range;

/**
 * Used to store the information needed to execute correctly edit offline check out action.
 *
 * @author T. Dossev
 */
public class EditOfflineCheckOutRequest extends ActionRequest {

	private static final long serialVersionUID = -5605664378779019622L;

	public static final String EDIT_OFFLINE_CHECK_OUT = "editOfflineCheckOut";

	private HttpServletResponse response;
	private Range range;
	private boolean forDownload;
	private String purpose;

	@Override
	public String getOperation() {
		return EDIT_OFFLINE_CHECK_OUT;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public Range getRange() {
		return range;
	}

	public void setRange(Range range) {
		this.range = range;
	}

	public boolean getForDownload() {
		return forDownload;
	}

	public void setForDownload(boolean forDownload) {
		this.forDownload = forDownload;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

}
