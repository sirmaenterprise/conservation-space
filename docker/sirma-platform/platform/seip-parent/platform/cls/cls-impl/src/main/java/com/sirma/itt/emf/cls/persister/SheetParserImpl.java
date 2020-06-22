package com.sirma.itt.emf.cls.persister;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.cls.columns.CLColumn;
import com.sirma.itt.emf.cls.util.JxlUtils;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.sep.cls.model.Code;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;
import com.sirma.sep.cls.parser.CodeListSheet;

import jxl.Cell;
import jxl.Sheet;
import jxl.format.BoldStyle;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Parses an excel sheet and creates {@link CodeList} and {@link CodeValue} objects from it's rows.
 *
 * @author Nikolay Velkov
 */
public class SheetParserImpl implements SheetParser {

	private static final String CODELISTS_SHEET_NAME = "codelists";
	private static final Colour HIGHLIGHT_COLOR = Colour.BRIGHT_GREEN;

	@Override
	public Sheet parseFromList(CodeListSheet sheet) {
		try {
			WritableWorkbook workbook = JxlUtils.createWorkbook();
			Sheet excelSheet = parseFromList(sheet, workbook);
			workbook.close();
			return excelSheet;
		} catch (WriteException | IOException e) {
			throw new EmfRuntimeException("Unable to parse code lists", e);
		}
	}

	@Override
	public Sheet parseFromList(CodeListSheet codeListSheet, WritableWorkbook workbook) {
		try {
			WritableSheet sheet = workbook.createSheet(CODELISTS_SHEET_NAME, 0);
			List<CodeList> list = codeListSheet.getCodeLists();

			sortCodesByValue(list);
			list.forEach(c -> sortCodesByValue(c.getValues()));

			exportCodeColumnsToSheet(sheet, list);
			exportCodeContentsToSheet(sheet, list);
			workbook.write();

			return sheet;
		} catch (IOException | WriteException e) {
			throw new EmfRuntimeException("Unable to parse code lists", e);
		}
	}

	@Override
	public CodeListSheet parseFromSheet(Sheet sheet) {
		int rows = sheet.getRows();
		CodeList codeList = null;
		List<CodeList> list = new LinkedList<>();
		for (int currentRow = 1; currentRow < rows; currentRow++) {
			if (isCellEmpty(sheet.getCell(findColumnByName(sheet, CLColumn.CL_VALUE), currentRow))) {
				continue;
			}
			boolean newCodelist = isCellBold(sheet.getCell(findColumnByName(sheet, CLColumn.CL_VALUE), currentRow));
			if (newCodelist) {
				codeList = convertRowToCodelist(sheet, currentRow);
				list.add(codeList);
			} else if (codeList != null) {
				CodeValue codeValue = convertRowToCodeValue(sheet, currentRow, codeList);
				codeList.getValues().add(codeValue);
			}
		}
		CodeListSheet sheetLocal = new CodeListSheet();
		sheetLocal.setCodeLists(list);
		return sheetLocal;
	}

	/**
	 * Generate a {@link CodeDescription} object from the description cells in the given row.
	 *
	 * @param sheet
	 *            the sheet from which to get the data
	 * @param row
	 *            the row on which the cell is located
	 * @return the constructed list of {@link CodeDescription} objects
	 */
	private static List<CodeDescription> generateCodeDescriptions(Sheet sheet, int row) {
		Map<String, CodeDescription> descriptions = new TreeMap<>();

		Cell[] headRowCells = sheet.getRow(0);
		for (int i = 0; i < headRowCells.length; i++) {
			String columnName = headRowCells[i].getContents();

			if (isDescriptionColumn(columnName)) {
				CodeDescription description = getCodeDescription(descriptions, columnName, CLColumn.DESCR.getName());
				description.setName(sheet.getCell(i, row).getContents());
			}

			if (isCommentColumn(columnName)) {
				CodeDescription description = getCodeDescription(descriptions, columnName, CLColumn.COMMENT.getName());
				description.setComment(sheet.getCell(i, row).getContents());
			}
		}

		return new ArrayList<>(descriptions.values());
	}

	private static CodeDescription getCodeDescription(Map<String, CodeDescription> descriptions, String columnName, String pattern) {
		String language = columnName.substring(pattern.length()).toUpperCase();

		CodeDescription description = descriptions.get(language);
		if (description == null) {
			description = new CodeDescription();
			description.setLanguage(language);
		}

		descriptions.put(language, description);

		return description;
	}

	/**
	 * Convert the given row in the sheet to a {@link CodeList} object.
	 *
	 * @param sheet
	 *            the sheet from which to get the data
	 * @param row
	 *            the row on which the cell is located
	 * @return the {@link CodeList} object
	 */
	private static CodeList convertRowToCodelist(Sheet sheet, int row) {
		CodeList codeList = new CodeList();
		populateCodeValues(codeList, sheet, row);

		codeList.setValues(new ArrayList<>());
		codeList.setDescriptions(generateCodeDescriptions(sheet, row));

		return codeList;
	}

	/**
	 * Convert the given row to a {@link CodeValue} object.
	 *
	 * @param sheet
	 *            the sheet from which to get the data
	 * @param row
	 *            the row on which the cell is located
	 * @param codeListParent
	 *            the code list parent
	 * @return the {@link CodeValue} object
	 */
	private static CodeValue convertRowToCodeValue(Sheet sheet, int row, CodeList codeListParent) {
		CodeValue codeValue = new CodeValue();
		populateCodeValues(codeValue, sheet, row);

		codeValue.setCodeListValue(codeListParent.getValue());
		codeValue.setActive(getActiveCellContents(sheet, row));
		codeValue.setDescriptions(generateCodeDescriptions(sheet, row));
		return codeValue;
	}

	/**
	 * Populate the {@link Code} object's common values.
	 *
	 * @param code
	 *            the {@link Code}
	 * @param sheet
	 *            the sheet from which to get the data
	 * @param row
	 *            the row
	 */
	private static void populateCodeValues(Code code, Sheet sheet, int row) {
		String value = getCellContents(sheet, CLColumn.CV_VALUE, row);
		String extra1 = getCellContents(sheet, CLColumn.EXTRA1, row);
		String extra2 = getCellContents(sheet, CLColumn.EXTRA2, row);
		String extra3 = getCellContents(sheet, CLColumn.EXTRA3, row);
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("Invalid data on row " + (row + 1));
		}

		code.setValue(value);
		code.setExtra1(extra1);
		code.setExtra2(extra2);
		code.setExtra3(extra3);
	}

	/**
	 * Exports all relevant code list columns to a given sheet
	 *
	 * @param sheet
	 *            the sheet the columns should be exported to
	 * @param codeLists
	 *            the collection of code lists used to resolve column which depend on present code lists or code values
	 */
	private static void exportCodeColumnsToSheet(WritableSheet sheet, Collection<CodeList> codeLists) {
		int col = 0;

		// create & add the basic code list columns
		// TODO: loop when columns are re-factored
		addColumn(sheet, col++, CLColumn.CL_VALUE);
		addColumn(sheet, col++, CLColumn.EXTRA1);
		addColumn(sheet, col++, CLColumn.EXTRA2);
		addColumn(sheet, col++, CLColumn.EXTRA3);
		addColumn(sheet, col++, CLColumn.ACTIVE);

		// construct the columns for descriptions & comments
		exportDescriptionColumnsToSheet(sheet, codeLists, col);
	}

	/**
	 * Export description columns to a given sheet. Descriptions are exported based on the current languages they
	 * contain. For each language a new column is exported for description & comment
	 *
	 * @param sheet
	 *            the sheet to which to export the columns
	 * @param codeLists
	 *            the collection of code lists used to resolve column names
	 * @param colStart
	 *            the column starting index from where to start exporting the columns
	 */
	private static void exportDescriptionColumnsToSheet(WritableSheet sheet, Collection<CodeList> codeLists, int colStart) {
		AtomicInteger column = new AtomicInteger(colStart);
		Set<String> languages = collectDescriptionLanguages(codeLists);

		BiFunction<String, CLColumn, String> builder = (d, c) -> c.getName() + d.toUpperCase();
		languages.forEach(d -> addColumn(sheet, column.getAndIncrement(), builder.apply(d, CLColumn.DESCR)));
		languages.forEach(d -> addColumn(sheet, column.getAndIncrement(), builder.apply(d, CLColumn.COMMENT)));
	}

	/**
	 * Exports the contents of a list of code lists along with the values they contain
	 *
	 * @param sheet
	 *            the sheet to which to export the code lists contents
	 * @param codeLists
	 *            the code lists to be exported
	 * @throws RowsExceededException
	 *             thrown when rows or columns indices are out of bound
	 * @throws WriteException
	 *             thrown when writing or exporting to the sheet has encountered a problem
	 */
	private static void exportCodeContentsToSheet(WritableSheet sheet, Collection<CodeList> codeLists) throws WriteException {
		int row = 1;
		for (CodeList codeList : codeLists) {
			exportCodeDetailsToSheet(sheet, codeList, row);
			exportCodeDescriptionsToSheet(sheet, codeList.getDescriptions(), row, true);

			// extract all code list values to the sheet
			List<CodeValue> codeValues = codeList.getValues();
			for (CodeValue codeValue : codeValues) {
				++row;
				exportCodeDetailsToSheet(sheet, codeValue, row);
				exportCodeDescriptionsToSheet(sheet, codeValue.getDescriptions(), row, false);
			}
			// group the code list values into a single collapsed group
			sheet.setRowGroup(row - codeValues.size() + 1, row, true);

			row += 2; // leave an empty row for better readability
		}
	}

	/**
	 * Exports the content of the code descriptions to the sheet.
	 *
	 * @param sheet
	 *            the sheet to which the code descriptions to be exported to
	 * @param descriptions
	 *            the code descriptions to be exported
	 * @param row
	 *            the current row for which to export the descriptions
	 */
	private static void exportCodeDescriptionsToSheet(WritableSheet sheet, List<CodeDescription> descriptions, int row,
			boolean isCodeList) {
		descriptions.forEach(description -> {
			String language = description.getLanguage();
			int descrIndex = findColumnByName(sheet, CLColumn.DESCR + language);
			int commentIndex = findColumnByName(sheet, CLColumn.COMMENT + language);

			// create and populate cells for the description of the specified code list
			addCell(sheet, descrIndex, row, description.getName(), isCodeList, isCodeList);
			addCell(sheet, commentIndex, row, description.getComment(), isCodeList, isCodeList);
		});
	}

	/**
	 * Exports common code details to a given sheet
	 *
	 * @param sheet
	 *            the sheet to which to export the contents of the code
	 * @param code
	 *            the code - could be either a {@link CodeList} or {@link CodeValue}
	 * @param row
	 *            the current row for which to export the descriptions
	 */
	private static void exportCodeDetailsToSheet(WritableSheet sheet, Code code, int row) {
		boolean isCodeList = code instanceof CodeList;
		// JXL is unable to color rows alone so we insert a dummy empty cell for code lists
		Serializable isActive = isCodeList ? null : String.valueOf(((CodeValue) code).isActive());

		addCell(sheet, findColumnByName(sheet, CLColumn.ACTIVE), row, isActive, isCodeList, isCodeList);
		addCell(sheet, findColumnByName(sheet, CLColumn.CL_VALUE), row, code.getValue(), isCodeList, isCodeList);
		addCell(sheet, findColumnByName(sheet, CLColumn.EXTRA1), row, code.getExtra1(), isCodeList, isCodeList);
		addCell(sheet, findColumnByName(sheet, CLColumn.EXTRA2), row, code.getExtra2(), isCodeList, isCodeList);
		addCell(sheet, findColumnByName(sheet, CLColumn.EXTRA3), row, code.getExtra3(), isCodeList, isCodeList);
	}

	/**
	 * Collects set of unique code languages contained inside the code descriptions
	 *
	 * @param codeLists
	 *            the code lists for which to collect languages
	 * @return the unique set of languages contained inside the code descriptions
	 */
	private static Set<String> collectDescriptionLanguages(Collection<CodeList> codeLists) {
		// preserve the order languages when inserting
		Set<String> languages = new LinkedHashSet<>();

		codeLists.forEach(list -> {
			languages.addAll(getDescriptionLanguages(list.getDescriptions()));
			list.getValues().forEach(value -> languages.addAll(getDescriptionLanguages(value.getDescriptions())));
		});
		return languages;
	}

	/**
	 * Sorts a collection of codes by their actual code value. The sort is performed by first trying to sort the values
	 * are integers if any integer values are present they will be sorted first, any alphanumeric values would be sorted
	 * lexicographically
	 *
	 * @param codes
	 *            the codes to be sorted
	 */
	private static <T extends Code> void sortCodesByValue(List<T> codes) {
		codes.sort((r, l) -> {
			String left = l.getValue();
			String right = r.getValue();

			try {
				return Integer.parseInt(right) - Integer.parseInt(left);
			} catch (NumberFormatException e) {
				return right.toUpperCase().compareTo(left.toUpperCase());
			}
		});
	}

	/**
	 * Extracts the language for each provided description
	 *
	 * @param descriptions
	 *            the descriptions for which to extract the languages
	 * @return list of languages mapped one to one with provided descriptions
	 */
	private static List<String> getDescriptionLanguages(List<CodeDescription> descriptions) {
		return descriptions.stream().map(d -> d.getLanguage().toUpperCase()).collect(Collectors.toList());
	}

	/**
	 * Replaces spaces and apostrophes.
	 *
	 * @param data
	 *            input string
	 * @return updated string
	 */
	private static String escapeData(String data) {
		if (StringUtils.isNotBlank(data)) {
			return data.replaceAll("\\'", "\\''").replaceAll("[\r\n]+", " ").replaceAll("(\\s*,\\s*)", ", ").replaceAll("\uFFFD",
					Character.toString((char) 176));
		}
		return data;
	}

	/**
	 * Extracts a cell contents from a sheet for a given row and column. In case a cell is non existent the contents of
	 * the cell is empty
	 *
	 * @param sheet
	 *            the sheet from which to extract the cell
	 * @param column
	 *            the column the cell belongs to
	 * @param row
	 *            the row index the cell is located at
	 * @return the extracted cell contents
	 * @throws IllegalArgumentException
	 *             when mandatory column content is not present
	 */
	private static String getCellContents(Sheet sheet, CLColumn column, int row) {
		int col = findColumnByName(sheet, column);

		if (col > -1) {
			Cell cell = sheet.getCell(col, row);
			return escapeData(cell.getContents().trim());
		} else if (column.isMandatory()) {
			throw new IllegalArgumentException(String.format("Invalid data on row: %d column: %s", row + 1, column));
		}
		return "";
	}

	/**
	 * Gets the value of a cell from the active column for a given row
	 * 
	 * @param sheet
	 *            the sheet from which to get the cell value
	 * @param row
	 *            the row at which to sample the sheet
	 * @return the active value from the sheet. Null when no value is present in the cell
	 */
	private static Boolean getActiveCellContents(Sheet sheet, int row) {
		String valueState = getCellContents(sheet, CLColumn.ACTIVE, row);
		return (valueState == null || valueState.isEmpty() || !valueState.matches(CLColumn.ACTIVE.getPattern())) ? null
				: Boolean.valueOf(valueState);
	}

	/**
	 * Finds the index of the {@link CLColumn} inside a sheet
	 *
	 * @param sheet
	 *            the sheet for which to find the given column
	 * @param column
	 *            the column to find
	 * @return the index of the column or negative if no such column was found
	 */
	private static int findColumnByName(Sheet sheet, CLColumn column) {
		return findColumnByName(sheet, column.getName());
	}

	/**
	 * Finds the index of a column with a given name inside a sheet
	 *
	 * @param sheet
	 *            the sheet for which to find the given column
	 * @param column
	 *            the column to find
	 * @return the index of the column or negative if no such column was found
	 */
	private static int findColumnByName(Sheet sheet, String column) {
		int length = sheet.getRow(0).length;
		Cell[] headerRow = sheet.getRow(0);
		return IntStream.range(0, length).filter(i -> column.equalsIgnoreCase(headerRow[i].getContents())).findFirst().orElse(-1);
	}

	/**
	 * Creates a column to a given sheet at specified column & row, with specific content
	 *
	 * @param sheet
	 *            the sheet for which to create the column
	 * @param column
	 *            the column index
	 * @param content
	 *            the content or title of the column
	 */
	private static void addColumn(WritableSheet sheet, int column, Serializable content) {
		sheet.insertColumn(column);
		sheet.getColumnView(column).setAutosize(true);
		addCell(sheet, column, 0, content, true, false);
	}

	/**
	 * Creates a cell for a given sheet at specific row and column location with a given content. The cell can be
	 * customized by specifying properties for the contained text or background color of the cell
	 *
	 * @param sheet
	 *            the sheet for which to create the cell
	 * @param col
	 *            the column index
	 * @param row
	 *            the row index
	 * @param content
	 *            the content of the cell
	 * @param bold
	 *            determines if the cell text should be bold
	 * @param highlight
	 *            determines if the cell background color should be highlighted
	 */
	private static void addCell(WritableSheet sheet, int col, int row, Serializable content, boolean bold, boolean highlight) {
		// skip invalid indices
		if (row < 0 || col < 0) {
			return;
		}

		try {
			WritableFont font = new WritableFont(WritableFont.ARIAL);
			if (bold) {
				font.setBoldStyle(WritableFont.BOLD);
			}
			WritableCellFormat cellFormat = new WritableCellFormat(font);
			if (highlight) {
				cellFormat.setBackground(HIGHLIGHT_COLOR);
			}
			String actualContent = content != null ? content.toString() : "";
			sheet.addCell(new Label(col, row, actualContent, cellFormat));
		} catch (WriteException e) {
			// we should not reach up to this point at all unless something really unexpected happens
			throw new EmfApplicationException("Unable to create spread sheet cell ", e);
		}
	}

	/**
	 * Checks if a given column is a {@link CLColumn#DESCR} column
	 *
	 * @param column
	 *            the column name to be checked
	 * @return true if it is a description column false otherwise
	 */
	private static boolean isDescriptionColumn(String column) {
		return startsWith(column, CLColumn.DESCR);
	}

	/**
	 * Checks if a given column is a {@link CLColumn#COMMENT} column
	 *
	 * @param column
	 *            the column name to be checked
	 * @return true if it is a description column false otherwise
	 */
	private static boolean isCommentColumn(String column) {
		return startsWith(column, CLColumn.COMMENT);
	}

	/**
	 * Checks if a given column is matching a given {@link CLColumn}
	 *
	 * @param column
	 *            column name to match
	 * @param toMatch
	 *            the {@link CLColumn} name pattern
	 * @return true if matches or false otherwise
	 */
	private static boolean startsWith(String column, CLColumn toMatch) {
		return column.startsWith(toMatch.getName());
	}

	/**
	 * Checks if the given cell has any content.
	 *
	 * @param cell
	 *            the cell to check
	 * @return true if the cell is empty or false if not
	 */
	private static boolean isCellEmpty(Cell cell) {
		return cell == null || cell.getContents().trim().isEmpty();
	}

	/**
	 * Checks if a content of a cell is in bold.
	 *
	 * @param cell
	 *            the provided cell
	 * @return true if the content of the cell is bold or false if not
	 */
	private static boolean isCellBold(Cell cell) {
		CellFormat cellFormat = cell.getCellFormat();
		return cellFormat != null && cellFormat.getFont().getBoldWeight() == BoldStyle.BOLD.getValue();
	}
}
