package com.sirma.itt.emf.semantic.model;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.URIUtil;

import com.sirma.itt.seip.Uri;

/**
 * Proxy class for the {@link org.eclipse.rdf4j.model.IRI} class.
 *
 * @author BBonev
 */
public class Rdf4JStringUriProxy implements Uri, IRI {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3714282304687802509L;

	/** The uri. */
	private String uri;

	/** The local name idx. */
	private int localNameIdx = -1;

	/** The namespace. */
	private String namespace;

	/** The local name. */
	private String localName;

	/**
	 * Instantiates a new open rdf uri proxy.
	 */
	public Rdf4JStringUriProxy() {

	}

	/**
	 * Instantiates a new open rdf uri proxy.
	 *
	 * @param uri
	 *            the uri
	 */
	public Rdf4JStringUriProxy(String uri) {
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
	public Rdf4JStringUriProxy(String namespace, String localName) {
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
			if (localNameIdx < 0) {
				localNameIdx = getLocalNameIndex(uri);
			}

			return uri.substring(0, localNameIdx);
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
			throw new IllegalArgumentException("No separator character founds in IRI: " + uri);
		}

		return separatorIdx + 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLocalName() {
		if (localName == null) {
			if (localNameIdx < 0) {
				localNameIdx = URIUtil.getLocalNameIndex(uri);
			}

			return uri.substring(localNameIdx);
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
		return uri;
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
			if (paramObject instanceof Rdf4JStringUriProxy) {
				return uri.equals(((Rdf4JStringUriProxy) paramObject).uri);
			} else if (paramObject instanceof IRI) {
				return uri.equals(((IRI) paramObject).stringValue());
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
