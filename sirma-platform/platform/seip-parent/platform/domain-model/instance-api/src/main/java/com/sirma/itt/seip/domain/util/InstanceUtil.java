package com.sirma.itt.seip.domain.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.tasks.TransactionMode;

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
@Singleton
public class InstanceUtil {

	private static InstanceUtil utilInstance;

	private DatabaseIdManager idManager;
	private InstanceContextService contextService;

	/**
	 * Instantiate InstanceUtil instance and initialize it's dependent services. The instance is used only to store the
	 * dependent services and should not be used directly
	 *
	 * @param idManager the id manager to set
	 * @param contextService the contextual service to set
	 */
	@Inject
	public InstanceUtil(DatabaseIdManager idManager, InstanceContextService contextService) {
		this.idManager = idManager;
		this.contextService = contextService;
	}

	/**
	 * Initialize the static utility instance
	 *
	 * @param instanceUtil
	 *            the util instance
	 */
	@Startup(phase = StartupPhase.DEPLOYMENT, order = 1, transactionMode = TransactionMode.NOT_SUPPORTED)
	public static void init(InstanceUtil instanceUtil) {
		utilInstance = instanceUtil;
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
		if (instance != null) {
			return utilInstance.contextService.getRootContext(instance).map(InstanceReference::toInstance).orElse(null);
		}
		return null;
	}

	/**
	 * Gets the direct parent of the given instance if any.
	 *
	 * @param instance
	 *            the instance
	 * @return the direct parent
	 */
	public static Instance getDirectParent(Instance instance) {
		if (instance != null) {
			return utilInstance.contextService.getContext(instance).map(InstanceReference::toInstance).orElse(null);
		}
		return null;
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
		if (includeNonPersisted || utilInstance.idManager.isPersisted(instance)) {
			path.add(instance);
		}
		Instance directParent = instance;
		while ((directParent = getDirectParent(directParent)) != null) {
			path.addFirst(directParent);
		}
		return (List<Instance>) path;
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
		return utilInstance.idManager.isIdPersisted(id);
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
		return utilInstance.idManager.isPersisted(instance);
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
