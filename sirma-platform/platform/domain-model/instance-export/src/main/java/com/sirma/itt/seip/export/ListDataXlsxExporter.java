package com.sirma.itt.seip.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.time.FormattedDate;
import com.sirma.itt.seip.time.FormattedDateTime;

/**
 * Utility class used for exporting list data to excel format.
 *
 * @author gshefkedov
 */
@ApplicationScoped
public class ListDataXlsxExporter implements ListDataExporter {

	private static final Logger LOGGER = Logger.getLogger(ListDataXlsxExporter.class);
	private static final String HEADINGS = "1headings";
	private static final String SHEET_NAME = "Datatable widget";
	private static final String EXPORT = "export-xlsx";
	private static final String ENTITY_LABEL = "Entity";

	@Inject
	private InstanceTypeResolver instanceResolver;

	@Inject
	private CodelistService codelistService;

	@Inject
	private InstanceLoadDecorator instanceDecorator;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private TypeConverter typeConverter;

	@Override
	public File export(ExportListDataXlsx request) {
		String headerType = request.getHeaderType();
		List<String> selectedInstances = request.getSelectedInstances();
		Set<String> selectedProperties = request.getSelectedProperties();
		XSSFWorkbook workbook = new XSSFWorkbook();
		String filename = request.getFileName();
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME);
		XSSFFont hlinkfont = workbook.createFont();
		CreationHelper createHelper = workbook.getCreationHelper();
		Map<String, Collection<Object>> data = processData(headerType, selectedInstances, selectedProperties);
		Set<String> keyset = data.keySet();
		int rownum = 0;
		for (String key : keyset) {
			rownum = processRow(workbook, sheet, hlinkfont, createHelper, data, rownum, key);
		}
		// + 1 because selectedProperties does not contain the first DTW column - "Entity"
		autoResizeSheetsColumns(selectedProperties.size() + 1, sheet);
		return exportXlsx(workbook, filename);
	}

	/**
	 * Auto resize column widths.
	 *
	 * @param columnCount
	 *            the column count in the generated sheet
	 * @param sheet
	 *            the sheet
	 */
	private static void autoResizeSheetsColumns(int columnCount, XSSFSheet sheet) {
		for (int i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	/**
	 * Processes one row of xlsx sheet.
	 *
	 * @param workbook
	 *            the workbook
	 * @param sheet
	 *            the sheet
	 * @param hlinkfont
	 *            the hyperlink font
	 * @param createHelper
	 *            the creation helper
	 * @param data
	 *            the data which will be saved in a xlsx workbook
	 * @param rownum
	 *            the current row num
	 * @param key
	 *            current key of data
	 * @return return row number
	 */
	private static int processRow(XSSFWorkbook workbook, XSSFSheet sheet, XSSFFont hlinkfont,
			CreationHelper createHelper, Map<String, Collection<Object>> data, int rownum, String key) {
		int rowNumber = rownum;
		Row row = sheet.createRow(rowNumber++);
		Collection<Object> objArr = data.get(key);
		int cellnum = 0;
		for (Object obj : objArr) {
			String value = extractValidValue(obj);
			Cell cell = row.createCell(cellnum++);
			CellStyle cs = workbook.createCellStyle();
			if (key == HEADINGS) {
				Font font = workbook.createFont();
				font.setBoldweight(Font.BOLDWEIGHT_BOLD);
				cs.setFont(font);
			}
			// hyperlinks for entity header
			if (obj instanceof Map) {
				setCellStyleForHyperlinks(hlinkfont, cs);
				XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(Hyperlink.LINK_URL);
				link.setAddress(((Map) obj).get("address").toString());
				cell.setHyperlink(link);
				value = ((Map) obj).get("linkLabel").toString();
			}

			if (obj instanceof List<?>) {
				List<?> values = (List<?>) obj;
				if (values.get(0) instanceof Map) {
					value = processHyperlinks(hlinkfont, createHelper, obj, cell, cs);
				} else {
					value = processMultiValueStrings(values);
				}
			}
			cs.setWrapText(true);
			cell.setCellStyle(cs);
			cell.setCellValue(String.valueOf(value));
		}
		return rowNumber;
	}

	/**
	 * Proceses links in excel cell. If the cell is multivalued then only labels are exported.
	 *
	 * @param hlinkfont
	 *            the excel sheet font. It is used for styling fonts.
	 * @param createHelper
	 *            the creation helper class for creating workbook objects.
	 * @param obj
	 *            the current object for processing
	 * @param cell
	 *            the current cell
	 * @param cs
	 *            the style sheet for styling cells.
	 * @return return strings representation of hyperlinks
	 */
	private static String processHyperlinks(XSSFFont hlinkfont, CreationHelper createHelper, Object obj, Cell cell,
			CellStyle cs) {
		List<Map<String, String>> array = (List<Map<String, String>>) obj;
		StringBuilder multivalue = new StringBuilder();
		if (array.size() == 1) {
			for (Map<String, String> currentHyperLink : array) {
				setCellStyleForHyperlinks(hlinkfont, cs);
				XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(Hyperlink.LINK_URL);
				link.setAddress(currentHyperLink.get("address"));
				cell.setHyperlink(link);
				multivalue.append(currentHyperLink.get("linkLabel")).append(System.lineSeparator());
			}
		} else {
			for (Map<String, String> currentHyperLink : array) {
				multivalue.append(currentHyperLink.get("linkLabel")).append(System.lineSeparator());
			}
		}
		return multivalue.toString();
	}

	private static String processMultiValueStrings(List<?> values) {
		StringBuilder multivalue = new StringBuilder(values.size());
		for (Object value : values) {
			// using new lines in cells - apache poi http://poi.apache.org/spreadsheet/quick-guide.html
			multivalue.append(value).append("\n");
		}
		return multivalue.toString();
	}

	/**
	 * Set cell style for hyperlinks.
	 *
	 * @param hlinkfont
	 *            the hyperlink font family
	 * @param cs
	 *            the cell style
	 */
	private static void setCellStyleForHyperlinks(XSSFFont hlinkfont, CellStyle cs) {
		hlinkfont.setUnderline(XSSFFont.U_SINGLE);
		hlinkfont.setColor(HSSFColor.BLUE.index);
		cs.setFont(hlinkfont);
	}

	/**
	 * Generates xlsx file by given workbook and filename.
	 *
	 * @param workbook
	 *            the workbook
	 * @param filename
	 *            the filename
	 * @return xlsx file
	 */
	private File exportXlsx(XSSFWorkbook workbook, String filename) {
		// Write the workbook in file system
		File exportDir = tempFileProvider.createLongLifeTempDir(EXPORT);
		File exportedFile = new File(exportDir, filename);
		try (FileOutputStream out = new FileOutputStream(exportedFile)) {
			workbook.write(out);
		} catch (IOException e) {
			LOGGER.error("Error during remote xlsx creation!", e);
		}
		return exportedFile;
	}

	private Map<String, Collection<Object>> processData(String headerType, List<String> selectedInstances,
			Set<String> selectedProperties) {
		Collection<Instance> instances = loadInstanceProperties(selectedInstances);
		if (!instances.isEmpty()) {
			return prepareData(instances, selectedProperties, headerType);
		}
		return Collections.emptyMap();
	}

	/**
	 * Prepares data which will be exported to xlsx.
	 *
	 * @param instances
	 *            list of instances
	 * @param props
	 *            the properties
	 * @param headerType
	 *            the header type
	 * @return data for generating xlsx
	 */
	private Map<String, Collection<Object>> prepareData(Collection<Instance> instances, Set<String> props,
			String headerType) {
		Set<Object> propsSet = new LinkedHashSet<>();
		Set<String> headings = getPropertyLabels(instances, props);
		String validHeaderType = setProperHeaderType(headerType, propsSet);
		propsSet.addAll(headings);
		Map<String, Collection<Object>> data = new LinkedHashMap<>(instances.size());
		data.put(HEADINGS, propsSet);
		for (Instance instance : instances) {
			Collection<Object> propertyValues = new ArrayList<>(props.size());
			if (propsSet.contains(ENTITY_LABEL)) {
				Map<String, String> header = sanitizeHeader(validHeaderType, instance);
				propertyValues.add(header);
			}
			for (Serializable property : props) {
				processProperty(instance, propertyValues, property, validHeaderType);
			}
			data.put(instance.getId().toString(), propertyValues);
		}

		return data;
	}

	/**
	 * Sets proper header type. E.g. if DTW's header type is "Do not display link of the objects" then sets default
	 * value compact_header as header type.
	 *
	 * @param passedHeaderType
	 *            the passed header type. Might be none, if not, then adds Entity column;
	 * @param propsSet
	 *            the properties set
	 * @return the proper header type
	 */
	private static String setProperHeaderType(String passedHeaderType, Set<Object> propsSet) {
		String headerType = passedHeaderType;
		if (!DefaultProperties.DEFAULT_HEADERS.contains(passedHeaderType)) {
			headerType = DefaultProperties.HEADER_COMPACT;
		} else {
			propsSet.add(ENTITY_LABEL);
		}
		return headerType;
	}

	private void processProperty(Instance instance, Collection<Object> propertyValues, Serializable property,
			String headerType) {
		PropertyDefinition definitionProperty = dictionaryService.getProperty(property.toString(), instance);
		Serializable propertyValue = "";
		if (definitionProperty != null) {
			propertyValue = getPropertyValues(instance, definitionProperty);
		}
		propertyValues.add(resolveHyperlinks(propertyValue, headerType));
	}

	private Object resolveHyperlinks(Serializable propertyValue, String headerType) {
		Collection<String> ids;
		if (propertyValue instanceof Collection<?>) {
			ids = new ArrayList<>((Collection<String>) propertyValue);
		} else if (propertyValue == null || propertyValue.equals(StringUtils.EMPTY)) {
			ids = Collections.emptyList();
		} else {
			ids = Collections.singletonList(String.valueOf(propertyValue));
		}
		Collection<Instance> instancesInCell = instanceResolver.resolveInstances(ids);
		return extractLinks(propertyValue, instancesInCell, headerType);
	}

	private Object extractLinks(Serializable propertyValue, Collection<Instance> instancesInCell, String headerType) {
		if (!instancesInCell.isEmpty()) {
			Collection<Map<String, String>> hyperlinkList = new ArrayList<>(instancesInCell.size());
			instanceDecorator.decorateResult(instancesInCell);
			for (Instance propertyInstance : instancesInCell) {
				Map<String, String> objProperty = sanitizeHeader(headerType, propertyInstance);
				hyperlinkList.add(objProperty);
			}
			return hyperlinkList;
		}
		return propertyValue;
	}

	private Serializable getPropertyValues(Instance instance, PropertyDefinition definitionProperty) {
		Serializable propertyValue = instance.get(definitionProperty.getIdentifier());
		Integer codelist = definitionProperty.getCodelist();
		if (codelist != null && propertyValue != null) {
			if (definitionProperty.isMultiValued().booleanValue()) {
				return processCodelistMultiValues(propertyValue, codelist);
			}
			return codelistService.getDescription(codelist, propertyValue.toString());
		}
		return getFormattedValues(definitionProperty, propertyValue);
	}

	private Serializable getFormattedValues(PropertyDefinition definitionProperty, Serializable propertyValue) {
		if (propertyValue != null) {
			if (DataTypeDefinition.DATETIME.equals(definitionProperty.getType())) {
				FormattedDateTime formattedDateTime = typeConverter.convert(FormattedDateTime.class, propertyValue);
				return formattedDateTime.getFormatted();
			}
			if (DataTypeDefinition.DATE.equals(definitionProperty.getType())) {
				FormattedDate formattedDateTime = typeConverter.convert(FormattedDate.class, propertyValue);
				return formattedDateTime.getFormatted();
			}
			ControlDefinition controlDefinition = definitionProperty.getControlDefinition();
			if (controlDefinition != null) {
				return getControlDefinitionLabel(propertyValue, controlDefinition);
			}
		}
		return propertyValue;
	}

	private static Serializable getControlDefinitionLabel(Serializable propertyValue,
			ControlDefinition controlDefinition) {
		Optional<PropertyDefinition> field = controlDefinition.getField(propertyValue.toString());
		if (field.isPresent()) {
			return field.get().getLabel();
		}
		return propertyValue;
	}

	private Serializable processCodelistMultiValues(Serializable propertyValue, Integer codelist) {
		Collection<String> codelistValues = (Collection<String>) propertyValue;
		// sb used for concatination of multivalued codelist fields
		StringBuilder multivalue = new StringBuilder();
		for (String value : codelistValues) {
			multivalue.append(codelistService.getDescription(codelist, value)).append(System.lineSeparator());
		}
		return multivalue;
	}

	/**
	 * Get property labels using their definition model.
	 *
	 * @param instances
	 *            list of instances
	 * @param properties
	 *            list of properties
	 * @return set of labels
	 */
	private Set<String> getPropertyLabels(Collection<Instance> instances, Set<String> properties) {
		Map<String, Set<String>> labels = new LinkedHashMap<>(properties.size());
		DefinitionModel model;
		for (Instance instance : instances) {
			model = dictionaryService.getInstanceDefinition(instance);
			for (String propertyName : properties) {
				Optional<PropertyDefinition> field = model.getField(propertyName);
				if (field.isPresent()) {
					String label = field.get().getLabel();
					addToLabelsMap(labels, propertyName, label);
				}
			}
		}
		return new LinkedHashSet<>(getPropertyMultiLabels(labels));
	}

	/**
	 * Add label to Map which later used for creating proper headings, where we can have multiple labels for a single
	 * heading.
	 *
	 * @param data
	 *            the data
	 * @param propertyName
	 *            the property name
	 * @param label
	 *            the label
	 */
	private static void addToLabelsMap(Map<String, Set<String>> data, String propertyName, String label) {
		if (data.containsKey(propertyName)) {
			data.get(propertyName).add(label);
		} else {
			Set<String> labels = new LinkedHashSet<>(1);
			labels.add(label);
			data.put(propertyName, labels);
		}
	}

	/**
	 * Convert Map with labels to set, where for each property join the labels to multi label.
	 *
	 * @param data
	 *            the data
	 * @return the property labels
	 */
	private static Set<String> getPropertyMultiLabels(Map<String, Set<String>> data) {
		Set<String> labels = new LinkedHashSet<>(data.size());
		for (Set<String> values : data.values()) {
			String label = values.stream().filter(Objects::nonNull).collect(Collectors.joining(","));
			labels.add(label);
		}
		return labels;
	}

	/**
	 * Removes html tags from header and extracts url from href if exist.
	 *
	 * @param headerType
	 *            the header type of the widget
	 * @param instance
	 *            the instance
	 * @return header of instance
	 */
	private Map<String, String> sanitizeHeader(String headerType, Instance instance) {
		String header = instance.getString(headerType);
		Document heading = Jsoup.parse(header);
		// getting <a> element from header
		Elements links = heading.select("a");
		Map<String, String> hyperlink = new HashMap<>();
		String path = links.attr("href");
		hyperlink.put("address", getInstanceUrl(path));
		hyperlink.put("linkLabel", heading.text());
		return hyperlink;
	}

	private String getInstanceUrl(String path) {
		String ui2url = systemConfiguration.getUi2Url().get();
		if (path.contains(ui2url)) {
			return path;
		}
		return ui2url + path;
	}

	/**
	 * Loads instances by given list of ids.
	 *
	 * @param selectedInstances
	 *            the ids
	 * @return list of instances
	 */
	private Collection<Instance> loadInstanceProperties(List<String> selectedInstances) {
		Collection<Instance> instances = instanceResolver.resolveInstances(selectedInstances);
		instanceDecorator.decorateResult(instances);
		return instances;
	}

	/**
	 * Processes null objects.
	 *
	 * @param obj
	 *            the object
	 * @return return string value of object or empty string
	 */
	public static String extractValidValue(Object obj) {
		String value = String.valueOf(obj);
		if ("null".equals(value)) {
			value = "";
		}
		return value;
	}
}