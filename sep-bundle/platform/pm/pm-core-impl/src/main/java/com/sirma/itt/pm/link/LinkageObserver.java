package com.sirma.itt.pm.link;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.event.cases.AfterCasePersistEvent;
import com.sirma.itt.cmf.event.document.AfterDocumentPersistEvent;
import com.sirma.itt.cmf.event.task.standalone.AfterStandaloneTaskPersistEvent;
import com.sirma.itt.cmf.event.task.workflow.AfterTaskPersistEvent;
import com.sirma.itt.cmf.services.adapter.CMFPermissionAdapterService;
import com.sirma.itt.cmf.services.impl.MailNotificationHelperService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.event.instance.GenericMovedEvent;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.event.resource.ResourcesChangedEvent;
import com.sirma.itt.emf.exceptions.DmsRuntimeException;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.mail.notification.MailNotificationService;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.util.DigestUtils;
import com.sirma.itt.emf.util.PathHelper;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.notification.ProjectAssignmentNotificationContext;

/**
 * Listener for linking logic for each registered instance event.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class LinkageObserver {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(LinkageObserver.class);
	/** The link service. */
	@Inject
	private LinkService linkService;
	/** permission adapter. */
	@Inject
	private CMFPermissionAdapterService permissionAdapterService;

	/** The notification service. */
	@Inject
	private MailNotificationService notificationService;

	/** The helper service. */
	@Inject
	private MailNotificationHelperService helperService;

	/** The properties service. */
	@Inject
	private PropertiesService propertiesService;

	/**
	 * On creation case link to root object.
	 * 
	 * @param event
	 *            the event to handle
	 */
	public void onCaseCreated(@Observes AfterCasePersistEvent event) {
		Instance parent = getParent(event.getInstance());
		associate(event.getInstance(), parent, "project_case");
	}

	/**
	 * On creation document link to root object.
	 * 
	 * @param event
	 *            the event to handle
	 */
	public void onDocumentCreated(@Observes AfterDocumentPersistEvent event) {
		Instance parent = getParent(event.getInstance());
		associate(event.getInstance(), parent, "project_documents");
	}

	/**
	 * On attaching a document to a case/project.
	 * 
	 * @param event
	 *            the event
	 */
	public void onDocumentAttach(@Observes InstanceAttachedEvent<Instance> event) {
		Instance child = event.getChild();
		if (child instanceof DocumentInstance) {
			Instance parent = getParent(event.getInstance());
			associate(child, parent, buildAssocName(event.getInstance(), event.getChild()));
		}
	}

	/**
	 * On detaching a document to a case/project.
	 * 
	 * @param event
	 *            the event
	 */
	public void onDocumentDetach(@Observes InstanceDetachedEvent<Instance> event) {
		Instance child = event.getChild();
		if (child instanceof DocumentInstance) {
			Instance parent = getParent(event.getInstance());
			dissociate(child, parent, buildAssocName(event.getInstance(), event.getChild()));
		}
	}

	/**
	 * Builds unique association name for the given parent/child up to the.
	 * 
	 * @param instance
	 *            the instance direct parent where the child is attached to/detached from
	 * @param child
	 *            the child being attached/detached
	 * @return the string {@link RootInstanceContext} of the given hierarchy. Builds a digest name
	 *         from the full path from child to root to create unique association name when linking
	 *         the child to the root
	 */
	private String buildAssocName(Instance instance, Instance child) {
		StringBuilder builder = new StringBuilder();
		String path = InstanceUtil.buildPath(instance, RootInstanceContext.class);
		builder.append(path).append(PathHelper.PATH_SPLIT_PATTERN).append(child.getId());
		return DigestUtils.calculateDigest(builder.toString());
	}

	/**
	 * On creation workflow task link to root object.
	 * 
	 * @param event
	 *            the event to handle
	 */
	public void onTaskCreated(@Observes AfterTaskPersistEvent event) {
		Instance parent = getParent(event.getInstance());
		associate(event.getInstance(), parent, "project_task");
	}

	/**
	 * On creation standalone task link to root object.
	 * 
	 * @param event
	 *            the event to handle
	 */
	public void onStandaloneTaskCreated(@Observes AfterStandaloneTaskPersistEvent event) {
		Instance parent = getParent(event.getInstance());
		associate(event.getInstance(), parent, "project_standalonetask");
	}

	/**
	 * Observes move event to reassociate an instance to new parent.
	 * 
	 * @param event
	 *            is the event with data
	 */
	public void onInstanceMoved(@Observes GenericMovedEvent event) {
		Instance instance = event.getInstance();
		Instance from = event.getNewParent();
		Instance oldFrom = event.getOldParent();
		String assocName = null;
		if (instance instanceof StandaloneTaskInstance) {
			assocName = "project_standalonetask";
		} else if (instance instanceof TaskInstance) {
			assocName = "project_task";
		} else if (instance instanceof DocumentInstance) {
			assocName = "project_documents";
		} else if (instance instanceof CaseInstance) {
			assocName = "project_case";
		}
		linkService.reassociate(from, instance, oldFrom, assocName);
	}

	/**
	 * Internal associate using {@link LinkService}.
	 *
	 * @param child
	 *            the association to
	 * @param parent
	 *            the association from
	 * @param assocName
	 *            optional association name
	 */
	private void associate(Instance child, Instance parent, String assocName) {
		// disabled mainly for imported documents and folders
		if (RuntimeConfiguration
				.isSet(RuntimeConfigurationProperties.DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS)) {
			return;
		}
		if (parent != null) {
			linkService.associate(parent, child, assocName);
		}
	}

	/**
	 * Internal dissociate using {@link LinkService}.
	 * 
	 * @param child
	 *            the association to
	 * @param parent
	 *            the association from
	 * @param assocName
	 *            optional association name
	 */
	private void dissociate(Instance child, Instance parent, String assocName) {
		if (parent != null) {
			linkService.dissociate(parent, child, assocName);
		}
	}

	/**
	 * On resource changed update membership in dms.
	 * 
	 * @param event
	 *            is the event
	 */
	@Secure(runAsSystem = true)
	public void onResourcesChanged(@Observes ResourcesChangedEvent event) {
		if (!(event.getInstance() instanceof ProjectInstance)) {
			return;
		}
		ProjectInstance instance = (ProjectInstance) event.getInstance();
		if (instance.getProperties() == null) {
			propertiesService.loadProperties(instance);
		}
		List<Resource> currentResources = event.getNewResources();
		try {
			permissionAdapterService.updateMembers(instance, currentResources);

			List<Resource> notifiable = new ArrayList<Resource>(event.getNewResources());
			// send notification for the new users only on project assignment
			sendMailInternalToUsers(instance, notifiable, event.getOldResources());

		} catch (DMSException e) {
			throw new DmsRuntimeException(e);
		}
	}

	/**
	 * Recursive iterate resources and send mail to users.
	 * 
	 * @param instance
	 *            is the project instance
	 * @param notifiable
	 *            is the list of users, groups mixed
	 * @param oldResources
	 *            resources is the current list of resources, to be skipped during sending
	 *            notification
	 */
	private void sendMailInternalToUsers(ProjectInstance instance, Collection<Resource> notifiable,
			List<Resource> oldResources) {
		for (Resource resource : notifiable) {
			if (resource.getType() == ResourceType.GROUP) {
				sendMailInternalToUsers(instance, helperService.getResourceService()
						.getContainedResources(resource), oldResources);
			} else if (!oldResources.contains(resource)) {
				try {
					ProjectAssignmentNotificationContext assignmentMailDelegate = new ProjectAssignmentNotificationContext(
							helperService, instance, resource);
					notificationService.sendEmail(assignmentMailDelegate);
				} catch (Exception e) {
					LOGGER.warn("Failed to send mail to " + resource.getDisplayName(), e);
				}
			}
		}
	}

	/**
	 * Get the root of the event instance object.
	 * 
	 * @param instance
	 *            the instance
	 * @return the parent or null if not found with {@link InstanceUtil}
	 */
	private Instance getParent(Instance instance) {
		Instance rootInstance = InstanceUtil.getRootInstance(instance, true);
		// if the root instance is not a root instance context (project) then we
		// do not want to link
		// it
		return rootInstance instanceof RootInstanceContext ? rootInstance : null;
	}

}
