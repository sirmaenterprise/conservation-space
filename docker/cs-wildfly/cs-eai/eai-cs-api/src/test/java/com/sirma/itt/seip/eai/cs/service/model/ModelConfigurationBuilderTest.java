package com.sirma.itt.seip.eai.cs.service.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.convert.TypeConverterUtilMock;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.util.XlsxModelParserTest;
import com.sirma.itt.seip.eai.util.XlsxModelParserTestUtil;

public class ModelConfigurationBuilderTest {

	private File testFile;

	@Before
	public void setUp() throws IOException {
		testFile = File.createTempFile("testxlsxmodels", "");
		testFile.mkdirs();
		testFile.delete();
		testFile.mkdirs();

		copy(XlsxModelParserTest.class.getResourceAsStream("types.xlsx"), new File(testFile, "types.xlsx"));
		copy(XlsxModelParserTest.class.getResourceAsStream("relations.xlsx"), new File(testFile, "relations.xlsx"));
		copy(XlsxModelParserTest.class.getResourceAsStream("common.xlsx"), new File(testFile, "common.xlsx"));
		copy(XlsxModelParserTest.class.getResourceAsStream("NGACO7001.xlsx"), new File(testFile, "NGACO7001.xlsx"));
		copy(XlsxModelParserTest.class.getResourceAsStream("NGACO7002.xlsx"), new File(testFile, "NGACO7002.xlsx"));
		TypeConverterUtilMock.setUpTypeConverter();
	}

	private void copy(InputStream resourceAsStream, File output) throws IOException {
		try (InputStream in = resourceAsStream; OutputStream out = new FileOutputStream(output)) {
			IOUtils.copy(in, out);
		}
	}

	@After
	public void tearDown() throws IOException {
		testFile.delete();
	}

	/**
	 * This test shows if the model configuration is sealed.
	 * 
	 * @throws EAIModelException
	 */
	@Test
	public void testValidData() throws EAIModelException {
		ModelConfiguration provideFromXlsx = ModelConfigurationBuilder.provideDataModelFromXlsx(testFile,
				XlsxModelParserTestUtil.provideXlsxPropertyMapping());
		Assert.assertFalse(provideFromXlsx.isSealed());
		provideFromXlsx.seal();
		Assert.assertTrue(provideFromXlsx.isSealed());
	}

	/**
	 * Test if the properties count is proper.
	 * 
	 * @throws EAIModelException
	 *             on parse error
	 */
	@Test
	public void testEntitiesCount() throws EAIModelException {
		ModelConfiguration provideFromXlsx = ModelConfigurationBuilder.provideDataModelFromXlsx(testFile,
				XlsxModelParserTestUtil.provideXlsxPropertyMapping());
		Assert.assertEquals(2, provideFromXlsx.getEntityTypes().size());
	}

	@Test
	public void testEntitiesModel() throws EAIModelException {
		ModelConfiguration provideFromXlsx = ModelConfigurationBuilder.provideDataModelFromXlsx(testFile,
				XlsxModelParserTestUtil.provideXlsxPropertyMapping());
		provideFromXlsx.seal();
		Assert.assertEquals("Title (local primary)",
				provideFromXlsx.getPropertyByExternalName("NGACO7001", "cultObj:title").getTitle());
		Assert.assertEquals("title",
				provideFromXlsx.getPropertyByExternalName("NGACO7001", "cultObj:title").getPropertyId());
		Assert.assertEquals("Title (local primary)",
				provideFromXlsx.getPropertyByExternalName("NGACO7002", "cultObj:title").getTitle());
	}
}
