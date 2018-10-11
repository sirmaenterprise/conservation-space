package com.sirma.sep.bpm.camunda.bpmn.execution.listeners;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;

/**
 * Abstract class used for BPM Status change listeners.
 * 
 * @author hlungov
 */
public abstract class BaseStatusChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	protected LinkService linkService;
	@Inject
	protected HeadersService headersService;
	@Inject
	protected InstanceTypeResolver instanceTypeResolver;
	@Inject
	protected DomainInstanceService domainInstanceService;

	protected void validateParameters(Object... parameters) {
		for (Object parameter : parameters) {
			Objects.requireNonNull(parameter, "Parameter: " + parameter + " is a required!");
		}
	}

	protected void doStatusChange(String businessId, Instance sourceObject, String relationId, String statusValue) {
		List<LinkReference> links = linkService.getLinks(sourceObject.toReference(), relationId);
		List<String> related = new ArrayList<>(links.size());
		for (LinkReference linkReference : links) {
			InstanceReference relatedTo = linkReference.getTo();
			// check the direction of relation
			if (!businessId.equals(relatedTo.getId())) {
				related.add(relatedTo.getId());
			} else {
				related.add(linkReference.getFrom().getId());
			}
		}
		if (related.isEmpty()) {
			LOGGER.warn("No instances are selected for automatic status change! Check your relation: {} to object: {}",
					relationId, sourceObject.getId());
			return;
		}
		// batch load all instances - needed with their loaded properties
		Collection<Instance> resolvedInstances = instanceTypeResolver.resolveInstances(related);
		for (Instance resolved : resolvedInstances) {
			// add uid operation and remove _ as splitter for business id.
			String actionId = "dynamicBPMNStatusChange-" + relationId.replace("_", "-");
			save(resolved, actionId, statusValue);
		}
	}

	/**
	 * Used for saving the instance in {@link #doStatusChange(String, Instance, String, String)} .
	 *
	 * @param instance
	 *            - the instance to save
	 * @param actionId
	 *            - the action id for save operation
	 * @param statusValue
	 *            - the new status value
	 */
	public abstract void save(Instance instance, String actionId, String statusValue);
}
