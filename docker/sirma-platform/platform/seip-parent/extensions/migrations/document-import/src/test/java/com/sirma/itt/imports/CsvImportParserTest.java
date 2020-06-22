package com.sirma.itt.imports;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.io.descriptor.ResourceFileDescriptor;
import com.sirma.itt.emf.util.EmfTest;

/**
 * The Class CsvImportParserTest.
 *
 * @author BBonev
 */
@Test
public class CsvImportParserTest extends EmfTest {

	/**
	 * Test parser.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void testParser() throws Exception {
		CsvImportParser parser = new CsvImportParser();
		ReflectionUtils.setFieldValue(parser, "typeConverter", createTypeConverter());
		Map<String, Serializable> properties = new LinkedHashMap<>();
		properties.put(DocumentProperties.FILE_LOCATOR, new ResourceFileDescriptor(
				"testENCImport.csv", CsvImportParserTest.class));
		Map<String, AnnotationEntry> pathMapping = new LinkedHashMap<>();
		Map<String, Instance> relations = new LinkedHashMap<>();
		Map<String, Instance> parsedFile = parser.parseFile(properties, pathMapping, relations);

		Assert.assertNotNull(parsedFile);
		Assert.assertFalse(parsedFile.isEmpty());

		Assert.assertTrue(parsedFile.containsKey("emf:0aba2e19-605c-4ae3-a998-5fbd0e5a06e5"));
		Assert.assertTrue(parsedFile.containsKey("emf:91aecaf1-f172-4eea-bf82-4de714d37871"));
		Assert.assertTrue(parsedFile.containsKey("emf:1520d7f2-feea-4078-9f7c-449b9ef57aee"));
		Assert.assertTrue(parsedFile.containsKey("emf:71512cb7-bf39-4ea3-8d13-a1ef760b587b"));

		Instance ecn = parsedFile.get("emf:0aba2e19-605c-4ae3-a998-5fbd0e5a06e5");
		Assert.assertTrue(ecn.getProperties().get("rdf:type") instanceof Uri);
		Assert.assertTrue(ecn.getProperties().get("emf:createdOn") instanceof Date);

		Serializable serializable = ecn.getProperties().get("pdm:mechanicalEngineerChecked");
		Assert.assertTrue(serializable instanceof Instance);
		Assert.assertNotNull(((Instance) serializable).getId());
	}

	/**
	 * Test with real data.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void testWithRealData() throws Exception {
		CsvImportParser parser = new CsvImportParser();
		ReflectionUtils.setFieldValue(parser, "typeConverter", createTypeConverter());
		Map<String, Serializable> properties = new LinkedHashMap<>();
		properties.put(DocumentProperties.FILE_LOCATOR, new ResourceFileDescriptor(
				"ECN25685_updated.csv", CsvImportParserTest.class));
		Map<String, AnnotationEntry> pathMapping = new LinkedHashMap<>();
		Map<String, Instance> relations = new LinkedHashMap<>();
		Map<String, Instance> parsedFile = parser.parseFile(properties, pathMapping, relations);

		Assert.assertNotNull(parsedFile);
		Assert.assertFalse(parsedFile.isEmpty());

		Instance ecn = parsedFile.get("emf:31501983-ae30-4e4a-976f-e52618d2f597");

		Assert.assertNotNull(ecn);
		Assert.assertTrue(ecn.getProperties().get("rdf:type") instanceof Uri);
		Assert.assertTrue(ecn.getProperties().get("emf:createdOn") instanceof Date);

		Serializable serializable = ecn.getProperties().get("pdm:mechanicalEngineerChecked");
		Assert.assertTrue(serializable instanceof Instance);
		Assert.assertNotNull(((Instance) serializable).getId());
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
		return converter;
	}
}
