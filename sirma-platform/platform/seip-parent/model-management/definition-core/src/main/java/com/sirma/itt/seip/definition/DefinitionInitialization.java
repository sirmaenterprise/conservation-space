package com.sirma.itt.seip.definition;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
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
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.seip.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.seip.definition.jaxb.TypeDefinition;
import com.sirma.itt.seip.definition.jaxb.Types;
import com.sirma.itt.seip.definition.schema.BaseSchemas;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.sep.xml.JAXBHelper;

/**
 * Provides methods for definition initialization. On startup loads all definition files and initialize/update the
 * current database state.
 * <p>
 * Refactored - to move in separate classes the functions for compiling and processing definitions
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionInitialization {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DefinitionManagementService definitionManagementService;

	@Inject
	private MutableDefinitionService mutableDefinitionService;

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private DefinitionCompilerHelper compilerHelper;

	@Inject
	private ObjectMapper dozerMapper;

	/**
	 * Initialize definitions. This should be called before all other definitions are loaded
	 */
	@OnTenantAdd(order = -1000)
	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = 1000000)
	public void onStart() {
		// load types
		refreshTypeDefinitions();

		// load default field types
		mutableDefinitionService.initializeBasePropertyDefinitions();
	}

	/**
	 * Update data types because we probably have changes in the model
	 *
	 * @param event
	 *            the event
	 */
	@Transactional
	void onSemanticDefinitionUpdate(@Observes LoadSemanticDefinitions event) {
		refreshTypeDefinitions();
	}

	private void refreshTypeDefinitions() {
		List<FileDescriptor> definitions = definitionManagementService.getDefinitions(DataTypeDefinition.class);

		// merge all datatypes found into a single datatype grouped by name
		Collection<List<TypeDefinition>> grouped = definitions
				.stream()
					.map(this::getContent)
					.filter(Objects::nonNull)
					.filter(validByXsd())
					.flatMap(toJaxbTypeDefinitions())
					.collect(Collectors.groupingBy(type -> type.getName().toLowerCase()))
					.values();

		List<DataType> datatypes = grouped
				.stream()
					.map(types -> types.stream().map(type -> dozerMapper.map(type, DataType.class)).reduce(
							new DataType(), mergeTypes()))
					.collect(Collectors.toList());

		// now persist the changes by overriding the existing items or adding them as new
		for (DataType typeDefinition : datatypes) {
			LOGGER.trace("Saving definition: {}", typeDefinition.getName());
			fillDefaultProperties(typeDefinition);
			mutableDefinitionService.saveDataTypeDefinition(typeDefinition);
		}
	}

	private File getContent(FileDescriptor location) {
		if (location == null) {
			return null;
		}

		File tempFile = tempFileProvider.createTempFile("tempContent", ".tmp");
		try {
			if (location.writeTo(tempFile) < 0) {
				tempFileProvider.deleteFile(tempFile);
				return null;
			}
		} catch (IOException e) {
			LOGGER.warn("Failed to read file from File System", e);
		}

		return tempFile;
	}

	private static void fillDefaultProperties(DataType typeDefinition) {
		if (typeDefinition.getDescription() == null) {
			typeDefinition.setDescription(typeDefinition.getName());
		}

		if (typeDefinition.getTitle() == null) {
			typeDefinition.setTitle(typeDefinition.getName());
		}
	}

	private static Predicate<File> validByXsd() {
		return file -> {
			List<Message> list = new LinkedList<>();
			if (!JAXBHelper.validateFile(file, BaseSchemas.TYPES_DEFINITION, list)) {
				String validationMessage = ValidationLoggingUtil.printMessages(list);
				LOGGER.error(validationMessage);
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
					String fileContents = IOUtils.toString(file.toURI());
					LOGGER.warn("Could not load file {} as {}\n{}", file.getAbsolutePath(), Types.class, fileContents);
					return Stream.empty();
				}
				List<TypeDefinition> type = typeDefs.getType();
				return type.stream();
			} catch (IOException e) {
				LOGGER.warn("Could not read file {}, because {}", file.getAbsolutePath(), e.getMessage());
				return Stream.empty();
			} finally {
				try {
					Files.delete(file.toPath());
				} catch (IOException e) {
					LOGGER.warn("Failed to clean downloaded definition file from: {}", file, e);
				}
			}
		};
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
}