package com.sirma.itt.cmf.beans.definitions.compile;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.CaseDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefImpl;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.cmf.xml.XmlType;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionIdentityUtil;
import com.sirma.itt.emf.definition.DefinitionManagementService;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.emf.definition.load.Definition;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.DefinitionType;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.jaxb.Label;
import com.sirma.itt.emf.definition.model.jaxb.Labels;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Implementation for loading case definitions.
 * 
 * @author BBonev
 */
@Definition
@DefinitionType(ObjectTypesCmf.CASE)
public class CaseDefinitionCompilerCallback implements
		DefinitionCompilerCallback<CaseDefinitionImpl> {

	private static final String DMS_MODEL_PREFIX = "cmf:";
	/** The logger. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CaseDefinitionCompilerCallback.class);
	/** The mutable dictionary service. */
	@Inject
	private MutableDictionaryService mutableDictionaryService;
	@Inject
	private DictionaryService dictionaryService;
	/** The definition management service. */
	@Inject
	private DefinitionManagementService definitionManagementService;
	/** The compiler. */
	@Inject
	private DefinitionCompilerHelper compiler;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPropertyRevision(CaseDefinitionImpl definition) {
		Long revision = definition.getRevision();
		compiler.setPropertyRevision(definition, revision);
		for (SectionDefinition sectionDefinition : definition.getSectionDefinitions()) {
			compiler.setPropertyRevision(sectionDefinition, revision);
			compiler.setTransactionalPropertyRevision(sectionDefinition, revision);
			for (DocumentDefinitionRef definitionRef : sectionDefinition.getDocumentDefinitions()) {
				((DocumentDefinitionRefImpl) definitionRef).setRevision(revision);
				compiler.setPropertyRevision(definitionRef, revision);
				compiler.setTransactionalPropertyRevision(definitionRef, revision);
			}
		}
		compiler.setTransactionalPropertyRevision(definition, revision);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void normalizeFields(CaseDefinitionImpl definition) {
		definition.initBidirection();

		definition.setCreationDate(new Date());
		definition.setLastModifiedDate(new Date());
		// initialize the revision
		if ((definition.getRevision() == null) || (definition.getRevision() == 0)) {
			definition.setRevision(1L);
		}

		String container = definition.getContainer();

		compiler.normalizeFields(definition.getFields(), definition, false, container);
		List<SectionDefinition> sectionDefs = definition.getSectionDefinitions();
		for (SectionDefinition sectionDefinition : sectionDefs) {
			compiler.normalizeFields(sectionDefinition.getFields(), sectionDefinition, false,
					container);
			for (TransitionDefinition transitionDefinition : sectionDefinition.getTransitions()) {
				transitionDefinition.setOwnerPrefix("section");
				compiler.normalizeFields(transitionDefinition.getFields(), transitionDefinition,
						false, container);
			}
			for (DocumentDefinitionRef definitionRef : sectionDefinition.getDocumentDefinitions()) {
				compiler.normalizeFields(definitionRef.getFields(), definitionRef, false, container);
				// fill the parent path so we can later can search it by it
				((DocumentDefinitionRefImpl) definitionRef).setParentPath(PathHelper
						.getPath(definitionRef));
				for (RegionDefinition regionDefinition : definitionRef.getRegions()) {
					compiler.normalizeFields(regionDefinition.getFields(), regionDefinition, false,
							container);
					if (regionDefinition.getControlDefinition() != null) {
						compiler.normalizeFields(regionDefinition.getControlDefinition()
								.getFields(), regionDefinition.getControlDefinition(), false,
								container);
					}
				}
				for (TransitionDefinition transitionDefinition : definitionRef.getTransitions()) {
					transitionDefinition.setOwnerPrefix("document");
					compiler.normalizeFields(transitionDefinition.getFields(),
							transitionDefinition, false, container);
				}
			}
			for (RegionDefinition regionDefinition : sectionDefinition.getRegions()) {
				compiler.normalizeFields(regionDefinition.getFields(), regionDefinition, false,
						container);
				if (regionDefinition.getControlDefinition() != null) {
					compiler.normalizeFields(regionDefinition.getControlDefinition().getFields(),
							regionDefinition.getControlDefinition(), false, container);
				}
			}
		}
		for (RegionDefinition regionDefinition : definition.getRegions()) {
			compiler.normalizeFields(regionDefinition.getFields(), regionDefinition, false,
					container);
			if (regionDefinition.getControlDefinition() != null) {
				compiler.normalizeFields(regionDefinition.getControlDefinition().getFields(),
						regionDefinition.getControlDefinition(), false, container);
			}
		}

		for (TransitionDefinition transitionDefinition : definition.getTransitions()) {
			transitionDefinition.setOwnerPrefix(getCallbackName());
			compiler.normalizeFields(transitionDefinition.getFields(), transitionDefinition, false,
					container);
		}

		AllowedChildrenHelper.optimizeAllowedChildrenConfiguration(definition);
		AllowedChildrenHelper.removeNotSupportedChildrenTypes(definition,
				new HashSet<>(Arrays.asList(ObjectTypesCmf.CASE)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String extractDefinitionId(CaseDefinitionImpl definition) {
		if (com.sirma.itt.commons.utils.string.StringUtils.isNotNullOrEmpty(definition
				.getIdentifier())) {
			return definition.getIdentifier();
		}
		// this code is for backward compatibility
		for (PropertyDefinition propDefinition : definition.getFields()) {
			String defName = propDefinition.getName();
			if (CaseProperties.TYPE.equals(defName)) {
				return propDefinition.getDefaultValue();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean updateReferences(CaseDefinitionImpl caseDefinition) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Updating document definition references for "
					+ caseDefinition.getIdentifier());
		}
		boolean trace = LOGGER.isTraceEnabled();
		List<SectionDefinition> sectionDefs = caseDefinition.getSectionDefinitions();
		for (SectionDefinition sectionDefinition : sectionDefs) {
			List<DocumentDefinitionRef> documentDefinitions = sectionDefinition
					.getDocumentDefinitions();
			if (trace) {
				LOGGER.trace("Processing section " + sectionDefinition.getIdentifier() + " with "
						+ sectionDefinition.getDocumentDefinitions().size()
						+ " document definitions");
			}
			// convert document references to actual definitions
			// if reference cannot be resolved then process is stopped and case
			// definition is not persisted
			int updatedDocs = 0;
			for (Iterator<DocumentDefinitionRef> it = documentDefinitions.iterator(); it.hasNext();) {
				DocumentDefinitionRef documentDefinitionRef = it.next();
				// we do nothing if the reference is present
				if (((DocumentDefinitionRefImpl) documentDefinitionRef).getDocumentDefinition() != null) {
					continue;
				}

				String referenceId = documentDefinitionRef.getReferenceId();
				if (trace) {
					LOGGER.trace("Processing section " + sectionDefinition.getIdentifier() + " -> "
							+ referenceId + "(" + documentDefinitionRef.getIdentifier() + ")");
				}
				if (StringUtils.isEmpty(referenceId)) {
					LOGGER.warn("No document reference in definition: " + documentDefinitionRef);
					it.remove();
				} else {
					referenceId += DefinitionIdentityUtil.SEPARATOR + caseDefinition.getContainer();
					// get the complete document definition
					DocumentDefinitionTemplate definition = dictionaryService.getDefinition(
							DocumentDefinitionTemplate.class, referenceId);
					if (definition == null) {
						LOGGER.error("Missing document definition: "
								+ referenceId
								+ ". Check if the ID is correct or add new document definition in you definitions file.");
						return false;
					}
					((DocumentDefinitionRefImpl) documentDefinitionRef)
							.setDocumentDefinition((DocumentDefinitionImpl) definition);

					updatedDocs++;
				}
			}
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Updated " + updatedDocs + " document definitions in section "
						+ sectionDefinition.getIdentifier());
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Label> getLabelDefinitions(Object source) {
		Labels labels = null;
		if (source instanceof com.sirma.itt.cmf.beans.jaxb.CaseDefinition) {
			com.sirma.itt.cmf.beans.jaxb.CaseDefinition c = (com.sirma.itt.cmf.beans.jaxb.CaseDefinition) source;
			labels = c.getLabels();
		}

		if (labels != null) {
			return labels.getLabel();
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CaseDefinitionImpl saveTemplate(CaseDefinitionImpl definition) {
		return mutableDictionaryService.saveDefinition(definition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CaseDefinitionImpl findTemplateInSystem(String identifier) {
		return (CaseDefinitionImpl) dictionaryService.getDefinition(CaseDefinition.class,
				identifier);
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
	public Class<CaseDefinitionImpl> getDefinitionClass() {
		return CaseDefinitionImpl.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getMappingClass() {
		return com.sirma.itt.cmf.beans.jaxb.CaseDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XmlType getXmlValidationType() {
		return XmlType.CASE_DEFINITION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validateCompiledDefinition(CaseDefinitionImpl definition) {
		boolean valid = true;
		valid &= compiler.executeValidators(definition);
		for (SectionDefinition sectionDefinition : definition.getSectionDefinitions()) {
			for (DocumentDefinitionRef documentDefinitionRef : sectionDefinition
					.getDocumentDefinitions()) {
				valid &= compiler.executeValidators(documentDefinitionRef);
			}
		}
		return valid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCallbackName() {
		return "case";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void prepareForPersist(CaseDefinitionImpl definition) {
		if (definition.getAbstract() == null) {
			definition.setAbstract(Boolean.FALSE);
		}
		compiler.synchRegionProperties(definition);
		compiler.setDefaultProperties(definition, DMS_MODEL_PREFIX, true);
		if ((definition.getExpression() != null)
				&& !compiler.validateExpression(definition, definition.getExpression())) {
			LOGGER.warn(" !!! Expression in case definition " + definition.getIdentifier() + " - "
					+ definition.getExpression() + " is not valid and will be removed !!!");
			definition.setExpression(null);
		}
		compiler.validateModelConditions(definition, definition);
		for (SectionDefinition sectionDefinition : definition.getSectionDefinitions()) {
			// not needed
			compiler.setDefaultProperties(sectionDefinition, DMS_MODEL_PREFIX, true);
			for (DocumentDefinitionRef definitionRef : sectionDefinition.getDocumentDefinitions()) {
				compiler.synchRegionProperties(definitionRef);
				compiler.setDefaultProperties(definitionRef, DMS_MODEL_PREFIX, true);
				DocumentDefinitionRefImpl refImpl = (DocumentDefinitionRefImpl) definitionRef;
				if (refImpl.getMandatory() == null) {
					refImpl.setMandatory(Boolean.FALSE);
				}
				if (refImpl.getStructured() == null) {
					refImpl.setStructured(Boolean.FALSE);
				}

				if ((refImpl.getExpression() != null)
						&& !compiler.validateExpression(refImpl, refImpl.getExpression())) {
					LOGGER.warn(" !!! Expression in document definition " + refImpl.getIdentifier()
							+ " - " + definition.getExpression()
							+ " is not valid and will be removed !!!");
					refImpl.setExpression(null);
				}
				compiler.validateModelConditions(refImpl, refImpl);

				for (Iterator<TransitionDefinition> it = definitionRef.getTransitions().iterator(); it
						.hasNext();) {
					TransitionDefinition transitionDefinition = it.next();
					// remove hidden definitions
					if (transitionDefinition.getDisplayType() == DisplayType.SYSTEM) {
						it.remove();
					}
					compiler.validateExpressions(definition, transitionDefinition);
					compiler.setDefaultProperties(transitionDefinition, DMS_MODEL_PREFIX);
				}
				compiler.sort(definitionRef.getTransitions());
			}

			for (Iterator<TransitionDefinition> it = sectionDefinition.getTransitions().iterator(); it
					.hasNext();) {
				TransitionDefinition transitionDefinition = it.next();
				// remove hidden definitions
				if (transitionDefinition.getDisplayType() == DisplayType.SYSTEM) {
					it.remove();
				}
				compiler.validateExpressions(definition, transitionDefinition);
				compiler.setDefaultProperties(transitionDefinition, DMS_MODEL_PREFIX);
			}
			compiler.sort(sectionDefinition.getTransitions());
		}

		for (Iterator<TransitionDefinition> it = definition.getTransitions().iterator(); it
				.hasNext();) {
			TransitionDefinition transitionDefinition = it.next();
			// remove hidden definitions
			if (transitionDefinition.getDisplayType() == DisplayType.SYSTEM) {
				it.remove();
			}
			compiler.validateExpressions(definition, transitionDefinition);
			compiler.setDefaultProperties(transitionDefinition, DMS_MODEL_PREFIX);
		}
		compiler.sort(definition.getTransitions());

		// TODO: add restricting of allowed children to remove not supported children
	}

	@Override
	public void warmUpCache() {
		dictionaryService.getAllDefinitions(CaseDefinition.class);
	}

	@Override
	public void saveTemplateProperties(CaseDefinitionImpl newDefinition,
			CaseDefinitionImpl oldDefinition) {
		compiler.saveProperties(newDefinition, oldDefinition);
		for (SectionDefinition sectionDefinition : newDefinition.getSectionDefinitions()) {
			compiler.saveProperties(sectionDefinition,
					find(oldDefinition, sectionDefinition.getIdentifier()));
			for (DocumentDefinitionRef definitionRef : sectionDefinition.getDocumentDefinitions()) {
				compiler.saveProperties(
						definitionRef,
						find(oldDefinition, sectionDefinition.getIdentifier(),
								definitionRef.getIdentifier()));
			}
		}
		for (TransitionDefinition transitionDefinition : newDefinition.getTransitions()) {
			compiler.saveProperties(
					transitionDefinition,
					WorkflowHelper.getTransitionById(oldDefinition,
							transitionDefinition.getIdentifier()));
		}
		newDefinition.initBidirection();
	}

	/**
	 * Finds document definition by section and document identifiers
	 * 
	 * @param definition
	 *            the source definition
	 * @param sectionId
	 *            the section id
	 * @param documentId
	 *            the document id
	 * @return the region definition model or <code>null</code>
	 */
	private RegionDefinitionModel find(CaseDefinitionImpl definition, String sectionId,
			String documentId) {
		if (definition == null) {
			return null;
		}
		for (SectionDefinition sectionDefinition : definition.getSectionDefinitions()) {
			if (EqualsHelper.nullSafeEquals(sectionId, sectionDefinition.getIdentifier())) {
				for (DocumentDefinitionRef documentDefinitionRef : sectionDefinition
						.getDocumentDefinitions()) {
					if (EqualsHelper.nullSafeEquals(documentId,
							documentDefinitionRef.getIdentifier())) {
						return documentDefinitionRef;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Find.
	 * 
	 * @param definition
	 *            the definition
	 * @param sectionId
	 *            the section id
	 * @return the definition model
	 */
	private DefinitionModel find(CaseDefinitionImpl definition, String sectionId) {
		if (definition == null) {
			return null;
		}
		for (SectionDefinition sectionDefinition : definition.getSectionDefinitions()) {
			if (EqualsHelper.nullSafeEquals(sectionId, sectionDefinition.getIdentifier())) {
				return sectionDefinition;
			}
		}
		return null;
	}

	@Override
	public void setReferenceMode() {
		// do nothing here
	}

	@Override
	public List<?> getFilterDefinitions(Object source) {
		return Collections.emptyList();
	}

}
