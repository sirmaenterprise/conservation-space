package com.sirma.itt.emf.audit.export;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.pm.domain.model.ProjectInstance;

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

	/** The temp folder. */
	@Inject
	@Config(name = EmfConfigurationProperties.TEMP_DIR)
	private String tempFolder;

	@Inject
	@Config(name = EmfConfigurationProperties.FULL_DATE_FORMAT, defaultValue = "dd MM yyyy HH:mm:ss")
	private String dateFormatFull;

	@Inject
	private SearchService searchService;

	private SimpleDateFormat dateFormatter = null;

	/**
	 * Initializes date formatter.
	 */
	@PostConstruct
	public void init() {
		dateFormatter = new SimpleDateFormat(dateFormatFull);
	}

	@Override
	public File exportAsCsv(List<AuditActivity> activities, String query, JSONArray columns)
			throws IOException, JSONException {
		TimeTracker tracker = TimeTracker.createAndStart();

		String filePath = tempFolder + "/audit_" + tracker.elapsedTime() + ".csv";
		File file = new File(filePath);

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filePath), Charset.forName("UTF-8")))) {
			writer.write('\ufeff');
			writer.write(query);
			writer.newLine();

			boolean appendComma = false;
			// Build header line
			for (int i = 0; i < columns.length(); i++) {
				JSONObject column = columns.getJSONObject(i);
				try {
					AuditActivity.class.getDeclaredField(column.getString("id"));
					writer.write(appendComma ? ";" : "");
					writer.write("\"" + column.getString("name") + "\"");
					appendComma = true;
				} catch (NoSuchFieldException | SecurityException e) {
					column.append("skip", true);
					LOGGER.error("There is no column \"" + column.getString("id")
							+ "\" in Audit Activity. Column will be skipped.", e);
				}
			}

			Map<String, Instance> contextMap = getContextMap();
			for (AuditActivity activity : activities) {
				appendComma = false;
				writer.newLine();
				for (int i = 0; i < columns.length(); i++) {
					JSONObject column = columns.getJSONObject(i);

					try {
						if (!column.has("skip")) {
							String value = getActivityFormattedValue(activity,
									column.getString("id"), contextMap);
							if (StringUtils.isNotEmpty(value)) {
								value = "\"" + value + "\"";
							}
							writer.write((appendComma ? ";" : "") + value);
							appendComma = true;
						}
					} catch (NoSuchMethodException | InvocationTargetException
							| IllegalAccessException e) {
						LOGGER.error("There is no column \"" + column.getString("id")
								+ "\" in Audit Activity.", e);
						return null;
					}
				}
			}
			writer.flush();
		}

		LOGGER.debug("CSV constructed in {} ms", tracker.stop());
		return file;
	}

	@Override
	public File exportAsPdf(List<AuditActivity> activities, String query, JSONArray columns)
			throws IOException, JSONException {
		TimeTracker tracker = TimeTracker.createAndStart();
		Document document = new Document();
		String filePath = tempFolder + "/audit_" + tracker.elapsedTime() + ".pdf";
		File file = new File(filePath);
		try {
			PdfWriter.getInstance(document, new FileOutputStream(file));
		} catch (DocumentException e) {
			throw new IOException("Error while creating the pdf file.", e);
		}

		int numberOfColumns = 0;
		for (int i = 0; i < columns.length(); i++) {
			JSONObject column = columns.getJSONObject(i);
			try {
				AuditActivity.class.getDeclaredField(column.getString("id"));
				numberOfColumns++;
			} catch (NoSuchFieldException | SecurityException e) {
				column.append("skip", true);
				LOGGER.error("There is no column \"" + column.getString("id")
						+ "\" in Audit Activity. Column will be skipped.", e);
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

		Map<String, Instance> contextMap = getContextMap();
		for (AuditActivity activity : activities) {
			addRow(table, activity, columns, contextMap);
		}

		try {
			document.add(table);
		} catch (DocumentException e) {
			throw new IOException(
					"The document hasnt been opened yet, or has been already closed.", e);
		}

		LOGGER.debug("PDF constructed in {} ms", tracker.stop());
		document.close();
		return file;
	}

	/**
	 * Performs additional formatting for an activity field
	 * 
	 * @param activity
	 *            the activity
	 * @param fieldId
	 *            the name of the field as it is declared in the {@link AuditActivity} class
	 * @param contextMap
	 *            a map with context values
	 * @return the activity field formatted value
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 */
	private String getActivityFormattedValue(AuditActivity activity, String fieldId,
			Map<String, Instance> contextMap) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		StringBuilder value = new StringBuilder();
		Object originalValue = PropertyUtils.getProperty(activity, fieldId);
		if (originalValue instanceof Date) {
			value = new StringBuilder(dateFormatter.format(originalValue));
		} else {
			originalValue = originalValue != null ? originalValue : "";
			if ("context".equals(fieldId)) {
				String[] originalValues = String.valueOf(originalValue).split(";");
				for (String string : originalValues) {
					String currentValue = "";
					Instance instance = contextMap.get(string);
					if (instance != null) {
						Map<String, Serializable> properties = instance.getProperties();
						String type = "";
						if (instance instanceof ProjectInstance) {
							type = "Project";
						} else if (instance instanceof CaseInstance) {
							type = "Case";
						}
						currentValue = properties.get(DefaultProperties.UNIQUE_IDENTIFIER) + " "
								+ properties.get(DefaultProperties.TITLE) + " (" + type + ")";
						value.append((value.length() == 0 ? "" : ", ") + currentValue);
					}
				}
			} else {
				value = new StringBuilder(originalValue.toString());
			}
		}
		return value.toString();
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
	 * @param contextMap
	 *            a map with context values
	 * @throws IOException
	 *             The font is invalid.
	 */
	private void addRow(PdfPTable table, AuditActivity activity, JSONArray columns,
			Map<String, Instance> contextMap) throws IOException, JSONException {
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
					String value = getActivityFormattedValue(activity, column.getString("id"),
							contextMap);
					table.addCell(new Phrase(value, font));
				}
			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				// Shouldn't happen. Columns are already checked
				LOGGER.error("There is no column \"" + column.getString("id")
						+ "\" in Audit Activity.", e);
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
	private void addHeaderCell(PdfPTable table, String text) {
		try {
			Font f = new Font(
					BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED));

			f.setColor(Color.white);
			f.setSize(4);
			PdfPCell cell = new PdfPCell(new Phrase(text, f));
			cell.setBackgroundColor(Color.DARK_GRAY);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
		} catch (DocumentException | IOException e) {
			LOGGER.error(
					"Could not add header in the document while exporting the audit log to pdf", e);
		}
	}

	/**
	 * Gets a map with all available projects and cases with their id as a key
	 * 
	 * @return the context map
	 */
	private Map<String, Instance> getContextMap() {
		Map<String, Instance> contextMap = new HashMap<>();

		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setQuery(Query.getEmpty());
		List<String> objectTypeValues = new ArrayList<>();
		objectTypeValues.add("emf:Case");
		objectTypeValues.add("emf:Project");
		searchArgs.getArguments().put("rdf:type", (Serializable) objectTypeValues);
		searchArgs.setPageNumber(1);
		searchArgs.setPageSize(10000);

		searchService.search(Instance.class, searchArgs);

		List<Instance> result = searchArgs.getResult();
		if (result != null) {
			for (Instance instance : result) {
				contextMap.put(String.valueOf(instance.getId()), instance);
			}
		}

		return contextMap;
	}

}
