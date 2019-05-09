package com.sirma.sep.definition.compile;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.seip.definition.compile.GenericDefinitionCompilerCallback;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;

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
	 */
	public void compile(List<GenericDefinitionImpl> definitions) {
		List<DefinitionNode> rootNodes = constructDependencyTree(definitions);
		rootNodes.forEach(node -> mergeDefinitionsWitParents(node, null));

		for (GenericDefinitionImpl definition : definitions) {
			// construct field paths
			compilerCallback.normalizeFields(definition);

			compilerHelper.removeDeletedElements(definition);
			removeSystemTransitions(definition);

			compilerHelper.prepareDefaultValueSuggests(definition);

			removeSystemRegions(definition);

			compilerHelper.setDefaultProperties(definition);

			// re-construct field paths in case they were modified
			compilerCallback.normalizeFields(definition);
		}
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

		return nodes.values()
				.stream()
				.filter(node -> Objects.isNull(node.definition.getParentDefinitionId()))
				.collect(Collectors.toList());
	}

	private static class DefinitionNode {
		private GenericDefinitionImpl definition;
		private List<DefinitionNode> children;

		DefinitionNode() {
			children = new ArrayList<>();
		}
	}

	private static void removeSystemTransitions(GenericDefinitionImpl definition) {
		definition.getTransitions().removeIf(transition -> {
			if (DisplayType.SYSTEM.equals(transition.getDisplayType())) {
				LOGGER.debug("Removing system transition '{}' from definition '{}'", transition.getIdentifier(),
						definition.getIdentifier());
				return true;
			}
			return false;
		});
	}

	private static void removeSystemRegions(GenericDefinition definition) {
		definition.getRegions().removeIf(region -> {
			if (DisplayType.SYSTEM.equals(region.getDisplayType())) {
				LOGGER.debug("Removing system region '{}' from '{}'", region.getIdentifier(), PathHelper.getPath(definition));
				return true;
			}
			return false;
		});
	}

}
