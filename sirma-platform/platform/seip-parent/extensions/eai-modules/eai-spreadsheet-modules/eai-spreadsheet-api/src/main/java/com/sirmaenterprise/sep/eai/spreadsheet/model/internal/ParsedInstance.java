package com.sirmaenterprise.sep.eai.spreadsheet.model.internal;

import static java.util.Objects.requireNonNull;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;

/**
 * The ParsedInstance is wrapper for parsed SEP instance keyed by the externalId in the store that is extracted from.
 * {@link ParsedInstance#getExternalId()} should return an id that this instance could be accessed directly and
 * uniquely.
 *
 * @author bbanchev
 */
public class ParsedInstance {

	private SpreadsheetEntryId externalId;
	private Instance parsed;
	private Instance context;
	private SpreadsheetEntry source;
	private final Operation saveOperation;

	/**
	 * Instantiates a new parsed instance.
	 *
	 * @param id the external id. Required non null
	 * @param parsed the parsed instance. Required non null
	 * @param source the source entry of this {@link ParsedInstance}. Required non null
	 * @param context the new context of the instance. It can be null, and in that case the current context is not
	 *        updated
	 * @param saveOperation the operation used when the {@link ParsedInstance#parsed} is saved
	 */
	public ParsedInstance(SpreadsheetEntryId id, Instance parsed, SpreadsheetEntry source, Instance context,
			Operation saveOperation) {
		externalId = requireNonNull(id, "External identifier is a required argument to construct model instance!");
		this.parsed = requireNonNull(parsed, "Parsed instance is a required argument to construct model instance!");
		this.source = requireNonNull(source, "Source instance is a required argument to construct model instance!");
		this.saveOperation = requireNonNull(saveOperation, "Operation is a required argument!");
		this.context = context;
	}

	/**
	 * Gets the external id - uid in the source object
	 *
	 * @return the external id
	 */
	public SpreadsheetEntryId getExternalId() {
		return externalId;
	}

	/**
	 * Gets the parsed instance as SEP instance (might not be persisted)
	 *
	 * @return the parsed instance
	 */
	public Instance getParsed() {
		return parsed;
	}

	/**
	 * Gets the {@link ParsedInstance} source data with optionally updated properties
	 *
	 * @return the updated source
	 */
	public SpreadsheetEntry getSource() {
		return source;
	}

	/**
	 * Gets the context of the instance.
	 *
	 * @return the context
	 */
	public Instance getContext() {
		return context;
	}

	public Operation getSaveOperation() {
		return saveOperation;
	}

	@Override
	public String toString() {
		return externalId + "/" + getParsed().getId();
	}
}