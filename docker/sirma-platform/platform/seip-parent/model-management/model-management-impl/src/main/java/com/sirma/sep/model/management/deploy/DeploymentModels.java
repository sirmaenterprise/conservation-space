package com.sirma.sep.model.management.deploy;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Wrapper for the {@link Models} instance used during validation and deployment process. The instance carries a
 * {@link Models} instance with applied the desired changes for deployment. The implementation also provides means the
 * client to query for changes for particular root node path.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 28/09/2018
 */
public class DeploymentModels {

	private final Models models;
	private List<ModelChangeSetInfo> deployableChanges = new LinkedList<>();

	DeploymentModels(Models models) {this.models = models;}

	/**
	 * Resolve changes applied for a root node identified by the given path.
	 *
	 * @param path the path identifying the root model node (class, definition, property)
	 * @return the list of changes applied on the model, if any
	 */
	public List<ModelChangeSetInfo> getDeployableChangesFor(Path path) {
		return deployableChanges.stream()
				.filter(changeSetInfo -> changeSetInfo.getChangeSet().getPath().equals(path))
				.collect(Collectors.toList());
	}

	/**
	 * Resolve a model node by the given path
	 *
	 * @param path the path to resolve
	 * @param <M> the expected model node type.
	 * @return the found model node. If the return actual model is not of the desired type a {@link ClassCastException}
	 * will be thrown.
	 */
	@SuppressWarnings("unchecked")
	public <M extends ModelNode> M resolveNode(Path path) {
		return (M) models.walk(path);
	}

	/**
	 * Returns the stored {@link Models} instance with the applied changes
	 *
	 * @return a mutable models instance
	 */
	public Models getModels() {
		return models;
	}

	void addChange(ModelChangeSetInfo changeSetInfo) {
		deployableChanges.add(changeSetInfo);
	}
}
