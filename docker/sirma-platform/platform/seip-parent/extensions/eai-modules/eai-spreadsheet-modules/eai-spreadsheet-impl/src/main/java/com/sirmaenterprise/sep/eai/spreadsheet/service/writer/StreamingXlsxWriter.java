package com.sirmaenterprise.sep.eai.spreadsheet.service.writer;

import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.IMPORT_STATUS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.ExtendedColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.service.util.SpreadsheetUtil;

import com.monitorjbl.xlsx.StreamingReader;

/**
 * Xlsx file copier using streaming reader and writer to the the file reading and generation
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/01/2018
 */
public class StreamingXlsxWriter {

	private Locale sheetLocate = Locale.ENGLISH;
	// The 'T" charachter from the ISO8601 date format should be escaped otherwise the generated excel could not be
	// parsed back with the API api as the format will be invalid and the date value will be parsed as number and fail
	private String defaultDateFormat = "YYYY-MM-DD\\Thh:mm:ss.sssZ";
	private boolean autoSizeColumns = true;
	// Default memory optimized workbook SXSSFWorkbook implementation
	// optional fast memory intensive XSSFWorkbook implementation
	private Supplier<Workbook> outputWorkbookFactory = SXSSFWorkbook::new;

	/**
	 * Copy the input xlsx file to the given output file and updating the output with the specified data.
	 *
	 * @param inputFile
	 * 		the input xlsx file
	 * @param outputFile
	 * 		the output file to write to
	 * @param spreadsheetEntries
	 * 		the data to pass or empty collection just to copy the file
	 * @throws IOException
	 * 		if cannot read or write to files
	 */
	public void write(File inputFile, File outputFile, List<SpreadsheetEntry> spreadsheetEntries) throws IOException {

		// convert data to sheet -> row -> columns mapping
		Map<String, Map<String, SpreadsheetEntry>> sheetMapping = new LinkedHashMap<>();
		// convert data to sheet -> all possible column names collection
		Map<String, Set<String>> sheetToColumnNames = new LinkedHashMap<>();
		for (SpreadsheetEntry spreadsheetEntry : spreadsheetEntries) {
			sheetMapping.computeIfAbsent(spreadsheetEntry.getSheet(), entry -> new LinkedHashMap<>())
					.put(spreadsheetEntry.getExternalId(), spreadsheetEntry);
			sheetToColumnNames.computeIfAbsent(spreadsheetEntry.getSheet(), entry -> new LinkedHashSet<>())
					.addAll(spreadsheetEntry.getProperties().keySet());
		}

		try (Workbook workbook = createStreamingReader(inputFile);
				Workbook output = outputWorkbookFactory.get();
				FileOutputStream resultStream = new FileOutputStream(outputFile)) {

			// we need to set to all sheets ImportStatus column, where the success of the import will be displayed
			// set as property of each entry
			int number = workbook.getNumberOfSheets();
			for (int sheet = 1; sheet <= number; sheet++) {
				sheetToColumnNames.computeIfAbsent(String.valueOf(sheet), entry -> new LinkedHashSet<>())
						.add(IMPORT_STATUS);
			}

			SheetProcessor sheetProcessor = new SheetProcessor(sheetMapping, sheetToColumnNames, workbook, output);
			sheetProcessor.setDefaultDateFormat(getDefaultDateFormat());
			sheetProcessor.setSheetLocate(getSheetLocate());
			sheetProcessor.setAutoSizeColumns(isAutoSizeColumns());

			for (Sheet sheet : workbook) {
				sheetProcessor.processSheet(sheet);
			}

			output.write(resultStream);
		}
	}

	private static Workbook createStreamingReader(File inputFile) {
		return StreamingReader.builder().rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
				.readComments()       // without this no comments will be read
				.open(inputFile);
	}

	public String getDefaultDateFormat() {
		return defaultDateFormat;
	}

	public StreamingXlsxWriter setDefaultDateFormat(String defaultDateFormat) {
		this.defaultDateFormat = defaultDateFormat;
		return this;
	}

	public Locale getSheetLocate() {
		return sheetLocate;
	}

	public StreamingXlsxWriter setSheetLocate(Locale sheetLocate) {
		this.sheetLocate = sheetLocate;
		return this;
	}

	public boolean isAutoSizeColumns() {
		return autoSizeColumns;
	}

	public StreamingXlsxWriter setAutoSizeColumns(boolean autoSizeColumns) {
		this.autoSizeColumns = autoSizeColumns;
		return this;
	}

	public Supplier<Workbook> getOutputWorkbookFactory() {
		return outputWorkbookFactory;
	}

	public StreamingXlsxWriter setOutputWorkbookFactory(Supplier<Workbook> outputWorkbookFactory) {
		this.outputWorkbookFactory = outputWorkbookFactory;
		return this;
	}

	private static class SheetProcessor {
		private final Map<String, Map<String, SpreadsheetEntry>> sheetMapping;
		private final Map<String, Set<String>> sheetToColumnNames;
		private final Map<Short, CellStyle> styles = new HashMap<>(256);
		private final Map<String, ExtendedColor> colors = new HashMap<>(128);
		private final Workbook inputWorkbook;
		private final Workbook outputWorkbook;

		private CellStyle styleWithItalic = null;
		private Locale sheetLocate;
		private String defaultDateFormat;
		private boolean autoSizeColumns;

		// temporary state during sheet processing
		private Map<String, Integer> headerToIndex;
		private Map<Integer, String> propertyToColumnIndex;

		SheetProcessor(Map<String, Map<String, SpreadsheetEntry>> sheetMapping,
				Map<String, Set<String>> sheetToColumnNames, Workbook workbook, Workbook output) {
			this.sheetMapping = sheetMapping;
			this.sheetToColumnNames = sheetToColumnNames;
			inputWorkbook = workbook;
			outputWorkbook = output;
		}

		void processSheet(Sheet sheet) {
			Sheet newSheet = createSheet(outputWorkbook, sheet);
			String sheetId = SpreadsheetUtil.getSheetId(inputWorkbook.getSheetIndex(sheet));
			Map<String, SpreadsheetEntry> rowsData = sheetMapping.getOrDefault(sheetId, Collections.emptyMap());
			Set<String> knownColumns = sheetToColumnNames.getOrDefault(sheetId, Collections.emptySet());

			int rowIndex = 0;
			Row headerRow = null;
			boolean isLastRowEmpty = false;
			headerToIndex = null;
			propertyToColumnIndex = null;

			for (Row inputRow : sheet) {
				if (propertyToColumnIndex == null) {
					propertyToColumnIndex = readerHeaderRow(inputRow);
					headerToIndex = propertyToColumnIndex.entrySet()
							.stream()
							.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
					setDefaultColumnWidth(newSheet, propertyToColumnIndex);
				}
				if (SpreadsheetUtil.isEmptyRow(inputRow)) {
					if (isLastRowEmpty) {
						// 2 rows in one after another are empty stop processing
						return;
					} else {
						isLastRowEmpty = true;
					}
					continue;
				} else {
					isLastRowEmpty = false;
				}
				SpreadsheetEntry rowData = rowsData.get(SpreadsheetUtil.getRowId(inputRow));

				Row newRow = createRow(newSheet, rowIndex++, inputRow);
				// this is done to register new columns in the header row when it's shorter than the rest of the rows
				// in order to not break the logic later that depends on the column count
				headerRow = fillMissingHeaderCells(headerRow, inputRow, newRow);
				copyRowData(newSheet, new HashSet<>(knownColumns), headerRow, inputRow, rowData, newRow);
			}
		}

		private void copyRowData(Sheet newSheet, Set<String> knownColumns, Row headerRow, Row inputRow,
				SpreadsheetEntry rowData, Row newRow) {
			boolean isHeaderRow = newRow == headerRow;

			for (Cell cellToCopy : inputRow) {
				if (isHeaderRow) {
					knownColumns.remove(cellToCopy.getStringCellValue());
				}
				Cell newCell = copyOrAddNewCell(newSheet, propertyToColumnIndex, rowData, newRow, knownColumns,
												cellToCopy);
				if (!isHeaderRow && autoSizeColumns) {
					// the cell auto sizing to resize the column if needed so that it will accommodate
					// the current column data
					newSheet.autoSizeColumn(newCell.getColumnIndex());
				}
			}

			// the remaining entries in processedColumns are one that are new for the file
			// they are added at the end of the row and if there is no header value for them it will be added
			// this will work only if no new data comes after the window of rows for the inout and output workbooks
			// as both keep only certain amount of rows in memory that can be accessed and modified after that
			// the rows become inaccessible, alternatively header rows can be added in advance but all data
			// should be scanned before that
			addNewDataToTheEndOfTheRow(newSheet, newRow, rowData, headerRow, knownColumns, isHeaderRow);
		}

		private Row createRow(Sheet newSheet, int rowIndex, Row inputRow) {
			Row newRow = newSheet.createRow(rowIndex);
			if (inputRow.getHeight() > 0) {
				newRow.setHeight(inputRow.getHeight());
			}
			newRow.setRowStyle(copyStyle(inputRow.getRowStyle()));
			return newRow;
		}

		private void addNewDataToTheEndOfTheRow(Sheet newSheet, Row newRow, SpreadsheetEntry rowData, Row headerRow,
				Set<String> processedColumns, boolean isHeaderRow) {
			for (String property : processedColumns) {
				Cell cell = null;
				Integer columnIndex = getColumnIndex(newSheet, headerRow, property);
				if (!isHeaderRow && rowData != null) {
					Object value = rowData.getProperties().get(property);
					if (value != null) {
						cell = addNewCell(newRow, value, columnIndex);
						// when the value of the cell is null and if the column is import status we add successfully imported text
						// italic font style is allied in order not to process the cell on next import
					} else if (property.equalsIgnoreCase(IMPORT_STATUS)) {
						cell = addNewCell(newRow, EAISpreadsheetConstants.SUCCESSFULLY_IMPORTED, columnIndex);
						applyItalicFontFormatting(cell);
					}
					// when no row data, but import status property exists, this indicates unsuccessful import
					// italic font style is applied in order not to process the cell on the next import (EAI import logic for bold/italic styles)
				} else if (!isHeaderRow && property.equalsIgnoreCase(IMPORT_STATUS)) {
					cell = addNewCell(newRow, EAISpreadsheetConstants.NOT_IMPORTED, columnIndex);
					applyItalicFontFormatting(cell);
				}

				if (cell != null && autoSizeColumns) {
					newSheet.autoSizeColumn(cell.getColumnIndex());
				}
			}
		}

		private void applyItalicFontFormatting(Cell cell) {
			// reuse the italic style
			if (styleWithItalic == null) {
				Font font = outputWorkbook.createFont();
				font.setItalic(true);
				styleWithItalic = outputWorkbook.createCellStyle();
				styleWithItalic.setFont(font);
			}
			cell.setCellStyle(styleWithItalic);
		}

		private Integer getColumnIndex(Sheet newSheet, Row headerRow, String property) {
			return headerToIndex.computeIfAbsent(property, headerName -> {
				Cell cell = addNewCell(headerRow, headerName, null);
				newSheet.setColumnWidth(cell.getColumnIndex(), headerName.length());
				return cell.getColumnIndex();
			});
		}

		private Cell copyOrAddNewCell(Sheet newSheet, Map<Integer, String> propertyToColumnIndex,
				SpreadsheetEntry rowData, Row newRow, Set<String> processedColumns, Cell c) {
			Cell newCell;
			if (rowData == null) {
				newCell = copyCellData(newSheet, newRow, c);
			} else {
				String propertyName = propertyToColumnIndex.get(c.getColumnIndex());

				// when the property is not processed, but still may have value in the original cell
				if (propertyName == null || !rowData.getProperties().containsKey(propertyName)) {
					newCell = copyCellData(newSheet, newRow, c);
				} else {
					Object value = rowData.getProperties().get(propertyName);
					processedColumns.remove(propertyName);
					newCell = setCellData(newSheet, newRow, c, value);
				}
			}
			return newCell;
		}

		private Row fillMissingHeaderCells(Row headerRow, Row sourceRow, Row newRow) {
			if (headerRow == null) {
				return newRow;
			} else if (headerRow.getLastCellNum() < sourceRow.getLastCellNum()) {
				// this should increase the cells count of the header row if the next rows has more columns than the header
				// first -1 is because the row.getLastCellNum() returns +1 for the max cells
				// second -1 is because we want 1 cell less so that we can set proper value later
				for (int colNum = headerRow.getLastCellNum(); colNum < sourceRow.getLastCellNum() - 1 - 1; colNum++) {
					headerRow.createCell(colNum);
				}
			}
			return headerRow;
		}

		private void setDefaultColumnWidth(Sheet newSheet, Map<Integer, String> propertyToColumnIndex) {
			CellStyle cellStyle = newSheet.getWorkbook().createCellStyle();
			cellStyle.setWrapText(false);
			cellStyle.setShrinkToFit(true);
			cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
			cellStyle.setAlignment(HorizontalAlignment.LEFT);
			propertyToColumnIndex.forEach(
					(columnIndex, value) -> newSheet.setDefaultColumnStyle(columnIndex, cellStyle));
			propertyToColumnIndex.forEach((columnIndex, value) -> newSheet.setColumnWidth(columnIndex, value.length()));
		}

		private Sheet createSheet(Workbook output, Sheet sheet) {
			Sheet newSheet = output.createSheet(sheet.getSheetName());
			newSheet.setAutobreaks(false);
			// if not set the rows become big to accommodate the big cells by forcibly wrapping them
			newSheet.setDefaultRowHeightInPoints((short) 15);
			// the number of visible characters in a column. The default value of 8 is too small
			// 15 looks good and accommodates most of the data without column expanding
			newSheet.setDefaultColumnWidth(15);
			if (isAutoSizeColumns() && newSheet instanceof SXSSFSheet) {
				((SXSSFSheet) newSheet).trackAllColumnsForAutoSizing();
			}
			return newSheet;
		}

		private Cell copyCellData(Sheet newSheet, Row newRow, Cell c) {
			Cell cell = newRow.createCell(c.getColumnIndex());
			cell.setCellStyle(copyStyle(c.getCellStyle()));
			cell.setCellComment(cloneComment(newSheet, c));
			cell.setCellType(c.getCellTypeEnum());
			if (c.getCellTypeEnum() == CellType.FORMULA) {
				cell.setCellFormula(c.getCellFormula());
			}
			copyCellValue(cell, c);
			return cell;
		}

		private void copyCellValue(Cell cell, Cell cellValue) {
			CellType cellType = cellValue.getCellTypeEnum();
			if (cellType == CellType.STRING) {
				cell.setCellValue(cellValue.getRichStringCellValue());
			} else if (cellType == CellType.BOOLEAN) {
				cell.setCellValue(cellValue.getBooleanCellValue());
			} else if (cellType == CellType.NUMERIC) {
				cell.setCellValue(cellValue.getNumericCellValue());
			}
		}

		private Comment cloneComment(Sheet newSheet, Cell cell) {
			if (cell.getCellComment() == null) {
				return null;
			}
			Drawing drawing = newSheet.createDrawingPatriarch();
			ClientAnchor anchor = outputWorkbook.getCreationHelper().createClientAnchor();
			anchor.setCol1(cell.getColumnIndex());
			anchor.setCol2(cell.getColumnIndex() + 1);
			anchor.setRow1(cell.getRowIndex());
			anchor.setRow2(cell.getRowIndex() + 3);

			Comment comment = drawing.createCellComment(anchor);
			comment.setVisible(Boolean.FALSE);
			comment.setString(cell.getCellComment().getString());

			return comment;
		}

		private CellStyle copyStyle(CellStyle cellStyle) {
			if (cellStyle == null) {
				return null;
			}
			final int defaultCellStyle = 0;
			if (cellStyle.getIndex() == defaultCellStyle) {
				// no need to create new style instances when the cell has no custom style
				// the original cell didn't have any specific style
				// so in the output workbook we should use the default style as well
				CellStyle defaultStyle = outputWorkbook.getCellStyleAt(defaultCellStyle);
				if (defaultStyle == null) {
					defaultStyle = outputWorkbook.createCellStyle();
				}
				return defaultStyle;
			}
			// the excel file has a limit on the number of fonts supported in a single file
			// this is why we try to reuse the styles
			return styles.computeIfAbsent(cellStyle.getIndex(), id -> {
				CellStyle outputCellStyle = outputWorkbook.createCellStyle();
				outputCellStyle.setFont(createFont(cellStyle));
				outputCellStyle.setHidden(cellStyle.getHidden());
				outputCellStyle.setWrapText(cellStyle.getWrapText());
				outputCellStyle.setShrinkToFit(cellStyle.getShrinkToFit());

				String dataFormatString = cellStyle.getDataFormatString();
				short dataFormat = outputWorkbook.getCreationHelper().createDataFormat().getFormat(dataFormatString);
				outputCellStyle.setDataFormat(dataFormat);
				outputCellStyle.setRotation(cellStyle.getRotation());
				outputCellStyle.setLocked(cellStyle.getLocked());
				outputCellStyle.setIndention(cellStyle.getIndention());
				outputCellStyle.setQuotePrefixed(cellStyle.getQuotePrefixed());

				outputCellStyle.setBorderBottom(cellStyle.getBorderBottomEnum());
				outputCellStyle.setBorderLeft(cellStyle.getBorderLeftEnum());
				outputCellStyle.setBorderTop(cellStyle.getBorderTopEnum());
				outputCellStyle.setBorderRight(cellStyle.getBorderRightEnum());

				outputCellStyle.setTopBorderColor(cellStyle.getTopBorderColor());
				outputCellStyle.setBottomBorderColor(cellStyle.getBottomBorderColor());
				outputCellStyle.setLeftBorderColor(cellStyle.getLeftBorderColor());
				outputCellStyle.setRightBorderColor(cellStyle.getRightBorderColor());

				// foreground color is mainly used instead of background color
				ExtendedColor fillForegroundColorColor = (ExtendedColor) cellStyle.getFillForegroundColorColor();
				if (fillForegroundColorColor != null) {
					if (fillForegroundColorColor.isIndexed()) {
						outputCellStyle.setFillForegroundColor(fillForegroundColorColor.getIndex());
					} else if (outputCellStyle instanceof XSSFCellStyle) {
						((XSSFCellStyle)outputCellStyle).setFillForegroundColor(
								(XSSFColor) getOrCreateColor(fillForegroundColorColor));
					}
					// else if not XSSFCellStyle we do not have any other way to set the color
					// leave it without color
				}

				outputCellStyle.setFillPattern(cellStyle.getFillPatternEnum());

				outputCellStyle.setAlignment(cellStyle.getAlignmentEnum());
				outputCellStyle.setVerticalAlignment(cellStyle.getVerticalAlignmentEnum());
				return outputCellStyle;
			});
		}

		private ExtendedColor getOrCreateColor(ExtendedColor sourceColor) {
			return colors.computeIfAbsent(
					sourceColor.getARGBHex(), hex -> {
						ExtendedColor color = outputWorkbook.getCreationHelper().createExtendedColor();
						color.setRGB(sourceColor.getRGB());
						color.setTint(sourceColor.getTint());
						return color;
					});
		}

		private Font createFont(CellStyle cellStyle) {
			Font sourceFont = inputWorkbook.getFontAt(cellStyle.getFontIndex());

			// try to find the font in the workbook if not found create new one
			// this should be done as the excel format has limit on the number of fonts supported in a single file
			Font font = outputWorkbook.findFont(sourceFont.getBold(), sourceFont.getColor(),
					sourceFont.getFontHeight(), sourceFont.getFontName(), sourceFont.getItalic(),
					sourceFont.getStrikeout(), sourceFont.getTypeOffset(), sourceFont.getUnderline());
			if (font != null && font.getCharSet() == sourceFont.getCharSet()) {
				return font;
			}
			font = outputWorkbook.createFont();
			font.setBold(sourceFont.getBold());
			font.setColor(sourceFont.getColor());
			font.setUnderline(sourceFont.getUnderline());
			font.setCharSet(sourceFont.getCharSet());
			font.setFontHeight(sourceFont.getFontHeight());
			font.setFontHeightInPoints(sourceFont.getFontHeightInPoints());
			font.setItalic(sourceFont.getItalic());
			font.setStrikeout(sourceFont.getStrikeout());
			font.setTypeOffset(sourceFont.getTypeOffset());
			font.setFontName(sourceFont.getFontName());
			return font;
		}

		private Cell setCellData(Sheet newSheet, Row newRow, Cell c, Object value) {
			Cell cell = newRow.createCell(c.getColumnIndex());
			cell.setCellStyle(copyStyle(c.getCellStyle()));
			cell.setCellComment(cloneComment(newSheet, c));
			if (c.getCellTypeEnum() == CellType.FORMULA) {
				cell.setCellFormula(c.getCellFormula());
			}
			fillCellValue(cell, value, c);
			return cell;
		}

		private Cell addNewCell(Row row, Object value, Integer columnIndex) {
			int newCellColumn = columnIndex == null ? row.getLastCellNum() : columnIndex;
			Cell cell = row.createCell(newCellColumn);
			fillCellValue(cell, value, null);
			return cell;
		}

		private Map<Integer, String> readerHeaderRow(Row row) {
			Map<Integer, String> map = new HashMap<>(row.getLastCellNum());
			for (Cell cell : row) {
				if (StringUtils.isNotBlank(cell.getStringCellValue())) {
					map.put(cell.getColumnIndex(), cell.getStringCellValue());
				}
			}
			return map;
		}

		private void fillCellValue(Cell cell, Object cellValue, Cell currentCell) {
			if (cellValue instanceof String) {
				cell.setCellValue((String) cellValue);
			} else if (cellValue instanceof Boolean) {
				cell.setCellValue((Boolean) cellValue);
			} else if (cellValue instanceof Number) {
				// there is only double format in excel so the type does not mather
				cell.setCellValue(((Number) cellValue).doubleValue());
			} else if (cellValue instanceof Date) {
				// for new values use the specified default date format and locale
				// otherwise if we copy a date value from the original file use the original format
				String dataFormat = DateFormatConverter.convert(getSheetLocate(), getDefaultDateFormat());
				if (currentCell != null && currentCell.getCellStyle() != null && DateUtil.isCellDateFormatted(
						currentCell)) {
					dataFormat = currentCell.getCellStyle().getDataFormatString();
				}
				// if no unique style is set to the cell we cannot modify the format
				// the cell needs its own style in order to set the format
				CellStyle cellStyle = getOrCreateUniqueCellStyle(cell);
				short dateFormat = outputWorkbook.getCreationHelper().createDataFormat().getFormat(dataFormat);
				cellStyle.setDataFormat(dateFormat);
				cell.setCellValue((Date) cellValue);
			} else if (cellValue instanceof Collection) {
				String value = ((Collection<Object>) cellValue).stream()
						.map(Object::toString)
						.collect(Collectors.joining("\n"));
				cell.setCellValue(outputWorkbook.getCreationHelper().createRichTextString(value));
			}
		}

		private CellStyle getOrCreateUniqueCellStyle(Cell cell) {
			CellStyle cellStyle = cell.getCellStyle();
			if (cellStyle.getIndex() == 0) {
				cellStyle = outputWorkbook.createCellStyle();
				cell.setCellStyle(cellStyle);
			}
			return cellStyle;
		}

		private String getDefaultDateFormat() {
			return defaultDateFormat;
		}

		void setDefaultDateFormat(String defaultDateFormat) {
			this.defaultDateFormat = defaultDateFormat;
		}

		private Locale getSheetLocate() {
			return sheetLocate;
		}

		void setSheetLocate(Locale sheetLocate) {
			this.sheetLocate = sheetLocate;
		}

		private boolean isAutoSizeColumns() {
			return autoSizeColumns;
		}

		private void setAutoSizeColumns(boolean autoSizeColumns) {
			this.autoSizeColumns = autoSizeColumns;
		}
	}
}
