package com.sirmaenterprise.sep.eai.spreadsheet.model;

import java.util.Locale;

import com.sirma.itt.seip.eai.service.model.EAIBaseConstants;

/**
 * Spreadsheet API specific constants as extension of {@link EAIBaseConstants}
 * 
 * @author bbanchev
 */
public class EAISpreadsheetConstants extends EAIBaseConstants {
	/** BG locale used for obtaining BG codelist descriptions. */
	public static final Locale LOCALE_BG = new Locale("bg");
	/** Import status data entry value. */
	public static final String IMPORT_STATUS = "ImportStatus";

	/** Xlsx mimetype. */
	public static final String XLSX_MIMETYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	/** Xls mimetype. */
	public static final String XLS_MIMETYPE = "application/vnd.ms-excel";

	/** CSV mimetype. */
	public static final String CSV_MIMETYPE = "text/csv";
	
	/** Text mimetype. */
	public static final String TEXT_MIMETYPE = "text/plain";

	/** The default sheet id. */
	public static final String DEFAULT_SHEET_ID = "0";
	
	/** Message for successful import */
	public static final String SUCCESSFULLY_IMPORTED = "Successfully imported";
	
	/** Message for failed import */
	public static final String NOT_IMPORTED = "Not imported";

	private EAISpreadsheetConstants() {
		// constants class
	}
}
