package com.sirma.itt.seip.mail;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;

/**
 * Concrete implementation of the {@link MailResourceProvider}.
 *
 * @author A. Kunchev
 */
@Singleton
public class MailResourceProviderImpl implements MailResourceProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private PermissionService permissionService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private ResourceService resourceService;

	@Inject
	private CodelistService codelistService;

	@Inject
	private MailNotificationHelperService mailNotificationHelperService;

	@Override
	public String getDisplayableProperty(Instance instance, Resource user, String property) {
		if (instance != null && user != null && StringUtils.isNotBlank(property)) {
			PropertyDefinition definitionProperty = definitionService.getProperty(property, instance);
			if (definitionProperty != null) {
				String propertyValue = instance.getString(property);
				Integer codelist = definitionProperty.getCodelist();
				if (StringUtils.isNotBlank(propertyValue) && codelist != null) {
					CodeValue codeValue = codelistService.getCodeValue(codelist, propertyValue);
					return codeValue != null
							? (String) codeValue.get(mailNotificationHelperService.getUserLanguage(user))
							: propertyValue;
				}
			}
			LOGGER.warn("Instance with id [{}] does not have a [{}] field definition.", instance.getId(), property);
			return instance.getIdentifier();
		}
		return "";
	}

	@Override
	public String getUserRole(Instance instance, Resource user) {
		if (instance != null && user != null) {
			RoleIdentifier role = permissionService
					.getPermissionAssignment(instance.toReference(), user.getId())
						.getRole();
			if (role != null) {
				return role.getIdentifier();
			}
		}
		return null;
	}

	@Override
	public String getLabel(String labelId) {
		if (StringUtils.isNotBlank(labelId)) {
			return labelProvider.getValue(labelId);
		}
		return null;
	}

	@Override
	public Resource getResource(String id, ResourceType type) {
		return resourceService.getResource(id, type);
	}

	@Override
	public Resource getResource(Serializable id) {
		return resourceService.getResource(id);
	}
}