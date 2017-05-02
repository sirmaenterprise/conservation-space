package com.sirma.itt.seip.serialization.xstream;

import java.io.Serializable;

import com.sirma.itt.seip.serialization.SerializationEngine;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Serialization engine implementation that uses XStream API to serialize and deserialize data.
 *
 * @author BBonev
 */
public class XStreamSerializationEngine implements SerializationEngine {

	private final XStream xStream;

	/**
	 * Creates the xml engine.
	 *
	 * @return the serialization engine
	 */
	public static SerializationEngine createXmlEngine() {
		XStream xStream = new XStream(new DomDriver());
		xStream.setMode(XStream.ID_REFERENCES);
		return new XStreamSerializationEngine(xStream);
	}

	/**
	 * Creates a one way JSON engine. Note the plain JSON stream driver produces invalid JSON format. Also this engine
	 * does not support deserialization.
	 *
	 * @return the serialization engine
	 */
	public static SerializationEngine createJsonEngine() {
		XStream xStream = new XStream(new JsonHierarchicalStreamDriver());
		return new XStreamSerializationEngine(xStream);
	}

	/**
	 * Creates a bidirectional Jettison JSON engine.
	 *
	 * @return the serialization engine
	 */
	public static SerializationEngine createJettisonEngine() {
		XStream xStream = new XStream(new JettisonMappedXmlDriver());
		return new XStreamSerializationEngine(xStream);
	}

	/**
	 * Instantiates a new x stream serialization engine.
	 *
	 * @param xStream
	 *            the XStream parser to use
	 */
	public XStreamSerializationEngine(XStream xStream) {
		this.xStream = xStream;
	}

	@Override
	public Serializable serialize(Object src) {
		return xStream.toXML(src);
	}

	@Override
	public Object deserialize(Serializable src) {
		return xStream.fromXML((String) src);
	}

}