package com.sirma.itt.seip.domain.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.InitializedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Utility class for instance context and back chain methods. The utility works with
 * {@link com.sirma.itt.seip.convert.TypeConverterUtil} to convert the {@link InstanceReference}s to {@link Instance}
 * objects
 *
 * @author BBonev
 * @deprecated Replaced by {@link InstanceType}
 */
@Deprecated
@Named
@ApplicationScoped
public class InstanceUtil {

	private static DatabaseIdManager idManager;

	/**
	 * Initialize the static utility with {@link DatabaseIdManager} instance
	 *
	 * @param generator
	 *            the generator
	 */
	@Startup(phase = StartupPhase.DEPLOYMENT, order = 1)
	public static void init(DatabaseIdManager generator) {
		idManager = generator;
	}

	/**
	 * Gets the root instance of the given instance. The method traverses the owning references all the way to the top
	 * instance.
	 *
	 * @param instance
	 *            the instance to traverse
	 * @return the root instance
	 */
	public static Instance getRootInstance(Instance instance) {
		return getParent(instance, null, true);
	}

	/**
	 * Gets the direct parent of the given instance if any.
	 *
	 * @param instance
	 *            the instance
	 * @return the direct parent
	 */
	public static Instance getDirectParent(Instance instance) {
		return getParent(instance, null, false);
	}

	/**
	 * Gets the path of instances for the given instance all the way to the root instance. The first element in the
	 * returned list is the root and the last element is the source instance. If the given instance is the root then the
	 * method will return a list with only one element (the argument). The returned link will not contain non persisted
	 * instances.
	 *
	 * @param instance
	 *            the source instance to get the path
	 * @return the path for the given instance
	 * @see #getParentPath(Instance, boolean)
	 */
	public static List<Instance> getParentPath(Instance instance) {
		return getParentPath(instance, false);
	}

	/**
	 * Constructs a list of instances representing the path to an instance. The first instance being the root and the
	 * last - the instance given to the method. If the {@code includeNonPersisted} parameter is {@code true}, then
	 * instances that are not yet persisted will also be included in the list.
	 *
	 * @param instance
	 *            Instance to build path for.
	 * @param includeNonPersisted
	 *            Include instances that are not yet persisted.
	 * @return the path to the instance.
	 */
	@SuppressWarnings("unchecked")
	public static List<Instance> getParentPath(Instance instance, boolean includeNonPersisted) {
		if (instance == null) {
			return Collections.emptyList();
		}
		Deque<Instance> path = new LinkedList<>();
		if (includeNonPersisted || idManager.isPersisted(instance)) {
			path.add(instance);
		}
		Instance directParent = instance;
		while ((directParent = getDirectParent(directParent)) != null) {
			path.addFirst(directParent);
		}
		return (List<Instance>) path;
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
	 * @return the parent
	 */
	private static Instance getParent(Instance instance, Instance currentParent, boolean recurse) {
		if (instance instanceof OwnedModel) {
			// owning instance is with priority over the reference due to the fact that we can have
			// a instance and not have a reference
			Instance parent = ((OwnedModel) instance).getOwningInstance();
			if (parent == null) {
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
					return getParent(parent, parent, recurse);
				}
				return parent;
			}
		}
		// failed to retrieve parent: parent deleted and the reference is still here or
		// something else
		return currentParent;
	}

	/**
	 * Builds the path of the given instance back to the root or to the given anchor. The path is build based on the DB
	 * ids of the instances and is separated by the
	 * {@code com.sirma.itt.seip.definition.util.PathHelper#PATH_SPLIT_PATTERN}. The method will return at least on
	 * element - the id of the current element.
	 *
	 * @param <T>
	 *            the instance type
	 * @param node
	 *            the node to process
	 * @param stopAt
	 *            if provided the path building will stop at a class that is the same or sub class of the given class.
	 *            If not present will build the full path.
	 * @return the string that represents the path
	 */
	public static <T extends Instance> String buildPath(T node, Class<?> stopAt) {
		StringBuilder builder = new StringBuilder();
		builder.append(node.getId());
		Instance directParent = node;
		while ((directParent = getDirectParent(directParent)) != null) {
			if (stopAt != null && stopAt.isAssignableFrom(directParent.getClass())) {
				break;
			}
			builder.append(PathElement.PATH_SEPARATOR).append(directParent.getId());
		}
		return builder.toString();
	}

	/**
	 * Searches for parent class of type the given class in the given instance hierarchy. If such parent exists it will
	 * be returned, otherwise <code>null</code> will be returned.
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
		Instance directParent = getDirectParent(instance);
		if (parentType.isInstance(directParent)) {
			return parentType.cast(directParent);
		}
		return getParent(parentType, directParent);
	}

	/**
	 * Checks if is id persisted. Proxy method for the {@link DatabaseIdManager#isIdPersisted(Serializable)}
	 *
	 * @param id
	 *            the id
	 * @return true, if is id persisted
	 * @see DatabaseIdManager#isIdPersisted(Serializable)
	 */
	public static boolean isIdPersisted(Serializable id) {
		return idManager.isIdPersisted(id);
	}

	/**
	 * Checks if is instance persisted. Proxy method for
	 * {@link DatabaseIdManager#isPersisted(com.sirma.itt.seip.Entity)}.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if is instance persisted
	 * @see DatabaseIdManager#isPersisted(com.sirma.itt.seip.Entity)
	 */
	public static boolean isPersisted(Instance instance) {
		return idManager.isPersisted(instance);
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

	/**
	 * Updated owned model of the target instance if implements the own model at all.
	 *
	 * @param target
	 *            the target instance to update
	 * @param source
	 *            the source instance to use for new parent.
	 * @deprecated Use {@link OwnedModel#setOwnedModel(Object, Instance)}
	 */
	@Deprecated
	public static void updatedOwnedModel(Instance target, Instance source) {
		OwnedModel.setOwnedModel(target, source);
	}

	/**
	 * Creates an {@link InstanceReference} using the provided id and type.
	 *
	 * @param id
	 *            Instance id.
	 * @param type
	 *            Instance type.
	 * @return {@link InstanceReference} instance.
	 */
	public static InstanceReference getInstanceReference(String id, String type) {
		InstanceReference reference = TypeConverterUtil.getConverter().convert(InstanceReference.class, type);
		reference.setIdentifier(id);
		return reference;
	}

	/**
	 * Creates and instance using the provided instance id and type.
	 *
	 * @param id
	 *            Instance id.
	 * @param type
	 *            Instance type.
	 * @return {@link Instance}
	 */
	public static Instance getInstance(String id, String type) {
		InstanceReference reference = InstanceUtil.getInstanceReference(id, type);
		InitializedInstance instance = TypeConverterUtil.getConverter().convert(InitializedInstance.class, reference);
		return instance.getInstance();
	}
}
