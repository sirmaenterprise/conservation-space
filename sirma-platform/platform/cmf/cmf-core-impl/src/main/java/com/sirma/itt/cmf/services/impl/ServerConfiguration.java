package com.sirma.itt.cmf.services.impl;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.services.ServerAdministration;
import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.content.ContentService;
import com.sirma.itt.seip.definition.DefinitionManagementService;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.MutableDictionaryService;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.seip.definition.compile.DefinitionLoader;
import com.sirma.itt.seip.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.seip.definition.jaxb.TypeDefinition;
import com.sirma.itt.seip.definition.jaxb.Types;
import com.sirma.itt.seip.definition.schema.BaseSchemas;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;

/**
 * Provides methods for server initialization. On startup loads all definition files and initialize/update the current
 * database state.
 * <p>
 * Refactored - to move in separate classes the functions for compiling and processing definitions
 *
 * @author BBonev
 */
@ApplicationScoped
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
	private ObjectMapper dozerMapper;

	/**
	 * Initialize definitions. This should be called before all other definitions are loaded
	 */
	@OnTenantAdd
	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = 1000000)
	public void onStart() {
		// load types
		refreshTypeDefinitions();

		// load default field types
		insertBaseDefinitions();
	}

	@Override
	public void refreshDefinitions() {
		loader.loadDefinitions();
	}

	/**
	 * Update data types because we probably have changes in the model
	 *
	 * @param event
	 *            the event
	 */
	void onSemanticDefinitionUpdate(@Observes LoadSemanticDefinitions event) {
		refreshTypeDefinitions();
	}

	@Override
	public void refreshTypeDefinitions() {
		List<FileDescriptor> definitions = definitionManagementService.getDefinitions(DataTypeDefinition.class);

		// merge all datatypes found into a single datatype grouped by name
		Collection<List<TypeDefinition>> grouped = definitions
				.stream()
					.map(contentService::getContent)
					.filter(Objects::nonNull)
					.filter(validByXsd())
					.flatMap(toJaxbTypeDefinitions())
					.collect(Collectors.groupingBy(type -> type.getName().toLowerCase()))
					.values();

		List<DataType> datatypes = grouped
				.stream()
					.map(types -> types
							.stream()
								.collect(Collectors.reducing(new DataType(), convertToDataType(), mergeTypes())))
					.collect(Collectors.toList());

		// now persist the changes by overriding the existing items or adding them as new
		for (DataType typeDefinition : datatypes) {
			LOGGER.trace("Loading definition: " + typeDefinition.getName());
			fillDefaultProperties(typeDefinition);
			// all names should have the same case
			DataTypeDefinition dataTypeDefinition = dictionaryService.getDataTypeDefinition(typeDefinition.getName());
			if (dataTypeDefinition == null) {
				mutableDictionaryService.saveDataTypeDefinition(typeDefinition);
			} else {
				typeDefinition.setId(dataTypeDefinition.getId());
				// just update the existing value
				mutableDictionaryService.saveDataTypeDefinition(typeDefinition);
			}
		}
	}

	private static void fillDefaultProperties(DataType typeDefinition) {
		if (typeDefinition.getDescription() == null) {
			typeDefinition.setDescription(typeDefinition.getName());
		}
		if (typeDefinition.getTitle() == null) {
			typeDefinition.setTitle(typeDefinition.getName());
		}
	}

	private Predicate<File> validByXsd() {
		return file -> {
			List<Message> list = new LinkedList<>();
			if (!compilerHelper.validateDefinition(file, BaseSchemas.TYPES_DEFINITION, list)) {
				LOGGER.error(ValidationLoggingUtil.printMessages(list));
				return false;
			}
			return true;
		};
	}

	private Function<File, Stream<TypeDefinition>> toJaxbTypeDefinitions() {
		return file -> {
			try {
				Types typeDefs = compilerHelper.load(file, Types.class);
				if (typeDefs == null) {
					return Stream.empty();
				}
				List<TypeDefinition> type = typeDefs.getType();
				return type.stream();
			} finally {
				if (file.delete()) {
					LOGGER.warn("Failed to clean downloaded definition file from: {}", file);
				}
			}
		};
	}

	private Function<TypeDefinition, DataType> convertToDataType() {
		return type -> dozerMapper.map(type, DataType.class);
	}

	private static BinaryOperator<DataType> mergeTypes() {
		return (existing, newData) -> {
			// make sure they have the same case
			existing.setName(newData.getName().toLowerCase());
			// override properties with incoming value
			existing.setJavaClassName(getOrDefault(newData.getJavaClassName(), existing.getJavaClassName()));
			existing.setTitle(getOrDefault(newData.getTitle(), existing.getTitle()));
			existing.setDescription(getOrDefault(newData.getDescription(), existing.getDescription()));
			// merge all uries
			Set<String> current = existing.getUries();
			Set<String> newUries = newData.getUries();
			Set<String> combined = new LinkedHashSet<>(current);
			combined.addAll(newUries);
			StringJoiner joiner = new StringJoiner(",");
			combined.forEach(joiner::add);
			existing.setUri(joiner.toString());
			return existing;
		};
	}

	@Override
	public void insertBaseDefinitions() {
		mutableDictionaryService.initializeBasePropertyDefinitions();
	}

	@Override
	public void resetCodelists() {
		eventService.fire(new ResetCodelistEvent());
	}
}
