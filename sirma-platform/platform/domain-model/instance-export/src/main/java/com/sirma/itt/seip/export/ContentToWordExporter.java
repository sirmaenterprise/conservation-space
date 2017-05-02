package com.sirma.itt.seip.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.docx4j.convert.in.xhtml.SepXHTMLImageHandler;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.export.renders.IdocRenderer;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.Idoc;
import com.sirmaenterprise.sep.content.idoc.SectionNode;

/**
 * The Class ContentToWordExporter.
 */
@ApplicationScoped
public class ContentToWordExporter implements WordExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String EXPORT_CSS = "export/export.css";
	private static String defaultExpotCss;

	static {
		try (InputStream in = ContentToWordExporter.class.getClassLoader().getResourceAsStream(EXPORT_CSS)) {
			String readFileToString = IOUtils.toString(in);
			defaultExpotCss = createStyleTag(readFileToString);
		} catch (IOException e) {
			LOGGER.error("Error loading export.css file", e);
		}
	}

	private static final String EXPORT = "export-docx";

	public static final String PATH_TO_EXPORT_TO_WORD_CSS_FILE = "system.export.to.word.css.file.path";

	@ConfigurationGroupDefinition(system = true, type = String.class, properties = { PATH_TO_EXPORT_TO_WORD_CSS_FILE })
	private static final String EXPORT_TO_WORD_CSS = "system.export.to.word.css";


	@ConfigurationPropertyDefinition(name = PATH_TO_EXPORT_TO_WORD_CSS_FILE,  system = true, label = "Path to file with css styles which have to be applayed to html before converting to word.")
	private static String pathToExternalExportToWordCssFile;

	@Inject
	@Configuration(EXPORT_TO_WORD_CSS)
	private ConfigurationProperty<String> exportToWordCss;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	@ExtensionPoint(IdocRenderer.PLUGIN_NAME)
	private Plugins<IdocRenderer> idocRenderers;

	@Override
	public File export(ExportWord request) {
		String targetId = request.getTargetId();
		if (StringUtils.isBlank(targetId)) {
			return null;
		}
		ContentInfo contentInfo = instanceContentService.getContent(targetId, Content.PRIMARY_VIEW);
		if (!contentInfo.exists()) {
			return null;
		}
		if (contentInfo.getLength() < 0L) {
			return null;
		}
		File exportDir = tempFileProvider.createLongLifeTempDir(EXPORT);
		String sectionId = request.getTabId();
		File file = new File(exportDir, UUID.randomUUID() + ".docx");
		try (FileOutputStream fos = new FileOutputStream(file)) {
			String idocContent = contentInfo.asString();
			Idoc document = Idoc.parse(idocContent);
			LOGGER.info("Start converting idoc content to ms-word");
			Document jsoupDocument = parseIdocContent(sectionId, targetId, document);
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
			NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
			wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
			ndp.unmarshalDefaultNumbering();
			XHTMLImporterImpl xhtmlImporter = new XHTMLImporterImpl(wordMLPackage);
			xhtmlImporter.setXHTMLImageHandler(new SepXHTMLImageHandler(xhtmlImporter, getEmbeddedImageLoader()));
			xhtmlImporter.setHyperlinkStyle("Hyperlink");
			// remove ugly elements cause by bug in UI but already saved in Idoc Content
			jsoupDocument.select("div[style~=^position: fixed; top: 0px; left: -1000px;$]").remove();
			jsoupDocument.select("div[style~=^left: -1000px; top: 0px; position: fixed;$]").remove();
			// This is a workaround for CMF-24010. When the image has width and height set for some wierd reason the
			// Docx4j library throws OutOfMemory for images that have explicit width and height set. This should not
			// affect the size of the images, because they are fit in the container.
			jsoupDocument.select("img").removeAttr("width").removeAttr("height");
			//remove align attribute from tables to prevent ugly look like into word after transformation.
			jsoupDocument.select("table").removeAttr("align");
			//Apply main css for export
			jsoupDocument.select("head").append(getExportToWordCss());
			// fix html from words/chars breaking transformation
			jsoupDocument.outputSettings().escapeMode(EscapeMode.xhtml);
			wordMLPackage.getMainDocumentPart().getContent().addAll(xhtmlImporter.convert(jsoupDocument.html(), null));
			wordMLPackage.save(fos);
			LOGGER.info("Finish converting idoc content to ms-word");
		} catch (IOException | Docx4JException | JAXBException e) {
			throw new EmfRuntimeException("Error during transformation of html to docx", e);
		}
		return file;
	}

	private Function<String, byte[]> getEmbeddedImageLoader() {
		InstanceContentService contentService = instanceContentService;
		return contentId -> {
			ContentInfo content = contentService.getContent(contentId, null);
			if (content.exists()) {
				try (InputStream stream = content.getInputStream()) {
					return IOUtils.toByteArray(stream);
				} catch (IOException e) {
					LOGGER.warn("Could not load content {}", contentId, e);
				}
			}
			return new byte[0];
		};
	}

	/**
	 * Go through each section and transforms the widgets to html tables.
	 *
	 * @param sectionId
	 *            the section id
	 * @param targetId
	 *            current target unique id
	 * @param document
	 *            the idoc document to parse
	 * @return the modified jsoup document
	 */
	private Document parseIdocContent(String sectionId, String targetId, Idoc document) {
		if (StringUtils.isNotBlank(sectionId)) {
			document.getSections().forEach(section -> parseOnlySelectedSection(sectionId, targetId, section));
		} else {
			document.getSections().forEach(section -> parseSection(targetId, section));
		}
		return document.getContent();
	}

	/**
	 * Parse only selected tab/section.
	 *
	 * @param sectionId
	 *            the section id.
	 * @param targetId
	 *            current target unique id
	 * @param section
	 *            node from idoc content
	 */
	private void parseOnlySelectedSection(String sectionId, String targetId, SectionNode section) {
		if (EqualsHelper.nullSafeEquals(section.getId(), sectionId)) {
			parseSection(targetId, section);
		} else {
			section.remove();
		}
	}

	/**
	 * Parses each section of Idoc.
	 *
	 * @param currentInstanceId
	 *            the current instance id
	 * @param section
	 *            the section
	 */
	private void parseSection(String currentInstanceId, SectionNode section) {
		// traverse the whole tree widget by widget
		section.layoutManagerNodes().forEach(invokeRenderForNodes(currentInstanceId));
		section.widgets().forEach(invokeRenderForNodes(currentInstanceId));
	}

	private Consumer<ContentNode> invokeRenderForNodes(String currentInstanceId) {
		return node -> idocRenderers.select(render -> render.accept(node)).ifPresent(
				render -> callIdocRenderer(render, currentInstanceId, node));
	}

	/**
	 * Call idoc renderer.
	 *
	 * @param iDocRenderer
	 *	 		the iDoc renderer
	 * @param currentInstanceId
	 * 			the current instance
	 * @param node
	 * 			the node.
	 */
	private static void callIdocRenderer(IdocRenderer iDocRenderer, String currentInstanceId, ContentNode node) {
		Element newElement = iDocRenderer.render(currentInstanceId, node);
		iDocRenderer.afterRender(newElement, node);
	}

	/**
	 * Fetch css style which have to be applied before converting to word. First will check if external file is configured, if
	 * yes this file will be loaded otherwise default one will be used.
	 * @return external css or default one.
	 */
	protected String getExportToWordCss() {
		return exportToWordCss.computeIfNotSet(() -> defaultExpotCss);
	}

	@ConfigurationConverter(EXPORT_TO_WORD_CSS)
	static String buildExportToWordCss(GroupConverterContext context) {
		String pathToExternalcssFile = context.get(PATH_TO_EXPORT_TO_WORD_CSS_FILE);
		if (StringUtils.isNotEmpty(pathToExternalcssFile)) {
			File externalCssFile = new File(pathToExternalcssFile);
			if (externalCssFile.exists() && externalCssFile.isFile()) {
				try (FileInputStream fileInputStream = new FileInputStream(externalCssFile);) {
					String readFileToString = IOUtils.toString(fileInputStream);
					return createStyleTag(readFileToString);
				} catch (IOException e) {
					LOGGER.error("Error loading external css file: " + pathToExternalcssFile, e);
				}
			}
		}
		return null;
	}

	/**
	 * Create style tag. For example:
	 *
	 * <pre>
	* &lt;STYLE type="text/css"&gt;
	*    <code>css</code>
	* &lt;/STYLE"&gt;
	 * </pre>
	 *
	 * @param css
	 *            to be added. For example:
	 *
	 *            <pre>
	 *     body {
	 *         width: 510px;
	 *         margin: 0 auto;
	 *         font-family: sans-serif, serif, monospace;
	 *         font-size: 12px;
	 *      }
	 *            </pre>
	 *
	 * @return created style tag or empty string if <code>css</code> is null or empty. For example:
	 *
	 *         <pre>
	 * &lt;STYLE type="text/css"&gt;
	 *    body {
	 *         width: 510px;
	 *         margin: 0 auto;
	 *         font-family: sans-serif, serif, monospace;
	 *         font-size: 12px;
	 *      }
	 * &lt;/STYLE"&gt;
	 *         </pre>
	 */
	private static String createStyleTag(String css) {
		if (StringUtils.isBlank(css)) {
			return "";
		}
		return new StringBuilder().append("<STYLE type=\"text/css\">").append(css).append("</STYLE>").toString();
	}

}