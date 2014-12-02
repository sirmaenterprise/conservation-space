package com.sirma.itt.cmf.services.impl;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.event.LoadTemplates;
import com.sirma.itt.cmf.services.ServerAdministration;
import com.sirma.itt.cmf.xml.XmlType;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.codelist.event.LoadCodelists;
import com.sirma.itt.emf.codelist.event.ResetCodelistEvent;
import com.sirma.itt.emf.definition.DefinitionManagementService;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.emf.definition.event.LoadAllDefinitions;
import com.sirma.itt.emf.definition.load.DefinitionLoader;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.jaxb.TypeDefinition;
import com.sirma.itt.emf.definition.model.jaxb.Types;
import com.sirma.itt.emf.domain.VerificationMessage;
import com.sirma.itt.emf.dozer.DozerMapper;
import com.sirma.itt.emf.event.ApplicationInitializationEvent;
import com.sirma.itt.emf.event.ApplicationStartupEvent;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.io.ContentService;
import com.sirma.itt.emf.patch.PatchDbService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.util.ValidationLoggingUtil;

/**
 * Provides methods for server initialization. On startup loads all definition files and
 * initialize/update the current database state.
 * <p>
 * Refactored - to move in separate classes the functions for compiling and processing definitions
 * 
 * @author BBonev
 */
@Startup
@Singleton
@DependsOn(value = { PatchDbService.SERVICE_NAME, SecurityContextManager.SERVICE_NAME })
public class ServerConfiguration implements ServerAdministration {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfiguration.class);

	@Inject
	private DefinitionManagementService definitionManagementService;

	@Inject
	private MutableDictionaryService mutableDictionaryService;
	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private ContentService contentService;

	@Inject
	private EventService eventService;

	@Inject
	private DefinitionLoader loader;

	@Inject
	private DefinitionCompilerHelper compilerHelper;

	@Inject
	private DozerMapper dozerMapper;

	/**
	 * Initialize definitions.
	 */
	@PostConstruct
	public void initialzeDefinitions() {
		// notify that application is about to start
		eventService.fire(new ApplicationInitializationEvent());
		// load types
		refreshTypeDefinitions();

		// load defualt field types
		insertBaseDefinitions();

		// NOTE-BBonev:
		// Fire events for asynchronous loading of definitions and codelists. If
		// synchronous and the loading time exceeds the allowed startup time of
		// the component then the server will fail to start!

		// start codelist loading
		eventService.fire(new LoadCodelists());
		// fire event to schedule definition loading
		eventService.fire(new LoadAllDefinitions());
		// start template initialization
		eventService.fire(new LoadTemplates());
		// notify that application is started
		eventService.fire(new ApplicationStartupEvent());
	}

	@Override
	@Secure
	public void refreshDefinitions() {
		loader.loadDefinitions();
	}

	@Override
	@Secure
	public void refreshTemplateDefinitions() {
		loader.loadTemplateDefinitions();
	}

	@Override
	@Secure
	public void refreshTypeDefinitions() {
		List<FileDescriptor> definitions = definitionManagementService
				.getDefinitions(DataTypeDefinition.class);

		for (FileDescriptor descriptor : definitions) {
			File file = contentService.getContent(descriptor);
			if (file == null) {
				LOGGER.error("Failed to download location: " + descriptor);
				continue;
			}
			try {
				List<VerificationMessage> list = new LinkedList<>();
				if (!compilerHelper.validateDefinition(file, XmlType.TYPES_DEFINITION, list)) {
					LOGGER.error(ValidationLoggingUtil.printMessages(list));
					continue;
				}
				Types typeDefs = compilerHelper.load(file, Types.class);
				if (typeDefs == null) {
					LOGGER.warn("Failed to parse definition: " + descriptor);
					continue;
				}
				List<TypeDefinition> type = typeDefs.getType();
				for (TypeDefinition typeDefinition : type) {
					LOGGER.trace("Loading definition: " + typeDefinition.getName());
					// all names should have the same case
					typeDefinition.setName(typeDefinition.getName().toLowerCase());
					DataTypeDefinition dataTypeDefinition = dictionaryService
							.getDataTypeDefinition(typeDefinition.getName());
					if (dataTypeDefinition == null) {
						DataType dataType = dozerMapper.getMapper().map(typeDefinition,
								DataType.class);
						mutableDictionaryService.saveDataTypeDefinition(dataType);
					} else {
						DataType copy = dozerMapper.getMapper().map(typeDefinition, DataType.class);
						copy.setId(dataTypeDefinition.getId());
						mutableDictionaryService.saveDataTypeDefinition(copy);
					}
				}
			} finally {
				file.delete();
			}
		}
	}

	@Override
	public void insertBaseDefinitions() {
		mutableDictionaryService.initializeBasePropertyDefinitions();
	}

	@Override
	@Secure
	public void refreshConstrainDefinitions() {
		// nothing to do here
	}

	@Override
	public void resetCodelists() {
		eventService.fire(new ResetCodelistEvent());
	}
}
