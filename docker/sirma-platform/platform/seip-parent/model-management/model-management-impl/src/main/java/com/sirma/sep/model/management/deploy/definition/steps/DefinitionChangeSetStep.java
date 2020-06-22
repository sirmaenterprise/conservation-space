package com.sirma.sep.model.management.deploy.definition.steps;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.sep.model.management.Path;

/**
 * Abstract step defining the behaviour for processing {@link DefinitionChangeSetPayload}.
 *
 * @author Mihail Radkov
 */
public abstract class DefinitionChangeSetStep {

	/**
	 * Determines if the provided model {@link com.sirma.sep.model.management.operation.ModelChangeSet} {@link Path} can be handled by the
	 * current step.
	 * <p>
	 * Every {@link Path} head must begin with "definition" and produce a <code>true</code> from {@link #checkPath(Path)} to be considered
	 * suitable for handling.
	 *
	 * @param path the path to be determined if it can be handled by the step
	 * @return <code>true</code> if the {@link Path} can be handled or <code>false</code> if it cannot
	 */
	public boolean canHandle(Path path) {
		Path head = path.head();
		return "definition".equals(head.getName()) && checkPath(path);
	}

	/**
	 * Thoroughly checks if the provided {@link Path } can be handled by the current step.
	 * <p>
	 * In contrast to {@link #canHandle(Path)} where only the head is checked, this should go further and check for specific nodes.
	 *
	 * @param path the path to check
	 * @return <code>true</code> if the {@link Path} can be handled or <code>false</code> if it cannot
	 */
	protected abstract boolean checkPath(Path path);

	/**
	 * Validates the provided {@link DefinitionChangeSetPayload}.
	 * <p>
	 * Override this method to implement custom step validation.
	 *
	 * @param definitionChangeSetPayload the payload to validate
	 * @return list of validation messages
	 */
	public List<ValidationMessage> validate(DefinitionChangeSetPayload definitionChangeSetPayload) {
		return Collections.emptyList();
	}

	/**
	 * Handles the provided {@link DefinitionChangeSetPayload} by executing specific step's logic.
	 * <p>
	 * If a step must notify for something specific, it should use {@link DefinitionChangeSetPayload#context}
	 *
	 * @param definitionChangeSetPayload the payload to handle
	 */
	public abstract void handle(DefinitionChangeSetPayload definitionChangeSetPayload);
}
