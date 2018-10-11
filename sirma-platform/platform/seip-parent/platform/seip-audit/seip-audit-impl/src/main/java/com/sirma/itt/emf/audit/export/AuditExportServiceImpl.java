package com.sirma.itt.emf.audit.export;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Exports {@link AuditActivity} to specific formats.
 *
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
public class AuditExportServiceImpl implements AuditExportService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditExportServiceImpl.class);

	/** The path to the font file. */
	private static final String FONT_PATH = "com/sirma/itt/emf/audit/export/ARIAL.TTF";

	/** Activity fields that are displayed with their HTML headers and need to be processed before export **/
	private static final List<String> HEADER_BASED_FIELDS = Collections
			.unmodifiableList(Arrays.asList("context", "objectTitle"));

	/** Separator for the contexts when exported **/
	private static final String CONTEXTS_SEPARATOR = " | ";

	/** The temp folder. */
	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private DateConverter dateConverter;

	@Override
	public File exportAsCsv(List<AuditActivity> activities, String query, JSONArray columns)
			throws IOException, JSONException {
		TimeTracker tracker = TimeTracker.createAndStart();

		File file = new File(tempFileProvider.getTempDir(), "audit_" + UUID.randomUUID() + ".csv");

		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")))) {
			writer.write('\ufeff');
			writer.write(query);
			writer.newLine();

			// Build header line
			buildHeaderLine(columns, writer);

			for (AuditActivity activity : activities) {
				writer.newLine();
				if (!writeActivity(columns, writer, activity)) {
					return null;
				}
			}
			writer.flush();
		}

		LOGGER.debug("CSV constructed in {} ms", tracker.stop());
		return file;
	}

	private boolean writeActivity(JSONArray columns, BufferedWriter writer, AuditActivity activity)
			throws JSONException, IOException {
		boolean appendComma = false;
		for (int i = 0; i < columns.length(); i++) {
			JSONObject column = columns.getJSONObject(i);

			try {
				if (column.has("skip")) {
					continue;
				}
				String value = getActivityFormattedValue(activity, column.getString("id"));
				if (StringUtils.isNotEmpty(value)) {
					value = value.replaceAll("\"", "\"\"");
					value = "\"" + value + "\"";
				}
				writer.write((appendComma ? ";" : "") + value);
				appendComma = true;
			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				LOGGER.error("There is no column \"{}\" in Audit Activity.", column.getString("id"), e);
				return false;
			}
		}
		return true;
	}

	private static void buildHeaderLine(JSONArray columns, BufferedWriter writer) throws JSONException, IOException {
		boolean appendComma = false;
		for (int i = 0; i < columns.length(); i++) {
			JSONObject column = columns.getJSONObject(i);
			try {
				AuditActivity.class.getDeclaredField(column.getString("id"));
				writer.write(appendComma ? ";" : "");
				writer.write("\"" + column.getString("name") + "\"");
				appendComma = true;
			} catch (NoSuchFieldException | SecurityException e) {
				column.append("skip", Boolean.TRUE);
				LOGGER.error("There is no column \"{}\" in Audit Activity. Column will be skipped.",
						column.getString("id"), e);
			}
		}
	}

	@Override
	public File exportAsPdf(List<AuditActivity> activities, String query, JSONArray columns)
			throws IOException, JSONException {
		File file = new File(tempFileProvider.getTempDir(), "audit_" + UUID.randomUUID() + ".pdf");
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			writePdfToStream(activities, query, columns, outputStream);
		} catch (DocumentException e) {
			throw new IOException("Error while creating the pdf file.", e);
		}
		return file;
	}

	private void writePdfToStream(List<AuditActivity> activities, String query, JSONArray columns,
			FileOutputStream outputStream) throws DocumentException, JSONException, IOException {
		Document document = new Document();
		PdfWriter.getInstance(document, outputStream);

		TimeTracker tracker = TimeTracker.createAndStart();
		int numberOfColumns = 0;
		for (int i = 0; i < columns.length(); i++) {
			JSONObject column = columns.getJSONObject(i);
			try {
				AuditActivity.class.getDeclaredField(column.getString("id"));
				numberOfColumns++;
			} catch (NoSuchFieldException | SecurityException e) {
				column.append("skip", Boolean.TRUE);
				LOGGER.error("There is no column \"{}\" in Audit Activity. Column will be skipped.",
						column.getString("id"), e);
			}
		}

		document.open();
		// Add the header column with the query.
		PdfPTable table = new PdfPTable(numberOfColumns);
		table.getDefaultCell().setUseAscender(true);
		table.getDefaultCell().setUseDescender(true);
		Font f = new Font();
		f.setColor(Color.white);
		f.setSize(6);
		PdfPCell cell = new PdfPCell(new Phrase(query, f));
		cell.setBackgroundColor(Color.DARK_GRAY);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setColspan(numberOfColumns);
		table.addCell(cell);
		// Add the header collumns with the titles of each column.

		for (int i = 0; i < columns.length(); i++) {
			JSONObject column = columns.getJSONObject(i);
			if (!column.has("skip")) {
				addHeaderCell(table, column.getString("name"));
			}
		}

		for (AuditActivity activity : activities) {
			addRow(table, activity, columns);
		}

		try {
			document.add(table);
		} catch (DocumentException e) {
			throw new IOException("The document hasnt been opened yet, or has been already closed.", e);
		}

		LOGGER.debug("PDF constructed in {} ms", tracker.stop());
		document.close();
	}

	/**
	 * Performs additional formatting for an activity field.
	 *
	 * @param activity
	 *            the activity
	 * @param fieldId
	 *            the name of the field as it is declared in the {@link AuditActivity} class
	 * @return the activity field formatted value
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 */
	private String getActivityFormattedValue(AuditActivity activity, String fieldId)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object originalValue = PropertyUtils.getProperty(activity, fieldId);
		if (originalValue instanceof Date) {
			return dateConverter.getSystemFullFormat().format(originalValue);
		} else {
			originalValue = originalValue != null ? originalValue : "";
			if (HEADER_BASED_FIELDS.contains(fieldId)) {
				return headersToPlainText(originalValue.toString());
			} else {
				return originalValue.toString();
			}

		}
	}

	/**
	 * Adds a row to the table by using the .toString() method of the {@link AuditActivity} object.
	 *
	 * @param table
	 *            the table
	 * @param activity
	 *            the audit activity
	 * @param columns
	 *            columns to be exported
	 * @throws IOException
	 *             The font is invalid.
	 */
	private void addRow(PdfPTable table, AuditActivity activity, JSONArray columns) throws IOException, JSONException {
		BaseFont bf;
		try {
			bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		} catch (DocumentException e) {
			throw new IOException("Could not create the rows font.", e);
		}
		Font font = new Font(bf, 4);

		for (int i = 0; i < columns.length(); i++) {
			JSONObject column = columns.getJSONObject(i);

			try {
				if (!column.has("skip")) {
					String value = getActivityFormattedValue(activity, column.getString("id"));
					table.addCell(new Phrase(value, font));
				}
			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				// Shouldn't happen. Columns are already checked
				LOGGER.error("There is no column \"{}\" in Audit Activity.", column.getString("id"), e);
				return;
			}
		}
	}

	/**
	 * Adds a header cell.
	 *
	 * @param table
	 *            the table
	 * @param text
	 *            the text
	 */
	private static void addHeaderCell(PdfPTable table, String text) {
		try {
			Font f = new Font(BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED));

			f.setColor(Color.white);
			f.setSize(4);
			PdfPCell cell = new PdfPCell(new Phrase(text, f));
			cell.setBackgroundColor(Color.DARK_GRAY);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
		} catch (DocumentException | IOException e) {
			LOGGER.error("Could not add header in the document while exporting the audit log to pdf", e);
		}
	}

	/**
	 * Converts the object header's HTML into plain text by concatenating all available object properties from the
	 * header.
	 *
	 * @param headersHtml
	 *            is the HTML of the header
	 * @return the header converted to plain text
	 */
	private String headersToPlainText(String headersHtml) {
		StringBuilder processedContexts = new StringBuilder();
		org.jsoup.nodes.Document doc = Jsoup.parse(headersHtml);
		Elements contexts = doc.getElementsByTag("a");
		if (contexts.isEmpty()) {
			return headersHtml;
		}
		boolean appendSeparator = false;
		for (org.jsoup.nodes.Element element : contexts) {
			if (appendSeparator) {
				processedContexts.append(CONTEXTS_SEPARATOR);
			}
			processedContexts.append(element.text());
			appendSeparator = true;
		}
		return processedContexts.toString();
	}
}
