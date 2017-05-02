package com.sirma.itt.seip.export;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.export.renders.DataTableWidget;
import com.sirma.itt.seip.export.renders.IdocRenderer;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.ContentNodeFactory;
import com.sirmaenterprise.sep.content.idoc.nodes.DefaultNodeBuilder;

/**
 * ContentToWordExporter class tests.
 *
 * @author Hristo Lungov
 */
public class ContentToWordExporterTest {

	private static final String EXPORT_FILE_NAME = "exportFileName";
	private static final String TARGET_ID = "targetId";
	private static final String TEST_HTML_FILE = "object-data-widget.html";
	private static final String EXPORT_TAB_ID = "test_tab_id1";

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
	@BeforeMethod
	public void setup() {
		DataTableWidget dataTableWidget = Mockito.mock(DataTableWidget.class);
		when(dataTableWidget.render(Matchers.anyString(), Matchers.any(ContentNode.class))).thenReturn(new Element(Tag.valueOf("table"), ""));
		when(dataTableWidget.accept(Matchers.any(ContentNode.class))).thenReturn(Boolean.TRUE);
		idocRenderers = new Plugins<>(IdocRenderer.PLUGIN_NAME, Arrays.asList(dataTableWidget));
		MockitoAnnotations.initMocks(this);
		ContentNodeFactory.getInstance().registerBuilder(new DefaultNodeBuilder());
	}

	/**
	 * Test method buildExportToWordCss scenario with empty file.
	 * @throws URISyntaxException 
	 */
	@Test
	public void buildExportToWordCssEmptyCssTest() throws URISyntaxException {
		GroupConverterContext context = mock(GroupConverterContext.class);
		File cssExternalfile = new File(getClass().getClassLoader().getResource("export/export-empty.css").toURI());
		Mockito.when(context.get(ContentToWordExporter.PATH_TO_EXPORT_TO_WORD_CSS_FILE)).thenReturn(cssExternalfile.getAbsolutePath());
		Assert.assertEquals(exporter.buildExportToWordCss(context), "");
	}
	
	/**
	 * Test method buildExportToWordCss scenario with directory.
	 * @throws URISyntaxException 
	 */
	@Test
	public void buildExportToWordCssWithDirectoryTest() throws URISyntaxException {
		GroupConverterContext context = mock(GroupConverterContext.class);
		File cssExternalfile = new File(getClass().getClassLoader().getResource("export/").toURI());
		Mockito.when(context.get(ContentToWordExporter.PATH_TO_EXPORT_TO_WORD_CSS_FILE)).thenReturn(cssExternalfile.getAbsolutePath());
		Assert.assertNull(exporter.buildExportToWordCss(context));
	}
	
	/**
	 * Test method buildExportToWordCss scenario with file.
	 * @throws URISyntaxException 
	 */
	@Test
	public void buildExportToWordCssTest() throws URISyntaxException {
		GroupConverterContext context = mock(GroupConverterContext.class);
		File cssExternalfile = new File(getClass().getClassLoader().getResource("export/export.css").toURI());
		Mockito.when(context.get(ContentToWordExporter.PATH_TO_EXPORT_TO_WORD_CSS_FILE)).thenReturn(cssExternalfile.getAbsolutePath());
		Assert.assertEquals(exporter.buildExportToWordCss(context), "<STYLE type=\"text/css\">same styles</STYLE>");
	}
	
	/**
	 * Test method buildExportToWordCss scenario file not exist.
	 */
	@Test
	public void buildExportToWordCssFileNotExistTest() {
		GroupConverterContext context = mock(GroupConverterContext.class);
		Mockito.when(context.get(ContentToWordExporter.PATH_TO_EXPORT_TO_WORD_CSS_FILE)).thenReturn("MissingFileName.txt");
		Assert.assertNull(exporter.buildExportToWordCss(context));
	}
	
	/**
	 * Test method buildExportToWordCss scenario without configuration.
	 */
	@Test
	public void buildExportToWordCssNotConfiguredTest() {
		GroupConverterContext context = mock(GroupConverterContext.class);
		Assert.assertNull(exporter.buildExportToWordCss(context));
	}
	
	/**
	 * Test export with no target id.
	 */
	@Test
	public void exportEmptyRequestTest() {
		ExportWord request = new ExportWord();
		Assert.assertNull(exporter.export(request));
	}

	/**
	 * Test export with not existing content.
	 */
	@Test
	public void exportNotExistingContentInfoTest() {
		ExportWord request = new ExportWord();
		request.setTargetId(TARGET_ID);
		Mockito.when(instanceContentService.getContent(TARGET_ID, Content.PRIMARY_VIEW)).thenReturn(ContentInfo.DO_NOT_EXIST);
		Assert.assertNull(exporter.export(request));
	}

	/**
	 * Test export with empty content.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void exportNoContentInfoTest() {
		ExportWord request = new ExportWord();
		request.setTargetId(TARGET_ID);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(contentInfo.getLength()).thenReturn(-1L);
		Mockito.when(instanceContentService.getContent(TARGET_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		Assert.assertNull(exporter.export(request));
	}

	/**
	 * Test export for Exception empty content.
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("boxing")
	@Test(expectedExceptions = { EmfRuntimeException.class })
	public void exportExceptionTest() throws IOException {
		ExportWord request = new ExportWord();
		request.setTargetId(TARGET_ID);
		request.setFileName(EXPORT_FILE_NAME);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(contentInfo.getLength()).thenReturn(1L);
		Mockito.when(instanceContentService.getContent(TARGET_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		Mockito.when(contentInfo.asString()).thenThrow(new IOException());
		File file = mock(File.class);
		when(file.exists()).thenReturn(false);
		ReflectionUtils.setField(file, "path", "/path/somePath");
		when(tempFileProvider.createLongLifeTempDir(anyString())).thenReturn(file);
		exporter.export(request);
		Assert.fail("Should not pass!!!");
	}

	/**
	 * Test export all content.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void exportTest() throws IOException {
		Mockito.when(exportToWordCss.computeIfNotSet(Matchers.any())).thenReturn("default css");
		ExportWord request = new ExportWord();
		request.setTargetId(TARGET_ID);
		request.setFileName(EXPORT_FILE_NAME);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(contentInfo.getLength()).thenReturn(1L);
		Mockito.when(instanceContentService.getContent(TARGET_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEST_HTML_FILE)) {
			Mockito.when(contentInfo.asString()).thenReturn(IOUtils.toString(is));
		}
		File targetDir = new File("target");
		when(tempFileProvider.createLongLifeTempDir(anyString())).thenReturn(targetDir);
		File exported = exporter.export(request);
		Assert.assertNotNull(exported);
		Assert.assertTrue(exported.canRead());
	}

	/**
	 * Test export tab content.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void exportTabTest() throws IOException {
		Mockito.when(exportToWordCss.computeIfNotSet(Matchers.any())).thenReturn("default css");
		ExportWord request = new ExportWord();
		request.setTargetId(TARGET_ID);
		request.setFileName(EXPORT_FILE_NAME);
		request.setTabId(EXPORT_TAB_ID);
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(contentInfo.getLength()).thenReturn(1L);
		Mockito.when(instanceContentService.getContent(TARGET_ID, Content.PRIMARY_VIEW)).thenReturn(contentInfo);
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEST_HTML_FILE)) {
			Mockito.when(contentInfo.asString()).thenReturn(IOUtils.toString(is));
		}
		File targetDir = new File("target");
		when(tempFileProvider.createLongLifeTempDir(anyString())).thenReturn(targetDir);
		File exported = exporter.export(request);
		Assert.assertNotNull(exported);
		Assert.assertTrue(exported.canRead());
	}
}
