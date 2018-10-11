package com.sirma.itt.emf.semantic.model;

import org.eclipse.rdf4j.model.IRI;

import com.sirma.itt.seip.Uri;

/**
 * Proxy class for the {@link org.eclipse.rdf4j.model.IRI} class.
 *
 * @author BBonev
 */
public class Rdf4JUriProxy implements Uri, IRI {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3714282304687802509L;

	/** The uri. */
	private IRI uri;

	/**
	 * Instantiates a new open rdf uri proxy.
	 */
	public Rdf4JUriProxy() {
		// nothing to define
	}

	/**
	 * Instantiates a new open rdf uri proxy.
	 *
	 * @param uri
	 *            the uri
	 */
	public Rdf4JUriProxy(IRI uri) {
		this.uri = uri;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNamespace() {
		return uri.getNamespace();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLocalName() {
		return uri.getLocalName();
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
			if (paramObject instanceof Rdf4JUriProxy) {
				return uri.equals(((Rdf4JUriProxy) paramObject).uri);
			} else if (paramObject instanceof IRI) {
				return uri.equals(paramObject);
			}
		}
		return false;
	}

	@Override
	public String stringValue() {
		return uri.stringValue();
	}

}
