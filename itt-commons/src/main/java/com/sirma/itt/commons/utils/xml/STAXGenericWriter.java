package com.sirma.itt.commons.utils.xml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public abstract class STAXGenericWriter {

	/** variables storing the value for new line for the system. */
	protected static final String NL = System.getProperty("line.separator");
	/** factory for xml writer. */
	protected static XMLOutputFactory xmlFactory = XMLOutputFactory
			.newInstance();
	/** current xml writer. created on invoking serialize method */
	protected XMLStreamWriter xmlWriter;
	/** used to hold attribute's tuples -> name-values. */
	protected Map<String, String> attributesMap = new LinkedHashMap<String, String>();

	/** Operation increment of indent. */
	protected final int inc = 0;
	/** Operation decrement of indent. */
	protected final int dec = 1;
	/** indent for pretty format. */
	protected String indent = "";
	protected OutputStreamWriter stream;

	protected static final String NAMESPACE = "sirma";
	/** namespace URI. */
	protected static final String NAMESPACE_URI = "http://www.sirma.com/itt/";

	/**
	 * entry point.
	 * 
	 * @param ouputFileName
	 *            is the filename to read
	 * @param validate
	 *            says whether to check against schema
	 * @param data
	 *            is the specific data to parse
	 * @throws Exception
	 *             if some occurs
	 */
	public void serializeXML(final Object data, final String ouputFileName,
			final boolean validate, String schemaOfXML) throws Exception {
		if (validate) {
			validate(ouputFileName, schemaOfXML);
		}
		stream = new OutputStreamWriter(new FileOutputStream(ouputFileName),
				"UTF-8");
		try {
			xmlWriter = xmlFactory.createXMLStreamWriter(stream);
			attributesMap = new LinkedHashMap<String, String>();
			serializeXML(ouputFileName, data);
		} finally {
			close();
		}
	}

	/**
	 * entry point;
	 * 
	 * @param ouputFileName
	 *            is the filename to read
	 * @param data
	 *            is the data to serialize
	 * @throws Exception
	 *             if some occurs
	 */
	public void serializeXML(final String ouputFileName, Object data) {

	}

	/**
	 * open tag, and depending other parameters do additional work.
	 * 
	 * @param tagName
	 *            is the name of the tag to open
	 * @param attributes
	 *            is map of attributes. If NULL they are ignored.
	 * @param content
	 *            if NULL ignored, else appended.
	 * @param closeNow
	 *            boolean which sees whether to close immediately tag, or to
	 *            hold it open.
	 * @throws XMLStreamException
	 *             if occurs
	 */
	protected void appendOpenTag(String tagName,
			Map<String, String> attributes, String content, boolean closeNow)
			throws XMLStreamException {
		if (attributes != null) {
			xmlWriter.writeCharacters(indent);
			xmlWriter.writeStartElement(tagName);
			Iterator<String> attributesIter = attributes.keySet().iterator();
			while (attributesIter.hasNext()) {
				String currentAttribute = attributesIter.next();
				xmlWriter.writeAttribute(currentAttribute, attributes
						.get(currentAttribute));
			}
			if (content != null) {
				xmlWriter.writeCharacters(content);
			}
			attributesMap.clear();
		} else if (content != null) {
			xmlWriter.writeCharacters(indent);
			xmlWriter.writeStartElement(tagName);
			xmlWriter.writeCharacters(content);
		}
		if (closeNow) {
			if (content == null && attributes == null) {
				return;
			}
			xmlWriter.writeEndElement();
		}
		xmlWriter.writeCharacters(NL);

	}

	/**
	 * close last tag and writes new line.
	 * 
	 * @throws XMLStreamException
	 *             if error while writing occurs
	 */
	protected void appendCloseTag() throws XMLStreamException {
		xmlWriter.writeCharacters(indent);
		xmlWriter.writeEndElement();
		xmlWriter.writeCharacters(NL);
	}

	/**
	 * flush, close the writer and set to null fields.
	 */
	protected void close() {

		if (xmlWriter != null) {
			try {
				xmlWriter.flush();
				xmlWriter.close();
				if (stream != null) {
					stream.close();
				}
				xmlWriter = null;
			} catch (XMLStreamException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		attributesMap.clear();
	}

	protected void closeStreamAtExit() {

	}

	/**
	 * changes indent.
	 * 
	 * @param op
	 *            is one of inc or dec
	 */
	protected void changeIndent(int op) {
		switch (op) {
			case inc:
				indent += "\t";
				break;
			case dec:
				indent = indent.replaceFirst("\\t", "");
				break;
			default:
				throw new IllegalArgumentException(
						"Illegal operation for indent!");
		}
	}

	/**
	 * validates current DM xml.
	 * 
	 * @param filename
	 *            is path to xml file
	 * @throws Exception
	 *             if xml is not valid agaisnt schema
	 */
	protected void validate(final String filename, String pathToXSD)
			throws Exception {
		SchemaValidator.validateXml(pathToXSD, filename);
	}
}
