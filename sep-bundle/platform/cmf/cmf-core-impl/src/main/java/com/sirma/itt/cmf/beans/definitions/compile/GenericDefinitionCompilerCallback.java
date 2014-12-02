package com.sirma.itt.cmf.beans.definitions.compile;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.cmf.xml.XmlType;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionIdentityUtil;
import com.sirma.itt.emf.definition.DefinitionManagementService;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.emf.definition.load.Definition;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.DefinitionType;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.jaxb.FilterDefinition;
import com.sirma.itt.emf.definition.model.jaxb.FilterDefinitions;
import com.sirma.itt.emf.definition.model.jaxb.Label;
import com.sirma.itt.emf.definition.model.jaxb.Labels;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Implementation for loading generic definitions.
 * 
 * @author BBonev
 */
@Definition
@DefinitionType(ObjectTypesCmf.GENERIC)
public class GenericDefinitionCompilerCallback implements
		DefinitionCompilerCallback<GenericDefinitionImpl> {

	private static final String DMS_MODEL_PREFIX = "cmf:";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GenericDefinitionCompilerCallback.class);

	@Inject
	private MutableDictionaryService mutableDictionaryService;
	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private DefinitionManagementService definitionManagementService;

	@Inject
	private DefinitionCompilerHelper compiler;

	@Override
	public void setPropertyRevision(GenericDefinitionImpl definition) {
		Long revision = definition.getRevision();
		setPropertyRevisionInternal(revision, definition);
	}

	/**
	 * Sets the property revision internal.
	 * 
	 * @param revision
	 *            the revision
	 * @param definition
	 *            the sub definition
	 */
	private void setPropertyRevisionInternal(Long revision, GenericDefinition definition) {
		compiler.setPropertyRevision(definition, revision);
		compiler.setTransactionalPropertyRevision(definition, revision);
		for (GenericDefinition subDefinition : definition.getSubDefinitions()) {
			setPropertyRevisionInternal(revision, subDefinition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void normalizeFields(GenericDefinitionImpl definition) {
		definition.initBidirection();

		definition.setCreationDate(new Date());
		definition.setLastModifiedDate(new Date());
		// initialize the revision
		if ((definition.getRevision() == null) || (definition.getRevision() == 0)) {
			definition.setRevision(1L);
		}

		normalizeFieldsInternal(definition.getContainer(), definition);

		AllowedChildrenHelper.optimizeAllowedChildrenConfiguration(definition);
	}

	/**
	 * Normalize fields internal.
	 * 
	 * @param container
	 *            the container
	 * @param definition
	 *            the definition
	 */
	private void normalizeFieldsInternal(String container, GenericDefinition definition) {
		compiler.normalizeFields(definition.getFields(), definition, false, container);
		for (GenericDefinition subDefinition : definition.getSubDefinitions()) {
			normalizeFieldsInternal(container, subDefinition);
		}
		for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
			transitionDefinition.setOwnerPrefix("section");
			compiler.normalizeFields(transitionDefinition.getFields(), transitionDefinition, false,
					container);
		}
		for (RegionDefinition regionDefinition : definition.getRegions()) {
			compiler.normalizeFields(regionDefinition.getFields(), regionDefinition, false,
					container);
			if (regionDefinition.getControlDefinition() != null) {
				compiler.normalizeFields(regionDefinition.getControlDefinition().getFields(),
						regionDefinition.getControlDefinition(), false, container);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String extractDefinitionId(GenericDefinitionImpl definition) {
		return definition.getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean updateReferences(GenericDefinitionImpl definition) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Updating sub definition references for " + definition.getIdentifier());
		}
		boolean trace = LOGGER.isTraceEnabled();
		// update the reference for the current definition
		boolean updated = updateReference(definition, trace);
		if (!updated) {
			// we have missing parent, no need to continue
			return false;
		}
		List<GenericDefinition> sectionDefs = definition.getSubDefinitions();
		// convert document references to actual definitions
		// if reference cannot be resolved then process is stopped and case
		// definition is not persisted
		int updatedDocs = 0;
		for (Iterator<GenericDefinition> it = sectionDefs.iterator(); it.hasNext();) {
			GenericDefinitionImpl child = (GenericDefinitionImpl) it.next();
			if (updateReference(child, trace)) {
				updatedDocs++;
			} else {
				// we have an invalid reference and should get out
				return false;
			}
		}
		if (trace) {
			LOGGER.trace("Updated " + updatedDocs + " child reference definitions for "
					+ definition.getIdentifier());
		}
		return true;
	}

	/**
	 * Update reference definition
	 * 
	 * @param definition
	 *            the definition
	 * @param trace
	 *            the trace
	 * @return true, if successful
	 */
	private boolean updateReference(GenericDefinitionImpl definition, boolean trace) {
		// we do nothing if the reference is present
		if (definition.getReferenceDefinition() != null) {
			return true;
		}

		String referenceId = definition.getReferenceId();
		if (trace) {
			LOGGER.trace("Processing definition " + definition.getIdentifier() + " with reference "
					+ referenceId);
		}
		if (StringUtils.isNullOrEmpty(referenceId)) {
			if (trace) {
				LOGGER.debug("No reference needed for child: " + definition.getIdentifier());
			}
			return true;
		}

		referenceId += DefinitionIdentityUtil.SEPARATOR + definition.getContainer();
		// get the complete document definition
		GenericDefinition reference = dictionaryService.getDefinition(
				GenericDefinition.class, referenceId);
		if (reference == null) {
			LOGGER.error("Missing generic definition: "
					+ referenceId
					+ ". Check if the ID is correct or add new generic definition in you definitions files.");
			return false;
		}
		definition.setReferenceDefinition(reference);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Label> getLabelDefinitions(Object source) {
		Labels labels = null;
		if (source instanceof com.sirma.itt.cmf.beans.jaxb.Definition) {
			com.sirma.itt.cmf.beans.jaxb.Definition c = (com.sirma.itt.cmf.beans.jaxb.Definition) source;
			labels = c.getLabels();
		}

		if (labels != null) {
			return labels.getLabel();
		}
		return Collections.emptyList();
	}

	@Override
	public List<FilterDefinition> getFilterDefinitions(Object source) {
		FilterDefinitions labels = null;
		if (source instanceof com.sirma.itt.cmf.beans.jaxb.Definition) {
			com.sirma.itt.cmf.beans.jaxb.Definition c = (com.sirma.itt.cmf.beans.jaxb.Definition) source;
			labels = c.getFilterDefinitions();
		}
		if (labels != null) {
			return labels.getFilter();
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GenericDefinitionImpl saveTemplate(GenericDefinitionImpl definition) {
		return mutableDictionaryService.saveDefinition(definition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GenericDefinitionImpl findTemplateInSystem(String identifier) {
		return (GenericDefinitionImpl) dictionaryService.getDefinition(
				GenericDefinition.class, identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FileDescriptor> getDefinitions() {
		return definitionManagementService.getDefinitions(getDefinitionClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<GenericDefinitionImpl> getDefinitionClass() {
		return GenericDefinitionImpl.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getMappingClass() {
		return com.sirma.itt.cmf.beans.jaxb.Definition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XmlType getXmlValidationType() {
		return XmlType.GENERIC_DEFINITION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validateCompiledDefinition(GenericDefinitionImpl definition) {
		boolean valid = true;
		valid &= compiler.executeValidators(definition);
		for (GenericDefinition subDefinition : definition.getSubDefinitions()) {
			valid &= validateCompiledDefinition((GenericDefinitionImpl) subDefinition);
		}
		return valid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCallbackName() {
		return "generic";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void prepareForPersist(GenericDefinitionImpl definition) {
		if (definition.getAbstract() == null) {
			definition.setAbstract(Boolean.FALSE);
		}
		prepareForPersistInternal(definition, definition);
	}

	/**
	 * Prepare for persist internal.
	 * 
	 * @param root
	 *            the root
	 * @param definition
	 *            the definition
	 */
	private void prepareForPersistInternal(GenericDefinitionImpl root, GenericDefinition definition) {
		compiler.synchRegionProperties(definition);
		compiler.setDefaultProperties(definition, DMS_MODEL_PREFIX, true);
		GenericDefinitionImpl refImpl = (GenericDefinitionImpl) definition;

		if ((refImpl.getExpression() != null)
				&& !compiler.validateExpression(refImpl, refImpl.getExpression())) {
			LOGGER.warn(" !!! Expression in generic definition " + refImpl.getIdentifier() + " - "
					+ root.getExpression() + " is not valid and will be removed !!!");
			refImpl.setExpression(null);
		}
		compiler.validateModelConditions(refImpl, refImpl);

		for (GenericDefinition child : root.getSubDefinitions()) {
			prepareForPersistInternal(root, child);
		}

		for (Iterator<TransitionDefinition> it = definition.getTransitions().iterator(); it
				.hasNext();) {
			TransitionDefinition transitionDefinition = it.next();
			// remove hidden definitions
			if (transitionDefinition.getDisplayType() == DisplayType.SYSTEM) {
				it.remove();
			}
			compiler.validateExpressions(root, transitionDefinition);
			compiler.setDefaultProperties(transitionDefinition, DMS_MODEL_PREFIX);
		}
		compiler.sort(definition.getTransitions());
	}

	@Override
	public void warmUpCache() {
		dictionaryService.getAllDefinitions(GenericDefinition.class);
	}

	@Override
	public void saveTemplateProperties(GenericDefinitionImpl newDefinition,
			GenericDefinitionImpl oldDefinition) {
		compiler.saveProperties(newDefinition, oldDefinition);
		for (TransitionDefinition transitionDefinition : newDefinition.getTransitions()) {
			compiler.saveProperties(
					transitionDefinition,
					WorkflowHelper.getTransitionById(oldDefinition,
							transitionDefinition.getIdentifier()));
		}
		for (GenericDefinition subDefinition : newDefinition.getSubDefinitions()) {
			saveTemplateProperties((GenericDefinitionImpl) subDefinition,
					(GenericDefinitionImpl) find(oldDefinition, subDefinition.getIdentifier()));
		}
		newDefinition.initBidirection();
	}

	/**
	 * Finds document definition by section and document identifiers.
	 * 
	 * @param definition
	 *            the source definition
	 * @param sectionId
	 *            the section id
	 * @return the region definition model or <code>null</code>
	 */
	private GenericDefinition find(GenericDefinition definition, String sectionId) {
		if (definition == null) {
			return null;
		}
		for (GenericDefinition subDefinition : definition.getSubDefinitions()) {
			if (EqualsHelper.nullSafeEquals(sectionId, subDefinition.getIdentifier())) {
				return subDefinition;
			}
		}
		return null;
	}

	@Override
	public void setReferenceMode() {
		// do nothing here
	}

}
