package com.sirmaenterprise.sep.roles.validation;

import java.io.File;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.exceptions.DefinitionValidationException;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.model.RoleInstance;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.sep.xml.JAXBHelper;
import com.sirmaenterprise.sep.roles.jaxb.RoleDefinition;
import com.sirmaenterprise.sep.roles.jaxb.Roles;
import com.sirmaenterprise.sep.roles.jaxb.SecurityXmlSchemaProvider;

/**
 * Validates permission roles definitions.
 * 
 * @author Vilizar Tsonev
 */
@Singleton
public class RolesValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String MISSING_DEFINITIONS_MESSAGE =
			"Missing permissions definitions! Permissions definitions should be uploaded for these roles: "
					+ buildRolesMessage() + ". Definition for administrator role is optional.";

	private static final Comparator<RoleInstance> ROLE_COMPARATOR = new RoleComparator();

	@Inject
	private ObjectMapper mapper;

	/**
	 * Validates the provided list of role definition files.</br>
	 * XSD schema validation is performed first. If it passes, the definitions get compiled and logical validations are
	 * performed - for circular dependencies, missing dependencies and more.
	 * 
	 * @param definitions is the list of definition files
	 * @return a list of collected errors
	 */
	public List<String> validate(List<File> definitions) {
		List<Message> xsdErrors = new LinkedList<>();
		List<RoleDefinition> roleDefinitions = new LinkedList<>();

		for (File file : definitions) {
			if (JAXBHelper.validateFile(file, SecurityXmlSchemaProvider.ROLES, xsdErrors)) {
				Roles roles = JAXBHelper.load(file, Roles.class);
				if (roles != null) {
					roleDefinitions.addAll(roles.getRole());
				}
			} else {
				LOGGER.warn("File [{}] is not valid and will not be loaded!", file.getName());
			}
		}
		if (!xsdErrors.isEmpty()) {
			return xsdErrors
					.stream()
					.map(Message::getMessage)
					.collect(Collectors.toList());
		}

		List<RoleInstance> roleInstances = convertToInternalModel(roleDefinitions);
		try {
			validateModel(roleInstances);
		} catch (DefinitionValidationException e) {
			return Collections.singletonList(e.getMessage());
		}

		return validateRoles(roleInstances);
	}

	private List<RoleInstance> convertToInternalModel(List<RoleDefinition> roleDefinitions) {
		List<RoleInstance> converted = new ArrayList<>(roleDefinitions.size());
		for (RoleDefinition roleDefinition : roleDefinitions) {
			converted.add(mapper.map(roleDefinition, com.sirma.itt.seip.permissions.model.RoleInstance.class));
		}
		return converted;
	}

	private void validateModel(List<RoleInstance> roles) {
		Map<String, RoleInstance> mapExternal = CollectionUtils.createLinkedHashMap(roles.size());
		Map<String, RoleInstance> mapInternal = CollectionUtils.createLinkedHashMap(roles.size());

		for (RoleInstance instance : roles) {
			if (instance.isExternal()) {
				addToMappedModel(mapExternal, instance);
			} else {
				addToMappedModel(mapInternal, instance);
			}
		}

		Collections.sort(roles, ROLE_COMPARATOR);

		validateForCycles(roles, mapExternal, mapInternal);
	}

	private List<String> validateRoles(List<RoleInstance> roleInstances) {
		List<String> publicRolesIds = SecurityModel.BaseRoles.PUBLIC.stream().map(RoleIdentifier::getIdentifier)
				.collect(Collectors.toList());
		String adminRoleId = SecurityModel.BaseRoles.ADMINISTRATOR.getIdentifier();

		int roleCount = 0;

		for (RoleInstance roleInstance : roleInstances) {
			String roleId = roleInstance.getRoleId().getIdentifier();
			if (publicRolesIds.contains(roleId) || adminRoleId.equals(roleId)) {
				roleCount++;
			}
		}

		if (roleCount < publicRolesIds.size() || roleCount > publicRolesIds.size() + 1) {
			return Collections.singletonList(MISSING_DEFINITIONS_MESSAGE);
		}

		return Collections.emptyList();
	}

	private void checkRoleDependencies(RoleInstance role, Map<String, RoleInstance> mappedExternal,
			Map<String, RoleInstance> mappedInternal, Collection<String> dependencyChain) {
		for (String include : role.getInclude()) {
			// create new sub chain to test each dependency branch
			Collection<String> localChain = new LinkedList<>(dependencyChain);

			String[] split = include.split("/");
			String roleId = split[0];
			if (role.getRoleId().getIdentifier().equalsIgnoreCase(roleId)
					&& checkForMatchingRole(roleId, role.isExternal(), mappedExternal, mappedInternal)) {
				throw new DefinitionValidationException(
						"Found internal role " + role.getRoleId() + " to depend on itself");
			}
			if (localChain.contains(roleId)) {
				checkForCircularDependencies(role, roleId, mappedExternal, mappedInternal, localChain);
			}
			localChain.add(roleId);

			RoleInstance dependecy;
			if (role.isExternal()) {
				dependecy = mappedInternal.get(roleId);
				if (dependecy == null) {
					dependecy = mappedExternal.get(roleId);
				}
			} else {
				dependecy = mappedInternal.get(roleId);
			}

			if (dependecy == null) {
				throw new DefinitionValidationException("Found role " + role.getRoleId()
						+ " to depend on missing role " + roleId + ". Dependency chain is: " + localChain);
			}

			checkRoleDependencies(dependecy, mappedExternal, mappedInternal, localChain);
		}
	}

	private static void checkForCircularDependencies(RoleInstance role, String roleId,
			Map<String, RoleInstance> mappedExternal, Map<String, RoleInstance> mappedInternal,
			Collection<String> localChain) {
		if (role.isExternal()) {
			// external role could depend only on internal roles
			if (mappedExternal.containsKey(roleId) && !mappedInternal.containsKey(roleId)) {
				localChain.add(roleId);
				throw new DefinitionValidationException(MessageFormat.format(
						"Found external role {0} to depend on a role with same id causing circular dependecies in dependecy chain {1}",
						role.getRoleId(), localChain));
			}
		} else {
			if (mappedExternal.containsKey(roleId) && !mappedInternal.containsKey(roleId)) {
				// internal roles could not depend on external roles
				localChain.add(roleId);
				throw new DefinitionValidationException(MessageFormat.format(
						"Found internal role {0} to depend on external role {1} causing circular dependecies in dependecy chain {2}",
						role.getRoleId(), roleId, localChain));
			} else if (mappedInternal.containsKey(roleId)) {
				localChain.add(roleId);
				throw new DefinitionValidationException(MessageFormat.format(
						"Found internal role {0} to depend on other internal role {1} causing circular dependecies in dependecy chain {2}",
						role.getRoleId(), roleId, localChain));
			}
		}
	}

	private static void addToMappedModel(Map<String, RoleInstance> mapped, RoleInstance instance) {
		String identifier = instance.getRoleId().getIdentifier();
		RoleInstance roleInstance = mapped.get(identifier);
		if (roleInstance != null) {
			throw new DefinitionValidationException(
					"Found duplicate role definitions with same declaration and scope id=" + roleInstance.getRoleId()
							+ " external=" + roleInstance.isExternal());
		}
		mapped.put(identifier, instance);
	}

	private void validateForCycles(List<RoleInstance> roles, Map<String, RoleInstance> mappedExternal,
			Map<String, RoleInstance> mappedInternal) {

		for (RoleInstance roleInstance : roles) {
			Collection<String> dependecyChain = new LinkedList<>();
			dependecyChain.add(roleInstance.getRoleId().getIdentifier());
			checkRoleDependencies(roleInstance, mappedExternal, mappedInternal, dependecyChain);
		}
	}

	/**
	 * Check for matching role depending on the boolean value. If <code>true</code> then the key will be matched using
	 * the first map and <code>false</code> against the second one.
	 */
	private static boolean checkForMatchingRole(String key, boolean external, Map<String, RoleInstance> mappedExternal,
			Map<String, RoleInstance> mappedInternal) {
		return external ? mappedExternal.containsKey(key) : mappedInternal.containsKey(key);
	}

	private static final class RoleComparator implements Comparator<RoleInstance>, Serializable {
		private static final long serialVersionUID = 3183039559542661858L;

		RoleComparator() {
		}

		@Override
		public int compare(RoleInstance instance1, RoleInstance instance2) {
			if (instance1.getRoleId().equals(instance2.getRoleId())) {
				if (instance1.isExternal()) {
					return instance2.isExternal() ? 0 : 1;
				}
				return instance2.isExternal() ? -1 : 0;
			}
			return 0;
		}
	}

	private static String buildRolesMessage() {
		return SecurityModel.BaseRoles.PUBLIC.stream().map(RoleIdentifier::getIdentifier).map(String::toLowerCase)
				.collect(Collectors.joining(", "));
	}

}
