package com.sirma.sep.export.word;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import com.sirma.sep.content.idoc.sanitizer.IdocSanitizer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.docx4j.convert.in.xhtml.XHTMLImageHandlerImpl;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;
import com.sirma.sep.export.ContentExportException;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.FileExporter;
import com.sirma.sep.export.SupportedExportFormats;
import com.sirma.sep.export.renders.IdocRenderer;

/**
 * Provides means for exporting HTML content to MS Word document. The implementation uses third party library (docx4j)
 * to create and convert the passed HTML to word document. <br>
 * This implementation is extension to {@link FileExporter} plug-in and should be used through specified service for the
 * export - {@link ExportService}. <br>
 * The process of exporting will retrieve the view content for the requested object/instance, then it will use our
 * {@link Idoc} API to process the HTML, before passing it to the external library that creates the word document. There
 * are additional adjustments to CSS styles so that the generated document to look like the content displayed in the web
 * page for that object/instance. This CSS styles are passed externally through configuration, which contains the path
 * the file with those styles.
 *
 * @author A. Kunchev
 */
@Extension(target = FileExporter.PLUGIN_NAME, order = 20)
public class ContentToWordExporter implements FileExporter<WordExportRequest> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String EXPORT_CSS = "export/export.css";
	private static final String EXPORT = "export-docx";
	private static final String PATH_TO_EXPORT_TO_WORD_CSS_FILE = "system.export.to.word.css.file.path";
	private static final String IMAGE = "img";
	private static final String STYLE_ATTR = "style";

	/**
	 * This is a regex pattern that matches only hexadecimal characters. More info can be found here
	 * http://www.regular-expressions.info/nonprint.html
	 */
	private static final Pattern PATTERN_XML_INVALID_CHARS = Pattern.compile("[\\c@-\\c_]");
	private static final Pattern LINE_HEIGHT_STYLE = Pattern.compile("line-height:\\s?([.\\d]+);");
	private static final Pattern DISPLAY_INLINE_STYLE = Pattern.compile("display:\\s*inline;");

	private static String defaultExportCss;

	static {
		ContentToWordExporter.addFontMappings();
		try (InputStream in = ContentToWordExporter.class.getClassLoader().getResourceAsStream(EXPORT_CSS)) {
			String readFileToString = IOUtils.toString(in);
			defaultExportCss = createStyleTag(readFileToString);
		} catch (IOException e) {
			LOGGER.error("Error loading export.css file", e);
		}
	}

	@ConfigurationGroupDefinition(system = true, type = String.class, properties = { PATH_TO_EXPORT_TO_WORD_CSS_FILE })
	private static final String EXPORT_TO_WORD_CSS = "system.export.to.word.css";

	@ConfigurationPropertyDefinition(name = PATH_TO_EXPORT_TO_WORD_CSS_FILE, system = true, label = "Path to file with css styles which have to be applayed to html before converting to word.")
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
	public Optional<File> export(WordExportRequest request) throws ContentExportException {
		String targetId = (String) request.getInstanceId();
		if (StringUtils.isBlank(targetId)) {
			LOGGER.warn("Missing instance id. Export to word will not produce result.");
			return Optional.empty();
		}

		ContentInfo contentInfo = instanceContentService.getContent(targetId, Content.PRIMARY_VIEW);
		if (!contentInfo.exists()) {
			LOGGER.warn("Failed to retrieve view content for the instance - {}, word export will not produce result.",
					targetId);
			return Optional.empty();
		}

		if (contentInfo.getLength() < 0L) {
			LOGGER.warn("The retrieved view content for instance - {} is empty, word export will not produce result.",
					targetId);
			return Optional.empty();
		}

		File file = getFile(request);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			LOGGER.info("Start converting idoc content to ms-word");
			WordprocessingMLPackage wordMLPackage = getPackage();
			XHTMLImporterImpl xhtmlImporter = new XHTMLImporterImpl(wordMLPackage);
			xhtmlImporter.setXHTMLImageHandler(new XHTMLImageHandlerImpl(xhtmlImporter, getEmbeddedImageLoader()));
			xhtmlImporter.setHyperlinkStyle("Text");
			String html = processIdocContent(request, contentInfo);
			String fixedContent = PATTERN_XML_INVALID_CHARS.matcher(html).replaceAll("");
			wordMLPackage.getMainDocumentPart().getContent().addAll(xhtmlImporter.convert(fixedContent, null));
			wordMLPackage.save(fos);
			LOGGER.info("Finish converting idoc content to ms-word");
			return Optional.of(file);
		} catch (IOException | Docx4JException | JAXBException e) {
			throw new ContentExportException("Error during transformation of html to docx", e);
		}
	}

	private File getFile(WordExportRequest request) {
		File exportDir = tempFileProvider.createLongLifeTempDir(EXPORT);
		String fileName = EqualsHelper.getOrDefault(request.getFileName(), UUID.randomUUID().toString());
		return new File(exportDir, fileName + ".docx");
	}

	private static WordprocessingMLPackage getPackage() throws InvalidFormatException, JAXBException {
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		return wordMLPackage;
	}

	private String processIdocContent(WordExportRequest request, ContentInfo contentInfo) throws IOException {
		// part of CMF-28469, already tainted documents need to be sanitized so no content is lost on export.
		Idoc document = new Idoc(IdocSanitizer.sanitizeLayouts(Jsoup.parseBodyFragment(contentInfo.asString())));
		Document jsoupDocument = parseIdocContent(request.getTabId(), (String) request.getInstanceId(), document);
		// remove ugly elements cause by bug in UI but already saved in Idoc Content
		jsoupDocument.select("div[style~=^position: fixed; top: 0px; left: -1000px;$]").remove();
		jsoupDocument.select("div[style~=^left: -1000px; top: 0px; position: fixed;$]").remove();
		// This is a workaround for CMF-24010. When the image has width and height set for some wierd reason the
		// Docx4j library throws OutOfMemory for images that have explicit width and height set. This should not
		// affect the size of the images, because they are fit in the container.
		jsoupDocument.select(IMAGE).removeAttr("width").removeAttr("height");
		// remove display: inline from inline styling since its not supported in word, see CMF-28366
		jsoupDocument.select(IMAGE).forEach(item -> {
			if (item.hasAttr(STYLE_ATTR)) {
				String styles = item.attr(STYLE_ATTR);
				Matcher displayInlineMatcher = DISPLAY_INLINE_STYLE.matcher(styles);
				if (displayInlineMatcher.find()) {
					item.attr(STYLE_ATTR, styles.replaceFirst(DISPLAY_INLINE_STYLE.pattern() , ""));
				}
			}
		});
		jsoupDocument.select("a").forEach(anchor -> {
			if (!anchor.children().isEmpty() && anchor.children().get(0).tagName() == IMAGE) {
				anchor.unwrap();
			}
		});
		// remove align attribute from tables to prevent ugly look like into word after transformation.
		jsoupDocument.select("table").removeAttr("align");
		// Apply main css for export
		jsoupDocument.select("head").append(getExportToWordCss());
		processInfoBoxes(jsoupDocument);
		resizeImagesInTables(jsoupDocument);
		// Doc4j create broken word document if html has empty row ("<tr></tr>").
		// So such rows have to be fixed.
		fixEmptyTableRow(jsoupDocument);
		processPageBreaks(jsoupDocument);
		processParagraphLineHeight(jsoupDocument);
		processListItemParagraphLineHeight(jsoupDocument);
		processBreakRows(jsoupDocument);
		// fix html from words/chars breaking transformation
		jsoupDocument.outputSettings().escapeMode(EscapeMode.xhtml);
		return jsoupDocument.html();
	}

	/**
	 * Go through each section and transforms the widgets to html tables.
	 *
	 * @param sectionId
	 *            the section id
	 * @param targetId
	 *            current target unique id
	 * @param document
	 *
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

	/**
	 * Processes the info-boxes. Replaces the divs with background because they aren't supported.
	 *
	 * @param document
	 *            the document with info boxes.
	 */
	private static void processInfoBoxes(Document document) {
		document.select("div.info-widget").forEach(infoWidget -> {
			Elements infoWidgetContent = infoWidget.children();
			String infoWidgetClass = infoWidget.attr("class");
			String infoWidgetContentHtml = infoWidgetContent.html();
			infoWidget.after("<span class='" + infoWidgetClass + "'><label class='info-content'>"
					+ infoWidgetContentHtml + "</label></span>");
			infoWidget.remove();
		});
	}

	private static void resizeImagesInTables(Document document) {
		document.select("td>img").stream().filter(element -> !element.hasAttr("style")).forEach(element ->
				// Pasted images doesn't have style element and the width should be set explicitly.
				element.attr("width", "75%"));
	}

	/**
	 * Searching for empty row ("
	 * <tr>
	 * </tr>
	 * ") and add child to it.
	 *
	 * @param document
	 *            - the document to be fixed.
	 */
	private static void fixEmptyTableRow(Document document) {
		document.select("tr").stream().filter(element -> element.children().isEmpty())
				.forEach(element -> element.appendElement("br"));
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

	private static void processPageBreaks(Document document) {
		document.select("div[style^=page-break-after:]").forEach(pageBreak -> {
			pageBreak.after("<p style=\"page-break-before: always;\"> </p>");
			pageBreak.remove();
		});
		Elements sections = document.select("section");
		for (int i = 0; i < sections.size() - 1; i++) {
			sections.get(i).after("<p style=\"page-break-before: always;\"> </p>");
		}
	}

	/**
	 * Convert line height style in paragraphs from ratio number to percentage to be properly processed by docx4j.
	 *
	 * @param element
	 *            - the element which line height should be processed
	 * @return calculated line height style
	 */
	private static String processLineHeight(Element element) {
		String style = element.attr("style");
		Matcher matcher = LINE_HEIGHT_STYLE.matcher(style);
		if (matcher.find()) {
			String lineHeightRatio = matcher.group(1);
			double percentage = Double.parseDouble(lineHeightRatio) * 100;
			style = matcher.replaceFirst("line-height:" + percentage + "%;");
			element.attr("style", style);
		}
		return style;
	}

	private static void processParagraphLineHeight(Document document) {
		document.select("p[style*=line-height:]").forEach(ContentToWordExporter::processLineHeight);
	}

	private static void processListItemParagraphLineHeight(Document document) {
		document.select("li").forEach(list -> {
			Elements paragraphs = list.select("p[style*=line-height:]");
			String style = "";
			for (Element paragraph : paragraphs) {
				style = processLineHeight(paragraph);
			}
			list.attr("style", style);
		});
	}

	private static void processBreakRows(Document document) {
		document.select("br").forEach(breakRow -> {
			breakRow.after("&#8204");
			breakRow.remove();
		});
	}

	private Consumer<ContentNode> invokeRenderForNodes(String currentInstanceId) {
		return node -> idocRenderers.select(render -> render.accept(node))
				.ifPresent(render -> callIdocRenderer(render, currentInstanceId, node));
	}

	private static void callIdocRenderer(IdocRenderer iDocRenderer, String currentInstanceId, ContentNode node) {
		Element newElement = iDocRenderer.render(currentInstanceId, node);
		iDocRenderer.afterRender(newElement, node);
	}

	/**
	 * Fetch css style which have to be applied before converting to word. First will check if external file is
	 * configured, if yes this file will be loaded otherwise default one will be used.
	 *
	 * @return external css or default one.
	 */
	protected String getExportToWordCss() {
		return exportToWordCss.computeIfNotSet(() -> defaultExportCss);
	}

	@ConfigurationConverter(EXPORT_TO_WORD_CSS)
	static String buildExportToWordCss(GroupConverterContext context) {
		String pathToExternalcssFile = context.get(PATH_TO_EXPORT_TO_WORD_CSS_FILE);
		if (StringUtils.isEmpty(pathToExternalcssFile)) {
			return null;
		}

		File externalCssFile = new File(pathToExternalcssFile);
		if (!externalCssFile.exists() || !externalCssFile.isFile()) {
			return null;
		}

		try (FileInputStream fileInputStream = new FileInputStream(externalCssFile);) {
			String readFileToString = IOUtils.toString(fileInputStream);
			return createStyleTag(readFileToString);
		} catch (IOException e) {
			LOGGER.error("Error loading external css file: " + pathToExternalcssFile, e);
			return null;
		}
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

	@Override
	public String getName() {
		return SupportedExportFormats.WORD.getFormat();
	}

	private static void addFontMappings() {
		XHTMLImporterImpl.addFontMapping("Cousine", "Courier New");
		XHTMLImporterImpl.addFontMapping("Tinos", "Times New Roman");
		XHTMLImporterImpl.addFontMapping("Arimo", "Arial");
		XHTMLImporterImpl.addFontMapping("Carlito", "Calibri");
		XHTMLImporterImpl.addFontMapping("Caladea", "Cambria");
		XHTMLImporterImpl.addFontMapping("Open Sans", "Arial");
	}

}