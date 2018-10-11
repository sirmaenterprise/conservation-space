package com.sirma.itt.seip.definition.compile;

import java.util.Date;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.MutableDefinitionService;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.GenericDefinition;

/**
 * Implementation for loading generic definitions.
 *
 * @author BBonev
 */
public class GenericDefinitionCompilerCallback {

	@Inject
	private MutableDefinitionService mutableDefinitionService;

	@Inject
	private DefinitionCompilerHelper compiler;

	public void setPropertyRevision(GenericDefinitionImpl definition) {
		Long revision = definition.getRevision();
		setPropertyRevisionInternal(revision, definition);
	}

	private void setPropertyRevisionInternal(Long revision, GenericDefinition definition) {
		compiler.setPropertyRevision(definition, revision);
		compiler.setTransactionalPropertyRevision(definition, revision);
	}

	public void normalizeFields(GenericDefinitionImpl definition) {
		if (definition.getAbstract() == null) {
			definition.setAbstract(Boolean.FALSE);
		}

		definition.initBidirection();

		definition.setCreationDate(new Date());
		definition.setLastModifiedDate(new Date());
		// initialize the revision
		if (definition.getRevision() == null || definition.getRevision().longValue() == 0) {
			definition.setRevision(1L);
		}

		normalizeFieldsInternal(definition.getContainer(), definition);

		GenericDefinitionCompilerHelper.optimizeAllowedChildrenConfiguration(definition);
	}

	private void normalizeFieldsInternal(String container, GenericDefinition definition) {
		compiler.normalizeFields(definition.getFields(), definition, false, container);

		for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
			TransitionDefinitionImpl transitionDefinitionImpl = (TransitionDefinitionImpl) transitionDefinition;

			transitionDefinitionImpl.setOwnerPrefix("section");
			compiler.normalizeFields(transitionDefinition.getFields(), transitionDefinition, false, container);
		}
		for (RegionDefinition regionDefinition : definition.getRegions()) {
			compiler.normalizeFields(regionDefinition.getFields(), regionDefinition, false, container);
			if (regionDefinition.getControlDefinition() != null) {
				compiler.normalizeFields(regionDefinition.getControlDefinition().getFields(),
						regionDefinition.getControlDefinition(), false, container);
			}
		}

		compiler.optimizeStateTransitions(definition);
	}

	public GenericDefinitionImpl saveDefinition(GenericDefinitionImpl definition) {
		return mutableDefinitionService.saveDefinition(definition);
	}

	public void saveDefinitionProperties(GenericDefinitionImpl newDefinition, GenericDefinitionImpl oldDefinition) {
		compiler.saveProperties(newDefinition, oldDefinition);
		for (TransitionDefinition transitionDefinition : newDefinition.getTransitions()) {
			TransitionDefinition oldTransition = null;
			if (oldDefinition != null) {
				oldTransition = oldDefinition.getTransitionByName(transitionDefinition.getIdentifier()).orElse(null);
			}
			compiler.saveProperties(transitionDefinition,oldTransition);
		}

		newDefinition.initBidirection();
	}

}
