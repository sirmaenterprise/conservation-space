package com.sirma.sep.definition.compile;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.seip.definition.compile.GenericDefinitionCompilerCallback;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Compiles definitions by merging each definition with each parent.
 *
 * @author Adrian Mitev
 */
public class DefinitionCompiler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@Any
	private GenericDefinitionCompilerCallback compilerCallback;

	@Inject
	private DefinitionCompilerHelper compilerHelper;

	/**
	 * Compiles list of definitions by executing the following steps:
	 * - constructing a dependency tree, traversing it DFS and merging each definition with its parent
	 * - remove all fields and regions marked for deletion (which also includes transitions marked as system)
	 * - preparing values of suggestion fields
	 * - synchronize region properties
	 * - set default properties
	 * - sort transitions and groups
	 * - normalize fields
	 *
	 * @param definitions list of definitions to compile
	 * @return list errors found during compilation
	 */
	public List<String> compile(List<GenericDefinitionImpl> definitions) {
		List<String> errors = new ArrayList<>();

		definitions.forEach(this::initializeSourceIds);

		List<DefinitionNode> rootNodes = constructDependencyTree(definitions);
		rootNodes.forEach(node -> mergeDefinitionsWitParents(node, null));

		for (GenericDefinitionImpl definition: definitions) {
			// construct field paths
			compilerCallback.normalizeFields(definition);

			compilerHelper.removeDeletedElements(definition);
			removeSystemTransitions(definition);

			removeInheritedFieldsThatAreDuplicateInTheChild(definition);

			compilerHelper.prepareDefaultValueSuggests(definition);
			List<String> fieldErrors = compilerHelper.synchRegionProperties(definition);

			compilerHelper.setDefaultProperties(definition);

			// re-construct field paths in case they were modified
			compilerCallback.normalizeFields(definition);

			List<String> definitionErrors = fieldErrors.stream()
					.map(message -> "Error found in definition '" + definition.getIdentifier() + "': " + message)
					.collect(Collectors.toList());
			errors.addAll(definitionErrors);
		}

		return errors;
	}

	private void initializeSourceIds(GenericDefinitionImpl definition) {
		definition.fieldsStream().map(field -> (FieldDefinitionImpl) ((PropertyDefinitionProxy) field).getTarget())
								 .forEach(field-> field.setSource(definition.getIdentifier()));
	}

	private void mergeDefinitionsWitParents(DefinitionNode node, DefinitionNode parent) {
		compilerCallback.normalizeFields(node.definition);
		node.definition.initBidirection();

		if (parent != null) {
			node.definition.mergeFrom(parent.definition);
			compilerCallback.normalizeFields(node.definition);
		}

		node.children.forEach(child -> mergeDefinitionsWitParents(child, node));
	}

	private static List<DefinitionNode> constructDependencyTree(List<GenericDefinitionImpl> definitions) {
		Map<String, DefinitionNode> nodes = new HashMap<>();

		for (GenericDefinitionImpl definition : definitions) {
			DefinitionNode definitionNode = nodes.computeIfAbsent(definition.getIdentifier(), id -> new DefinitionNode());
			definitionNode.definition = definition;

			if (definition.getParentDefinitionId() != null) {
				DefinitionNode parentNode = nodes.computeIfAbsent(definition.getParentDefinitionId(), id -> new DefinitionNode());
				parentNode.children.add(definitionNode);
			}
		}

		return nodes.entrySet().stream()
								.filter(node -> node.getValue().definition.getParentDefinitionId() == null)
								.map(Entry::getValue)
								.collect(Collectors.toList());
	}

	private static class DefinitionNode {
		private GenericDefinitionImpl definition;
		private List<DefinitionNode> children;

		DefinitionNode() {
			children = new ArrayList<>();
		}
	}

	private static void removeInheritedFieldsThatAreDuplicateInTheChild(GenericDefinitionImpl definition) {
		// children without parent don't inherit anything
		if (definition.getParentDefinitionId() == null) {
			return;
		}

		Set<String> fieldsDefinedInTheCurrentDefinition = definition.fieldsStream()
								 .map(DefinitionCompiler::getActualField)
								 .filter(field -> definition.getIdentifier().equals(field.getSource()))
								 .map(FieldDefinitionImpl::getIdentifier)
								 .collect(Collectors.toSet());

		Predicate<? super PropertyDefinition> duplicateFieldRemovalPredicate = field -> {
			if (fieldsDefinedInTheCurrentDefinition.contains(field.getIdentifier())) {
				return !definition.getIdentifier().equals(getActualField(field).getSource());
			}

			return false;
		};

		definition.getFields().removeIf(duplicateFieldRemovalPredicate);

		definition.getRegions().forEach(region -> region.getFields().removeIf(duplicateFieldRemovalPredicate));
	}

	private static FieldDefinitionImpl getActualField(PropertyDefinition field) {
		return (FieldDefinitionImpl) ((PropertyDefinitionProxy)field).getTarget();
	}

	private static void removeSystemTransitions(GenericDefinitionImpl definition) {
		for (Iterator<TransitionDefinition> it = definition.getTransitions().iterator(); it.hasNext();) {
			TransitionDefinition transition = it.next();

			if (transition.getDisplayType() == DisplayType.SYSTEM) {
				LOGGER.debug("Removing transition '{}' from definition '{}' because it is marked as system",
						transition.getIdentifier(), definition.getIdentifier());
				it.remove();
			}
		}
	}

}
