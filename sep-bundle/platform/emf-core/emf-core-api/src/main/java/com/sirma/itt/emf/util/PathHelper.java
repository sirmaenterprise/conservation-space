package com.sirma.itt.emf.util;

import java.util.List;
import java.util.regex.Pattern;

import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Identity;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;

/**
 * Helper class for dealing with path.
 *
 * @author BBonev
 */
public class PathHelper {

	public static final Pattern PATH_SPLIT_PATTERN = Pattern.compile(PathElement.PATH_SEPARATOR);

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
		if ((p1 == null) && (p2 == null)) {
			return true;
		} else if ((p1 != null) && (p2 != null)) {
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
		if ((p1 == null) && (p2 == null)) {
			return true;
		} else if ((p1 != null) && (p2 != null)) {
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
		if ((node == null) || (element == null)) {
			return null;
		}
		String path = getPath(element);
		if ((path == null) || (path.length() == 0)) {
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
		if ((node == null) || (path == null)) {
			return null;
		}
		String[] paths = PATH_SPLIT_PATTERN.split(path);
		Node current = node;
		if (!node.getIdentifier().equals(paths[0])) {
			// the roots are different
			return null;
		}
		for (int i = 1; i < paths.length; i++) {
			String part = paths[i];
			if (current.hasChildren()) {
				Node child = current.getChild(part);
				current = child;
				// not found
				if (current == null) {
					break;
				}
			} else if (current.getIdentifier().equals(part)) {
				break;
			} else {
				break;
			}
		}
		return current;
	}

	/**
	 * Finds property property into the given definition model and property path. The path is
	 * searched into the model then the property is retrieved from the found model by the path.
	 *
	 * @param model
	 *            the model
	 * @param parentPath
	 *            the parent path
	 * @param propertyName
	 *            the property name
	 * @return the property definition
	 */
	public static PropertyDefinition findProperty(DefinitionModel model, PathElement parentPath,
			String propertyName) {
		Identity holder = iterateByPath(model, parentPath);
		if (holder == null) {
			return null;
		}
		if (holder instanceof DefinitionModel) {
			PropertyDefinition find = find(((DefinitionModel) holder).getFields(), propertyName);
			if ((find == null) && (holder instanceof RegionDefinitionModel)) {
				for (RegionDefinition definition : ((RegionDefinitionModel) holder).getRegions()) {
					find = find(((DefinitionModel) definition).getFields(), propertyName);
					if (find != null) {
						return find;
					}
				}
			} else {
				return find;
			}
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
			// REVIEW: this could be changed to non ignore case, but must check all cases where used
			// not to break something!
			if (EqualsHelper.nullSafeEquals(e.getIdentifier(), identity, true)) {
				return e;
			}
		}
		return null;
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
}
