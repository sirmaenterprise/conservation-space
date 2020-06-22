/**
 * Copyright (c) 2010 14.05.2010 , Sirma ITT.
 */
package com.sirma.sep.xml;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing some utility methods for working with XMLs. Such as removing the empty tags from a XML and etc.
 *
 * @author B.Bonev
 */
public class XmlTools {

	private static final Logger LOGGER = LoggerFactory.getLogger(XmlTools.class);

	/**
	 * Pattern that matches all empty tags
	 */
	private static Pattern MATCH_EMPTY_TAGS = Pattern
			.compile("(<\\w*:?\\w+\\s*/>)|(<(\\w*:?\\w+)></\\3>)|(<(\\w*:?\\w+)>\\s*</\\5>)");

	/**
	 * Pattern that matches empty lines in XML.
	 */
	private static Pattern MATCH_EMPTY_LINES = Pattern.compile("\r?\n\\s*\r?\n");

	public static final String DEFAULT_XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	public static final String DEFAULT_SCHEMA = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
	public static final String NO_NAMESPACE_SCHEMA_LOCALTION = "xsi:noNamespaceSchemaLocation=\"{0}\"";
	private static final MessageFormat SCHEMA_LOCATION_FORMATTER = new MessageFormat(NO_NAMESPACE_SCHEMA_LOCALTION);
	private static final Pattern XML_DECLARATION_PATTERN = Pattern
			.compile("(<\\?xml version=\"1\\.0\" encoding=\"[\\w-]+\"\\?>)?\\r?\\n?"
					+ "<(\\w+)\\s*?(xmlns:xsi=\"http://www\\.w3\\.org/2001/XMLSchema-instance\")?"
					+ "\\s*?(xsi:noNamespaceSchemaLocation=\"(.+?)\")?\\s*?>(.*?)</\\2>", Pattern.DOTALL);

	/**
	 * Captures in group with number 3 the XSD content (without the schema tag)
	 */
	private static final Pattern XSD_CONTENT_PATTERN = Pattern
			.compile("((?:<\\?(?:xml version=\"[\\d\\.]+\")?\\s*(?:encoding=\"[\\w-]+\\s*\")?\\?>)?\\r?\\n?"
					+ "<([\\w]+):schema(?:\\s+xmlns:\\2=\"http://www\\.w3\\.org/2001/XMLSchema\"|"
					+ "\\s+(?:element|attribute)FormDefault=\"(?:un)?qualified\"|"
					+ "\\s+targetNamespace=\"[\\W\\w]+?\"|\\s+xmlns:[\\w]+=\"[\\W\\w]+?\"|"
					+ "\\s+version=\"[\\d\\.]+\")*>)\\s*(?:<!--.*?-->\\r?\\n?\\s*)*"
					+ "(?:<\\2:include\\s+schemaLocation=\".+?\"\\s*/>\\r?\\n?\\s*)*"
					+ "(?:<!--.*?-->\\r?\\n?\\s*)*(.*)(</\\2:schema>)", Pattern.DOTALL);

	/**
	 * Instantiates a new xml tools.
	 */
	private XmlTools() {
		// utility class
	}

	/**
	 * Strips all tags in format &lt;tag/&gt; or &lt;tag&gt;&lt;/tag&gt;
	 *
	 * @param xml
	 *            the input xml
	 * @return the result xml after the clean up
	 */
	public static String stripEmptyTags(String xml) {
		if (xml == null) {
			return xml;
		}
		Matcher matcher = MATCH_EMPTY_TAGS.matcher(xml);
		try {
			String result = matcher.replaceAll("");
			if (result.length() == xml.length()) {
				return result;
			}
			// removes all left over blank lines
			matcher = MATCH_EMPTY_LINES.matcher(result);
			String noLinces = matcher.replaceAll("\n");
			if (noLinces.length() == result.length()) {
				return noLinces;
			}
			return stripEmptyTags(noLinces);
		} catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
			LOGGER.debug("Exception occured during empty tags stripping", ex);
		}
		return xml;
	}

	/**
	 * Merges the given XSDs into single XSD with header from the first entry. The method will not include
	 * &lt;xs:include /&gt; entries. Also will check the default prefix for the http://www.w3.org/2001/XMLSchema
	 * namespace in rest of the files and if finds a discrepancies then will change them to the first schema. <br>
	 * If some of the given XSDs does not conform the validation pattern if will be skipped.<br>
	 * Here is the pattern the will be validated against:<br>
	 * <code>((?:&lt;\?(?:xml version=&quot;[\d\.]+&quot;)?\s*(?:encoding=&quot;[\w-]+\s*&quot;)?\?&gt;)?\r?\n?&lt;([\w]+):schema(?:\s+xmlns:\2=&quot;http://www\.w3\.org/2001/XMLSchema&quot;|\s+(?:element|attribute)FormDefault=&quot;(?:un)?qualified&quot;|\s+targetNamespace=&quot;[\W\w]+?&quot;|\s+xmlns:[\w]+=&quot;[\W\w]+?&quot;|\s+version=&quot;[\d\.]+&quot;)*&gt;)\s*(?:&lt;!--.*?--&gt;\r?\n?\s*)*(?:&lt;\2:include\s+schemaLocation=&quot;.+?&quot;\s* /&gt;\r?\n?\s*)*(?:&lt;!--.*?--&gt;\r?\n?\s*)*(.*)(&lt;/\2:schema&gt;)</code>
	 *
	 * @param xsds
	 *            is the loaded XSDs to merge.
	 * @return the result XSD from the merger.
	 */
	public static String mergeXSD(String[] xsds) {
		StringBuilder builder = new StringBuilder(computeLength(xsds));
		final int headerGroup = 1;
		final int xsGroup = 2;
		final int contentGroup = 3;
		final int endGroup = 4;
		String xs = null;
		String newXs = null;
		String endTag = null;
		boolean isXsSet;
		for (String xsd : xsds) {
			isXsSet = true;
			Matcher matcher = XSD_CONTENT_PATTERN.matcher(xsd);
			if (matcher.find()) {
				if (xs == null) {
					xs = matcher.group(xsGroup);
					endTag = matcher.group(endGroup);
				} else {
					isXsSet = false;
					newXs = matcher.group(xsGroup);
				}
				String content = matcher.group(contentGroup);
				if (content == null) {
					// invalid XSD, no content found continue with next file.
					// the case is barely possible but just in case
					LOGGER.warn("The given XSD does not have a content");
					continue;
				}
				// we have different xs prefix
				if (!isXsSet) {
					// REVIEW i think there should be used replace instead of replaceAll
					content = content.replaceAll(xs + ":", newXs + ":");
				}
				if (builder.length() == 0) {
					builder.append(matcher.group(headerGroup));
				}
				builder.append(content);
			} else {
				LOGGER.warn("The given schema was not valid. Skipping: " + xsd);
			}
		}
		if (endTag != null && builder.length() != 0) {
			builder.append(endTag);
		} else {
			LOGGER.warn("Method argument does not contains XSD files");
		}
		return builder.toString();
	}

	/**
	 * Adds default XML instance namespace and schema location for no namespace location to the given XML.
	 *
	 * @param xml
	 *            is the XML to handle
	 * @param schemaName
	 *            is the schema location
	 * @return updated XML
	 * @deprecated not used for now. I don't think it has any purpose
	 */
	@Deprecated
	public static String addSchemaDeclaration(String xml, String schemaName) {
		StringBuilder xmlBuilder = new StringBuilder(xml.length() + 200);
		Matcher matcher = XML_DECLARATION_PATTERN.matcher(xml);
		if (!matcher.find()) {
			throw new IllegalArgumentException("Argument was not a valid XML.");
		}
		String xmlDeclaration = matcher.group(1);
		String xmlRootElement = matcher.group(2);
		String defaultSchemaDeclaration = matcher.group(3);
		String schemaDeclaration = matcher.group(4);
		String declaredSchemaName = matcher.group(5);
		if (declaredSchemaName != null && !schemaName.equalsIgnoreCase(declaredSchemaName)) {
			LOGGER.warn("Found schema declaration " + declaredSchemaName + " that does not match expected schema "
					+ schemaName);
		}
		String schemaLocation = SCHEMA_LOCATION_FORMATTER.format(new String[] { schemaName });
		if (xmlDeclaration == null) {
			xmlBuilder.append(DEFAULT_XML_DECLARATION).append("\n");
		} else {
			xmlBuilder.append(xmlDeclaration).append("\n");
		}
		xmlBuilder.append('<').append(xmlRootElement);
		xmlBuilder.append(' ');
		if (defaultSchemaDeclaration == null) {
			xmlBuilder.append(DEFAULT_SCHEMA);
		} else {
			xmlBuilder.append(defaultSchemaDeclaration);
		}
		xmlBuilder.append(' ');
		if (declaredSchemaName == null) {
			xmlBuilder.append(schemaLocation);
		} else {
			xmlBuilder.append(schemaDeclaration);
		}
		xmlBuilder.append('>').append(matcher.group(6)).append("</").append(xmlRootElement).append('>');
		return xmlBuilder.toString();
	}

	/**
	 * Returns the XML root tag name.
	 *
	 * @param xml
	 *            the xml content
	 * @return the tag name or <code>null</code> if the given string is not valid XML.
	 */
	public static String getXMLRootTagName(String xml) {
		if (xml == null) {
			return null;
		}
		// we get the last XML tag name
		int start = xml.lastIndexOf('/');
		int collon = xml.lastIndexOf(':');
		int end = xml.lastIndexOf('>');
		// not found tag boundaries or not in proper order
		if (start < 0 || end <= 0 || start >= end) {
			return null;
		}
		if (collon > 0 && collon < end && start < collon) {
			start = collon;
		}
		return xml.substring(start + 1, end);
	}

	/**
	 * Calculates the total needed buffer length depending on the given XSDs.
	 *
	 * @param xsds
	 *            are the loaded XSDs.
	 * @return the calculated length.
	 */
	private static int computeLength(String[] xsds) {
		int length = 0;
		for (String string : xsds) {
			length += string.length();
		}
		return length;
	}

}
