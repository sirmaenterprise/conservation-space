package com.sirma.itt.seip.instance.actions.publish;

/**
 * Request object to execute the publish as PDF operation action
 *
 * @author BBonev
 */
public class PublishAsPdfActionRequest extends PublishActionRequest {

	private static final long serialVersionUID = -7196248185133377777L;
	protected static final String ACTION_NAME = "publishAsPdf";

	/**
	 * Gets the operation.
	 *
	 * @return the operation
	 */
	@Override
	public String getOperation() {
		return ACTION_NAME;
	}

}
