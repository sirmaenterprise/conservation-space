package com.sirma.itt.seip.definition.util;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Helper class for dealing with path.
 *
 * @author BBonev
 */
public class PathHelper {

	public static final Pattern PATH_SPLIT_PATTERN = Pattern.compile(PathElement.PATH_SEPARATOR);

	/**
	 * Instantiates a new path helper.
	 */
	private PathHelper() {
		// utility class
	}

	/**
	 * Constructs the path from the given path element.
	 *
	 * @param element
	 *            the element
	 * @return the path
	 */
	public static String getPath(PathElement element) {
		if (element == null) {
			return null;
		}
		PathElement parent = element.getParentElement();
		if (parent == null) {
			return element.getPath();
		}
		if (element.getPath() == null) {
			return getPath(parent);
		}
		return getPath(parent) + PathElement.PATH_SEPARATOR + element.getPath();
	}

	/**
	 * Checks if the given paths points to the same virtual location
	 *
	 * @param p1
	 *            the first path
	 * @param p2
	 *            the second path
	 * @return true, if equal
	 */
	public static boolean equalsPaths(PathElement p1, PathElement p2) {
		if (p1 == null && p2 == null) {
			return true;
		} else if (p1 != null && p2 != null) {
			String path1 = getPath(p1);
			String path2 = getPath(p2);
			return EqualsHelper.nullSafeEquals(path1, path2, true);
		} else {
			return false;
		}
	}

	/**
	 * Equals paths. Checks if the given path element matches the given path
	 *
	 * @param p1
	 *            the path element to compare
	 * @param p2
	 *            the path to check
	 * @return true, if equal
	 */
	public static boolean equalsPaths(PathElement p1, String p2) {
		if (p1 == null && p2 == null) {
			return true;
		} else if (p1 != null && p2 != null) {
			String path1 = getPath(p1);
			String path2 = p2;
			return EqualsHelper.nullSafeEquals(path1, path2, true);
		} else {
			return false;
		}
	}

	/**
	 * Gets the root element.
	 *
	 * @param element
	 *            the element
	 * @return the root element
	 */
	public static PathElement getRootElement(PathElement element) {
		if (element.getParentElement() == null) {
			return element;
		}
		return getRootElement(element.getParentElement());
	}

	/**
	 * Gets the root path.
	 *
	 * @param element
	 *            the element
	 * @return the root path
	 */
	public static String getRootPath(PathElement element) {
		if (element.getParentElement() == null) {
			return extractRootPath(element.getPath());
		}
		return getRootPath(element.getParentElement());
	}

	/**
	 * Iterate the given node model by the given path.
	 *
	 * @param node
	 *            the node
	 * @param element
	 *            the element
	 * @return the identity
	 */
	public static Identity iterateByPath(Node node, PathElement element) {
		if (node == null || element == null) {
			return null;
		}
		String path = getPath(element);
		if (path == null || path.length() == 0) {
			return null;
		}
		return iterateByPath(node, path);
	}

	/**
	 * Iterate the given node model by the given path.
	 *
	 * @param node
	 *            the node
	 * @param path
	 *            the path
	 * @return the identity
	 */
	public static Identity iterateByPath(Node node, String path) {
		if (node == null || path == null) {
			return null;
		}
		String[] paths = PATH_SPLIT_PATTERN.split(path);
		if (!node.getIdentifier().equals(paths[0])) {
			// if we are not at the root we may try to go there and try again
			if (node instanceof PathElement) {
				PathElement rootElement = getRootElement((PathElement) node);
				// no more elements above so get out
				if (!EqualsHelper.nullSafeEquals(rootElement, node)) {
					return iterateByPath(rootElement, path);
				}
			}
			// the roots are different
			return null;
		}
		return iterateByPathInternal(paths, node);
	}

	private static Identity iterateByPathInternal(String[] paths, Node start) {
		Node current = start;
		for (int i = 1; i < paths.length; i++) {
			String part = paths[i];
			if (current.hasChildren()) {
				Node child = current.getChild(part);
				current = child;
				if (current == null) {
					// not found
					return null;
				}
			} else if (current.getIdentifier().equals(part)) {
				// found
				break;
			} else {
				// paths does not match
				return null;
			}
		}
		return current;
	}

	/**
	 * Finds property property into the given definition model and property path. The path is searched into the model
	 * then the property is retrieved from the found model by the path.
	 *
	 * @param model
	 *            the model
	 * @param parentPath
	 *            the parent path
	 * @param propertyName
	 *            the property name
	 * @return the property definition
	 */
	public static PropertyDefinition findProperty(DefinitionModel model, PathElement parentPath, String propertyName) {
		Identity holder = iterateByPath(model, parentPath);
		if (holder == null) {
			return null;
		}
		if (holder instanceof DefinitionModel) {
			return ((DefinitionModel) holder)
					.fieldsStream()
						.filter(field -> isSameIdentity(propertyName, field))
						.findFirst()
						.orElse(null);

		} else if (holder instanceof Node) {
			Node child = ((Node) holder).getChild(propertyName);
			if (child instanceof PropertyDefinition) {
				return (PropertyDefinition) child;
			}
		}
		return null;
	}

	/**
	 * Finds an element from the given list of identity objects
	 *
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list
	 * @param identity
	 *            the identity
	 * @return the found element or <code>null</code> if not found
	 */
	public static <E extends Identity> E find(List<E> list, String identity) {
		for (E e : list) {
			if (isSameIdentity(identity, e)) {
				return e;
			}
		}
		return null;
	}

	private static <E extends Identity> boolean isSameIdentity(String identity, E e) {
		// REVIEW: this could be changed to non ignore case, but must check all cases where used
		// not to break something!
		return EqualsHelper.nullSafeEquals(e.getIdentifier(), identity, true);
	}

	/**
	 * Checks if the given two {@link Identity} objects are equal by identifiers.
	 *
	 * @param i1
	 *            the i1
	 * @param i2
	 *            the i2
	 * @return true, if is same
	 */
	public static boolean isSame(Identity i1, Identity i2) {
		int nullCompare = EqualsHelper.nullCompare(i1, i2);
		if (nullCompare != 2) {
			return nullCompare == 0;
		}
		return EqualsHelper.nullSafeEquals(i1.getIdentifier(), i2.getIdentifier(), false);
	}

	/**
	 * Extract root path.
	 *
	 * @param parentPath
	 *            the parent path
	 * @return the string
	 */
	public static String extractRootPath(String parentPath) {
		if (parentPath == null) {
			return null;
		}
		String[] split = PATH_SPLIT_PATTERN.split(parentPath, 2);
		return split[0];
	}

	/**
	 * Extract last element in passed path.
	 *
	 * @param path
	 *            the path from which the last element will be extracted
	 * @return the last element of the passed path
	 */
	public static String extractLastElementInPath(String path) {
		if (StringUtils.isBlank(path)) {
			return null;
		}

		String[] split = PATH_SPLIT_PATTERN.split(path);
		return split[split.length - 1];
	}
}
