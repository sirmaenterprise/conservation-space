package com.sirma.itt.emf.cls.persister;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Cell;
import jxl.Sheet;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cls.columns.CLColumn;
import com.sirma.itt.emf.cls.entity.Code;
import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeListDescription;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.entity.CodeValueDescription;
import com.sirma.itt.emf.cls.entity.Description;
import com.sirma.itt.emf.cls.entity.Tenant;
import com.sirma.itt.emf.cls.validator.CellValidator;

/**
 * Parses an excel sheet and creates {@link CodeList} and {@link CodeValue} objects from it's rows.
 * 
 * @author Nikolay Velkov
 */
public class SheetParser {

	/** Used to avoid repetition when persisting to the database. */
	private Map<String, Tenant> uniqueTenants = new HashMap<String, Tenant>();

	/**
	 * Parses a sheet and converts it's rows into {@link CodeList} and {@link CodeValue} objects.
	 * 
	 * @param sheet
	 *            the sheet to be parsed
	 * @return the parsed {@link CodeList} and {@link CodeValue} objects
	 */
	public List<CodeList> parseXLS(Sheet sheet) {
		int rows = sheet.getRows();
		CodeList codeList = null;
		List<CodeList> list = new LinkedList<CodeList>();
		for (int currentRow = 1; currentRow < rows; currentRow++) {
			if (CellValidator.isCellEmpty(sheet.getCell(CLColumn.VALUE.getColumn(), currentRow))) {
				continue;
			}
			boolean newCodelist = CellValidator.isCellBold(sheet.getCell(
					CLColumn.VALUE.getColumn(), currentRow));
			if (newCodelist) {
				codeList = convertRowToCodelist(sheet, currentRow);
				list.add(codeList);
			} else {
				CodeValue codeValue = convertRowToCodeValue(sheet, currentRow, codeList);
				codeList.getCodeValues().add(codeValue);
			}
		}
		return list;
	}

	/**
	 * Creates a {@link Tenant} object from the tenant cell in the given row.
	 * 
	 * @param sheet
	 *            the sheet from which to get the data
	 * @param row
	 *            the row on which the cell is located
	 * @return the generated {@link Tenant}
	 */
	private Tenant generateTenant(Sheet sheet, int row) {
		String tenantID = cellToString(sheet.getCell(CLColumn.TENANT_ID.getColumn(), row));
		Tenant tenant;
		tenant = new Tenant();
		tenant.setName(tenantID);
		tenant.setContact("mail@mail.mail");

		return tenant;
	}

	/**
	 * Generate a {@link CodeValueDescription} object from the description cells in the given row.
	 * 
	 * @param sheet
	 *            the sheet from which to get the data
	 * @param row
	 *            the row on which the cell is located
	 * @return the constructed list of {@link CodeValueDescription} objects
	 */
	private List<CodeValueDescription> generateCodeValueDescriptions(Sheet sheet, int row) {
		List<CodeValueDescription> descriptions = new ArrayList<CodeValueDescription>();
		CodeValueDescription descrBG = new CodeValueDescription();
		CodeValueDescription descrEN = new CodeValueDescription();
		populateDescriptions(descrBG, descrEN, sheet, row);
		descriptions.add(descrBG);
		descriptions.add(descrEN);
		return descriptions;
	}

	/**
	 * generate a {@link CodeListDescription} object from the description cells in the given row.
	 * 
	 * @param sheet
	 *            the sheet from which to get the data
	 * @param row
	 *            the row on which the cell is located
	 * @return the constructed list of {@link CodeListDescription} objects
	 */
	private List<CodeListDescription> generateCodeListDescriptions(Sheet sheet, int row) {
		List<CodeListDescription> descriptions = new ArrayList<CodeListDescription>();
		CodeListDescription descrBG = new CodeListDescription();
		CodeListDescription descrEN = new CodeListDescription();
		populateDescriptions(descrBG, descrEN, sheet, row);
		descriptions.add(descrBG);
		descriptions.add(descrEN);
		return descriptions;
	}

	/**
	 * Generate bulgarian and english descriptions from the given sheet and row.
	 * 
	 * @param descriptionBG
	 *            the bulgarian description
	 * @param descriptionEN
	 *            the english description
	 * @param sheet
	 *            the sheet from which to get the data
	 * @param row
	 *            the row on which the cell is located
	 */
	private void populateDescriptions(Description descriptionBG, Description descriptionEN,
			Sheet sheet, int row) {
		String commentBG = cellToString(sheet.getCell(CLColumn.COMMENTBG.getColumn(), row));
		String commentEN = cellToString(sheet.getCell(CLColumn.COMMENTEN.getColumn(), row));
		String descrBg = cellToString(sheet.getCell(CLColumn.DESCRBG.getColumn(), row));
		String descrEn = cellToString(sheet.getCell(CLColumn.DESCREN.getColumn(), row));

		descriptionBG.setDescription(descrBg);
		descriptionBG.setComment(commentBG);
		descriptionBG.setLanguage("BG");

		descriptionEN.setDescription(descrEn);
		descriptionEN.setComment(commentEN);
		descriptionEN.setLanguage("EN");
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
	private CodeList convertRowToCodelist(Sheet sheet, int row) {
		CodeList codeList = new CodeList();
		populateCodeValues(codeList, sheet, row);
		String displayType = cellToString(sheet.getCell(CLColumn.DISPLAY_TYPE.getColumn(), row));
		String sortBy = cellToString(sheet.getCell(CLColumn.SORT_BY.getColumn(), row));
		Tenant tenant = generateTenant(sheet, row);
		Set<Tenant> tenants = new HashSet<Tenant>();
		if (!uniqueTenants.containsKey(tenant.getName())) {
			uniqueTenants.put(tenant.getName(), tenant);
			tenants.add(tenant);
		} else {
			tenants.add(uniqueTenants.get(tenant.getName()));

		}
		codeList.setCodeValues(new ArrayList<CodeValue>());
		codeList.setDescriptions(generateCodeListDescriptions(sheet, row));
		codeList.setTenants(tenants);
		codeList.setDisplayType(displayType);
		codeList.setSortBy(sortBy);
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
	private CodeValue convertRowToCodeValue(Sheet sheet, int row, CodeList codeListParent) {
		CodeValue codeValue = new CodeValue();
		populateCodeValues(codeValue, sheet, row);
		String orderString = sheet.getCell(CLColumn.ORDER.getColumn(), row).getContents();
		int order = 0;
		if (orderString.length() > 0) {
			order = Integer.parseInt(orderString);
		}
		codeValue.setDescriptions(generateCodeValueDescriptions(sheet, row));
		codeValue.setCodeListId(codeListParent.getValue());
		codeValue.setOrder(order);
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
	private void populateCodeValues(Code code, Sheet sheet, int row) {
		String value = cellToString(sheet.getCell(CLColumn.VALUE.getColumn(), row));
		String masterValue = cellToString(sheet.getCell(CLColumn.MASTERCL.getColumn(), row));
		String extra1 = cellToString(sheet.getCell(CLColumn.EXTRA1.getColumn(), row));
		String extra2 = cellToString(sheet.getCell(CLColumn.EXTRA2.getColumn(), row));
		String extra3 = cellToString(sheet.getCell(CLColumn.EXTRA3.getColumn(), row));
		String extra4 = cellToString(sheet.getCell(CLColumn.EXTRA4.getColumn(), row));
		String extra5 = cellToString(sheet.getCell(CLColumn.EXTRA5.getColumn(), row));
		SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy");
		String validFrom = sheet.getCell(CLColumn.VALID_FROM.getColumn(), row).getContents();
		String validTo = sheet.getCell(CLColumn.VALID_TO.getColumn(), row).getContents();
		Date validFromDate = null;
		Date validToDate = null;
		try {
			validFromDate = dateFormat.parse(validFrom);

		} catch (ParseException e) {
			// Empty dates are just skipped and inserted as null in the
			// database.
		}

		try {
			validToDate = dateFormat.parse(validTo);
		} catch (ParseException e) {
			// Empty dates are just skipped and inserted as null in the
			// database.
		}
		if (value.isEmpty()) {
			throw new IllegalArgumentException("Invalid data on row " + (row + 1));
		}

		code.setValue(value);
		code.setMasterValue(masterValue);
		code.setExtra1(extra1);
		code.setExtra2(extra2);
		code.setExtra3(extra3);
		code.setExtra4(extra4);
		code.setExtra5(extra5);
		code.setValidFrom(validFromDate);
		code.setValidTo(validToDate);
		code.setCreatedOn(new Date());
	}

	/**
	 * Replaces spaces and apostrophes.
	 * 
	 * @param data
	 *            input string
	 * @return updated string
	 */
	private String escapeData(String data) {
		if (StringUtils.isNotNullOrEmpty(data)) {
			return data.replaceAll("\\'", "\\''").replaceAll("[\r\n]+", " ")
					.replaceAll("(\\s*,\\s*)", ", ")
					.replaceAll("\uFFFD", Character.toString((char) 176));
		}
		return null;
	}

	/**
	 * Convert the given {@link Cell} to a string after trimming and replacing spaces and
	 * apostrophes from it's content.
	 * 
	 * @param cell
	 *            the cell to be converted
	 * @return the converted string
	 */
	private String cellToString(Cell cell) {
		return escapeData(cell.getContents().trim());
	}

}
