package com.sirma.itt.seip.eai.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty.EntityPropertyMapping;
import com.sirma.itt.seip.eai.model.mapping.EntityRelation;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchFormCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchOrderCriterion;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * The XlsxModelParser parse series of mapping models for EAI. The parser relays on the streams provided which are
 * expected to be in correct format.
 */
public class XlsxModelParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private XlsxModelParser() {
		// util class
	}

	/**
	 * Provides reference ids for needed columns during xls parse.
	 *
	 * @author bbanchev
	 */
	public enum ModelPropertyIds {
		/** id of data mapping to external id. */
		MAPPING_DATA_CONVERT,
		/** URI in SEIP. */
		URI,
		/** property name in SEIP. */
		PPROPERTY_ID,
		/** Title of property. */
		TITLE,
		/** Codelist of property. */
		CODELIST_ID,
		/** DataType of property. */
		DATA_TYPE,
		/** Is mandatory property in SEIP. */
		MANDATORY_SEIP,
		/** If yes property is used in light request - otherwise it is ignored from processing. */
		THIN_REQUEST_USAGE;
	}

	/**
	 * Parses the model xlsx - entity type or common.xlsx. All rows in the xlsx that fulfills the
	 * {@link #isDataRow(XSSFWorkbook, Row)} check are returned
	 *
	 * @param input
	 *            the stream to read from
	 * @param identifier
	 *            is id the of resource to parse
	 * @param columnMapping
	 *            the column mapping - see {@link ModelPropertyIds} for keys, value is the column number
	 * @return the list of entity properties in this xlsx
	 */
	public static List<EntityProperty> parseModelXlsx(InputStream input, String identifier,
			Map<String, Integer> columnMapping) {
		List<EntityProperty> result = null;
		// Create Workbook instance holding reference to .xlsx file
		try (XSSFWorkbook workbook = new XSSFWorkbook(new BufferedInputStream(input))) {
			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);
			result = new ArrayList<>(sheet.getLastRowNum() - sheet.getFirstRowNum());
			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (isDataRow(workbook, row)) {
					importProperty(row, result, columnMapping, identifier, workbook);
				}
			}
		} catch (Exception e) {
			throw new EmfRuntimeException("Failed to parse " + identifier + " using config " + columnMapping, e);
		}
		return result;

	}

	/**
	 * Parses the search model xlsx - entity search properties in search.xlsx. All rows in the xlsx that are imported=Y
	 * and has uri are collected
	 *
	 * @param input
	 *            the stream to read from
	 * @param identifier
	 *            is id the of resource to parse
	 * @param columnMapping
	 *            the column mapping - see {@link ModelPropertyIds} for keys, value is the column number
	 * @return the list of entity properties in this xlsx
	 */
	public static List<EntitySearchCriterion> parseSearchModelXlsx(InputStream input, String identifier) {
		List<EntitySearchCriterion> result = null;
		// Create Workbook instance holding reference to .xlsx file
		try (XSSFWorkbook workbook = new XSSFWorkbook(new BufferedInputStream(input))) {
			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);
			result = new ArrayList<>(sheet.getLastRowNum() - sheet.getFirstRowNum());
			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (isDataRow(workbook, row)) {
					importCriteria(row, result);
				}
			}
		} catch (EmfRuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new EmfRuntimeException("Failed to parse " + identifier, e);
		}
		return result;

	}

	private static void importCriteria(Row row, List<EntitySearchCriterion> result) {
		EntitySearchCriterion criteria;
		String propertyId = readCellAsString(row, 0);

		Integer orderByPosition = readCellAsInteger(row, 3);
		if (orderByPosition != null) {
			criteria = new EntitySearchOrderCriterion();
			criteria.setPropertyId(propertyId);
			((EntitySearchOrderCriterion) criteria).setOrderPosition(orderByPosition);
			result.add(criteria);
		}
		String mapping = readCellAsString(row, 1);
		if (mapping != null) {
			criteria = new EntitySearchFormCriterion();
			criteria.setPropertyId(propertyId);
			((EntitySearchFormCriterion) criteria).setMapping(mapping);
			((EntitySearchFormCriterion) criteria).setOperator(readCellAsString(row, 2));
			((EntitySearchFormCriterion) criteria).setVisible(readCellAsBoolean(row, 4));
			result.add(criteria);
		}
	}

	private static void importProperty(Row row, List<EntityProperty> result, Map<String, Integer> columnMapping,
			String identifier, XSSFWorkbook workbook) {
		String uri = readCellAsString(row, columnMapping.get(ModelPropertyIds.URI.toString()).intValue());
		if (StringUtils.isBlank(uri)) {
			LOGGER.warn("Row {} in {} has value but URI is missing. Row is ignored!", Integer.valueOf(row.getRowNum()),
					identifier);
			return;
		}
		Boolean isImported = readCellAsBoolean(row, 1);
		// check if system row - italic
		if (!isImported.booleanValue() && !isItalicRow(workbook, row)) {
			LOGGER.warn("Row {} in {} is ignored in model since it is marked as not imported!",
					Integer.valueOf(row.getRowNum()), identifier);
			return;
		}

		EntityProperty entry = new EntityProperty();
		setPropertyTitle(entry, row, columnMapping);
		setPropertyId(entry, row, columnMapping);
		setPropertyConvertName(entry, row, columnMapping);
		setPropertyCodelist(entry, row, columnMapping);
		setPropertyMandatory(entry, row, columnMapping);
		setPropertyType(entry, row, columnMapping);
		entry.setUri(uri);
		result.add(entry);
	}

	/**
	 * Parses the types xlsx - the list of types.
	 *
	 * @param input
	 *            the stream to read from
	 * @param identifier
	 *            is id the of resource to parse
	 * @return the list of entity types
	 */
	public static List<EntityType> parseTypesXlsx(InputStream input, String identifier) {
		List<EntityType> result = null;
		try (XSSFWorkbook workbook = new XSSFWorkbook(new BufferedInputStream(input))) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			result = new ArrayList<>(sheet.getLastRowNum() - sheet.getFirstRowNum());
			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (isDataRow(workbook, row)) {
					importType(row, result, identifier);
				}
			}
		} catch (Exception e) {
			throw new EmfRuntimeException("Failed to parse " + identifier, e);
		}
		return result;
	}

	private static void importType(Row row, List<EntityType> result, String identifier) {
		String definitionId = readCellAsString(row, 0);
		Boolean isImported = readCellAsBoolean(row, 2);
		if (!isImported.booleanValue()) {
			LOGGER.warn("Row {} in {} is ignored in model since it is marked as not imported!",
					Integer.valueOf(row.getRowNum()), identifier);
			return;
		}
		if (definitionId == null) {
			LOGGER.warn("Row {} in {} has value but identifier is missing. Row is ignored!",
					Integer.valueOf(row.getRowNum()), identifier);
			return;
		}
		EntityType entity = new EntityType();
		entity.setIdentifier(definitionId);
		entity.setMapping(readCellAsString(row, 4));
		entity.setTitle(readCellAsString(row, 1));
		entity.setUri(readCellAsString(row, 3));
		result.add(entity);
	}

	/**
	 * Parses the relations xlsx - the list of relations.
	 *
	 * @param input
	 *            the stream to read from
	 * @param identifier
	 *            is id the of resource to parse
	 * @return the list of entity types
	 */
	public static List<EntityRelation> parseRelationsXlsx(InputStream input, String identifier) {
		List<EntityRelation> result = null;
		try (XSSFWorkbook workbook = new XSSFWorkbook(new BufferedInputStream(input))) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			result = new ArrayList<>(sheet.getLastRowNum() - sheet.getFirstRowNum());
			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (isDataRow(workbook, row)) {
					importRelation(row, result, identifier);
				}
			}
		} catch (Exception e) {
			throw new EmfRuntimeException("Failed to parse " + identifier, e);
		}
		return result;
	}

	private static void importRelation(Row row, List<EntityRelation> result, String identifier) {
		String uri = readCellAsString(row, 7);
		if (uri == null) {
			LOGGER.warn("Row {} in {} has value but URI is missing. Row is ignored!", Integer.valueOf(row.getRowNum()),
					identifier);
			return;
		}
		Boolean isImported = readCellAsBoolean(row, 1);
		if (!isImported.booleanValue()) {
			LOGGER.warn("Row {} in {} is ignored!", Integer.valueOf(row.getRowNum()), identifier);
			return;
		}
		EntityRelation entity = new EntityRelation();
		entity.setTitle(readCellAsString(row, 0));
		String relationsMappings = readCellAsString(row, 8);
		if (relationsMappings != null) {
			entity.addMappings(relationsMappings.split("\\s+"));
		}
		entity.setUri(uri);
		entity.setDomain(readCellAsString(row, 3));
		entity.setRange(readCellAsString(row, 4));
		result.add(entity);
	}

	/**
	 * Checks if row is not bold or strikeout or italic or if there is any data on the provided row. If so, the row is
	 * considered as data row
	 */
	private static boolean isDataRow(XSSFWorkbook workbook, Row row) {
		if (isBoldOrStrikeout(workbook, row)) {
			return false;
		}
		if (isItalicRow(workbook, row)) {
			return true;
		}
		if (row.getRowNum() < 1 || row.getFirstCellNum() == -1
				|| readCellAsString(row, row.getFirstCellNum()) == null) {
			return false;
		}
		return true;

	}

	private static boolean isItalicRow(XSSFWorkbook workbook, Row row) {
		if (row.isFormatted()) {
			XSSFFont rowFont = workbook.getFontAt(row.getRowStyle().getFontIndex());
			return rowFont.getItalic();
		}
		return false;
	}

	private static boolean isBoldOrStrikeout(XSSFWorkbook workbook, Row row) {
		if (row.isFormatted()) {
			XSSFFont rowFont = workbook.getFontAt(row.getRowStyle().getFontIndex());
			return rowFont.getStrikeout() || rowFont.getBold();
		}
		return false;
	}

	private static void setPropertyType(EntityProperty entry, Row row, Map<String, Integer> columnMapping) {
		String type = readCellAsString(row, columnMapping.get(ModelPropertyIds.DATA_TYPE.toString()).intValue());
		entry.setType(type);
	}

	private static void setPropertyMandatory(EntityProperty entry, Row row, Map<String, Integer> columnMapping) {
		String cellValue = readCellAsString(row,
				columnMapping.get(ModelPropertyIds.MANDATORY_SEIP.toString()).intValue());
		entry.setMandatory("M".equals(cellValue));
	}

	private static void setPropertyCodelist(EntityProperty entry, Row row, Map<String, Integer> columnMapping) {
		Integer codelist = readCellAsInteger(row,
				columnMapping.get(ModelPropertyIds.CODELIST_ID.toString()).intValue());
		entry.setCodelist(codelist);
	}

	private static void setPropertyConvertName(EntityProperty entry, Row row, Map<String, Integer> columnMapping) {
		String name = readCellAsString(row,
				columnMapping.get(ModelPropertyIds.MAPPING_DATA_CONVERT.toString()).intValue());
		if (name != null) {
			entry.addMapping(EntityPropertyMapping.AS_DATA, name);
		}
	}

	private static void setPropertyTitle(EntityProperty entry, Row row, Map<String, Integer> columnMapping) {
		String name = readCellAsString(row, columnMapping.get(ModelPropertyIds.TITLE.toString()).intValue());
		entry.setTitle(name);
	}

	private static void setPropertyId(EntityProperty entry, Row row, Map<String, Integer> columnMapping) {
		String name = readCellAsString(row, columnMapping.get(ModelPropertyIds.PPROPERTY_ID.toString()).intValue());
		entry.setPropertyId(name);
	}

	private static Boolean readCellAsBoolean(Row row, int cellNumber) {
		Cell cell = row.getCell(cellNumber);
		if (cell == null) {
			return Boolean.FALSE;
		}
		String booleanAsString = cell.getStringCellValue().trim();
		if (StringUtils.isBlank(booleanAsString)) {
			return Boolean.FALSE;
		}
		booleanAsString = booleanAsString.toLowerCase();
		// check the different representations of true
		return Boolean.valueOf(
				"y".equals(booleanAsString) || "(tick)".equals(booleanAsString) || "true".equals(booleanAsString));
	}

	private static String readCellAsString(Row row, int cellNumber) {
		Cell cell = row.getCell(cellNumber);
		if (cell == null) {
			return null;
		}
		String value = cell.getStringCellValue().replaceAll("(^\\h*)|(\\h*$)", "").trim();
		if (value.isEmpty()) {
			return null;
		}
		return value;
	}

	@SuppressWarnings("deprecation")
	private static Integer readCellAsInteger(Row row, int cellNumber) {
		Cell cell = row.getCell(cellNumber);
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return Integer.valueOf((int) cell.getNumericCellValue());
		} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			String value = readCellAsString(row, cellNumber);
			if (value == null || value.isEmpty()) {
				return null;
			}
			return Integer.valueOf(value);
		}
		return null;
	}
}