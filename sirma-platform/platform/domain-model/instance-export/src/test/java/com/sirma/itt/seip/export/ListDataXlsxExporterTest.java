package com.sirma.itt.seip.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.util.LinkProviderService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.time.FormattedDate;
import com.sirma.itt.seip.time.FormattedDateTime;

/**
 * Tests for {@link ListDataXlsxExporter}
 *
 * @author gshevkedov
 */
public class ListDataXlsxExporterTest {
	private static final String ARTIST_NAMES = "artistNames";
	private static final String COMPACT_HEADER = "compact_header";
	private static final String JOHN_DOE = "John Doe";
	private static final String INSTANCE_ID = "emf:0f892feb-c75c-4f5f-a78a-6d17d5074d14";
	private static final String UI2_URL = "/asd/asd/asd/asd";
	private static final String DATE_TIME_VALUE = "17.11.16 14.50";

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private InstanceTypeResolver instanceResolver;

	@Mock
	private InstanceLoadDecorator instanceDecorator;

	@Mock
	private CodelistService codelistService;

	@Mock
	private LinkProviderService linkProviderService;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private TempFileProvider tempFileProvider;

	@Mock
	private TypeConverter typeConverter;

	@InjectMocks
	private ListDataXlsxExporter widgetToXlsxExporter;

	private ExportListDataXlsx request;
	private PropertyDefinition propertyDefintion;
	private Instance instance;
	private File tempFile;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		request = new ExportListDataXlsx();
		request.setHeaderType(COMPACT_HEADER);
		request.setFileName("src/test/resources/test-file");
		List<String> selectedInstances = new ArrayList<>();
		selectedInstances.add(INSTANCE_ID);
		Set<String> selectedProperties = new HashSet<>();
		selectedProperties.add(ARTIST_NAMES);
		List<Instance> instances = new ArrayList<>();
		instance = Mockito.mock(Instance.class);
		instances.add(instance);
		request.setSelectedInstances(selectedInstances);
		request.setSelectedProperties(selectedProperties);
		Mockito.when(instanceResolver.resolveInstances(Mockito.anyCollection())).thenReturn(instances);
		DefinitionModel model = Mockito.mock(DefinitionModel.class);
		Mockito.when(dictionaryService.getInstanceDefinition(Mockito.any(Instance.class))).thenReturn(model);
		propertyDefintion = Mockito.mock(PropertyDefinition.class);
		Optional<PropertyDefinition> field = Optional.of(propertyDefintion);
		Mockito.when(model.getField(Mockito.anyString())).thenReturn(field);
		Mockito.when(instance.getString(Mockito.anyString())).thenReturn("Test");
		Mockito.when(propertyDefintion.getLabel()).thenReturn("Artist Names");
		Mockito.when(linkProviderService.buildLink(Mockito.any(Instance.class))).thenReturn(UI2_URL);
		tempFile = new File("src/test/resources/test-file");
		Mockito.when(tempFileProvider.createTempFile(Mockito.anyString(), Mockito.anyString())).thenReturn(tempFile);
		Mockito.when(dictionaryService.getProperty(ARTIST_NAMES, instance)).thenReturn(propertyDefintion);
		ConfigurationProperty configProperty = Mockito.mock(ConfigurationProperty.class);
		Mockito.when(systemConfiguration.getUi2Url()).thenReturn(configProperty);
		Mockito.when(configProperty.get()).thenReturn(UI2_URL);
		Mockito.when(codelistService.getDescription(0, JOHN_DOE)).thenReturn(JOHN_DOE);
		Mockito.when(instance.getId()).thenReturn(INSTANCE_ID);
		Mockito.when(propertyDefintion.getIdentifier()).thenReturn(ARTIST_NAMES);
	}

	@After
	public void clearTestResources() {
		tempFile.deleteOnExit();
	}

	@Test
	public void export() {
		ExportListDataXlsx request = new ExportListDataXlsx();
		request.setHeaderType(COMPACT_HEADER);
		request.setFileName("src/test/resources/test-file");
		List<String> selectedInstances = new ArrayList<>();
		Set<String> selectedProperties = new HashSet<>();
		List<Instance> instances = new ArrayList<>();
		request.setSelectedInstances(selectedInstances);
		request.setSelectedProperties(selectedProperties);
		Mockito.when(instanceResolver.resolveInstances(selectedInstances)).thenReturn(instances);
		File generatedFile = widgetToXlsxExporter.export(request);
		assertNotNull(generatedFile);
		generatedFile.deleteOnExit();
	}

	@Test
	public void exportHyperlynks() {
		Mockito.when(instance.get(Mockito.anyString())).thenReturn(JOHN_DOE);
		File generatedFile = widgetToXlsxExporter.export(request);
		assertNotNull(generatedFile);
		generatedFile.deleteOnExit();
	}

	@Test
	public void exportMultivalueCodelistFields() {
		List<String> multivalue = new ArrayList<>(1);
		multivalue.add(ARTIST_NAMES);
		Mockito.when(instance.get(Mockito.anyString())).thenReturn((Serializable) multivalue);
		Mockito.when(propertyDefintion.isMultiValued()).thenReturn(new Boolean(true));
		File generatedFile = widgetToXlsxExporter.export(request);
		assertNotNull(generatedFile);
		generatedFile.deleteOnExit();
	}

	@Test
	public void exportMultivalueHyperlinksFields() {
		List<String> multivalue = new ArrayList<>(1);
		multivalue.add(ARTIST_NAMES);
		Mockito.when(instance.get(Mockito.anyString())).thenReturn((Serializable) multivalue);
		List<Instance> instances = new ArrayList<>();
		instances.add(instance);
		instances.add(instance);
		Mockito.when(instanceResolver.resolveInstances(Mockito.anyCollection())).thenReturn(instances);
		Mockito.when(propertyDefintion.isMultiValued()).thenReturn(new Boolean(true));
		File generatedFile = widgetToXlsxExporter.export(request);
		assertNotNull(generatedFile);
		generatedFile.deleteOnExit();
	}

	@Test
	public void exportMultivalueStringFields() {
		List<String> multivalue = new ArrayList<>(1);
		multivalue.add(ARTIST_NAMES);
		Mockito.when(instance.get(Mockito.anyString())).thenReturn((Serializable) multivalue);
		List<Instance> instances = new ArrayList<>();
		instances.add(instance);
		instances.add(instance);
		Mockito.when(instanceResolver.resolveInstances(Mockito.anyCollection())).thenReturn(instances);
		Mockito.when(propertyDefintion.isMultiValued()).thenReturn(new Boolean(true));
		request.getSelectedProperties().clear();
		request.getSelectedProperties().add("titleAlternative");
		Mockito.when(propertyDefintion.getLabel()).thenReturn("Title alternative");
		Mockito.when(dictionaryService.getProperty("titleAlternative", instance)).thenReturn(propertyDefintion);
		Mockito.when(propertyDefintion.getIdentifier()).thenReturn("titleAlternative");
		Collection<String> ids = new ArrayList<>(1);
		ids.add("artistNames");
		Mockito.when(instanceResolver.resolveInstances(ids)).thenReturn(Collections.emptyList());
		Mockito.when(propertyDefintion.getCodelist()).thenReturn(null);
		File generatedFile = widgetToXlsxExporter.export(request);
		assertNotNull(generatedFile);
		generatedFile.deleteOnExit();
	}

	@Test
	public void exportDateTimeValues() {
		Mockito.when(propertyDefintion.getCodelist()).thenReturn(null);
		Mockito.when(propertyDefintion.getType()).thenReturn("datetime");
		FormattedDateTime formattedDateTime = Mockito.mock(FormattedDateTime.class);
		Mockito.when(typeConverter.convert(FormattedDateTime.class, DATE_TIME_VALUE)).thenReturn(formattedDateTime);
		Mockito.when(formattedDateTime.getFormatted()).thenReturn(DATE_TIME_VALUE);
		Mockito.when(instance.get(Mockito.anyString())).thenReturn(DATE_TIME_VALUE);
		Mockito.when(propertyDefintion.isMultiValued()).thenReturn(new Boolean(true));
		File generatedFile = widgetToXlsxExporter.export(request);
		assertNotNull(generatedFile);
		generatedFile.deleteOnExit();
	}

	@Test
	public void exportDateValues() {
		Mockito.when(propertyDefintion.getCodelist()).thenReturn(null);
		Mockito.when(propertyDefintion.getType()).thenReturn("date");
		FormattedDate formattedDateTime = Mockito.mock(FormattedDate.class);
		Mockito.when(typeConverter.convert(FormattedDate.class, DATE_TIME_VALUE)).thenReturn(formattedDateTime);
		Mockito.when(formattedDateTime.getFormatted()).thenReturn(DATE_TIME_VALUE);
		Mockito.when(instance.get(Mockito.anyString())).thenReturn(DATE_TIME_VALUE);
		Mockito.when(propertyDefintion.isMultiValued()).thenReturn(new Boolean(true));
		File generatedFile = widgetToXlsxExporter.export(request);
		assertNotNull(generatedFile);
		generatedFile.deleteOnExit();
	}

	@Test
	public void exportLabelFromControlDefinition() {
		Mockito.when(instance.get(Mockito.anyString())).thenReturn(JOHN_DOE);
		Mockito.when(propertyDefintion.getCodelist()).thenReturn(null);
		Mockito.when(propertyDefintion.getType()).thenReturn("definition");
		ControlDefinition controlDefinition = Mockito.mock(ControlDefinition.class);
		Mockito.when(propertyDefintion.getControlDefinition()).thenReturn(controlDefinition);
		Optional<PropertyDefinition> field = Optional.of(propertyDefintion);
		Mockito.when(controlDefinition.getField(Mockito.anyString())).thenReturn(field);
		File generatedFile = widgetToXlsxExporter.export(request);
		assertNotNull(generatedFile);
		generatedFile.deleteOnExit();
	}

	@Test
	public void exportDefaultPropertyValue() {
		File generatedFile = widgetToXlsxExporter.export(request);
		assertNotNull(generatedFile);
		generatedFile.deleteOnExit();
	}

	@Test
	public void extractValidValue() {
		assertEquals("", widgetToXlsxExporter.extractValidValue(null));
		assertEquals("some text", widgetToXlsxExporter.extractValidValue("some text"));
	}
}
