package com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg;

import java.util.Objects;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices;

/**
 * Wrapper for request argument related to {@link SpreadsheetEAIServices#PREPARE} service.
 * 
 * @author bbanchev
 */
public class ReadRequestArgument {
	private InstanceReference context;
	private InstanceReference source;

	/**
	 * Instantiate new argument.
	 * 
	 * @param source
	 *            - the local content source. Required non null
	 * @param context
	 *            - the context to use for read operation.
	 */
	public ReadRequestArgument(InstanceReference source, InstanceReference context) {
		Objects.requireNonNull(source, "Source instance is a required argument!");
		this.source = source;
		this.context = context;
	}

	/**
	 * Gets the set source.
	 *
	 * @return the source
	 */
	public InstanceReference getSource() {
		return source;
	}

	/**
	 * Gets the set context.
	 *
	 * @return the context
	 */
	public InstanceReference getContext() {
		return context;
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append("ReadRequestArgument [source=")
					.append(getSource())
					.append(", context=")
					.append(getContext())
					.append("]")
					.toString();
	}

}
