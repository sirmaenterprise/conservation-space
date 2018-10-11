package com.sirma.sep.export.xlsx.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;

/**
 * Excel document. This class contains implementation for generation of excel document.
 *
 * @author Boyan Tonchev.
 */
public abstract class ExportExcelDocument {

	private static final Logger LOGGER = Logger.getLogger(ExportExcelDocument.class);
	private static final String SHEET_NAME = "Data table widget";

	private CreationHelper createHelper;
	private CellStyle hyperLinkCellStyle;
	private CellStyle normalCellStyle;
	private CellStyle headingCellStyle;
	private Map<String, String> headersInfo;
	private Map<ExportExcelCell.Type, CellBuilder<Cell, Serializable>> cellBuilders;
	
	protected Workbook workbook;
	protected Sheet sheet;

	/**
	 * Initialize builder.
	 *
	 * @param workbook
	 * 		- the workbook used for export
	 * @param headersInfo
	 * 		- the information about columns headers.
	 */
	public ExportExcelDocument(Workbook workbook, Map<String, String> headersInfo) {
		this.workbook = workbook;
		sheet = workbook.createSheet(SHEET_NAME);
		this.headersInfo = headersInfo;
		createHelper = workbook.getCreationHelper();
		hyperLinkCellStyle = createHyperLinkCellStyle(workbook);
		normalCellStyle = createNormalCellStyle(workbook);
		headingCellStyle = createHeadingCellStyle(workbook);
		createHeaderRow();
		initCelBuilders();
	}

	/**
	 * Write the excel document to <code>file</code>.
	 *
	 * @param file
	 * 		- where excel document have to be written.
	 */
	public void writeToFile(File file) {
		autoResizeSheetsColumns(headersInfo.size() + 1, sheet);
		try (FileOutputStream out = new FileOutputStream(file)) {
			workbook.write(out);
		} catch (IOException e) {
			LOGGER.error("Error during remote xlsx creation!", e);
		}
	}

	/**
	 * Create row and populate it with values of <code>exportExcelRow</code>
	 *
	 * @param exportExcelRow
	 * 		- contains row values which have to be populated.
	 */
	public void populateRow(ExportExcelRow exportExcelRow) {
		Row row = sheet.createRow(sheet.getLastRowNum() + 1);
		int cellNumber = 0;
		for (String propertyName : headersInfo.keySet()) {
			Cell cell = row.createCell(cellNumber++);
			ExportExcelCell property = exportExcelRow.getProperty(propertyName);
			cellBuilders.get(property.getType()).addValueToCell(cell, property.getValue());
		}
	}
	
	/**
	 * Get file extension needed for correct export to excel. Can be .xlsx or .xls
	 * 
	 * @return file extension
	 */
	public abstract String getFileExtension();

	protected void addCellBuilder(ExportExcelCell.Type type, CellBuilder<Cell, Serializable> cellBuilder) {
		cellBuilders.put(type, cellBuilder);
	}
	
	private void initCelBuilders() {
		cellBuilders = new EnumMap(ExportExcelCell.Type.class);
		cellBuilders.put(ExportExcelCell.Type.LINK, createLinkCellBuilder());
		cellBuilders.put(ExportExcelCell.Type.OBJECTS, createObjectsCellBuilder());
		cellBuilders.put(ExportExcelCell.Type.OBJECT, createObjectCellBuilder());
	}

	/**
	 * Create header row and populate header row of excel.
	 */
	private void createHeaderRow() {
		Row row = sheet.createRow(0);
		int cellNumber = 0;
		for (String value: headersInfo.values()) {
			Cell cell = row.createCell(cellNumber++);
			cell.setCellStyle(headingCellStyle);
			cell.setCellValue(extractValidValue(value));
		}
	}

	/**
	 * Processes null objects.
	 *
	 * @param obj
	 * 		the object
	 * @return return string value of object or empty string
	 */
	private static String extractValidValue(Object obj) {
		return obj == null ? "" : obj.toString();
	}

	/**
	 * Auto resize column widths.
	 *
	 * @param columnCount
	 * 		the column count in the generated sheet
	 * @param sheet
	 * 		the sheet
	 */
	private static void autoResizeSheetsColumns(int columnCount, Sheet sheet) {
		for (int i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private static CellStyle createNormalCellStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setWrapText(true);
		return cellStyle;
	}

	private static CellStyle createHeadingCellStyle(Workbook workbook) {
		Font font = workbook.createFont();
		CellStyle cs = workbook.createCellStyle();
		font.setBold(true);
		cs.setFont(font);
		cs.setWrapText(true);
		return cs;
	}

	private static CellStyle createHyperLinkCellStyle(Workbook workbook) {
		CellStyle cs = workbook.createCellStyle();
		Font hyperLinkFont = workbook.createFont();
		hyperLinkFont.setUnderline(XSSFFont.U_SINGLE);
		hyperLinkFont.setColor(HSSFColor.BLUE.index);
		cs.setFont(hyperLinkFont);
		cs.setWrapText(true);
		return cs;
	}

	@FunctionalInterface
	public interface CellBuilder<Cell, Serializable> { // NOSONAR
		/**
		 * Add <code>value</code> to cell.
		 *
		 * @param cell
		 * 		- the cell where value have to be added.
		 * @param value
		 * 		- the value which have to be populated.
		 */
		void addValueToCell(Cell cell, Serializable value);
	}

	/**
	 * Create builder for creation of a link.
	 *
	 * @return the link builder.
	 */
	private CellBuilder<Cell, Serializable> createLinkCellBuilder() {
		return (cell, value) -> {
			ExportExcelLink hyperLinkData = (ExportExcelLink) value;
			Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
			link.setAddress(hyperLinkData.getAddress());
			cell.setHyperlink(link);
			cell.setCellStyle(hyperLinkCellStyle);
			cell.setCellValue(extractValidValue(hyperLinkData.getLabel()));
		};
	}

	/**
	 * Create objects builder. <code>value</code> is list with objects (toString() method will be used for extract
	 * cell value).
	 * @return created objects builder.
	 */
	private CellBuilder<Cell, Serializable> createObjectsCellBuilder() {
		return (cell, value) -> {
			List<?> values = (List<?>) value;
			String cellValue = values.stream()
					.map(Object::toString)
					.filter(StringUtils::isNotBlank)
					.collect(Collectors.joining("\n"));
			cell.setCellStyle(normalCellStyle);
			cell.setCellValue(extractValidValue(cellValue));
		};
	}

	/**
	 * Create object builder. <code>value</code> is a object (toString() method will be used for extract
	 * cell value).
	 * @return created object builder.
	 */
	private CellBuilder<Cell, Serializable> createObjectCellBuilder() {
		return (cell, value) -> {
			cell.setCellStyle(normalCellStyle);
			cell.setCellValue(extractValidValue(value));
		};
	}
}
