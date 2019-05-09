package com.sirma.sep.model.management.persistence;

import java.util.Set;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import com.sirma.itt.seip.domain.validation.ValidationMessageBuilder;

public class SemanticDatabaseMessageBuilder extends ValidationMessageBuilder {

	public static final String EXCESS_RESOURCES = "model.management.deploy.semantic.excess.resources";
	public static final String MISSING_RESOURCES = "model.management.deploy.semantic.missing.resources";
	public static final String RESOURCES_MISMATCH = "model.management.deploy.semantic.resources.mismatch";

	public void excessResources(Statement statement, Set<Value> found) {
		String nodeId = statement.getSubject().toString();
		warning(nodeId, EXCESS_RESOURCES, nodeId, statement.getPredicate().toString(), found.toString());
	}

	public void missingResources(Statement statement) {
		String nodeId = statement.getSubject().toString();
		warning(nodeId, MISSING_RESOURCES, nodeId, statement.getPredicate().toString(), statement.getObject().toString());
	}

	public void resourceMismatch(Statement statement, Set<Value> found) {
		String nodeId = statement.getSubject().toString();
		warning(nodeId, RESOURCES_MISMATCH, nodeId, statement.getPredicate().toString(), statement.getObject().toString(),
				found.toString());
	}

}
