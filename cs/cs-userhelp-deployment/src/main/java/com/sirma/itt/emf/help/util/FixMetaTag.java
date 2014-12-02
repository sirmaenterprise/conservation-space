package com.sirma.itt.emf.help.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

/**
 * Close meta tags.
 *
 * @author Boyan Tonchev
 */
public class FixMetaTag {

	private static final String OLD_DOCTYPE_TAG = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
	private static final String NEW_DOCTYPE_TAG = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";

	private static final String OLD_HTML_TAG = "<html>";
	private static final String NEW_HTML_TAG = "<html xmlns=\"http://www.w3.org/1999/xhtml\">";

	private static final String OLD_META_TAG = "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">";
	private static final String NEW_META_TAG = "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>";

	private static final int IMAGE_WIDTH_PIXEL = 680;
	private static final String OLD_IMAGE = "<img";
	private static final String NEW_IMAGE = "<img class=\"bigImage\"";

	private static final String OLD_HREF_ALFRESCO = "<a href=\"http://www.alfresco.com/help/34/community/sharehelp/\">";
	private static final String NEW_HREF_ALFRESCO = "<a href=\"http://www.alfresco.com/help/34/community/sharehelp/\" target=\"_blank\">";

	private static final String PAGE_METADATA_TAG = "<div class=\"page-metadata\">";
	private static final String PAGE_FOOTER_TAG = "<div id=\"footer\">";
	private static final String PAGE_BREADCRUM_SECTION_TAG = "<div id=\"breadcrumb-section\">";
	private static final String PAGE_ATTACHMENT_TAG = "<h2 id=\"attachments\" class=\"pageSectionTitle\">Attachments:</h2>";
	private static final String TAG_CLOSE_DIV = "</div>";

	private static final Map<String, String> tagToBeReplaced = new HashMap<String, String>();

	static {
		tagToBeReplaced.put(FixMetaTag.OLD_DOCTYPE_TAG, FixMetaTag.NEW_DOCTYPE_TAG);
		tagToBeReplaced.put(FixMetaTag.OLD_HTML_TAG, FixMetaTag.NEW_HTML_TAG);
		tagToBeReplaced.put(FixMetaTag.OLD_META_TAG, FixMetaTag.NEW_META_TAG);
		tagToBeReplaced.put(FixMetaTag.OLD_HREF_ALFRESCO, FixMetaTag.NEW_HREF_ALFRESCO);
	}
	static Pattern pattern = Pattern.compile("<img(.*?)\">(?!</img>)");
	private static String pathToPages = "";
	static String sperator = System.getProperty("line.separator");

	/**
	 * Close meta tags.
	 *
	 * @param pathToDir
	 *            first argument is path to directory with .html pages.
	 * @throws IOException
	 */
	public static void main(String[] pathToDir) throws IOException {

		File dir = null;

		dir = new File(pathToDir[0]);
		pathToPages = pathToDir[0];
		File[] allFiles = dir.listFiles();
		String line = "";
		BufferedReader inFile = null;

		boolean isMetaTag = false;
		boolean isFooterTag = false;
		boolean isBreadcrumbSection = false;

		for (File file : allFiles) {
			String fileNameString = file.getAbsolutePath();
			try {
				if (fileNameString.endsWith(".html")) {
					inFile = new BufferedReader(new FileReader(file));
					StringWriter writer = new StringWriter();
					while ((line = inFile.readLine()) != null) {

						// remove metadata for creator of page
						if (line.contains(PAGE_METADATA_TAG)) {
							isMetaTag = true;
							continue;
						}
						if (isMetaTag) {
							if (line.contains(TAG_CLOSE_DIV)) {
								isMetaTag = false;
							}
							continue;
						}
						// Remove footer tag
						if (line.contains(PAGE_FOOTER_TAG)) {
							isFooterTag = true;
							continue;
						}
						if (isFooterTag) {
							if (line.contains(TAG_CLOSE_DIV)) {
								isFooterTag = false;
							}
							continue;
						}
						// remove attachments
						if (line.contains(PAGE_ATTACHMENT_TAG)) {
							continue;
						}

						if (line.contains(PAGE_BREADCRUM_SECTION_TAG)) {
							isBreadcrumbSection = true;
							continue;
						}
						if (isBreadcrumbSection) {
							if (line.contains(TAG_CLOSE_DIV)) {
								isBreadcrumbSection = false;
							}
							continue;
						}

						line = replaceTags(line);
						writer.append(line).append(sperator);
					}
					String finalResult = writer.toString();
					Matcher matcher = pattern.matcher(finalResult);
					while (matcher.find()) {
						String group = matcher.group();
						finalResult = finalResult.replace(group, group + "</img>");
					}
					inFile.close();
					System.out.println("Storing " + file);
					FileWriter fileWriter = new FileWriter(file);
					fileWriter.append(finalResult);
					fileWriter.flush();
					fileWriter.close();

				}
			} finally {
				IOUtils.closeQuietly(inFile);
			}
		}
	}

	/**
	 * Replace all tags form <code>tagToBeReplaced</code>
	 *
	 * @param line
	 *            if line contain same tag from <code>tagToBeReplaced</code>, method will replace
	 *            it.
	 * @return line with replaced tags.
	 * @throws IOException
	 */
	private static String replaceTags(String line) throws IOException {
		for (String oldTag : tagToBeReplaced.keySet()) {
			if (line.contains(oldTag)) {
				line = line.replace(oldTag, tagToBeReplaced.get(oldTag));
			}
		}

		if (line.contains(OLD_IMAGE)) {
			if (line.contains("<img src=\"")) {
				String src = line.substring(line.indexOf("<img src=\"") + 10);
				src = src.substring(0, src.indexOf("\""));

				String pathToImage = pathToPages + "\\" + src;

				File sourceFile = new File(pathToImage);
				BufferedImage img = ImageIO.read(sourceFile);
				int imgWight = img.getWidth();

				// convert px to mm
				if (imgWight > IMAGE_WIDTH_PIXEL) {
					line = line.replace(OLD_IMAGE, NEW_IMAGE);
				} else {
				}
			}
		}
		return line;
	}
}
