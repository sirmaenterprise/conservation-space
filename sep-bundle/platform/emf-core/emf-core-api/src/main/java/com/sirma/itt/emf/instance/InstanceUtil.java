package com.sirma.itt.emf.instance;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceContext;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Utility class for instance context and back chain methods. The utility works with
 * {@link com.sirma.itt.emf.converter.TypeConverterUtil} to convert the {@link InstanceReference}s
 * to {@link Instance} objects
 * 
 * @author BBonev
 */
@Named
@ApplicationScoped
public class InstanceUtil {

	/**
	 * Gets the root instance of the given instance. The method traverses the owning references all
	 * the way to the top instance. The returned instance will not be initialized by default.
	 * <p>
	 * <b>NOTE:</b> The method will return new instance every call and the instance will not have
	 * properties so if needed they should be initialized explicitly or called the method
	 * {@link #getRootInstance(Instance, boolean)} with <code>true</code>.
	 * 
	 * @param instance
	 *            the instance to traverse
	 * @return the root instance
	 */
	public static Instance getRootInstance(Instance instance) {
		return getRootInstance(instance, false);
	}

	/**
	 * Gets the root instance of the given instance. The method traverses the owning references all
	 * the way to the top instance.
	 * 
	 * @param instance
	 *            the instance to traverse
	 * @param initialize
	 *            if instance should be initialized or not
	 * @return the root instance
	 */
	public static Instance getRootInstance(Instance instance, boolean initialize) {
		return getParent(instance, null, true, initialize);
	}

	/**
	 * Gets the direct parent of the given instance if any. The returned instance will not be
	 * initialized by default.
	 * <p>
	 * <b>NOTE:</b> The method will return new instance every call and the instance will not have
	 * properties so if needed they should be initialized explicitly or called the method
	 * {@link #getDirectParent(Instance, boolean)} with <code>true</code>.
	 * 
	 * @param instance
	 *            the instance
	 * @return the direct parent
	 */
	public static Instance getDirectParent(Instance instance) {
		return getDirectParent(instance, false);
	}

	/**
	 * Gets the direct parent of the given instance if any.
	 * 
	 * @param instance
	 *            the instance
	 * @param initialize
	 *            if instance should be initialized or not
	 * @return the direct parent
	 */
	public static Instance getDirectParent(Instance instance, boolean initialize) {
		return getParent(instance, null, false, initialize);
	}

	/**
	 * Gets the path of instances for the given instance all the way to the root instance. The first
	 * element in the returned list is the root and the last element is the source instance. If the
	 * given instance is the root then the method will return a list with only one element (the
	 * argument). The returned instances will not be initialized by default.
	 * <p>
	 * <b>NOTE:</b> The method will return new instances every call and the instances will not have
	 * properties so if needed they should be initialized explicitly or called the method
	 * {@link #getParentPath(Instance, boolean)} with <code>true</code>.
	 * 
	 * @param instance
	 *            the source instance to get the path
	 * @return the path
	 */
	public static List<Instance> getParentPath(Instance instance) {
		return getParentPath(instance, false);
	}

	/**
	 * Gets the path of instances for the given instance all the way to the root instance. The first
	 * element in the returned list is the root and the last element is the source instance. If the
	 * given instance is the root then the method will return a list with only one element (the
	 * argument).
	 * 
	 * @param instance
	 *            the source instance to get the path
	 * @param initialize
	 *            if instances should be initialized or not
	 * @return the path
	 */
	public static List<Instance> getParentPath(Instance instance, boolean initialize) {
		return InstanceUtil.getParentPath(instance, initialize, false);
	}

	/**
	 * Constructs a list of instances representing the path to an instance. The first instance being
	 * the root and the last - the instance given to the method. If the {@code includeNonPersisted}
	 * parameter is {@code true}, then instances that are not yet persisted will also be included in
	 * the list.
	 * 
	 * @param instance
	 *            Instance to build path for.
	 * @param initialize
	 *            if instances should be initialized or not
	 * @param includeNonPersisted
	 *            Include instances that are not yet persisted.
	 * @return the path to the instance.
	 */
	public static List<Instance> getParentPath(Instance instance, boolean initialize,
			boolean includeNonPersisted) {
		if (instance == null) {
			return Collections.emptyList();
		}
		LinkedList<Instance> path = new LinkedList<Instance>();
		if (includeNonPersisted || InstanceUtil.isPersisted(instance)) {
			path.add(instance);
		}
		Instance directParent = instance;
		while ((directParent = getDirectParent(directParent, initialize)) != null) {
			path.addFirst(directParent);
		}
		return path;
	}

	/**
	 * Gets the first found context in the instance graph. This means that if the current instance
	 * is a {@link InstanceContext} then the current instance will be returned without other checks.
	 * If this is not the case then call the {@link #getParentContext(Instance)} method.
	 * <p>
	 * <b>NOTE:</b> The method will return new instance every call and the instance will not have
	 * properties so if needed they should be initialized explicitly or called the method
	 * {@link #getContext(Instance, boolean)} with <code>true</code>.
	 * 
	 * @param instance
	 *            the instance to get the first context of
	 * @return the current instance or the first context found in the instance graph on the way back
	 *         or <code>null</code> if no context found at all.
	 */
	public static Instance getContext(Instance instance) {
		return getContext(instance, false);
	}

	/**
	 * Gets the first found context in the instance graph. This means that if the current instance
	 * is a {@link InstanceContext} then the current instance will be returned without other checks.
	 * If this is not the case then call the {@link #getParentContext(Instance)} method.
	 * 
	 * @param instance
	 *            the instance to get the first context of
	 * @param initialize
	 *            the initialize
	 * @return the current instance or the first context found in the instance graph on the way back
	 *         or <code>null</code> if no context found at all.
	 */
	public static Instance getContext(Instance instance, boolean initialize) {
		if (instance instanceof InstanceContext) {
			return instance;
		}
		return getParentContext(instance, initialize);
	}

	/**
	 * Gets the parent context of the given instance. The method will search for
	 * {@link InstanceContext} starting from the first parent of the given instance if any.
	 * <p>
	 * <b>NOTE:</b> The method will return new instance every call and the instance will not have
	 * properties so if needed they should be initialized explicitly or called the method
	 * {@link #getParentContext(Instance, boolean)} with <code>true</code>.
	 * 
	 * @param instance
	 *            the instance
	 * @return the first parent context found or <code>null</code> if not parent or parent contexts
	 *         are found.
	 */
	public static Instance getParentContext(Instance instance) {
		return getParentContext(instance, false);
	}

	/**
	 * Gets the parent context of the given instance. The method will search for
	 * {@link InstanceContext} starting from the first parent of the given instance if any.
	 * 
	 * @param instance
	 *            the instance
	 * @param initialize
	 *            the initialize
	 * @return the first parent context found or <code>null</code> if not parent or parent contexts
	 *         are found.
	 */
	public static Instance getParentContext(Instance instance, boolean initialize) {
		Instance directParent = instance;
		while ((directParent = getDirectParent(directParent, initialize)) != null) {
			if (directParent instanceof InstanceContext) {
				return directParent;
			}
		}
		return null;
	}

	/**
	 * Gets the parent.
	 * 
	 * @param instance
	 *            the instance
	 * @param currentParent
	 *            the current parent
	 * @param recurse
	 *            the recurse
	 * @param initialize
	 *            the initialize
	 * @return the parent
	 */
	private static Instance getParent(Instance instance, Instance currentParent, boolean recurse,
			boolean initialize) {
		if (instance instanceof OwnedModel) {
			Instance parent;
			if (initialize) {
				parent = ((OwnedModel) instance).getOwningInstance();
			} else {
				InstanceReference reference = ((OwnedModel) instance).getOwningReference();
				if (reference == null) {
					// no parent instance set
					return currentParent;
				}
				parent = reference.toInstance();
			}
			if (parent != null) {
				// to prevent infinite recursion
				if (recurse && !EqualsHelper.nullSafeEquals(instance.getId(), parent.getId())) {
					return getParent(parent, parent, recurse, initialize);
				}
				return parent;
			}
		}
		// failed to retrieve parent: parent deleted and the reference is still here or
		// something else
		return currentParent;
	}

	/**
	 * Builds the path of the given instance back to the root or to the given anchor. The path is
	 * build based on the DB ids of the instances and is separated by the
	 * {@link PathHelper#PATH_SPLIT_PATTERN}. The method will return at least on element - the id of
	 * the current element.
	 * 
	 * @param <T>
	 *            the instance type
	 * @param node
	 *            the node to process
	 * @param stopAt
	 *            if provided the path building will stop at a class that is the same or sub class
	 *            of the given class. If not present will build the full path. Useful with use of
	 *            {@link InstanceContext} or
	 *            {@link com.sirma.itt.emf.instance.model.RootInstanceContext}.
	 * @return the string that represents the path
	 */
	public static <T extends Instance> String buildPath(T node, Class<?> stopAt) {
		StringBuilder builder = new StringBuilder();
		builder.append(node.getId());
		Instance directParent = node;
		while ((directParent = getDirectParent(directParent, true)) != null) {
			if ((stopAt != null) && stopAt.isAssignableFrom(directParent.getClass())) {
				break;
			}
			builder.append(PathHelper.PATH_SPLIT_PATTERN).append(directParent.getId());
		}
		return builder.toString();
	}

	/**
	 * Gets of the given type from the given instance hierarchy. if such parent exists it will be
	 * returned, otherwise <code>null</code> will be returned.
	 * 
	 * @param <I>
	 *            the needed parent type
	 * @param parentType
	 *            the parent type class
	 * @param instance
	 *            the instance to get the parent
	 * @return the parent instance or <code>null</code>
	 */
	public static <I> I getParent(Class<I> parentType, Instance instance) {
		if (instance == null) {
			return null;
		}
		Instance directParent = getDirectParent(instance, true);
		if (parentType.isInstance(directParent)) {
			return parentType.cast(directParent);
		}
		return getParent(parentType, directParent);
	}

	/**
	 * Checks if is id persisted. Proxy method for the
	 * {@link SequenceEntityGenerator#isIdPersisted(Serializable)}
	 * 
	 * @param id
	 *            the id
	 * @return true, if is id persisted
	 * @see SequenceEntityGenerator#isIdPersisted(Serializable)
	 */
	public static boolean isIdPersisted(Serializable id) {
		boolean persisted = SequenceEntityGenerator.isIdPersisted(id);
		return persisted;
	}

	/**
	 * Checks if is instance persisted. Proxy method for
	 * {@link SequenceEntityGenerator#isPersisted(com.sirma.itt.emf.domain.model.Entity)}.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if is instance persisted
	 * @see SequenceEntityGenerator#isPersisted(com.sirma.itt.emf.domain.model.Entity)
	 */
	public static boolean isPersisted(Instance instance) {
		boolean persisted = SequenceEntityGenerator.isPersisted(instance);
		return persisted;
	}

	/**
	 * Checks if is not persisted. Reverse of the method {@link #isPersisted(Instance)}.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if is instance persisted
	 */
	public static boolean isNotPersisted(Instance instance) {
		return !isPersisted(instance);
	}

}
