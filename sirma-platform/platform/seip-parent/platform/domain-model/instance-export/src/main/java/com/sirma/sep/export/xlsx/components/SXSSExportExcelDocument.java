package com.sirma.sep.export.xlsx.components;

import java.io.File;
import java.util.Map;

import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Excel document. This class contains implementation for generation of excel document using SXSSFWorkbook
 *
 * @author S.Djulgerova
 */
public class SXSSExportExcelDocument extends ExportExcelDocument {

	/**
	 * Initialize builder.
	 *
	 * @param headersInfo
	 *            - the information about columns headers.
	 * @param rowsInMemory
	 *            - how many rows to be hold in memory. For example: if <code>rowsInMemory</code> is 10 this means when
	 *            we add row 10 all rows will be flushed to hard disk and RAM will be released.
	 */
	public SXSSExportExcelDocument(Map<String, String> headersInfo, int rowsInMemory) {
		super(new SXSSFWorkbook(rowsInMemory), headersInfo);
		((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
	}

	@Override
	public void writeToFile(File file) {
		((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
		super.writeToFile(file);
	}

	@Override
	public String getFileExtension() {
		return ".xlsx";
	}

}
