package com.sirmaenterprise.sep.eai.spreadsheet.model.request;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg.ReadRequestArgument;

/**
 * Defines {@link ServiceRequest} for content processing in spreadsheet EAI API as read request.
 * 
 * @author bbanchev
 */
public class SpreadsheetReadServiceRequest implements ServiceRequest {

	private static final long serialVersionUID = -3641557208207864190L;
	protected ReadRequestArgument request;

	/**
	 * Instantiates a new spreadsheet read service request.
	 *
	 * @param request
	 *            the request read argument
	 */
	public SpreadsheetReadServiceRequest(ReadRequestArgument request) {
		this.request = request;
	}

	/**
	 * Gets the request.
	 *
	 * @return the request
	 */
	protected ReadRequestArgument getRequest() {
		return request;
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public InstanceReference getSource() {
		return request.getSource();
	}

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public InstanceReference getContext() {
		return request.getContext();
	}

	@Override
	public String toString() {
		return "Read request: " + request;
	}
}
