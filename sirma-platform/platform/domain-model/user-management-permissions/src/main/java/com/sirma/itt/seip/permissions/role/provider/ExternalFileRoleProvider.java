package com.sirma.itt.seip.permissions.role.provider;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.definition.jaxb.JAXBHelper;
import com.sirma.itt.seip.permissions.ExternalRoleParser;
import com.sirma.itt.seip.permissions.model.jaxb.RoleDefinition;
import com.sirma.itt.seip.permissions.model.jaxb.Roles;
import com.sirma.itt.seip.permissions.model.jaxb.SecurityXmlSchemaProvider;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Role provider used to load the provider definitions.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = RoleProviderExtension.TARGET_NAME, order = 0.5)
public class ExternalFileRoleProvider implements RoleProviderExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalFileRoleProvider.class);
	private static final IOFileFilter XML_FILE_FILTER = new XmlIOFileFilter();

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.role.mapping.location", type = File.class, label = "Location for the role mapping", system = true, sensitive = true, shared = false)
	private ConfigurationProperty<File> mappingLocation;

	@Inject
	private ExternalRoleParser roleParser;

	@Override
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> chainedRoles) {
		if (!mappingLocation.isSet()) {
			LOGGER.info("External permission provider not configured. Extension is disabled!");
			return chainedRoles;
		}

		if (!mappingLocation.get().isDirectory()) {
			LOGGER.warn("The external configuration location {} does not points to a directory",
					mappingLocation.get().getAbsolutePath());
			return chainedRoles;
		}

		Collection<File> files = FileUtils.listFiles(mappingLocation.get(), XML_FILE_FILTER, null);

		LOGGER.trace("Loading role definition files from: {}", mappingLocation.get().getAbsolutePath());
		List<RoleDefinition> roleDefinitions = readDefinitions(files);
		return roleParser.processExternalRoles(chainedRoles, roleDefinitions);
	}

	/**
	 * Read definitions.
	 *
	 * @param files
	 *            the files
	 * @return the list
	 */
	private static List<RoleDefinition> readDefinitions(Collection<File> files) {
		List<Message> errors = new LinkedList<>();
		List<RoleDefinition> roleDefinitions = new LinkedList<>();
		for (File file : files) {
			LOGGER.trace("Loading file: {}", file.getName());
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

	/**
	 * XML file filter
	 *
	 * @author BBonev
	 */
	private static final class XmlIOFileFilter implements IOFileFilter {
		XmlIOFileFilter() {
			// prevent synthetic error
		}

		@Override
		public boolean accept(File dir, String name) {
			return false;
		}

		@Override
		public boolean accept(File file) {
			return file.canRead() && file.getName().endsWith("xml");
		}
	}

}
