package com.sirma.itt.commons.utils.xml;

import static javax.xml.stream.XMLStreamConstants.ATTRIBUTE;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class STAXGenericReader {

	/** variables storing the value for new line for the system. */
	protected static final String NL = System.getProperty("line.separator");
	/** factory for xml readers. */
	protected static XMLInputFactory xmlFactory = null;
	/** instance to current object. used by the singelton. */
	protected static STAXGenericReader parser = null;
	/** reader for the current parsing. Created by the factory */
	protected XMLStreamReader xmlReader;
	/** last open tag. need to be known. */
	protected String lastOpenTag;

	/**
	 * protected constructor used by getInstance.
	 */
	protected STAXGenericReader() {
		xmlFactory = XMLInputFactory.newInstance();
		xmlFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
				Boolean.TRUE);
		xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
				Boolean.FALSE);
		xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE,
				Boolean.FALSE);
	}

	/**
	 * process next event.
	 * 
	 * @param eventType
	 *            is the type of the event. Defined as one of XMLEvent.
	 * @throws Exception
	 *             if unknown event is triggered.
	 */
	protected void processEvent(final int eventType) throws Exception {
		switch (eventType) {
			case START_ELEMENT:
				processStartElement();
				break;
			case END_ELEMENT:
				processEndElement();
				break;
			case CHARACTERS:
				processContent();
				break;
			case ATTRIBUTE:
				procesAttribute();
				break;
			case COMMENT:
				printComment();
				break;
			case END_DOCUMENT:
				break;
			default:
				throw new Exception("Unimplemented EVENT: " + eventType);

		}

	}

	protected void procesAttribute() {
	}

	/**
	 * process event of opening tag.
	 * 
	 * @throws Exception
	 *             if occurs
	 */
	protected abstract void processStartElement() throws Exception;

	/**
	 * process content between tags.
	 */
	protected abstract void processContent() throws Exception;

	/**
	 * process closing tag.
	 * 
	 * @throws Exception
	 *             if all stacks' element have not been poped
	 */
	protected abstract void processEndElement() throws Exception;

	/**
	 * override if needed. prints the comment if occurs.
	 */
	protected void printComment() {

	}

	/**
	 * get attributes for current tag by name(should be known), as iterating
	 * through all attribute of the current tag. If not met null is return
	 * 
	 * @param attribute
	 *            is the attr name
	 * @return the value for the name or null if not found
	 */
	protected String getAttributeValue(final String attribute) {
		int count = xmlReader.getAttributeCount();
		if ((count > 0) && (attribute != null)) {
			for (int i = 0; i < count; i++) {
				if (attribute.equals(xmlReader.getAttributeName(i).toString())) {
					return xmlReader.getAttributeValue(i);
				}
			}
		}
		return null;
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

	/**
	 * close the reader and set to null fields.
	 * 
	 * @throws XMLStreamException
	 *             if xmlReader does not succeed.
	 */
	protected void close() throws XMLStreamException {
		if (xmlReader != null) {
			xmlReader.close();
			xmlReader = null;
		}

	}
}
