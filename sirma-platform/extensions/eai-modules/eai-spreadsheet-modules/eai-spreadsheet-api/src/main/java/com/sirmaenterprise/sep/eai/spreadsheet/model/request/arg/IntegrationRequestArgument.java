package com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg;

import java.util.Collection;
import java.util.Objects;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetEntryId;

/**
 * Wrapper for request argument related to {@link SpreadsheetEAIServices#RETRIEVE} service. In addition to
 * {@link ReadRequestArgument} contains additional parameters
 * 
 * @author bbanchev
 */
public class IntegrationRequestArgument extends ReadRequestArgument {
	private Collection<SpreadsheetEntryId> entries;
	private InstanceReference report;

	/**
	 * Instantiate new argument.
	 * 
	 * @param source
	 *            - the local content source. Required non null
	 * @param report
	 *            - the generated report. Required non null
	 * @param context
	 *            - the context where to store instances. Might be null
	 * @param entries
	 *            - the set of external identifiers to retrieve
	 */
	public IntegrationRequestArgument(InstanceReference source, InstanceReference report, InstanceReference context,
			Collection<SpreadsheetEntryId> entries) {
		super(source, context);
		Objects.requireNonNull(report, "Report is a required argument!");
		this.report = report;
		this.entries = entries;
	}

	/**
	 * Gets the required entries.
	 *
	 * @return the entries to import
	 */
	public Collection<SpreadsheetEntryId> getEntries() {
		return entries;
	}
 

	@Override
	public String toString() {
		return new StringBuilder()
				.append("ReadRequestArgument [source=")
					.append(getSource())
					.append(", context=")
					.append(getContext())
					.append(", entries=")
					.append(entries)
					.append(", report=")
					.append(report)
					.append("]")
					.toString();
	}
}
