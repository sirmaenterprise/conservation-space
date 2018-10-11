package com.sirma.itt.seip.instance.actions.download;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Used to contain the additional properties needed to execute the download action properly.
 *
 * @author A. Kunchev
 */
public class DownloadRequest extends ActionRequest {

	public static final String DOWNLOAD = "download";

	private static final long serialVersionUID = -6733057221979970600L;

	private String purpose;

	@Override
	public String getOperation() {
		return DOWNLOAD;
	}

	/**
	 * Getter for download purpose.
	 *
	 * @return the purpose
	 */
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Setter for the download purpose.
	 *
	 * @param purpose
	 *            the purpose to set
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
}
