package com.sirma.itt.seip.instance.context;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Responsible for restoring/retrieving/changing an instance context. The service work with any type of instance key -
 * ({@link Instance}, {@link InstanceReference} or its db id). <br>
 *     The method for changing the context does not actually change the context in the database but rather updates the
 * given instance parameter. In order for the changes to be persisted the instance should be persisted using the proper
 * service.
 *
 * @author svelikov
 * @author BBonev
 * @author bbanchev
 */
public interface InstanceContextService {

	/** Relation used to define parent to child simple link (non transitive). */
	String TREE_PARENT_TO_CHILD = "emf:parentOf";

	/** Relation used to define child to parent simple link (non transitive). */
	String HAS_PARENT = "emf:hasParent";

	/** Relation used to define parent to child link (transitive). */
	String HAS_CHILD_URI = "emf:hasChild";

	/** Relation used to define child to parent link (transitive). */
	String PART_OF_URI = "ptop:partOf";

	/**
	 * Finds the context of given instance. The value might be obtained either from cache if bound or by querying
	 * database.
	 *
	 * @param instance
	 *            the instance is any type of instance ({@link Instance}, {@link InstanceReference} or its db id)
	 * @return the context or {@link Optional#empty()} if this instance has no context
	 */
	Optional<InstanceReference> getContext(Serializable instance);

	/**
	 * Finds the context path of given instance. The value might be obtained either from cache or by querying database.
	 * Path is ordered in chain of context's contexts.
	 *
	 * @param instance
	 *            the instance is any type of instance ({@link Instance}, {@link InstanceReference} or its db id)
	 * @return the context paths as ordered list. If instance has no context list is empty
	 */
	default List<InstanceReference> getContextPath(Serializable instance) {
		LinkedList<InstanceReference> fullContext = new LinkedList<>();
		Optional<InstanceReference> context = getContext(instance);
		while (context.isPresent()) {
			fullContext.addFirst(context.get());
			context = getContext(context.get());
		}
		return fullContext;
	}

	/**
	 * Finds the context path of given instance and includes the source instance as last child. The value might be
	 * obtained either from cache or by querying database. Path is ordered as specified in
	 * {@link #getContextPath(Serializable)}.
	 *
	 * @param instance
	 *            the instance is any type of instance ({@link Instance}, {@link InstanceReference} or its db id)
	 * @return the context paths as ordered list. If instance has no context list is single entry the source instance.
	 *         If instance is not valid list might be empty
	 */
	List<InstanceReference> getFullPath(Serializable instance);

	/**
	 * Finds the root context of given instance. The value might be obtained either from cache or by querying database.
	 *
	 * @param instance
	 *            the instance is any type of instance ({@link Instance}, {@link InstanceReference} or its db id)
	 * @return the root context or {@link Optional#empty()} if this instance has no context
	 */
	Optional<InstanceReference> getRootContext(Serializable instance);

	/**
	 * Binds the context of given instance. This operations updates only cache and properties of the provided instance
	 * {@link Instance}. No changes will be stored to the database. In order to for the changes to take affect the
	 * instance should be persisted using the proper service for that.
	 *
	 * @param instance
	 *            the instance to set the context to
	 * @param context
	 *            is the new context to set if overwrite is applied. Value might be of any type of instance
	 *            ({@link Instance}, {@link InstanceReference} or its db id). Might be null to clean context
	 *            information.
	 */
	void bindContext(Instance instance, Serializable context);

	/**
	 * Checks if context is changed.
	 *
	 * @param instance
	 *            to be checked.
	 * @return true if context of <code>instance</code> is changed.
	 */
	boolean isContextChanged(Instance instance);

}
