package com.sirma.itt.seip.domain;

import java.io.Serializable;

import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Path element proxy
 *
 * @author BBonev
 */
public class PathElementProxy implements PathElement, GenericProxy<PathElement>, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3422719279897598537L;

	/** The element. */
	private PathElement element;

	/** The path. */
	private String path;

	private PathElement target;

	/**
	 * Instantiates a new path element proxy.
	 *
	 * @param path
	 *            the path
	 */
	public PathElementProxy(String path) {
		this.path = path;
	}

	/**
	 * Instantiates a new path element proxy.
	 *
	 * @param path
	 *            the path
	 * @param element
	 *            the element
	 * @param target
	 *            the target
	 */
	public PathElementProxy(String path, PathElement element, PathElement target) {
		this.path = path;
		this.element = element;
		this.target = target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		return element;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PathElementProxy [");
		builder.append("path=");
		builder.append(path);
		if (element != null) {
			builder.append(", element={HAVE_PARENT}");
		} else {
			builder.append(", element={NO_PARENT}");
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean hasChildren() {
		return element != null;
	}

	@Override
	public Node getChild(String name) {
		if (hasChildren() && EqualsHelper.nullSafeEquals(element.getIdentifier(), name, true)) {
			return element;
		}
		return null;
	}

	@Override
	public String getIdentifier() {
		return getPath();
	}

	@Override
	public void setIdentifier(String identifier) {
		path = identifier;
	}

	@Override
	public PathElement getTarget() {
		return target;
	}

	@Override
	public void setTarget(PathElement target) {
		this.target = target;
	}

	@Override
	public PathElement cloneProxy() {
		return new PathElementProxy(getPath(), getParentElement(), getTarget());
	}

	@Override
	public PathElement createCopy() {
		return cloneProxy();
	}

}
