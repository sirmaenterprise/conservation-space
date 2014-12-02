package com.sirma.itt.emf.serialization.xstream;

import java.io.Serializable;

import com.sirma.itt.emf.serialization.SerializationEngine;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Serialization engine implementation that uses XStream API to serialize and deserialize data.
 *
 * @author BBonev
 */
public class XStreamSerializationEngine implements SerializationEngine {

	/** The x stream. */
	private XStream xStream;

	/**
	 * Instantiates a new x stream serialization engine.
	 */
	public XStreamSerializationEngine() {
		xStream = new XStream(new DomDriver());
		xStream.setMode(XStream.ID_REFERENCES);
	}

	@Override
	public Serializable serialize(Object src) {
		String xml = xStream.toXML(src);
		return xml;
	}

	@Override
	public Object deserialize(Serializable src) {
		Object object = xStream.fromXML((String) src);
		return object;
	}

}