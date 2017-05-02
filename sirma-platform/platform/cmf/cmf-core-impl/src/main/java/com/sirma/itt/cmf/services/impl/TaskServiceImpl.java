package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.WorkLogEntry;
import com.sirma.itt.cmf.constants.LinkConstantsCmf;
import com.sirma.itt.cmf.event.task.worklog.AddedWorkLogEntryEvent;
import com.sirma.itt.cmf.event.task.worklog.DeleteWorkLogEntryEvent;
import com.sirma.itt.cmf.event.task.worklog.UpdatedWorkLogEntryEvent;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Default implementation for the {@link TaskService} interface. The service tries to act as a proxy for standalone and
 * workflow task services.
 *
 * @author BBonev
 */
@ApplicationScoped
public class TaskServiceImpl implements TaskService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private LinkService linkService;

	@Inject
	private ResourceService resourceService;

	@Inject
	private EventService eventService;

	@Inject
	private SecurityContext securityContext;

	@Override
	@Transactional(TxType.REQUIRED)
	public Serializable logWork(InstanceReference task, String userId, Map<String, Serializable> loggedData) {
		if (task == null || userId == null || loggedData == null) {
			LOGGER.warn("Missing required input data to log work on task: taskRef=" + task + ", user=" + userId
					+ ", data=" + loggedData);
			return null;
		}

		Resource resource = resourceService.getResource(userId, ResourceType.USER);
		if (resource == null) {
			LOGGER.warn("No user found in the system with name=" + userId);
			return null;
		}

		Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.enable();
		Options.GENERATE_RANDOM_LINK_ID.enable();
		try {
			Pair<Serializable, Serializable> pair = linkService.link(task,
					typeConverter.convert(InstanceReference.class, resource), LinkConstantsCmf.LOGGED_WORK, null,
					loggedData);
			if (pair.getFirst() != null) {
				eventService.fire(new AddedWorkLogEntryEvent(task, pair.getFirst(), userId, loggedData));
			}
			return pair.getFirst();
		} finally {
			Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.disable();
			Options.GENERATE_RANDOM_LINK_ID.disable();
		}
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean updateLoggedWork(Serializable id, Map<String, Serializable> loggedData) {
		if (id == null) {
			LOGGER.warn("The id is required parameter to update the logged data.");
			return false;
		}
		if (loggedData == null || loggedData.isEmpty()) {
			LOGGER.warn("No data to update was provided for id=" + id);
			return false;
		}
		Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.enable();
		try {
			LinkReference linkReference = linkService.getLinkReference(id);
			if (linkReference == null) {
				LOGGER.warn("The work log entry with id=" + id + " does not exists!");
				return false;
			}
			String originalUserId = linkReference.getTo().getIdentifier();
			Map<String, Serializable> oldLoggedData = linkReference.getProperties();

			if (linkService.updateLinkProperties(id, loggedData)) {
				// fire event for the executed operation
				String updatingUserId = securityContext.getAuthenticated().getIdentityId();
				eventService.fire(new UpdatedWorkLogEntryEvent(linkReference.getFrom(), id, updatingUserId,
						originalUserId, loggedData, oldLoggedData));
				return true;
			}
		} finally {
			Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.disable();
		}
		LOGGER.warn("Failed to update logged work data for entry with id=" + id);
		return false;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean deleteLoggedWork(Serializable id) {
		if (id == null) {
			LOGGER.warn("The id is required parameter to remove the logged data.");
			return false;
		}
		LinkReference linkReference = linkService.getLinkReference(id);
		if (linkReference == null) {
			LOGGER.warn("No work log entry with id=" + id + " found for deletion!");
			return false;
		}
		linkService.removeLinkById(id);
		// notify for deletion
		eventService.fire(new DeleteWorkLogEntryEvent(linkReference.getFrom(), id,
				linkReference.getTo().getIdentifier(), linkReference.getProperties()));
		return true;
	}

	@Override
	public List<WorkLogEntry> getLoggedData(Instance instance) {
		if (instance == null) {
			return Collections.emptyList();
		}
		Options.LOAD_LINK_PROPERTIES.enable();
		List<LinkReference> references;
		try {
			references = linkService.getLinks(instance.toReference(), LinkConstantsCmf.LOGGED_WORK);
		} finally {
			Options.LOAD_LINK_PROPERTIES.disable();
		}
		List<WorkLogEntry> workLogEntries = new ArrayList<>(references.size());
		for (LinkReference linkInstance : references) {
			WorkLogEntry entry = new WorkLogEntry();
			entry.setId(linkInstance.getId());
			String userDbId = linkInstance.getTo().getIdentifier();
			Resource resource = (Resource) resourceService.loadByDbId(userDbId);
			if (resource != null) {
				entry.setUser(resource.getName());
				entry.setUserDisplayName(resource.getDisplayName());
			} else {
				entry.setUser("unknow.user");
				entry.setUserDisplayName("Unknow user");
			}
			entry.setProperties(linkInstance.getProperties());
			workLogEntries.add(entry);
		}
		return workLogEntries;
	}
}
