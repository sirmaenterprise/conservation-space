package com.sirma.itt.emf.audit.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.sirma.itt.emf.audit.activity.AuditActivity;

/**
 * Service for exporting {@link AuditActivity} to specific formats.
 *
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
public interface AuditExportService {

	/**
	 * Exports the provided {@link List} of {@link AuditActivity} to a CSV file.
	 *
	 * @param activities
	 *            the provided {@link List} of {@link AuditActivity}
	 * @param query
	 *            the query params
	 * @param columns
	 *            columns to be exported
	 * @return a {@link File} containing the activities in CSV format
	 * @throws IOException
	 *             if a problem occurs while exporting
	 */
	File exportAsCsv(List<AuditActivity> activities, String query, JSONArray columns) throws IOException, JSONException;

	/**
	 * Exports the provided {@link List} of {@link AuditActivity} to a PDF file.
	 *
	 * @param activities
	 *            the provided {@link List} of {@link AuditActivity}
	 * @param query
	 *            the query params
	 * @param columns
	 *            columns to be exported
	 * @return a {@link File} containing the activities in PDF format
	 * @throws IOException
	 *             if a problem occurs while exporting
	 */
	File exportAsPdf(List<AuditActivity> activities, String query, JSONArray columns) throws IOException, JSONException;
}
