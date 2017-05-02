package com.sirma.itt.emf.semantic.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.permissions.PermissionAssignmentChange;
import com.sirma.itt.seip.permissions.PermissionInheritanceChange;
import com.sirma.itt.seip.permissions.PermissionModelChangedEvent;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.resources.security.CmfSecurityException;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Security;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * Listens for {@link PermissionModelChangedEvent} event and updates semantic model
 *
 * @author bbanchev
 */
@Singleton
public class PermissionsChangeObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsChangeObserver.class);

	@Inject
	private javax.enterprise.inject.Instance<RepositoryConnection> repositoryConnection;

	@Inject
	private NamespaceRegistryService registryService;
	@Inject
	private SearchService searchService;

	@Inject
	private ContextualMap<String, CommonInstance> roleMapping;

	@Inject
	private RoleService roleService;

	@PostConstruct
	void init() {
		roleMapping.initializeWith(this::initializeRoleMapping);
	}

	/**
	 * Initialize role mapping between semantic roles and SEP security roles
	 *
	 * @return Mapping between semantic roles and SEP security roles
	 */
	@SuppressWarnings("squid:UnusedPrivateMethod")
	private Map<String, CommonInstance> initializeRoleMapping() {
		SearchArguments<CommonInstance> rolesMappingFilter = prepareSearchArguments(
				SemanticQueries.QUERY_GET_ROLES_MAPPING);
		searchService.searchAndLoad(CommonInstance.class, rolesMappingFilter);

		List<CommonInstance> instanceRolesResult = rolesMappingFilter.getResult();
		Map<String, CommonInstance> tempRoleMapping = CollectionUtils.createHashMap(instanceRolesResult.size());
		for (CommonInstance role : instanceRolesResult) {
			tempRoleMapping.put(role.getAsString("sepRoleId"), role);
		}

		return tempRoleMapping;
	}

	/**
	 * Handles changes in permission model
	 *
	 * @param event
	 *            the event holding data
	 */
	public void onPermissionsChanges(@Observes PermissionModelChangedEvent event) {
		InstanceReference reference = event.getInstance();

		TimeTracker timeTracker = new TimeTracker().begin();
		URI instanceURI = registryService.buildUri(reference.getIdentifier());
		Map<Serializable, CommonInstance> instanceRoles = getInstanceRoles(instanceURI);

		Collection<PermissionAssignmentChange> changesSet = event.getChangesSet();

		Model addModel = new LinkedHashModel();
		Model removeModel = new LinkedHashModel();

		for (PermissionAssignmentChange nextChange : changesSet) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Processing " + nextChange);
			}
			String authorityId = nextChange.getAuthorityId();
			if (nextChange instanceof PermissionInheritanceChange) {
				handleInheritanceChange(instanceURI, addModel, removeModel, nextChange);
			} else if (nextChange.isNewAssignmentChange()) {
				setAuthorityPermissions(addModel, authorityId, nextChange.getRoleIdAfter(), instanceURI, instanceRoles);
			} else if (nextChange.isRemoveAssignmentChange()) {
				setAuthorityPermissions(removeModel, authorityId, nextChange.getRoleIdBefore(), instanceURI,
						instanceRoles);
			} else if (nextChange.getRoleIdAfter() != null
					&& !nextChange.getRoleIdAfter().equals(nextChange.getRoleIdBefore())
					|| Options.FORCE_PERMISSIONS.isSet()) {
				// remove existing permissions
				setAuthorityPermissions(removeModel, authorityId, nextChange.getRoleIdBefore(), instanceURI,
						instanceRoles);
				// add new permissions
				setAuthorityPermissions(addModel, authorityId, nextChange.getRoleIdAfter(), instanceURI, instanceRoles);
			}
		}

		URI dataGraph = registryService.getDataGraph();
		RepositoryConnection connection = repositoryConnection.get();
		SemanticPersistenceHelper.removeModel(connection, removeModel, dataGraph);
		if (!removeModel.isEmpty()) {
			LOGGER.debug("Security Model [{}]  removed from reporitory.", removeModel);
		}

		SemanticPersistenceHelper.saveModel(connection, addModel, dataGraph);
		if (!addModel.isEmpty()) {
			LOGGER.debug("Security Model [{}]  added to reporitory.", addModel);
		}

		LOGGER.debug("Update of permission model for instance {} took: {}", reference.getIdentifier(),
				timeTracker.stopInSeconds());
	}

	void handleInheritanceChange(URI instanceURI, Model addModel, Model removeModel,
			PermissionAssignmentChange nextChange) {
		PermissionInheritanceChange inheritanceChange = (PermissionInheritanceChange) nextChange;
		String parentBefore = inheritanceChange.getInheritedFromBefore();
		String parentAfter = inheritanceChange.getInheritedFromAfter();

		if (parentBefore != null && parentAfter == null) {
			// remove inherited
			setInheritedPermissions(instanceURI, parentBefore, removeModel);
		}

		if (parentBefore == null && parentAfter != null) {
			if (!inheritanceChange.isManagersOnly()) {
				setInheritedPermissions(instanceURI, parentAfter, addModel);
			}

			setManagerPermissions(instanceURI, parentAfter, addModel);
		}

		// Also handles cases where the parentBefore and parentAfter are the same but the managersOnly flag is changed
		if (parentBefore != null && parentAfter != null) {

			if (!parentBefore.equals(parentAfter)) {
				setInheritedPermissions(instanceURI, parentBefore, removeModel);
				setManagerPermissions(instanceURI, parentBefore, removeModel);
			}

			setManagerPermissions(instanceURI, parentAfter, addModel);

			if (!inheritanceChange.isManagersOnly()) {
				setInheritedPermissions(instanceURI, parentAfter, addModel);
			} else {
				setInheritedPermissions(instanceURI, parentAfter, removeModel);
			}
		}
	}

	/**
	 * * Adds the permissions of the authority for the role to the Model
	 * <p/>
	 * Viewer and Consumer has only read permissions<br/>
	 * No Permissions role has no permission predicate <br/>
	 * All others have read and write permissions
	 *
	 * @param model
	 *            Model of triplets
	 * @param authorityId
	 *            ID of the authority
	 * @param role
	 *            Role of the authority
	 * @param instanceURI
	 *            URI of the instance that has changed permissions
	 * @param instanceRoles
	 *            List with available roles for the instance
	 * @param instanceRoles
	 */
	private void setAuthorityPermissions(Model model, Serializable authorityId, String role, URI instanceURI,
			Map<Serializable, CommonInstance> instanceRoles) {
		URI authorityUri = registryService.buildUri(authorityId.toString());
		CommonInstance semanticRole = instanceRoles.computeIfAbsent(getSemanticRoleType(role),
				k -> createRole(role, instanceURI, model));
		URI roleURI = registryService.buildUri(semanticRole.getId().toString());
		model.add(authorityUri, Security.ASSIGNED_TO, roleURI);
	}

	/**
	 * Sets manager permissions between the current instance and the parent instance by adding isManagerOf relation.
	 *
	 * @param instance
	 *            Current instance
	 * @param parentInstance
	 *            Parent instance
	 * @param model
	 *            Model to add the permission relations
	 */
	private void setManagerPermissions(URI instanceURI, Serializable parentInstance, Model model) {
		URI parentURI = getParentURI(parentInstance);

		model.add(parentURI, Security.IS_MANAGER_OF, instanceURI);
	}

	/**
	 * Sets the inherited permissions between the current instance and the parent instance and builds URIs for them
	 *
	 * @param instance
	 *            Current instance
	 * @param parentInstance
	 *            Parent instance
	 * @param model
	 *            Model to add the permission relations
	 */
	private void setInheritedPermissions(URI instanceURI, Serializable parentInstance, Model model) {
		URI parentURI = getParentURI(parentInstance);

		setInheritedPermissions(instanceURI, parentURI, model);
	}

	/**
	 * Sets the inherited permissions between the current instance and the parent instance. Three relations are added
	 * between the two instances: canRead, canWrite, noPermission
	 *
	 * @param instance
	 *            Current instance URI
	 * @param parentInstance
	 *            Parent instance URI
	 * @param model
	 *            Model to add the permission relations
	 */
	private static void setInheritedPermissions(URI instanceURI, URI parentURI, Model model) {
		model.add(parentURI, Security.HAS_PERMISSION, instanceURI);
	}

	/**
	 * Builds the parent URI from the parent instance. If the parent instance is InstanceReference then the parent id is
	 * get from the identifier. If the parent is Instance then it is get from the id of the object.
	 *
	 * @param parentInstance
	 *            Parent instance
	 * @return URI of the parent
	 */
	private URI getParentURI(Serializable parentInstance) {
		String parentId;
		if (parentInstance instanceof InstanceReference) {
			parentId = ((InstanceReference) parentInstance).getIdentifier();
		} else if (parentInstance instanceof Instance) {
			parentId = ((Instance) parentInstance).getId().toString();
		} else {
			parentId = parentInstance.toString();
		}
		return registryService.buildUri(parentId);
	}

	private Map<Serializable, CommonInstance> getInstanceRoles(URI instanceURI) {
		SearchArguments<CommonInstance> rolesFilter = prepareSearchArguments(
				SemanticQueries.QUERY_GET_SECURITY_ROLES_FOR_INSTANCE);
		rolesFilter.getArguments().put("entity", instanceURI);
		searchService.searchAndLoad(CommonInstance.class, rolesFilter);

		List<CommonInstance> instanceRolesResult = rolesFilter.getResult();
		Map<Serializable, CommonInstance> instanceRoles = CollectionUtils.createHashMap(instanceRolesResult.size());
		for (CommonInstance role : instanceRolesResult) {
			String roleType = role.getAsString("roleType");
			if (StringUtils.isNotNullOrEmpty(roleType)) {
				instanceRoles.put(roleType, role);
			}
		}

		return instanceRoles;
	}

	private CommonInstance createRole(String roleId, URI instanceURI, Model model) {
		Serializable semanticRoleType = getSemanticRoleType(roleId);

		String newRoleId = generateRoleId(instanceURI, semanticRoleType);
		CommonInstance instanceRole = new CommonInstance();
		instanceRole.setId(newRoleId);
		instanceRole.add("sepRoleId", roleId);

		URI roleUri = registryService.buildUri(newRoleId);
		model.add(roleUri, RDF.TYPE, Security.ROLE);
		model.add(roleUri, Security.HAS_ROLE_TYPE, registryService.buildUri(semanticRoleType.toString()));
		model.add(roleUri, EMF.CREATED_ON, ValueFactoryImpl.getInstance().createLiteral(new Date()));

		if (roleService.isManagerRole(roleId)) {
			model.add(roleUri, Security.IS_MANAGER_OF, instanceURI);
		}
		model.add(roleUri, Security.HAS_PERMISSION, instanceURI);
		return instanceRole;
	}

	private String generateRoleId(URI instanceURI, Serializable semanticRoleId) {
		return instanceURI.toString() + "_" + registryService.buildUri(semanticRoleId.toString()).getLocalName();
	}

	private SearchArguments<CommonInstance> prepareSearchArguments(SemanticQueries query) {
		SearchArguments<CommonInstance> rolesFilter = searchService.getFilter(query.getName(), CommonInstance.class,
				null);
		rolesFilter.setDialect(SearchDialects.SPARQL);
		rolesFilter.getQueryConfigurations().put(SPARQLQueryHelper.INCLUDE_INFERRED_CONFIGURATION, Boolean.FALSE);
		rolesFilter.setPageSize(0);
		rolesFilter.setMaxSize(0);
		rolesFilter.setPermissionsType(QueryResultPermissionFilter.NONE);
		return rolesFilter;
	}

	private Serializable getSemanticRoleType(String roleId) {
		CommonInstance semanticRole = roleMapping.get(roleId);
		if (semanticRole != null) {
			return semanticRole.getId();
		}
		throw new CmfSecurityException(
				"Unknown security Role! Security role not found in the mapping between Semantic roles and Sep roles!");
	}
}
