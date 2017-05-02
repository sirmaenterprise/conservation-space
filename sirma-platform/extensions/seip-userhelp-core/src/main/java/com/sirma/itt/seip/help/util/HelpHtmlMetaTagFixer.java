package com.sirma.itt.seip.help.util;

import static com.sirma.itt.seip.help.util.HelpGeneratorUtil.UTF_8;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.sirma.itt.seip.help.exception.HelpGenerationException;

/**
 * Close meta tags.
 *
 * @author Boyan Tonchev
 */
public class HelpHtmlMetaTagFixer {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	private static final String OLD_DOCTYPE_TAG = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
	private static final String NEW_DOCTYPE_TAG = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";

	private static final String OLD_HTML_TAG = "<html>";
	private static final String NEW_HTML_TAG = "<html xmlns=\"http://www.w3.org/1999/xhtml\">";

	private static final String OLD_META_TAG = "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">";
	private static final String NEW_META_TAG = "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>";

	private static final String OLD_IMAGE = "<img";
	private static final String NEW_IMAGE = "<img class=\"bigImage\"";

	private static final String OLD_HREF_ALFRESCO = "<a href=\"http://www.alfresco.com/help/34/community/sharehelp/\">";
	private static final String NEW_HREF_ALFRESCO = "<a href=\"http://www.alfresco.com/help/34/community/sharehelp/\" target=\"_blank\">";

	private static final String PAGE_METADATA_TAG = "<div class=\"page-metadata\">";
	private static final String PAGE_FOOTER_TAG = "<div id=\"footer\">";
	private static final String PAGE_BREADCRUM_SECTION_TAG = "<div id=\"breadcrumb-section\">";
	private static final String PAGE_ATTACHMENT_TAG = "<h2 id=\"attachments\" class=\"pageSectionTitle\">Attachments:</h2>";
	private static final String TAG_CLOSE_DIV = "</div>";

	private static final int IMAGE_WIDTH_PIXEL = 680;

	private static final Map<String, String> REPLACEABLE_TAGS = new HashMap<>();

	static {
		REPLACEABLE_TAGS.put(HelpHtmlMetaTagFixer.OLD_DOCTYPE_TAG, HelpHtmlMetaTagFixer.NEW_DOCTYPE_TAG);
		REPLACEABLE_TAGS.put(HelpHtmlMetaTagFixer.OLD_HTML_TAG, HelpHtmlMetaTagFixer.NEW_HTML_TAG);
		REPLACEABLE_TAGS.put(HelpHtmlMetaTagFixer.OLD_META_TAG, HelpHtmlMetaTagFixer.NEW_META_TAG);
		REPLACEABLE_TAGS.put(HelpHtmlMetaTagFixer.OLD_HREF_ALFRESCO, HelpHtmlMetaTagFixer.NEW_HREF_ALFRESCO);
	}

	private static final Pattern PATTERN_IMG = Pattern.compile("<img(.*?)\">(?!</img>)");

	private HelpHtmlMetaTagFixer() {
	}

	/**
	 * Close meta tags.
	 *
	 * @param pathToPages
	 *            - path to directory with .html pages.
	 */
	public static void run(File pathToPages) {

		File[] allFiles = HelpGeneratorUtil.listHtmlFiles(pathToPages);
		try {
			for (File file : allFiles) {
				processNextFile(file);
			}
		} catch (Exception e) {
			throw new HelpGenerationException("Metatag update failed!", e);
		}
	}

	private static void processNextFile(File file) throws IOException {
		try (BufferedReader inFile = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8))) {
			String finalResult = fixMetaTags(file.getParentFile(), inFile);
			LOGGER.info("Storing " + file);
			try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file), UTF_8)) {
				fileWriter.append(finalResult);
				fileWriter.flush();
			}

		}
	}

	private static String fixMetaTags(File pathToPages, BufferedReader inFile) throws IOException {
		String line = "";
		boolean isSkipped = false;

		StringWriter writer = new StringWriter();
		while ((line = inFile.readLine()) != null) {
			// remove metadata for creator of page
			if (line.contains(PAGE_METADATA_TAG) || line.contains(PAGE_FOOTER_TAG)
					|| line.contains(PAGE_BREADCRUM_SECTION_TAG)) {
				isSkipped = true;
			} else if (isSkipped) {
				if (line.contains(TAG_CLOSE_DIV)) {
					isSkipped = false;
				}
			} else if (line.contains(PAGE_ATTACHMENT_TAG)) {
				// remove attachments
			} else {
				line = replaceTags(pathToPages, line);
				writer.append(line).append(HelpGeneratorUtil.NL);
			}
		}
		String finalResult = writer.toString();
		Matcher matcher = PATTERN_IMG.matcher(finalResult);
		while (matcher.find()) {
			String group = matcher.group();
			finalResult = finalResult.replace(group, group + "</img>");
		}
		return finalResult;
	}

	/**
	 * Replace all tags form <code>{@link #REPLACEABLE_TAGS}</code>
	 *
	 * @param pathToPages
	 * @param lineToReplace
	 *            if line contain same tag from <code>tagToBeReplaced</code>, method will replace it.
	 * @return line with replaced tags.
	 * @throws IOException
	 */
	private static String replaceTags(File pathToPages, String lineToReplace) throws IOException {
		String line = lineToReplace;
		for (Entry<String, String> tagReplacement : REPLACEABLE_TAGS.entrySet()) {
			CharSequence oldTag = tagReplacement.getKey();
			CharSequence replacement = tagReplacement.getValue();
			if (line.contains(oldTag)) {
				line = line.replace(oldTag, replacement);
			}
		}

		int indexOfImgTag = -1;
		if ((indexOfImgTag = line.indexOf("<img src=\"")) > -1) {
			String src = line.substring(indexOfImgTag + 10);
			src = src.substring(0, src.indexOf('\\'));

			File sourceFile = new File(pathToPages, src);
			BufferedImage img = ImageIO.read(sourceFile);
			int imgWight = img.getWidth();

			// convert px to mm
			if (imgWight > IMAGE_WIDTH_PIXEL) {
				line = line.replace(OLD_IMAGE, NEW_IMAGE);
			}
		}
		return line;
	}
}
