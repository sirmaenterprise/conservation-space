package com.sirma.sep.model.management.deploy.semantic;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.Statement;

import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Represents a set of database changes that are generated on some set ot original changes. Original changes will never
 * be empty but statements for addition and removal could be if the original changes exclude one another. <br>
 * For example the first change set a property to true and the second sets the property back to false. Such changes
 * does not produce statements to be applied to the database.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/08/2018
 */
class SemanticChange {
	private final List<ModelChangeSetInfo> originalChanges;
	private final List<Statement> toAdd = new LinkedList<>();
	private final List<Statement> toRemove = new LinkedList<>();

	SemanticChange(List<ModelChangeSetInfo> originalChanges) {
		this.originalChanges = originalChanges;
	}

	List<ModelChangeSetInfo> getOriginalChanges() {
		return originalChanges;
	}

	void toAdd(Statement statement) {
		toAdd.add(statement);
	}

	void toRemove(Statement statement) {
		toRemove.add(statement);
	}

	Stream<Statement> getToAdd() {
		return toAdd.stream();
	}

	Stream<Statement> getToRemove() {
		return toRemove.stream();
	}
}
