package com.sirma.itt.seip.instance.content.publish;

import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Request DTO for performing the upload revision action.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 25/06/2018
 */
public class UploadRevisionRequest extends ActionRequest {

	public static final String OPERATION_NAME = "uploadRevision";

	private final UploadRequest uploadRequest;
	private final String contentPurpose;

	public UploadRevisionRequest(UploadRequest uploadRequest, String contentPurpose) {
		this.uploadRequest = uploadRequest;
		this.contentPurpose = contentPurpose;
	}

	public UploadRequest getUploadRequest() {
		return uploadRequest;
	}

	public String getContentPurpose() {
		return contentPurpose;
	}

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}
}
