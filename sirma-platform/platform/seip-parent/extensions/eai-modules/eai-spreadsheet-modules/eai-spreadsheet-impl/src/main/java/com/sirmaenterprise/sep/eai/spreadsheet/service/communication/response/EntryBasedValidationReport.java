package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetEntryId;

/**
 * Extend {@link ErrorBuilderProvider} by including messages by key
 * 
 * @author bbanchev
 */
class EntryBasedValidationReport extends ErrorBuilderProvider {
	/** Message for success. */
	public static final String MSG_SUCCESSFUL_VALIDATION = "Successful validation!";

	private Map<SpreadsheetEntryId, StringBuilder> errors = new TreeMap<>(comparator());

	private static Comparator<SpreadsheetEntryId> comparator() {
		return (o1, o2) -> {
			int sheetCompare = Integer.compare(Integer.parseInt(o1.getSheetId()), Integer.parseInt(o2.getSheetId()));
			if (sheetCompare != 0) {
				return sheetCompare;
			}
			return Integer.compare(Integer.parseInt(o1.getExternalId()), Integer.parseInt(o2.getExternalId()));
		};
	}

	@Override
	public synchronized boolean hasErrors() {
		return !errors.isEmpty();
	}

	@Override
	public synchronized EntryBasedValidationReport append(Object data) {
		return setAndAppend(null, data);
	}

	synchronized EntryBasedValidationReport setAndAppend(SpreadsheetEntryId key, Object data) {
		if (key == null) {
			return (EntryBasedValidationReport) super.append(data);
		}
		errors.computeIfAbsent(key, mapKey -> new StringBuilder(256)).append(data);
		return this;
	}

	@Override
	protected int getInitialLength() {
		return 256 * errors.size();
	}

	@Override
	public synchronized String build() {
		if (hasErrors() && !super.hasErrors()) {
			// build and cache errors
			errors.forEach((k, v) -> append(k).append(":").separator().append(v).separator().separator());
		}
		return super.build().trim();
	}
}
