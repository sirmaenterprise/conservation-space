package com.sirma.itt.seip.permissions.role.provider;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.definition.DefinitionManagementService;
import com.sirma.itt.seip.definition.jaxb.JAXBHelper;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.permissions.ExternalRoleParser;
import com.sirma.itt.seip.permissions.model.RoleInstance;
import com.sirma.itt.seip.permissions.model.jaxb.RoleDefinition;
import com.sirma.itt.seip.permissions.model.jaxb.Roles;
import com.sirma.itt.seip.permissions.model.jaxb.SecurityXmlSchemaProvider;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.annotation.RunAsSystem;

/**
 * Role provider used to load the provider definitions.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = RoleProviderExtension.TARGET_NAME, order = 0.6)
public class ExternalDefinitionRoleProvider implements RoleProviderExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalDefinitionRoleProvider.class);
	@Inject
	private DefinitionManagementService managementService;

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private ExternalRoleParser roleParser;

	@Override
	@RunAsSystem
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> chainedRoles) {
		List<FileDescriptor> definitions = managementService.getDefinitions(RoleInstance.class);

		List<RoleDefinition> roleDefinitions = readDefinitions(definitions);
		roleParser.processExternalRoles(chainedRoles, roleDefinitions);
		return chainedRoles;
	}

	/**
	 * Read definitions from the given descriptors
	 *
	 * @param files
	 *            the files
	 * @return the list
	 */
	private List<RoleDefinition> readDefinitions(Collection<FileDescriptor> files) {
		List<Message> errors = new LinkedList<>();
		List<RoleDefinition> roleDefinitions = new LinkedList<>();
		for (FileDescriptor descriptor : files) {
			File file = getContent(descriptor);
			if (file == null) {
				continue;
			}
			LOGGER.debug("Loading file: {}", file.getName());
			if (JAXBHelper.validateFile(file, SecurityXmlSchemaProvider.ROLES, errors)) {
				Roles roles = JAXBHelper.load(file, Roles.class);
				if (roles != null) {
					roleDefinitions.addAll(roles.getRole());
				}
			} else {
				LOGGER.warn("File [{}] is not valid and will not be loaded!", file.getName());
			}
		}

		if (!errors.isEmpty()) {
			LOGGER.warn("Found {} errors while loading roles", errors.size());
		}
		return roleDefinitions;
	}

	private File getContent(FileDescriptor descriptor) {
		File tempFile = tempFileProvider.createTempFile("permissions", null);
		try {
			descriptor.writeTo(tempFile);
		} catch (IOException e) {
			LOGGER.warn("Could not download permission definition: ", descriptor.getId(), e);
			tempFileProvider.deleteFile(tempFile);
			tempFile = null;
		}
		return tempFile;
	}

}
