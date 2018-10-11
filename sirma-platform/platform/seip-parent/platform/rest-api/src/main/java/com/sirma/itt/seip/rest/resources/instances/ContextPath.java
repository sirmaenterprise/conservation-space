package com.sirma.itt.seip.rest.resources.instances;

import java.util.Iterator;
import java.util.List;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Wraps a collection of instances that represents an instance context. The first instance is the root parent and the
 * last instance is the instance that is the last child in the hierarchy.
 *
 * @author BBonev
 */
public class ContextPath implements Iterable<Instance> {

	private final List<Instance> path;

	/**
	 * Instantiates a new context path.
	 *
	 * @param path
	 *            the path
	 */
	public ContextPath(List<Instance> path) {
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public List<Instance> getPath() {
		return path;
	}

	@Override
	public Iterator<Instance> iterator() {
		return path.iterator();
	}

}
