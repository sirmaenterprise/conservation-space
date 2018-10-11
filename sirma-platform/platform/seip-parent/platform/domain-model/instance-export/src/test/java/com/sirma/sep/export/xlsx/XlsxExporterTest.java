package com.sirma.sep.export.xlsx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonReader;

import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.properties.RichtextPropertiesDao;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.time.FormattedDate;
import com.sirma.itt.seip.time.FormattedDateTime;
import com.sirma.sep.export.ContentExportException;
import com.sirma.sep.export.renders.utils.TestExportExcelUtil;
import com.sirma.sep.export.xlsx.action.ExportXlsxRequest;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * Tests for {@link XlsxExporter}
 *
 * @author Boyan Tonchev
 */
@RunWith(DataProviderRunner.class)
public class XlsxExporterTest {

	private static final String SYSTEM_ID_LABEL = "System id";
	private static final String MULTIPLE_OBJECTS_ID_LABEL = "Multiple objects";

	private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));
	private static final String UI_2_URL = "http://host:port";

	private static final String INSTANCE_IDENTIFIER = "GEC20001";
	private static final String INSTANCE_ID = "emf:4809832c-2d40-4329-8369-f8b2df549cd0";
	private static final String INSTANCE_LINK_LABEL = "Instance one compact header";
	private static final String INSTANCE_COMPACT_HEADER = "<a href=\"/" + INSTANCE_ID + "\">" + INSTANCE_LINK_LABEL
			+ "</a>";

	private static final String INSTANCE_PARENT_IDENTIFIER = "ET120001";
	private static final String INSTANCE_PARENT_ID = "emf:4809832c-2d40-4329-8369-f8b2df549cd2";
	private static final String INSTANCE_PARENT_LINK_LABEL = "Parent instance compact header";
	private static final String INSTANCE_PARENT_COMPACT_HEADER = "<a href=\"/" + INSTANCE_PARENT_ID + "\">"
			+ INSTANCE_PARENT_LINK_LABEL + "</a>";
	private static final String INSTANCE_PARENT_PROPERTY_OBJECT_VALUE = "Value of object property of parent instance";

	private static final String INSTANCE_PARENT_TWO_IDENTIFIER = "ET120001";
	private static final String INSTANCE_PARENT_TWO_ID = "emf:4809832c-2d40-4329-8369-f8b2df549cd3";
	private static final String INSTANCE_PARENT_TWO_LINK_LABEL = "Parent two instance compact header";
	private static final String INSTANCE_PARENT_TWO_COMPACT_HEADER = "<a href=\"/" + INSTANCE_PARENT_TWO_ID + "\">"
			+ INSTANCE_PARENT_TWO_LINK_LABEL + "</a>";
	private static final String INSTANCE_PARENT_TWO_PROPERTY_OBJECT_VALUE = "Value of object property of parent two instance";

	private static final String INSTANCE_ONE_PROPERTY_TITLE_VALUE = "Title of instance one";
	private static final String PROPERTY_TITLE_NAME = "title";
	private static final String PROPERTY_DEPARTMENT_NAME = "emf:department";
	private static final String PROPERTY_IDENTIFIER_NAME = "identifier";
	private static final String PROPERTY_HAS_PARENT_NAME = "hasParent";
	private static final String PROPERTY_CONTROL_DEFINITION_NAME = "controlDefinition";
	private static final String PROPERTY_DATE_NAME = "date";
	private static final String PROPERTY_DATE_TIME_NAME = "dateTime";
	private static final String PROPERTY_SINGLE_CODE_LIST = "singleCodlist";
	private static final String PROPERTY_MULTY_CODE_LIST = "multiCodelist";

	private static final String FORMATTED_DATE_VALUE = "formated date";
	private static final String FORMATTED_DATE_TIME_VALUE = "formated date time";
	private static final String CODE_LIST_SINGLE_VALUE = "Label of code list single value";
	private static final String CODE_LIST_MULTI_VALUE_ONE = "Label of code list value one";
	private static final String CODE_LIST_MULTI_VALUE_TWO = "Label of code list value two";

	private static final String PATH_TO_TEST_RESOURCE = "xlsx/";
	private static final String TEST_FILE_MANUALLY_WITH_ENTITY_COLUMN = "test-file-manually-with-entity-column.json";
	private static final String TEST_FILE_MANUALLY_WITH_CONTROL_DEFINITION = "test-file-manually-with-control-definition.json";
	private static final String TEST_FILE_MANUALLY_WITH_DATE_TIME = "test-file-manually-with-date-time.json";
	private static final String TEST_FILE_MANUALLY_WITH_DATE = "test-file-manually-with-date.json";
	private static final String TEST_FILE_MANUALLY_WITH_NULL_OBJECT_PROPERTY = "test-file-manually-with-null-object-property.json";
	private static final String TEST_FILE_MANUALLY_WITH_SINGLE_CODE_LIST = "test-file-manually-with-single-codelist.json";
	private static final String TEST_FILE_MANUALLY_WITH_MULTY_CODE_LIST = "test-file-manually-with-multi-codelist.json";
	private static final String TEST_FILE_AUTOMATICALLY_WITH_ENTITY_COLUMN = "test-file-automatically-with-entity-column.json";
	private static final String TEST_FILE_AUTOMATICALLY_WITH_ENTITY_COLUMN_WITH_ORDER_AND_DIRECTION = "test-file-automatically-with-entity-column-with-order-direction.json";
	private static final String TEST_FILE_AUTOMATICALLY_WITH_ENTITY_COLUMN_WITH_ORDER = "test-file-automatically-with-entity-column-with-order.json";
	private static final String TEST_FILE_AUTOMATICALLY_WITH_ENTITY_COLUMN_WITH_DIRECTION = "test-file-automatically-with-entity-column-with-direction.json";
	private static final String TEST_FILE_MANUALLY_WITH_LOCKED_BY_COLUMN = "test-file-manually-with-lockedBy-column.json";
	private static final String TEST_FILE_SHOW_INSTANCE_ID_TRUE = "test-file-show-instance-id-true.json";
	private static final String TEST_FILE_SHOW_INSTANCE_ID_FALSE = "test-file-show-instance-id-false.json";
	private static final String TEST_FILE_MANUALLY_WITH_SUBPROPERTY = "test-file-manually-with-subproperty.json";

	@Mock
	private ConfigurationProperty<Integer> exportExcelBatchSize;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	protected HeadersService headersService;

	@Mock
	protected DefinitionService definitionService;

	@Mock
	private TempFileProvider tempFileProvider;

	@Mock
	protected TypeConverter typeConverter;

	@Mock
	protected CodelistService codelistService;

	@Mock
	private SearchService searchService;

	@Mock
	private JsonToConditionConverter converter;

	@InjectMocks
	private XlsxExporter xlsxExporter;

	@Mock
	private DbDao dbDao;

	@Mock
	protected LockService lockService;
	
	@Mock
	protected LabelProvider labelProvider;

	@Mock
	protected RichtextPropertiesDao richtextPropertiesDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(exportExcelBatchSize.get()).thenReturn(100);
		ConfigurationProperty<String> ui2Url = mock(ConfigurationProperty.class);
		when(ui2Url.get()).thenReturn(UI_2_URL);
		when(systemConfiguration.getUi2Url()).thenReturn(ui2Url);
		when(tempFileProvider.createLongLifeTempDir("export-xlsx")).thenReturn(TEMP_DIR);
		when(labelProvider.getLabel("widget.export.multiple.objects")).thenReturn(MULTIPLE_OBJECTS_ID_LABEL);
		when(labelProvider.getLabel("exportXlsx.header.system.id.label")).thenReturn(SYSTEM_ID_LABEL);
		xlsxExporter.init();
	}

	@Test
	public void should_GenerateExcelFileWthSystemIdColumn_When_ShowInstanceIdIsSetFalse()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		String lockedByIdentifier = "userDefinition";
		String lockedById = "emf:boyan@tenant.bg";

		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			InstanceMockBuilder lockedBy = new InstanceMockBuilder(lockedById)
					.setIdentifier(lockedByIdentifier)
					.setInstanceHeader(DefaultProperties.HEADER_COMPACT, null);
			instanceMockBuilder.setLockedBy(lockedById);
			setupDbDao(Arrays.asList(instanceMockBuilder, lockedBy));
			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_SHOW_INSTANCE_ID_FALSE))
					.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);

			// Verifying that cell system id not exist.
			TestExportExcelUtil.assertStringValueNotExist(sheet, 0, SYSTEM_ID_LABEL);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFileWthSystemIdColumn_When_ShowInstanceIdIsSetTrue()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		String lockedByIdentifier = "userDefinition";
		String lockedById = "emf:boyan@tenant.bg";

		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			InstanceMockBuilder lockedBy = new InstanceMockBuilder(lockedById)
					.setIdentifier(lockedByIdentifier)
					.setInstanceHeader(DefaultProperties.HEADER_COMPACT, null);

			instanceMockBuilder.setLockedBy(lockedById);

			setupDbDao(Arrays.asList(instanceMockBuilder, lockedBy));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_SHOW_INSTANCE_ID_TRUE))
					.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of system id header name
			TestExportExcelUtil.assertStringValue(sheet, 0, 2, SYSTEM_ID_LABEL);
			// verification of instance id value
			TestExportExcelUtil.assertStringValue(sheet, 1, 2, INSTANCE_ID);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFileWthSystemIdColumn_When_ShowInstanceIdIsMissing()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setObjectValue(PROPERTY_HAS_PARENT_NAME, INSTANCE_PARENT_ID);

			InstanceMockBuilder object = setObjectPropertyInstanceOne();
			object.setObjectValue(PROPERTY_HAS_PARENT_NAME, INSTANCE_PARENT_ID);
			object.setTextValue("hasParent:title", "subtitle");
			setupDbDao(Arrays.asList(instanceMockBuilder, object));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_SUBPROPERTY)).get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of system id header name
			TestExportExcelUtil.assertStringValue(sheet, 0, 8, SYSTEM_ID_LABEL);
			// verification of instance id value
			TestExportExcelUtil.assertStringValue(sheet, 1, 8, INSTANCE_ID);

		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_HeaderIsNottSet()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		String lockedByIdentifier = "userDefinition";
		String lockedById = "emf:boyan@tenant.bg";

		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			InstanceMockBuilder lockedBy = new InstanceMockBuilder(lockedById)
					.setIdentifier(lockedByIdentifier)
						.setInstanceHeader(DefaultProperties.HEADER_COMPACT, null);

			instanceMockBuilder.setLockedBy(lockedById);

			setupDbDao(Arrays.asList(instanceMockBuilder, lockedBy));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_LOCKED_BY_COLUMN))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Entity");
			TestExportExcelUtil.assertStringValue(sheet, 0, 1, "Locked by");
			// verification of hasParent value
			TestExportExcelUtil.assertLinkValue(sheet, 1, 0, INSTANCE_LINK_LABEL, UI_2_URL + "/" + INSTANCE_ID);
			TestExportExcelUtil.assertLinkValue(sheet, 1, 1, "", "");
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_LockedByIstSet()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		String lockedByIdentifier = "userDefinition";
		String lockedById = "emf:boyan@tenant.bg";
		String lockedByLinkLabel = "boyan";
		String lockedByCompactHeader = "<a href=\"/" + lockedById + "\">" + lockedByLinkLabel + "</a>";

		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			InstanceMockBuilder lockedBy = new InstanceMockBuilder(lockedById)
					.setIdentifier(lockedByIdentifier)
						.setInstanceHeader(DefaultProperties.HEADER_COMPACT, lockedByCompactHeader);

			instanceMockBuilder.setLockedBy(lockedById);

			setupDbDao(Arrays.asList(instanceMockBuilder, lockedBy));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_LOCKED_BY_COLUMN))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Entity");
			TestExportExcelUtil.assertStringValue(sheet, 0, 1, "Locked by");
			// verification of hasParent value
			TestExportExcelUtil.assertLinkValue(sheet, 1, 0, INSTANCE_LINK_LABEL, UI_2_URL + "/" + INSTANCE_ID);
			TestExportExcelUtil.assertLinkValue(sheet, 1, 1, lockedByLinkLabel, UI_2_URL + "/" + lockedById);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_LockedByIsNotSet()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setLockedBy(null);

			setupDbDao(Collections.singleton(instanceMockBuilder));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_LOCKED_BY_COLUMN))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Entity");
			TestExportExcelUtil.assertStringValue(sheet, 0, 1, "Locked by");
			// verification of hasParent value
			TestExportExcelUtil.assertLinkValue(sheet, 1, 0, INSTANCE_LINK_LABEL, UI_2_URL + "/" + INSTANCE_ID);
			TestExportExcelUtil.assertStringValue(sheet, 1, 1, "");
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_SelectedPropertiesAreByParentType()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			InstanceMockBuilder instanceMockBuilder = new InstanceMockBuilder(INSTANCE_ID)
					.setInstanceHeader(DefaultProperties.HEADER_COMPACT, INSTANCE_COMPACT_HEADER)
						.setIdentifier("unknownIdentifier")
						.setTextValue(PROPERTY_TITLE_NAME, INSTANCE_ONE_PROPERTY_TITLE_VALUE)
						.setTextValue(PROPERTY_IDENTIFIER_NAME, "unknownIdentifier")
						.setTypeId("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#SubDocument")
						.setOptionalEmptyProperty("emf:version")
						.setSuperTypes(
								Arrays.asList("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case",
										"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document"));
			setupDbDao(Collections.singleton(instanceMockBuilder));
			SearchArguments<Instance> searchArgs = setupSearchService();
			searchArgs.setResult(Collections.singletonList(instanceMockBuilder.getInstance()));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE
							+ TEST_FILE_AUTOMATICALLY_WITH_ENTITY_COLUMN_WITH_ORDER_AND_DIRECTION))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Entity");
			TestExportExcelUtil.assertStringValue(sheet, 0, 1, "Version");
			TestExportExcelUtil.assertStringValue(sheet, 0, 2, "Title, Name, User");
			// verification of entity value
			TestExportExcelUtil.assertLinkValue(sheet, 1, 0, INSTANCE_LINK_LABEL, UI_2_URL + "/" + INSTANCE_ID);
			TestExportExcelUtil.assertStringValue(sheet, 1, 1, "");
			TestExportExcelUtil.assertStringValue(sheet, 1, 2, "Title of instance one");
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_ClassIsSetIntoHeadersInfo()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			InstanceMockBuilder instanceMockBuilder = new InstanceMockBuilder(INSTANCE_ID)
					.setInstanceHeader(DefaultProperties.HEADER_COMPACT, INSTANCE_COMPACT_HEADER)
						.setIdentifier("unknownIdentifier")
						.setTextValue(PROPERTY_TITLE_NAME, INSTANCE_ONE_PROPERTY_TITLE_VALUE)
						.setTextValue(PROPERTY_IDENTIFIER_NAME, "unknownIdentifier")
						.setTypeId("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document")
						.setOptionalEmptyProperty("emf:version");
			setupDbDao(Collections.singleton(instanceMockBuilder));
			SearchArguments<Instance> searchArgs = setupSearchService();
			searchArgs.setResult(Collections.singletonList(instanceMockBuilder.getInstance()));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE
							+ TEST_FILE_AUTOMATICALLY_WITH_ENTITY_COLUMN_WITH_ORDER_AND_DIRECTION))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Entity");
			TestExportExcelUtil.assertStringValue(sheet, 0, 1, "Version");
			TestExportExcelUtil.assertStringValue(sheet, 0, 2, "Title, Name, User");
			// verification of entity value
			TestExportExcelUtil.assertLinkValue(sheet, 1, 0, INSTANCE_LINK_LABEL, UI_2_URL + "/" + INSTANCE_ID);
			TestExportExcelUtil.assertStringValue(sheet, 1, 1, "");
			TestExportExcelUtil.assertStringValue(sheet, 1, 2, "Title of instance one");
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	@UseDataProvider("should_NotSetSorter_When_OdrdeOrDirectionAreNotSetDP")
	public void should_NotSetSorter_When_OdrderOrDirectionAreNotSet(String pathToJsonFile)
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			SearchArguments<Instance> searchArgs = setupSearchService();

			// execute tested method
			testFile = xlsxExporter.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + pathToJsonFile)).get();

			// Verification
			assertEquals(1, searchArgs.getPageNumber());
			assertEquals(1000, searchArgs.getMaxSize());

			List<Sorter> sorters = searchArgs.getSorters();
			assertTrue(sorters.size() == 0);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@DataProvider
	public static Object[][] should_NotSetSorter_When_OdrdeOrDirectionAreNotSetDP() {
		return new Object[][] { { TEST_FILE_AUTOMATICALLY_WITH_ENTITY_COLUMN_WITH_ORDER },
				{ TEST_FILE_AUTOMATICALLY_WITH_ENTITY_COLUMN },
				{ TEST_FILE_AUTOMATICALLY_WITH_ENTITY_COLUMN_WITH_DIRECTION } };
	}

	@Test
	public void should_SetSorter_When_OdrderOrDirectionAreSet()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			SearchArguments<Instance> searchArgs = setupSearchService();

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE
							+ TEST_FILE_AUTOMATICALLY_WITH_ENTITY_COLUMN_WITH_ORDER_AND_DIRECTION))
						.get();

			// Verification
			assertEquals(1, searchArgs.getPageNumber());
			assertEquals(1000, searchArgs.getMaxSize());

			List<Sorter> sorters = searchArgs.getSorters();
			assertTrue(sorters.size() == 1);
			Sorter sorter = sorters.get(0);
			assertEquals("dcterms:title", sorter.getSortField());
			assertFalse(sorter.isAscendingOrder());
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_ASelectedPropertyIsCodeListMultiValue()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		String codeListValue = "date property value";
		String codeListValueTwo = "date property value two";
		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setCodeListMultiValue(2, PROPERTY_MULTY_CODE_LIST,
					new ArrayList<>(Arrays.asList(codeListValue, codeListValueTwo)), codeListValue, codeListValueTwo);
			setupDbDao(Collections.singleton(instanceMockBuilder));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_MULTY_CODE_LIST))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Multi Codelist Label");
			// verification of hasParent value
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, CODE_LIST_MULTI_VALUE_ONE, CODE_LIST_MULTI_VALUE_TWO);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_ASelectedPropertyIsCodeListSingleValue()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		String codeListValue = "date property value";
		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setCodeListSingleValue(1, PROPERTY_SINGLE_CODE_LIST, codeListValue);
			setupDbDao(Collections.singleton(instanceMockBuilder));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_SINGLE_CODE_LIST))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Single Codlist Label");
			// verification of hasParent value
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, CODE_LIST_SINGLE_VALUE);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_ASelectedPropertyIsDataTimeProperty()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		String dateTimeValue = "date property value";
		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setDateTimeValue(PROPERTY_DATE_TIME_NAME, dateTimeValue);
			setupDbDao(Collections.singleton(instanceMockBuilder));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_DATE_TIME))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Date Time Label");
			// verification of hasParent value
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, FORMATTED_DATE_TIME_VALUE);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_ASelectedPropertyIsDataProperty()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		String dateValue = "date property value";
		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setDateValue(PROPERTY_DATE_NAME, dateValue);
			setupDbDao(Collections.singleton(instanceMockBuilder));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_DATE))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Date Label");
			// verification of hasParent value
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, FORMATTED_DATE_VALUE);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_ASelectedPropertyIsControlDefinition()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		String controlDefinitionLabel = "Control definition";
		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setControlDefinitionValue(PROPERTY_CONTROL_DEFINITION_NAME, controlDefinitionLabel);
			setupDbDao(Collections.singleton(instanceMockBuilder));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_CONTROL_DEFINITION))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Control Definition Label");
			// verification of hasParent value
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, controlDefinitionLabel);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_ASelectedPropertyIsTwoObjectInstance()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setObjectValue(PROPERTY_HAS_PARENT_NAME,
					new ArrayList<>(Arrays.asList(INSTANCE_PARENT_ID, INSTANCE_PARENT_TWO_ID)));
			InstanceMockBuilder firstObject = setObjectPropertyInstanceOne();
			InstanceMockBuilder secondObject = setObjectPropertyInstanceTwo();

			setupDbDao(Arrays.asList(instanceMockBuilder, firstObject, secondObject));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_NULL_OBJECT_PROPERTY))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Has Parent");
			// verification of hasParent value
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, INSTANCE_PARENT_LINK_LABEL,
					INSTANCE_PARENT_TWO_LINK_LABEL);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_ASelectedPropertyIsAObjectInstance()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setObjectValue(PROPERTY_HAS_PARENT_NAME, INSTANCE_PARENT_ID);
			InstanceMockBuilder object = setObjectPropertyInstanceOne();

			setupDbDao(Arrays.asList(instanceMockBuilder, object));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_NULL_OBJECT_PROPERTY))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Has Parent");
			// verification of hasParent value
			TestExportExcelUtil.assertLinkValue(sheet, 1, 0, INSTANCE_PARENT_LINK_LABEL,
					UI_2_URL + "/" + INSTANCE_PARENT_ID);
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_ASelectedPropertyIsEmptyObject()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setObjectValue(PROPERTY_HAS_PARENT_NAME, "");
			setupDbDao(Collections.singleton(instanceMockBuilder));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_NULL_OBJECT_PROPERTY))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Has Parent");
			// verification of hasParent value
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, "");
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_ASelectedPropertyIsNullObject()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setObjectValue(PROPERTY_HAS_PARENT_NAME, null);
			setupDbDao(Collections.singleton(instanceMockBuilder));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_NULL_OBJECT_PROPERTY))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Has Parent");
			// verification of hasParent value
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, "");
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_ASelectedPropertyIsText()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			// setup two text values
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			setupDbDao(Collections.singleton(instanceMockBuilder));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_ENTITY_COLUMN))
						.get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Has Parent");
			TestExportExcelUtil.assertStringValue(sheet, 0, 1, "Entity");
			TestExportExcelUtil.assertStringValue(sheet, 0, 2, "Title");
			TestExportExcelUtil.assertStringValue(sheet, 0, 3, "Identifier");
			TestExportExcelUtil.assertStringValue(sheet, 0, 4, "Type");
			TestExportExcelUtil.assertStringValue(sheet, 0, 5, "Version");
			// verification of entity value
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, "");
			TestExportExcelUtil.assertLinkValue(sheet, 1, 1, INSTANCE_LINK_LABEL, UI_2_URL + "/" + INSTANCE_ID);
			TestExportExcelUtil.assertStringValue(sheet, 1, 2, INSTANCE_ONE_PROPERTY_TITLE_VALUE);
			TestExportExcelUtil.assertStringValue(sheet, 1, 3, INSTANCE_IDENTIFIER);
			TestExportExcelUtil.assertStringValue(sheet, 1, 4, "");
			TestExportExcelUtil.assertStringValue(sheet, 1, 5, "");
		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void getName() {
		assertEquals("excel", xlsxExporter.getName());
	}

	@Test
	public void should_GenerateExcelFile_When_ASelectedPropertyHasSubproperties()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setObjectValue(PROPERTY_HAS_PARENT_NAME, INSTANCE_PARENT_ID);

			InstanceMockBuilder object = setObjectPropertyInstanceOne();
			object.setObjectValue(PROPERTY_HAS_PARENT_NAME, INSTANCE_PARENT_ID);
			object.setTextValue("hasParent:title", "subtitle");
			setupDbDao(Arrays.asList(instanceMockBuilder, object));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_SUBPROPERTY)).get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Has Parent");
			TestExportExcelUtil.assertStringValue(sheet, 0, 1, "Title");
			TestExportExcelUtil.assertStringValue(sheet, 0, 2, "HasParent");
			// verification of hasParent and title value
			TestExportExcelUtil.assertLinkValue(sheet, 1, 0, INSTANCE_PARENT_LINK_LABEL,
					UI_2_URL + "/" + INSTANCE_PARENT_ID);
			TestExportExcelUtil.assertStringValue(sheet, 1, 1, INSTANCE_PARENT_PROPERTY_OBJECT_VALUE);
			TestExportExcelUtil.assertLinkValue(sheet, 1, 2, INSTANCE_PARENT_LINK_LABEL,
					UI_2_URL + "/" + INSTANCE_PARENT_ID);

		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_PropertyWithNoValueHasSubproperties()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setObjectValue(PROPERTY_HAS_PARENT_NAME, INSTANCE_PARENT_ID);

			InstanceMockBuilder object = setObjectPropertyInstanceOne();
			object.setObjectValue(PROPERTY_HAS_PARENT_NAME, new ArrayList<>());
			object.setTextValue("hasParent:title", "subtitle");
			setupDbDao(Arrays.asList(instanceMockBuilder, object));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_SUBPROPERTY)).get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);
			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Has Parent");
			TestExportExcelUtil.assertStringValue(sheet, 0, 1, "Title");
			TestExportExcelUtil.assertStringValue(sheet, 0, 2, "HasParent");
			// verification of hasParent and title value
			TestExportExcelUtil.assertLinkValue(sheet, 1, 0, INSTANCE_PARENT_LINK_LABEL,
					UI_2_URL + "/" + INSTANCE_PARENT_ID);
			TestExportExcelUtil.assertStringValue(sheet, 1, 1, INSTANCE_PARENT_PROPERTY_OBJECT_VALUE);
			TestExportExcelUtil.assertStringValue(sheet, 1, 2, "");

		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	@Test
	public void should_GenerateExcelFile_When_MultipleObjectsHasSubproperties()
			throws URISyntaxException, IOException, ContentExportException {
		File testFile = null;
		try {
			InstanceMockBuilder instanceMockBuilder = setUpInstance();
			instanceMockBuilder.setObjectValue(PROPERTY_HAS_PARENT_NAME,
					new ArrayList<>(Arrays.asList(INSTANCE_PARENT_ID, INSTANCE_PARENT_TWO_ID)));
			InstanceMockBuilder firstObject = setObjectPropertyInstanceOne();
			InstanceMockBuilder secondObject = setObjectPropertyInstanceTwo();

			setupDbDao(Arrays.asList(instanceMockBuilder, firstObject, secondObject));

			// execute tested method
			testFile = xlsxExporter
					.export(initExportXlsxBuilder(PATH_TO_TEST_RESOURCE + TEST_FILE_MANUALLY_WITH_SUBPROPERTY)).get();

			// Verification
			Sheet sheet = TestExportExcelUtil.getSheet(testFile, 0);

			// verification of headers
			TestExportExcelUtil.assertStringValue(sheet, 0, 0, "Has Parent");
			TestExportExcelUtil.assertStringValue(sheet, 0, 1, "Title");
			// verification of hasParent and title value
			TestExportExcelUtil.assertStringValue(sheet, 1, 0, INSTANCE_PARENT_LINK_LABEL,
					INSTANCE_PARENT_TWO_LINK_LABEL);
			TestExportExcelUtil.assertStringValue(sheet, 1, 1, MULTIPLE_OBJECTS_ID_LABEL);

		} finally {
			TestExportExcelUtil.deleteFile(testFile);
		}
	}

	private SearchArguments<Instance> setupSearchService() {
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setResult(Collections.emptyList());
		when(searchService.parseRequest(any(SearchRequest.class))).thenReturn(searchArgs);
		return searchArgs;
	}

	private InstanceMockBuilder setObjectPropertyInstanceTwo() {
		return new InstanceMockBuilder(INSTANCE_PARENT_TWO_ID)
				.setInstanceHeader(DefaultProperties.HEADER_COMPACT, INSTANCE_PARENT_TWO_COMPACT_HEADER)
					.setIdentifier(INSTANCE_PARENT_TWO_IDENTIFIER)
					.setTextValue(PROPERTY_TITLE_NAME, INSTANCE_PARENT_TWO_PROPERTY_OBJECT_VALUE)
					.setTextValue(PROPERTY_IDENTIFIER_NAME, INSTANCE_PARENT_TWO_IDENTIFIER);
	}

	private InstanceMockBuilder setObjectPropertyInstanceOne() {
		return new InstanceMockBuilder(INSTANCE_PARENT_ID)
				.setInstanceHeader(DefaultProperties.HEADER_COMPACT, INSTANCE_PARENT_COMPACT_HEADER)
					.setIdentifier(INSTANCE_PARENT_IDENTIFIER)
					.setTextValue(PROPERTY_TITLE_NAME, INSTANCE_PARENT_PROPERTY_OBJECT_VALUE)
					.setTextValue(PROPERTY_IDENTIFIER_NAME, INSTANCE_PARENT_IDENTIFIER);
	}

	private InstanceMockBuilder setUpInstance() {
		return new InstanceMockBuilder(INSTANCE_ID)
				.setInstanceHeader(DefaultProperties.HEADER_COMPACT, INSTANCE_COMPACT_HEADER)
					.setIdentifier(INSTANCE_IDENTIFIER)
					.setTextValue(PROPERTY_TITLE_NAME, INSTANCE_ONE_PROPERTY_TITLE_VALUE)
					.setTextValue(PROPERTY_IDENTIFIER_NAME, INSTANCE_IDENTIFIER);
	}

	@SuppressWarnings("unchecked")
	private void setupDbDao(Collection<InstanceMockBuilder> instanceMockBuilders) {
		Map<Serializable, Instance> instances = new HashMap<>(instanceMockBuilders.size());
		instanceMockBuilders.forEach(instanceMockBuilder -> {
			Instance instance = instanceMockBuilder.getInstance();
			instances.put(instance.getId(), instance);
		});

		when(dbDao.fetchWithNamed(anyString(), anyList())).thenAnswer(invocation -> {
			List<Pair<String, Object>> params = invocation.getArgumentAt(1, List.class);
			List<String> instancesRequest = (List<String>) params.get(0).getSecond();
			List<Instance> result = new ArrayList<>();
			instancesRequest.forEach(instanceId -> {
				result.add(instances.get(instanceId));
			});
			return result;
		});
	}

	private class InstanceMockBuilder {
		private Instance instance;
		private DefinitionModel instanceDefinition;
		private InstanceType instanceType;

		protected InstanceMockBuilder(String instanceId) {
			instance = mock(Instance.class);
			when(instance.getId()).thenReturn(instanceId);
			instanceDefinition = mock(DefinitionModel.class);
			when(definitionService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);
		}

		protected InstanceMockBuilder setSuperTypes(List<String> superTypesIds) {
			initInstanceType();
			Set<InstanceType> superTypes = new HashSet<>(superTypesIds.size());
			superTypesIds.forEach(parentId -> {
				InstanceType mock = mock(InstanceType.class);
				when(mock.getId()).thenReturn(parentId);
				superTypes.add(mock);
			});
			when(instanceType.getSuperTypes()).thenReturn(superTypes);
			return this;
		}

		protected InstanceMockBuilder setLockedBy(String lockedBy) {
			setObjectValue(DefaultProperties.LOCKED_BY, lockedBy);
			LockInfo lockInfo = mock(LockInfo.class);
			when(lockInfo.getLockedBy()).thenReturn(lockedBy);
			when(lockService.lockStatus(any(InstanceReference.class))).thenReturn(lockInfo);
			return this;
		}

		protected InstanceMockBuilder setInstanceHeader(String headerName, String headerValue) {
			when(headersService.generateInstanceHeader(eq(instance), eq(headerName))).thenReturn(headerValue);
			return this;
		}

		protected InstanceMockBuilder setIdentifier(String identifier) {
			when(instance.getIdentifier()).thenReturn(identifier);
			return this;
		}

		protected InstanceMockBuilder setTextValue(String propertyName, String propertyValue) {
			setValue(propertyName, propertyValue, DataTypeDefinition.TEXT);
			return this;
		}

		protected InstanceMockBuilder setObjectValue(String propertyName, Serializable propertyValue) {
			setValue(propertyName, propertyValue, DataTypeDefinition.URI);
			return this;
		}

		protected InstanceMockBuilder setTypeId(String typeId) {
			initInstanceType();
			when(instanceType.getId()).thenReturn(typeId);
			return this;
		}

		private void initInstanceType() {
			if (instanceType == null) {
				instanceType = mock(InstanceType.class);
				when(instance.type()).thenReturn(instanceType);
			}
		}

		protected void setDateValue(String propertyName, Serializable propertyValue) {
			PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
			when(propertyDefinition.getType()).thenReturn(DataTypeDefinition.DATE);
			when(propertyDefinition.getIdentifier()).thenReturn(propertyName);
			DataTypeDefinition dataType = mock(DataTypeDefinition.class);
			when(dataType.getName()).thenReturn(DataTypeDefinition.DATE);
			when(propertyDefinition.getDataType()).thenReturn(dataType);
			when(instanceDefinition.getField(propertyName)).thenReturn(Optional.of(propertyDefinition));
			FormattedDate formattedDate = mock(FormattedDate.class);
			when(formattedDate.getFormatted()).thenReturn(FORMATTED_DATE_VALUE);
			when(typeConverter.convert(FormattedDate.class, propertyValue)).thenReturn(formattedDate);
			when(instance.get(eq(propertyName))).thenReturn(propertyValue);
		}

		protected void setDateTimeValue(String propertyName, Serializable propertyValue) {
			PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
			when(propertyDefinition.getType()).thenReturn(DataTypeDefinition.DATETIME);
			when(propertyDefinition.getIdentifier()).thenReturn(propertyName);
			DataTypeDefinition dataType = mock(DataTypeDefinition.class);
			when(dataType.getName()).thenReturn(DataTypeDefinition.DATETIME);
			when(propertyDefinition.getDataType()).thenReturn(dataType);
			when(instanceDefinition.getField(propertyName)).thenReturn(Optional.of(propertyDefinition));
			FormattedDateTime formattedDate = mock(FormattedDateTime.class);
			when(formattedDate.getFormatted()).thenReturn(FORMATTED_DATE_TIME_VALUE);
			when(typeConverter.convert(FormattedDateTime.class, propertyValue)).thenReturn(formattedDate);
			when(instance.get(eq(propertyName))).thenReturn(propertyValue);
		}

		protected void setCodeListSingleValue(int codelist, String propertyName, String propertyValue) {
			PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
			when(propertyDefinition.getCodelist()).thenReturn(codelist);
			when(propertyDefinition.getIdentifier()).thenReturn(propertyName);
			DataTypeDefinition codelistDataType = mock(DataTypeDefinition.class);
			when(codelistDataType.getName()).thenReturn(DataTypeDefinition.TEXT);
			when(propertyDefinition.getDataType()).thenReturn(codelistDataType);
			when(instanceDefinition.getField(propertyName)).thenReturn(Optional.of(propertyDefinition));
			when(codelistService.getDescription(codelist, propertyValue)).thenReturn(CODE_LIST_SINGLE_VALUE);
			when(instance.get(eq(propertyName))).thenReturn(propertyValue);
		}

		protected void setCodeListMultiValue(int codelist, String propertyName, ArrayList<String> propertyValue,
				String propertyValueOne, String propertyValueTwo) {
			PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
			when(propertyDefinition.getCodelist()).thenReturn(codelist);
			when(propertyDefinition.getIdentifier()).thenReturn(propertyName);
			DataTypeDefinition codelistDataType = mock(DataTypeDefinition.class);
			when(codelistDataType.getName()).thenReturn(DataTypeDefinition.TEXT);
			when(propertyDefinition.getDataType()).thenReturn(codelistDataType);
			when(instanceDefinition.getField(propertyName)).thenReturn(Optional.of(propertyDefinition));
			when(propertyDefinition.isMultiValued()).thenReturn(true);
			when(codelistService.getDescription(codelist, propertyValueOne)).thenReturn(CODE_LIST_MULTI_VALUE_ONE);
			when(codelistService.getDescription(codelist, propertyValueTwo)).thenReturn(CODE_LIST_MULTI_VALUE_TWO);
			when(instance.get(eq(propertyName))).thenReturn(propertyValue);
		}

		protected void setControlDefinitionValue(String propertyName, String propertyValue) {
			PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
			ControlDefinition controlDefinition = mock(ControlDefinition.class);
			when(propertyDefinition.getControlDefinition()).thenReturn(controlDefinition);
			when(propertyDefinition.getIdentifier()).thenReturn(propertyName);
			when(propertyDefinition.getLabel()).thenReturn(propertyValue);
			when(controlDefinition.getField(propertyValue)).thenReturn(Optional.of(propertyDefinition));
			DataTypeDefinition controlDefinitionDataType = mock(DataTypeDefinition.class);
			when(controlDefinitionDataType.getName()).thenReturn(DataTypeDefinition.TEXT);
			when(propertyDefinition.getDataType()).thenReturn(controlDefinitionDataType);
			when(instanceDefinition.getField(propertyName)).thenReturn(Optional.of(propertyDefinition));
			when(instance.get(eq(propertyName))).thenReturn(propertyValue);
		}

		protected InstanceMockBuilder setOptionalEmptyProperty(String propertyName) {
			when(instanceDefinition.getField(propertyName)).thenReturn(Optional.empty());
			return this;
		}

		private void setValue(String propertyName, Serializable propertyValue, String dataTypeDefinition) {
			PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
			DataTypeDefinition dataType = mock(DataTypeDefinition.class);
			when(dataType.getName()).thenReturn(dataTypeDefinition);
			when(propertyDefinition.getDataType()).thenReturn(dataType);
			when(propertyDefinition.getIdentifier()).thenReturn(propertyName);
			when(instanceDefinition.getField(propertyName)).thenReturn(Optional.of(propertyDefinition));
			when(instance.get(eq(propertyName))).thenReturn(propertyValue);
		}

		public Instance getInstance() {
			return instance;
		}
	}

	private XlsxExportRequest initExportXlsxBuilder(String pathToJsonRequest) throws IOException {
		try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(pathToJsonRequest);
				JsonReader reader = Json.createReader(resourceAsStream)) {
			ExportXlsxRequest request = new ExportXlsxRequest();
			request.setRequestJson(reader.readObject());
			return request.toXlsxExportRequest();
		}
	}
}