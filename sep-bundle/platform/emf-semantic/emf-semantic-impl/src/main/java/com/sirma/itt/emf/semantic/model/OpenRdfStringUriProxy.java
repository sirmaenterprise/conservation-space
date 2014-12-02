package com.sirma.itt.emf.semantic.model;

import org.openrdf.model.URI;
import org.openrdf.model.util.URIUtil;

import com.sirma.itt.emf.domain.model.Uri;

/**
 * Proxy class for the {@link org.openrdf.model.URI} class.
 * 
 * @author BBonev
 */
public class OpenRdfStringUriProxy implements Uri, URI {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3714282304687802509L;

	/** The uri. */
	private String uri;

	/** The local name idx. */
	private int localNameIdx;

	/** The namespace. */
	private String namespace;

	/** The local name. */
	private String localName;

	/**
	 * Instantiates a new open rdf uri proxy.
	 */
	public OpenRdfStringUriProxy() {

	}

	/**
	 * Instantiates a new open rdf uri proxy.
	 * 
	 * @param uri
	 *            the uri
	 */
	public OpenRdfStringUriProxy(String uri) {
		this.uri = uri;
	}

	/**
	 * Instantiates a new open rdf string uri proxy.
	 * 
	 * @param namespace
	 *            the namespace
	 * @param localName
	 *            the local name
	 */
	public OpenRdfStringUriProxy(String namespace, String localName) {
		this.namespace = namespace;
		this.localName = localName;
		uri = namespace + "#" + localName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNamespace() {
		if (namespace == null) {
			if (this.localNameIdx < 0) {
				this.localNameIdx = getLocalNameIndex(uri);
			}

			return this.uri.substring(0, this.localNameIdx);
		}
		return namespace;
	}

	/**
	 * Gets the local name index.
	 * 
	 * @param uri
	 *            the uri
	 * @return the local name index
	 */
	public static int getLocalNameIndex(String uri) {
		int separatorIdx = uri.indexOf(35);

		if (separatorIdx < 0) {
			separatorIdx = uri.lastIndexOf(47);
		}

		if (separatorIdx < 0) {
			separatorIdx = uri.lastIndexOf(58);
		}

		if (separatorIdx < 0) {
			throw new IllegalArgumentException("No separator character founds in URI: " + uri);
		}

		return (separatorIdx + 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLocalName() {
		if (localName == null) {
			if (this.localNameIdx < 0) {
				this.localNameIdx = URIUtil.getLocalNameIndex(this.uri);
			}

			return this.uri.substring(this.localNameIdx);
		}
		return localName;
	}

	/**
	 * To string.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		return uri.toString();
	}

	/**
	 * Hash code.
	 * 
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	/**
	 * Equals.
	 * 
	 * @param paramObject
	 *            the param object
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object paramObject) {
		if (uri != null) {
			if (paramObject instanceof OpenRdfStringUriProxy) {
				return uri.equals(((OpenRdfStringUriProxy) paramObject).uri);
			} else if (paramObject instanceof URI) {
				return uri.equals(paramObject);
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String stringValue() {
		return uri;
	}

}
