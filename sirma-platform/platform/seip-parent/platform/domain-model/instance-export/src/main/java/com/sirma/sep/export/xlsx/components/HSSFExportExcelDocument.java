package com.sirma.sep.export.xlsx.components;

import java.io.Serializable;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Excel document. This class contains implementation for generation of excel document using HSSFWorkbook
 *
 * @author S.Djulgerova
 */
public class HSSFExportExcelDocument extends ExportExcelDocument {

	/**
	 * Initialize builder.
	 *
	 * @param headersInfo
	 *            - the information about columns headers.
	 */
	public HSSFExportExcelDocument(Map<String, String> headersInfo) {
		super(new HSSFWorkbook(), headersInfo);
		addCellBuilder(ExportExcelCell.Type.RICHTEXT, createRichtextCellBuilder());
	}

	/**
	 * Create richtext builder. <code>value</code> is a richtext with html tags and styles.
	 * 
	 * @return created richtext builder.
	 */
	private CellBuilder<Cell, Serializable> createRichtextCellBuilder() {
		return (cell, value) -> HtmlToExcelHelper.convert((HSSFWorkbook) workbook, (String) value, cell);
	}

	@Override
	public String getFileExtension() {
		return ".xls";
	}

}
