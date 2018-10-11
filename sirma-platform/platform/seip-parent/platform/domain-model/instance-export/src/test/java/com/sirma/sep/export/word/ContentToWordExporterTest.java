package com.sirma.sep.export.word;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeFactory;
import com.sirma.sep.content.idoc.nodes.DefaultNodeBuilder;
import com.sirma.sep.export.ContentExportException;
import com.sirma.sep.export.renders.DataTableWidgetRenderer;
import com.sirma.sep.export.renders.IdocRenderer;
import com.sirma.sep.export.renders.utils.TestExportExcelUtil;
import com.sirma.sep.export.word.WordExportRequest.WordExportRequestBuilder;

/**
 * ContentToWordExporter class tests.
 *
 * @author Hristo Lungov
 */
@SuppressWarnings("static-method")
public class ContentToWordExporterTest {

	private static final String EXPORT_FILE_NAME = "exportFileName";
	private static final String TARGET_ID = "targetId";
	private static final String TEST_HTML_FILE = "object-data-widget.html";
	private static final String EXPORT_TAB_ID = "test_tab_id1";
	private static final String PATH_TO_EXPORT_TO_WORD_CSS_FILE = "system.export.to.word.css.file.path";

	@InjectMocks
	private ContentToWordExporter exporter;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private TempFileProvider tempFileProvider;

	@Mock
	private ConfigurationProperty<String> exportToWordCss;

	@Spy
	private Plugins<IdocRenderer> idocRenderers;

	/**
	 * Runs before each method and setup mockito.
	 */
	@Before
	public void setup() {
		DataTableWidgetRenderer dataTableWidget = Mockito.mock(DataTableWidgetRenderer.class);
		when(dataTableWidget.render(Matchers.anyString(), Matchers.any(ContentNode.class)))
				.thenReturn(new Element(Tag.valueOf("table"), ""));
		when(dataTableWidget.accept(Matchers.any(ContentNode.class))).thenReturn(Boolean.TRUE);
		idocRenderers = new Plugins<>(IdocRenderer.PLUGIN_NAME, Arrays.asList(dataTableWidget));
		MockitoAnnotations.initMocks(this);
		ContentNodeFactory.getInstance().registerBuilder(new DefaultNodeBuilder());
	}

	/**
	 * Test method buildExportToWordCss scenario with empty file.
	 *
	 * @throws URISyntaxException
	 */
	@Test
	public void buildExportToWordCssEmptyCssTest() throws URISyntaxException {
		GroupConverterContext context = mock(GroupConverterContext.class);
		File cssExternalfile = new File(getClass().getClassLoader().getResource("export/export-empty.css").toURI());
		Mockito.when(context.get(PATH_TO_EXPORT_TO_WORD_CSS_FILE)).thenReturn(cssExternalfile.getAbsolutePath());
		assertEquals("", ContentToWordExporter.buildExportToWordCss(context));
	}

	/**
	 * Test method buildExportToWordCss scenario with directory.
	 *
	 * @throws URISyntaxException
	 */
	@Test
	public void buildExportToWordCssWithDirectoryTest() throws URISyntaxException {
		GroupConverterContext context = mock(GroupConverterContext.class);
		File cssExternalfile = new File(getClass().getClassLoader().getResource("export/").toURI());
		Mockito.when(context.get(PATH_TO_EXPORT_TO_WORD_CSS_FILE)).thenReturn(cssExternalfile.getAbsolutePath());
		assertNull(ContentToWordExporter.buildExportToWordCss(context));
	}

	/**
	 * Test method buildExportToWordCss scenario with file.
	 *
	 * @throws URISyntaxException
	 */
	@Test
	public void buildExportToWordCssTest() throws URISyntaxException {
		GroupConverterContext context = mock(GroupConverterContext.class);
		File cssExternalfile = new File(getClass().getClassLoader().getResource("export/export.css").toURI());
		Mockito.when(context.get(PATH_TO_EXPORT_TO_WORD_CSS_FILE)).thenReturn(cssExternalfile.getAbsolutePath());
		assertEquals("<STYLE type=\"text/css\">same styles</STYLE>",
				ContentToWordExporter.buildExportToWordCss(context));
	}

	/**
	 * Test method buildExportToWordCss scenario file not exist.
	 */
	@Test
	public void buildExportToWordCssFileNotExistTest() {
		GroupConverterContext context = mock(GroupConverterContext.class);
		Mockito.when(context.get(PATH_TO_EXPORT_TO_WORD_CSS_FILE)).thenReturn("MissingFileName.txt");
		assertNull(ContentToWordExporter.buildExportToWordCss(context));
	}

	/**
	 * Test method buildExportToWordCss scenario without configuration.
	 */
	@Test
	public void buildExportToWordCssNotConfiguredTest() {
		GroupConverterContext context = mock(GroupConverterContext.class);
		assertNull(ContentToWordExporter.buildExportToWordCss(context));
	}

	/**
	 * Test export with no target id.
	 */
	@Test
	public void exportEmptyRequestTest() throws ContentExportException {
		WordExportRequest request = new WordExportRequestBuilder().setInstanceId("").buildRequest();
		assertFalse(exporter.export(request).isPresent());
	}

	/**
	 * Test export with not existing content.
	 */
	@Test
	public void exportNotExistingContentInfoTest() throws ContentExportException {
		WordExportRequest request = new WordExportRequestBuilder().setInstanceId(TARGET_ID).buildRequest();
		Mockito.when(instanceContentService.getContent(TARGET_ID, Content.PRIMARY_VIEW)).thenReturn(
				ContentInfo.DO_NOT_EXIST);
		assertFalse(exporter.export(request).isPresent());
	}

	/**
	 * Test export with empty content.
	 */
	@Test
	@SuppressWarnings("boxing")
	public void exportNoContentInfoTest() throws ContentExportException {
		WordExportRequest request = new WordExportRequestBuilder().setInstanceId(TARGET_ID).buildRequest();
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(contentInfo.getLength()).thenReturn(-1L);
		Mockito.when(instanceContentService.getContent(TARGET_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		assertFalse(exporter.export(request).isPresent());
	}

	/**
	 * Test export for Exception empty content.
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("boxing")
	@Test(expected = ContentExportException.class)
	public void exportExceptionTest() throws IOException, ContentExportException {
		WordExportRequest request = new WordExportRequestBuilder()
				.setInstanceId(TARGET_ID)
					.setFileName(EXPORT_FILE_NAME)
					.buildRequest();

		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(contentInfo.getLength()).thenReturn(1L);
		Mockito.when(instanceContentService.getContent(TARGET_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		Mockito.when(contentInfo.asString()).thenThrow(new IOException());
		File file = mock(File.class);
		when(file.exists()).thenReturn(false);
		ReflectionUtils.setFieldValue(file, "path", "/path/somePath");
		when(tempFileProvider.createLongLifeTempDir(anyString())).thenReturn(file);
		exporter.export(request);
	}

	/**
	 * Test export all content.
	 */
	@Test
	@SuppressWarnings("boxing")
	public void exportTest() throws IOException, ContentExportException {
		Optional<File> exported = Optional.empty();
		try {
			Mockito.when(exportToWordCss.computeIfNotSet(Matchers.any())).thenReturn("default css");
			WordExportRequest request = new WordExportRequestBuilder()
					.setInstanceId(TARGET_ID)
						.setFileName(EXPORT_FILE_NAME)
						.buildRequest();

			ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
			Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
			Mockito.when(contentInfo.getLength()).thenReturn(1L);
			Mockito.when(instanceContentService.getContent(TARGET_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
			try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEST_HTML_FILE)) {
				Mockito.when(contentInfo.asString()).thenReturn(IOUtils.toString(is));
			}
			File targetDir = new File("target");
			when(tempFileProvider.createLongLifeTempDir(anyString())).thenReturn(targetDir);
			exported = exporter.export(request);
			assertTrue(exported.isPresent());
			assertTrue(exported.get().canRead());
		} finally {
			if (exported.isPresent()) {
				TestExportExcelUtil.deleteFile(exported.get());
			}
		}
	}

	/**
	 * Test export tab content.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void exportTabTest() throws IOException, ContentExportException {
		Optional<File> exported = Optional.empty();
		try {
			Mockito.when(exportToWordCss.computeIfNotSet(Matchers.any())).thenReturn("default css");
			WordExportRequest request = new WordExportRequestBuilder()
					.setInstanceId(TARGET_ID)
						.setFileName(EXPORT_FILE_NAME)
						.setTabId(EXPORT_TAB_ID)
						.buildRequest();
			ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
			Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
			Mockito.when(contentInfo.getLength()).thenReturn(1L);
			Mockito.when(instanceContentService.getContent(TARGET_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
			try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEST_HTML_FILE)) {
				Mockito.when(contentInfo.asString()).thenReturn(IOUtils.toString(is));
			}
			File targetDir = new File("target");
			when(tempFileProvider.createLongLifeTempDir(anyString())).thenReturn(targetDir);
			exported = exporter.export(request);
			assertTrue(exported.isPresent());
			assertTrue(exported.get().canRead());
		} finally {
			if (exported.isPresent()) {
				TestExportExcelUtil.deleteFile(exported.get());
			}
		}
	}

	@Test
	public void getName() {
		assertEquals("word", exporter.getName());
	}

}
