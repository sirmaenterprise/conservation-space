package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.TransactionScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Transactional scope buffer for changed instances.
 *
 * @author BBonev
 */
@TransactionScoped
public class ChangedInstanceBuffer implements Serializable {

	private static final long serialVersionUID = 1711300318180984765L;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Set<Serializable> changedInstances = new HashSet<>();

	/**
	 * Register that instance is modified. The argument could be an {@link InstanceReference}, {@link Instance} or just
	 * instance id as {@link String}.
	 *
	 * @param item
	 *            the item
	 */
	public void addChange(Object item) {
		if (item == null) {
			return;
		}
		if (item instanceof InstanceReference) {
			addNonNullValue(changedInstances, ((InstanceReference) item).getId());
		} else
		if (item instanceof Instance) {
			addNonNullValue(changedInstances, ((Instance) item).getId());
		} else if (item instanceof String) {
			changedInstances.add((Serializable) item);
		} else {
			LOGGER.warn("Tried to add unsupported change item: {}", item);
		}
	}

	/**
	 * Gets all collected instance identifiers.
	 *
	 * @return the changes or empty collection
	 */
	public synchronized Collection<Serializable> getChanges() {
		return changedInstances;
	}

	/**
	 * Gets all collected instance identifiers and reset the state. Any call after this method will return emtpy
	 * collection unless new changes are added before that.
	 *
	 * @return the changes or empty collection
	 */
	public synchronized Collection<Serializable> getChangesAndReset() {
		List<Serializable> changes = new ArrayList<>(changedInstances);
		changedInstances.clear();
		return changes;
	}
}
