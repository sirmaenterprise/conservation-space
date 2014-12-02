package com.sirma.itt.pm.notification;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.cmf.notification.AbstractMailNotificationContext;
import com.sirma.itt.cmf.services.impl.MailNotificationHelperService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * The ProjectAssignmentMailDelegate is notification wrapper for project assignments events
 */
public class ProjectAssignmentNotificationContext extends AbstractMailNotificationContext {

	/** The context. */
	private Instance context;

	/** The user. */
	private Resource user;

	/**
	 * Instantiates a new project assignment mail delegate.
	 * 
	 * @param helperService
	 *            the helper service
	 * @param context
	 *            the context
	 * @param user
	 *            the user
	 */
	public ProjectAssignmentNotificationContext(MailNotificationHelperService helperService,
			ProjectInstance context, Resource user) {
		super(helperService);
		this.context = context;
		this.user = user;
	}

	@Override
	public String getTemplateId() {
		return "project_assign.ftl";
	}

	@Override
	public String getSubject() {
		return helperService.getLabelProvider().getValue("notification.project.assignment.subject");
	}

	@Override
	public Collection<Resource> getSendTo() {
		return Collections.singletonList(user);
	}

	@Override
	public Map<? extends String, ? extends Object> getModel() {
		Map<String, Object> modelMap = new HashMap<>(5);
		modelMap.put("user", user);
		modelMap.put("rootContext", context);
		modelMap.put("role", helperService.getResourceService().getResourceRole(context, user)
				.getRole());
		return modelMap;
	}

}
