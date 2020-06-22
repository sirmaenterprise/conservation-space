package com.sirma.itt.seip.permissions.sync.batch;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.persistence.SemanticPersistenceHelper;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Security;

/**
 * Synchronization step that writes a chunk of permissions changes to the semantic database. The implementation
 * handles changes in parent inheritance, library inheritance, permission changes and adds missing permission role
 * definitions. The implementation handles list of {@link PermissionsDiff} instances
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/06/2017
 */
@Named
public class InstancePermissionWriter extends AbstractItemWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private NamespaceRegistryService registryService;
	@Inject
	private PermissionSyncUtil syncUtil;
	@Inject
	private RepositoryConnection repositoryConnection;

	@Override
	public void writeItems(List<Object> items) throws Exception {
		Model toAdd = new LinkedHashModel();
		Model toRemove = new LinkedHashModel();

		TimeTracker timeTracker = TimeTracker.createAndStart();

		items.stream()
				.map(PermissionsDiff.class::cast)
				.filter(PermissionsDiff::hasChanges)
				.forEach(entry -> processDiff(entry, toAdd, toRemove));

		IRI dataGraph = registryService.getDataGraph();
		SemanticPersistenceHelper.removeModel(repositoryConnection, toRemove, dataGraph);
		if (!toRemove.isEmpty()) {
			LOGGER.debug(toRemove.stream().map(Object::toString).collect(Collectors.joining
					("\n-", "Removing from repository:\n", "")));
		}

		SemanticPersistenceHelper.saveModel(repositoryConnection, toAdd, dataGraph);
		if (!toAdd.isEmpty()) {
			LOGGER.debug(toAdd.stream().map(Object::toString).collect(Collectors.joining
					("\n+", "Adding to repository:\n", "")));
		}

		LOGGER.debug("Updated permissions for {} instances that took: {} ms", items.size(), timeTracker.stop());
	}

	private void processDiff(PermissionsDiff diff, Model toAdd, Model toRemove) {
		IRI instanceId = registryService.buildUri(diff.getTargetId());
		Map<String, String> instanceRoles = diff.getInstanceRoles();
		diff.getToAdd().forEach(entry ->
				processEntry(instanceId, entry.authority, entry.roleType, instanceRoles, toAdd));
		diff.getToRemove().forEach(entry ->
				processEntry(instanceId, entry.authority, entry.roleType, instanceRoles, toRemove));

		if (diff.isParentInheritanceChanged()) {
			handleInheritanceChange(instanceId, diff.getParentInheritanceToRemove(), diff.getParentInheritanceToAdd(),
					toAdd, toRemove);
		}

		if (diff.isLibraryPermissionsChanged()) {
			handleInheritanceChange(instanceId, diff.getLibraryInheritanceToRemove(), diff.getLibraryInheritanceToAdd(),
					toAdd, toRemove);
		}
	}

	private void handleInheritanceChange(IRI instanceId, Set<String> currentInherited, String newInherited, Model toAdd,
			Model toRemove) {
		if (isEmpty(currentInherited) && newInherited != null) {
			setInheritedPermissions(instanceId, newInherited, toAdd);
			return;
		}
		if (newInherited == null) {
			//removed inheritance
			currentInherited.forEach(old -> setInheritedPermissions(instanceId, old, toRemove));
		} else if (currentInherited.contains(newInherited) && currentInherited.size() > 1) {
			// more than one current entry, one of them is the correct value
			currentInherited.stream().filter(item -> !item.equals(newInherited)).forEach(extra ->
					setInheritedPermissions(instanceId, extra, toRemove));
		} else {
			// non of the current values is the correct one
			currentInherited.forEach(old -> setInheritedPermissions(instanceId, old, toRemove));
			setInheritedPermissions(instanceId, newInherited, toAdd);
		}
	}

	/**
	 * Sets the inherited permissions between the current instance and the parent instance and builds URIs for them
	 *
	 * @param instanceURI Current instance
	 * @param parentInstance Parent instance
	 * @param model Model to add the permission relations
	 */
	private void setInheritedPermissions(IRI instanceURI, String parentInstance, Model model) {
		IRI parentURI = registryService.buildUri(parentInstance);

		model.add(parentURI, Security.HAS_PERMISSION, instanceURI);
	}

	private void processEntry(IRI instanceId, String authorityId, String roleType, Map<String, String> instanceRoles,
			Model model) {
		IRI authority = registryService.buildUri(authorityId);
		String roleId = instanceRoles.computeIfAbsent(roleType, k -> createRole(roleType, instanceId, model));
		IRI roleURI = registryService.buildUri(roleId);
		model.add(authority, Security.ASSIGNED_TO, roleURI);
	}

	private String createRole(String semanticRoleType, IRI instanceURI, Model model) {
		String newRoleId = generateRoleId(instanceURI, semanticRoleType);

		IRI roleUri = registryService.buildUri(newRoleId);
		model.add(roleUri, RDF.TYPE, Security.ROLE);
		model.add(roleUri, Security.HAS_ROLE_TYPE, registryService.buildUri(semanticRoleType));
		model.add(roleUri, EMF.CREATED_ON, SimpleValueFactory.getInstance().createLiteral(new Date()));

		if (syncUtil.isManagerRoleType(semanticRoleType)) {
			model.add(roleUri, Security.IS_MANAGER_OF, instanceURI);
		}
		model.add(roleUri, Security.HAS_PERMISSION, instanceURI);
		return newRoleId;
	}

	private String generateRoleId(IRI instanceURI, Serializable semanticRoleId) {
		return instanceURI.toString() + "_" + registryService.buildUri(semanticRoleId.toString()).getLocalName();
	}
}
