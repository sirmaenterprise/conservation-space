package com.sirma.sep.template.patches;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.content.ContentAdapterService;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.template.TemplateServiceImplTest;
import com.sirma.itt.seip.template.db.TemplateContentEntity;
import com.sirma.itt.seip.template.db.TemplateEntity;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;

import liquibase.exception.CustomChangeException;

import org.mockito.stubbing.OngoingStubbing;

/**
 * Tests {@link TemplatesContentsMigratePatchTest}.
 *
 * @author Vilizar Tsonev
 */
public class TemplatesContentsMigratePatchTest {

	@InjectMocks
	private TemplatesContentMigratePatch patch;

	@Mock
	private DbDao dbDao;

	@Mock
	private ContentAdapterService adapterService;

	@Mock
	private DefinitionCompilerHelper compilerHelper;

	@Mock
	private RESTClient restClient;

	@Mock
	private TempFileProvider tempFileProvider;

	@Mock
	private ContentAdapterService contentAdapterService;

	@Spy
	private TransactionSupportFake transactionSupport;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private static final String ALFRESCO_HELP_TEMPLATE_NODE = "{\"item\" : { \"fileName\" : \"helptemplate_valid.xml\" }}";

	private static final String ALFRESCO_TEXTILE_TEMPLATE_NODE = "{\"item\" : { \"fileName\" : \"textilestemplate_valid.xml\" }}";

	private static final String ALFRESCO_EMPTY_NODE = "{\"item\" : {}}";

	private InputStream stream1;

	private InputStream stream2;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		withExistingDummyTemplate("test");
	}

	@After
	public void cleanup() throws IOException {
		if (stream1 != null) {
			stream1.close();
		}
		if (stream2 != null) {
			stream2.close();
		}
	}

	@Test
	public void should_Correctly_Persist_Template_Contents()
			throws CustomChangeException, DMSClientException, IOException {
		withTemplateIds(new String[] { "helptemplate", "sampleDmsId" }, new String[] { "basetextilestemplate", "sampleDmsId" });
		withDmsNodes(ALFRESCO_HELP_TEMPLATE_NODE, ALFRESCO_TEXTILE_TEMPLATE_NODE);

		withTemplateDefinitions("helptemplate_valid.xml", "textilestemplate_valid.xml");

		TemplateContentEntity expectedEntity1 = new TemplateContentEntity();
		expectedEntity1.setId("helptemplate");
		expectedEntity1.setContent("Help template sample content");
		expectedEntity1.setFileName("helptemplate_valid.xml");

		TemplateContentEntity expectedEntity2 = new TemplateContentEntity();
		expectedEntity2.setId("basetextilestemplate");
		expectedEntity2.setContent("Sample content");
		expectedEntity2.setFileName("textilestemplate_valid.xml");

		patch.execute(null);

		ArgumentCaptor<TemplateContentEntity> captor = ArgumentCaptor.forClass(TemplateContentEntity.class);
		verify(dbDao, times(2)).saveOrUpdate(captor.capture());

		List<TemplateContentEntity> capturedEntities = captor.getAllValues();
		TemplateContentEntity actualEntity1 = capturedEntities.get(0);
		TemplateContentEntity actualEntity2 = capturedEntities.get(1);

		assertEquals(expectedEntity1, actualEntity1);
		assertEquals(expectedEntity2, actualEntity2);
	}

	@Test
	public void shouldSkipMigrationForEmptyContent() throws DMSClientException, IOException, CustomChangeException {
		withTemplateIds(new String[] { "template_with_missing_content", "sampleDmsId" });
		withDmsNodes(ALFRESCO_HELP_TEMPLATE_NODE);
		withTemplateDefinitions("template_with_missing_content.xml", "textilestemplate_valid.xml");

		verifyNoTemplateSaving();
	}

	@Test
	public void shouldSkipMigrationForIncorrectContent() throws DMSClientException, IOException, CustomChangeException {
		withTemplateIds(new String[] { "template_with_missing_content", "sampleDmsId" });
		withDmsNodes(ALFRESCO_HELP_TEMPLATE_NODE);
		withTemplateDefinitions("invalid_template_definition.html", "textilestemplate_valid.xml");

		verifyNoTemplateSaving();
	}

	@Test
	public void shouldSkipMigrationForMissingRemoteDMSId() throws DMSClientException, IOException,
			CustomChangeException {
		withTemplateIds(new String[] { "helptemplate", "" });
		withDmsNodes(ALFRESCO_HELP_TEMPLATE_NODE);
		withTemplateDefinitions("helptemplate_valid.xml", "textilestemplate_valid.xml");

		verifyNoTemplateSaving();
	}

	private void verifyNoTemplateSaving() throws CustomChangeException {
		patch.execute(null);
		verify(dbDao, times(0)).saveOrUpdate(any());
	}

	@Test(expected = EmfApplicationException.class)
	public void should_Throw_Exception_If_Null_DMS_Node_Is_Returned()
			throws CustomChangeException, DMSClientException {
		withTemplateIds(new String[] { "template1", "sampleDmsId" }, new String[] { "template2", "sampleDmsId" });
		withDmsNodes(null, null);

		patch.execute(null);
	}

	@Test(expected = EmfApplicationException.class)
	public void should_Throw_Exception_If_Template_FileName_Cannot_Be_Extracted()
			throws CustomChangeException, DMSClientException {
		withTemplateIds(new String[] { "template1", "sampleDmsId" },
				new String[] { "template2", "sampleDmsId" });
		withDmsNodes(ALFRESCO_EMPTY_NODE, ALFRESCO_EMPTY_NODE);

		patch.execute(null);
	}

	@Test(expected = EmfApplicationException.class)
	public void should_Throw_Exception_If_Template_Is_Missing_A_Content_Field()
			throws CustomChangeException, DMSClientException, IOException {
		withTemplateIds(new String[] { "template1", "sampleDmsId" },
				new String[] { "template2", "sampleDmsId" });
		withDmsNodes(ALFRESCO_HELP_TEMPLATE_NODE, ALFRESCO_TEXTILE_TEMPLATE_NODE);

		withTemplateDefinitions("helptemplate_missing_content_field.xml", "textilestemplate_valid.xml");

		patch.execute(null);
	}

	@Test(expected = EmfApplicationException.class)
	public void should_Interrupt_Migration_If_Error_Occurs_While_Retrieving_Filename_Descriptor()
			throws DMSClientException, CustomChangeException {
		withTemplateIds(new String[] { "template1", "failingNode" }, new String[] { "template2", "failingNode" });

		doThrow(DMSClientException.class).when(restClient).request(anyString(), any(HttpMethod.class));
		patch.execute(null);
	}

	@SuppressWarnings("unchecked")
	private void withTemplateIds(String[]... ids) {
		when(dbDao.fetchWithNative(anyString(), anyList())).thenReturn(Arrays.asList(ids));
	}

	private Template withExistingDummyTemplate(String identifier) {
		Template dummyTemplate = new Template();
		dummyTemplate.setId(identifier);
		return dummyTemplate;
	}

	private void withDmsNodes(String... nodes) throws DMSClientException {
		OngoingStubbing<String> stub = when(
				restClient.request(eq(ServiceURIRegistry.NODE_DETAILS + "sampleDmsId"), any(HttpMethod.class)));
		for (String node : nodes) {
			stub = stub.thenReturn(node);
		}
	}

	private void withTemplateDefinitions(String fileName1, String fileName2) throws IOException, DMSClientException {
		File sourceFile1 = createTempFile(fileName1);
		File sourceFile2 = createTempFile(fileName2);

		stream1 = new FileInputStream(sourceFile1);
		stream2 = new FileInputStream(sourceFile2);
		// mock the 'remote' template files downloaded from the DMS. These are the input files under test.
		when(restClient.request(any(HttpMethod.class), anyString())).thenReturn(stream1, stream2);

		// create empty destination files where the downloaded from DMS file streams will write their contents
		File destinationFile1 = tempFolder.newFile("file1.xml");
		File destinationFile2 = tempFolder.newFile("file2.xml");
		when(tempFileProvider.createTempFile(anyString(), any())).thenReturn(destinationFile1, destinationFile2);
	}

	private File createTempFile(String fileName) throws IOException {
		File originalFile = new File(TemplateServiceImplTest.class
				.getClassLoader()
				.getResource(
						TemplateServiceImplTest.class.getPackage().getName().replace('.', '/') + "/" + fileName)
				.getFile());
		// since the TemplateImportService cleans up the files after use, create temporary files for the test
		File temporaryTestFile = tempFolder.newFile(fileName);
		FileUtils.copyFile(originalFile, temporaryTestFile);
		return temporaryTestFile;
	}
}
