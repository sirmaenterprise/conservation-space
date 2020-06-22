package com.sirma.itt.imports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.evaluation.BaseEvaluatorTest;
import com.sirma.itt.emf.evaluation.ExpressionEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionEvaluatorManager;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.io.descriptor.ResourceFileDescriptor;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.imports.converters.EmfInstanceConverterProvider;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;

/**
 * The Class HtmlAnnotationParserTest.
 *
 * @author BBonev
 */
@Test
public class HtmlAnnotationParserTest extends BaseEvaluatorTest {

	/**
	 * Test annotations.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	public void testAnnotations() throws Exception {
		Map<String, AnnotationEntry> pathMapping = parseMappingFile("testENCImport.csv");

		Assert.assertFalse(pathMapping.isEmpty());

		HtmlAnnotationParser annotationParser = new HtmlAnnotationParser();
		ReflectionUtils.setFieldValue(annotationParser, "typeConverter", createTypeConverter());
		ReflectionUtils.setFieldValue(annotationParser, "expressionsManager", createManager());
		InstanceService<Instance, DefinitionModel> instanceService = Mockito
				.mock(InstanceService.class);
		Mockito.when(
				instanceService.save(Mockito.any(Instance.class), Mockito.any(Operation.class)))
				.thenAnswer(new Answer<Instance>() {

					@Override
					public Instance answer(InvocationOnMock invocation) throws Throwable {
						return (Instance) invocation.getArguments()[0];
					}
				});
		ReflectionUtils.setFieldValue(annotationParser, "instanceService", instanceService);

		Map<String, Serializable> htmlProperties = new LinkedHashMap<>();
		htmlProperties.put(DocumentProperties.FILE_LOCATOR, new ResourceFileDescriptor(
				"testENCImport.htm", HtmlAnnotationParserTest.class));

		Instance instance = annotationParser.createAnnotation(null, pathMapping, htmlProperties);

		Assert.assertNotNull(instance);
		Serializable serializable = instance.getProperties().get(DocumentProperties.FILE_LOCATOR);
		Assert.assertTrue(serializable instanceof FileDescriptor);

		FileDescriptor descriptor = (FileDescriptor) serializable;
		InputStream download = descriptor.getInputStream();
		Assert.assertNotNull(download);
		// saveToFile(download, "testENCImport_output.htm");
	}

	/**
	 * Save to file.
	 *
	 * @param download
	 *            the download
	 * @param path
	 *            the path
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void saveToFile(InputStream download, String path) throws FileNotFoundException, IOException {
		File file = new File(path);
		FileOutputStream stream = new FileOutputStream(file);
		IOUtils.copyLarge(download, stream);

		download.close();
		stream.close();
	}

	/**
	 * Test annotations.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void testAnnotationsWithRealData() throws Exception {
		processFile("ECN25685_updated.csv", "ECN25685_old.htm", false);
		processFile("Tr_ECN23985.csv", "ECN23985.htm", false);
		processFile("Tr_ECN25685.csv", "ECN25685.html", false);
	}

	/**
	 * Process file.
	 * 
	 * @param inputFile
	 *            the input file
	 * @param inputHtm
	 *            the input htm
	 * @param writeOutputFile
	 *            the write output file
	 * @throws Exception
	 *             the exception
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void processFile(String inputFile, String inputHtm, boolean writeOutputFile) throws Exception, FileNotFoundException, IOException {
		Map<String, AnnotationEntry> pathMapping = parseMappingFile(inputFile);

		Assert.assertFalse(pathMapping.isEmpty());

		HtmlAnnotationParser annotationParser = new HtmlAnnotationParser();
		ReflectionUtils.setFieldValue(annotationParser, "typeConverter", createTypeConverter());
		ReflectionUtils.setFieldValue(annotationParser, "expressionsManager", createManager());
		InstanceService<Instance, DefinitionModel> instanceService = Mockito
				.mock(InstanceService.class);
		Mockito.when(
				instanceService.save(Mockito.any(Instance.class), Mockito.any(Operation.class)))
				.thenAnswer(new Answer<Instance>() {

					@Override
					public Instance answer(InvocationOnMock invocation) throws Throwable {
						return (Instance) invocation.getArguments()[0];
					}
				});
		ReflectionUtils.setFieldValue(annotationParser, "instanceService", instanceService);

		Map<String, Serializable> htmlProperties = new LinkedHashMap<>();
		htmlProperties.put(DocumentProperties.FILE_LOCATOR, new ResourceFileDescriptor(
				inputHtm, HtmlAnnotationParserTest.class));

		Instance instance = annotationParser.createAnnotation(null, pathMapping, htmlProperties);

		Assert.assertNotNull(instance);
		Serializable serializable = instance.getProperties().get(DocumentProperties.FILE_LOCATOR);
		Assert.assertTrue(serializable instanceof FileDescriptor);

		FileDescriptor descriptor = (FileDescriptor) serializable;
		InputStream download = descriptor.getInputStream();
		Assert.assertNotNull(download);
		if (writeOutputFile) {
			File file = new File(inputHtm);
			String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
			String ext = file.getName().substring(file.getName().lastIndexOf('.'),
					file.getName().length());
			saveToFile(download, name + "_annotated" + ext);
		}
	}

	/**
	 * Parses the mapping file.
	 *
	 * @param name
	 *            the name
	 * @return the map
	 * @throws Exception
	 *             the exception
	 */
	private Map<String, AnnotationEntry> parseMappingFile(String name)
			throws Exception {
		CsvImportParser parser = new CsvImportParser();
		ReflectionUtils.setFieldValue(parser, "typeConverter", createTypeConverter());
		Map<String, Serializable> properties = new LinkedHashMap<>();
		properties.put(DocumentProperties.FILE_LOCATOR, new ResourceFileDescriptor(name,
				CsvImportParserTest.class));
		Map<String, AnnotationEntry> pathMapping = new LinkedHashMap<>();
		Map<String, Instance> relations = new LinkedHashMap<>();
		Map<String, Instance> parsedFile = parser.parseFile(properties, pathMapping, relations);
		return pathMapping;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TypeConverter createTypeConverter() {
		TypeConverter converter = super.createTypeConverter();
		converter.addConverter(String.class, Uri.class, new Converter<String, Uri>() {
			@Override
			public Uri convert(String source) {
				final String[] split = source.split(":");
				return new Uri() {

					@Override
					public String getNamespace() {
						return split[0];
					}

					@Override
					public String getLocalName() {
						return split[1];
					}
				};
			}
		});

		EmfInstanceConverterProvider provider = new EmfInstanceConverterProvider();
		DictionaryService service = Mockito.mock(DictionaryService.class);
		ObjectDefinition definition = Mockito.mock(ObjectDefinition.class);
		Mockito.when(definition.getRevision()).thenReturn(1L);
		Mockito.when(service.getDefinition(Mockito.eq(ObjectDefinition.class), Mockito.anyString()))
				.thenReturn(definition);
		ReflectionUtils.setFieldValue(provider, "dictionaryService", service);
		provider.register(converter);
		return converter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionEvaluatorManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		list.add(initEvaluator(new InstanceLinkExpressionEvaluator(), manager, converter));
		list.add(initEvaluator(new UserLinkExpressionEvaluator(), manager, converter));
		return list;
	}
}
