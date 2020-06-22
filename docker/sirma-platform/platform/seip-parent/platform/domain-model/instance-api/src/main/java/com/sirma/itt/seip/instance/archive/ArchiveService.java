package com.sirma.itt.seip.instance.archive;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.DependencyResolver;
import com.sirma.itt.seip.domain.util.DependencyResolvers;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Defines means for storing and retrieving not permanent deleted instances (soft deleted instances). There are defined
 * methods for asynchronous execution.
 *
 * @author BBonev
 */
public interface ArchiveService {

	/**
	 * Schedule deletion of an instance and all dependent objects provided via the
	 * {@link DependencyResolvers#getResolver(Instance)}. This is the method that a public API should call to trigger
	 * instance deletion and all it's children. The method will archive all instances by default. If this is not desired
	 * behavior call {@link #scheduleDelete(Instance, Operation, boolean)} with <code>true</code> for last argument
	 *
	 * @param instance
	 *            to add to the deletionQueue.
	 * @param operation
	 *            the operation that triggered the deletion
	 * @return information about the executed operation. This inclused the ids of the deleted instances, includes the
	 * children of the target instance and the related instances to the deleted one.
	 */
	default InstanceDeleteResult scheduleDelete(Instance instance, Operation operation) {
		return scheduleDelete(instance, operation, false);
	}

	/**
	 * Schedule deletion of an instance and all dependent objects provided via the {@link DependencyResolver}. When
	 * deletion is in progress for an instance calls to this method should not trigger new scheduling but rather update
	 * the currently running process. The method may not perform the actual delete before exiting.
	 *
	 * @param instance
	 *            to add to the deletionQueue.
	 * @param operation
	 *            the operation that triggered the deletion
	 * @param permanent
	 *            if the instance and it's children should be deleted permanently
	 * @return information about the executed operation. This inclused the ids of the deleted instances, includes the
	 * children of the target instance and the related instances to the deleted one.
	 */
	InstanceDeleteResult scheduleDelete(Instance instance, Operation operation, boolean permanent);

	/**
	 * Perform immediate deletion of the given instance. After calling this method the instance will be moved to the
	 * archive store and no longer accessible via general loading services.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 */
	void delete(Instance instance, Operation operation);

	/**
	 * Perform immediate deletion of the given instance. After calling this method the instance will no longer
	 * accessible via general loading services. The instance could be moved to the archive store if the archive
	 * parameter is <code>true</code> or if <code>false</code> will be deleted without option for restore.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param archive
	 *            if <code>true</code> the instance could be restored later
	 */
	void delete(Instance instance, Operation operation, boolean archive);

	/**
	 * Batch load instances by primary database IDs.
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids of the instances that should be loaded
	 * @return collection of found instances
	 */
	<S extends Serializable> Collection<ArchivedInstance> loadByDbId(List<S> ids);

	/**
	 * Batch load instances by primary database IDs.
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids of the instances that should be loaded
	 * @param loadProperties
	 *            to load the full tree properties or only for the root level
	 * @return collection of found instances
	 */
	<S extends Serializable> Collection<ArchivedInstance> loadByDbId(List<S> ids, boolean loadProperties);

	/**
	 * Represents a result of the instance delete operation. Carries information about the deleted instance and it's
	 * child instances and for the instances that were referencing the deleted instances.
	 *
	 * @author BBonev
	 */
	class InstanceDeleteResult {
		private final Collection<String> deletedInstances;
		private final Collection<String> relatedInstances;

		/**
		 * Instantiate new instance with the given immutable relations
		 *
		 * @param deletedInstances the deleted instances
		 * @param relatedInstances the referenced instances to the deleted one
		 */
		public InstanceDeleteResult(Collection<String> deletedInstances,
				Collection<String> relatedInstances) {
			this.deletedInstances = deletedInstances;
			this.relatedInstances = relatedInstances;
		}

		/**
		 * Get the actually deleted instances by the delete operation. This includes the trigger instance and it's
		 * child instances
		 *
		 * @return the deleted instances ids.
		 */
		public Collection<String> getDeletedInstances() {
			if (deletedInstances == null) {
				return Collections.emptyList();
			}
			return deletedInstances;
		}

		/**
		 * The instances that were referring to the deleted instances. These are instances affected by the deletion.
		 * These are instances that if cached are durty and need to be refetched.
		 *
		 * @return the changed instances after the delete operation
		 */
		public Collection<String> getRelatedInstances() {
			if (relatedInstances == null) {
				return Collections.emptyList();
			}
			return relatedInstances;
		}
	}
}
