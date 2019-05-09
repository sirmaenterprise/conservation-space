package com.sirma.itt.seip.template;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static com.sirma.itt.seip.template.TemplateProperties.CORRESPONDING_INSTANCE;
import static com.sirma.itt.seip.template.TemplateProperties.PRIMARY;
import static com.sirma.itt.seip.template.TemplateProperties.PURPOSE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_RULE;
import static com.sirma.itt.seip.template.TemplateProperties.TYPE_ATTRIBUTE;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.jaxb.ComplexFieldDefinition;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.exceptions.DefinitionValidationException;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.template.db.TemplateDao;
import com.sirma.itt.seip.template.jaxb.TemplateSchemaProvider;
import com.sirma.sep.xml.JAXBHelper;

/**
 * Default implementation of {@link TemplateImportService}.
 *
 * @author Vilizar Tsonev
 */
@Singleton
public class TemplateImportServiceImpl implements TemplateImportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private TemplateServiceImpl templateService;

	@Inject
	private EventService eventService;

	@Inject
	private ObjectMapper mapper;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private TemplateDao templateDao;

	@Inject
	private TemplateValidator templateValidator;

	@Override
	public List<String> validate(TemplateValidationRequest validationRequest) {
		List<File> files = loadFromPath(validationRequest.getPath());

		List<Message> xsdErrors = new LinkedList<>();
		List<String> businessValidationErrors = new ArrayList<>();
		List<Template> templates = new ArrayList<>(files.size());

		for (File file : files) {
			if (!isFileExtensionValid(file)) {
				businessValidationErrors
						.add("File '" + file.getName() + "' is of invalid type. Only XML files are allowed.");
				continue;
			}
			if (!JAXBHelper.validateFile(file, TemplateSchemaProvider.TEMPLATE_PROVIDER, xsdErrors)) {
				continue;
			}
			com.sirma.itt.seip.template.jaxb.TemplateDefinition definition = JAXBHelper.load(file,
					com.sirma.itt.seip.template.jaxb.TemplateDefinition.class);

			Template template = toTemplate(definition);
			templates.add(template);
		}

		List<String> duplicateIdsErrors = validateForDuplicateIds(templates);
		if (!duplicateIdsErrors.isEmpty()) {
			businessValidationErrors.addAll(duplicateIdsErrors);
		} else {
			// logical validation can be correctly executed only if there are no duplicate ids
			List<Template> mergedWithExisting = mergeWithExistingActiveTemplates(templates);
			businessValidationErrors.addAll(TemplateValidator.validate(mergedWithExisting));
			businessValidationErrors.addAll(templateValidator.hasDefinition(templates, validationRequest::getDefinition));
			businessValidationErrors.addAll(templateValidator.validateRules(templates, validationRequest::getDefinition));
		}

		if (!xsdErrors.isEmpty() || !businessValidationErrors.isEmpty()) {
			return formatErrors(xsdErrors, businessValidationErrors);
		}

		return Collections.emptyList();
	}

	private List<Template> mergeWithExistingActiveTemplates(List<Template> newlyImported) {
		List<Template> existing = templateDao.getAllTemplates();
		Map<String, Template> existingMapped = existing
				.stream()
				.collect(CollectionUtils.toIdentityMap(Template::getId));
		Map<String, Template> newlyImportedMapped = newlyImported
				.stream()
				.collect(CollectionUtils.toIdentityMap(Template::getId));
		existingMapped.putAll(newlyImportedMapped);
		return new ArrayList<>(existingMapped.values());
	}

	@Override
	public void importTemplates(String directoryPath) {
		List<File> files = loadFromPath(directoryPath);

		LOGGER.info("Initiating reload of {} template definitions", files.size());

		files
				.stream()
					.map(file -> JAXBHelper.load(file, com.sirma.itt.seip.template.jaxb.TemplateDefinition.class))
					.map(TemplateImportServiceImpl::toTemplate)
					.forEach(templateService::saveOrUpdateImportedTemplate);

		eventService.fire(new TemplatesSynchronizedEvent());
	}

	private static List<String> formatErrors(List<Message> xsdErrors, List<String> businessValidationErrors) {
		if (xsdErrors.isEmpty()) {
			return businessValidationErrors;
		}
		List<String> xsdStringErrors = xsdErrors
				.stream()
				.map(Message::getMessage)
				.collect(Collectors.toList());

		List<String> errors = new ArrayList<>(xsdStringErrors.size() + businessValidationErrors.size());
		errors.addAll(xsdStringErrors);
		errors.addAll(businessValidationErrors);
		return errors;
	}

	private static Template toTemplate(com.sirma.itt.seip.template.jaxb.TemplateDefinition definition) {
		List<ComplexFieldDefinition> fields = definition.getFields().getField();

		Template templateData = new Template();
		templateData.setId(definition.getId());
		templateData.setTitle(extractValue(TITLE, fields));
		templateData.setForType(extractValue(TYPE_ATTRIBUTE, fields));
		templateData.setPurpose(extractValue(PURPOSE, fields));
		templateData.setPrimary(Boolean.valueOf(extractValue(PRIMARY, fields)));
		if (hasField(CORRESPONDING_INSTANCE, fields)) {
			templateData.setCorrespondingInstance(extractValue(CORRESPONDING_INSTANCE, fields));
		}
		if (hasField(TEMPLATE_RULE, fields)) {
			templateData.setRule(extractValue(TEMPLATE_RULE, fields));
		}
		templateData.setContent(extractValue(CONTENT, fields));

		return templateData;
	}

	private static String extractValue(String name, List<ComplexFieldDefinition> fields) {
		Optional<ComplexFieldDefinition> field = extractField(name, fields);
		if (field.isPresent()) {
			return field.get().getValue();
		}
		return null;
	}

	private static Optional<ComplexFieldDefinition> extractField(String name, List<ComplexFieldDefinition> fields) {
		return fields
				.stream()
				.filter(field -> name.equals(field.getName()))
				.findFirst();
	}

	private static boolean hasField(String name, List<ComplexFieldDefinition> fields) {
		return extractField(name, fields).isPresent();
	}

	private static List<File> loadFromPath(String path) {
		try (Stream<Path> pathsStream = Files.walk(Paths.get(path))) {
			return pathsStream
					.filter(Files::isRegularFile)
					.map(Path::toFile)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new EmfApplicationException("Failed to read template files from directory " + path, e);
		}
	}

	private static boolean isFileExtensionValid(File file) {
		return file.getName().toLowerCase().endsWith(".xml");
	}

	@Override
	public List<File> exportTemplates(List<String> ids) {
		File tempDir = tempFileProvider.createTempDir("templatesExport");
		List<Template> templates = ids
				.stream()
				.map(templateService::getTemplate)
				.collect(Collectors.toList());
		return toFilesList(templates, tempDir);
	}

	@Override
	public List<File> exportAllTemplates() {
		File tempDir = tempFileProvider.createTempDir("templatesExport");
		return toFilesList(templateDao.getAllTemplates(), tempDir);
	}

	private List<File> toFilesList(List<Template> templates, File tempDir) {
		return templates
				.stream()
				.peek(template -> template.setContent(templateService.getContent(template.getId())))
				.map(template -> toXmlFile(template, tempDir))
				.collect(Collectors.toList());
	}

	private File toXmlFile(Template template, File parentDir) {
		TemplateDefinition definition = toDefinition(template);

		com.sirma.itt.seip.template.jaxb.TemplateDefinition templateDefinition = mapper.map(definition,
				com.sirma.itt.seip.template.jaxb.TemplateDefinition.class);
		String xmlContent = convertToXML(templateDefinition);

		String fileName = template.getId() + ".xml";
		File file = new File(parentDir, fileName);
		try {
			FileUtils.writeStringToFile(file, xmlContent, StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOGGER.error("Failed to write content to temp file " + fileName, e);
		}
		return file;
	}

	private static String convertToXML(Object definition) {
		try {
			StringWriter stringWriter = new StringWriter();
			JAXBContext context = JAXBContext.newInstance(com.sirma.itt.seip.template.jaxb.TemplateDefinition.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(definition, stringWriter);
			return stringWriter.toString();
		} catch (JAXBException e) {
			throw new DefinitionValidationException("Cannot generate template due to " + e.getMessage(), e);
		}
	}

	private TemplateDefinition toDefinition(Template template) {
		TemplateDefinitionImpl definition = new TemplateDefinitionImpl();
		definition.setIdentifier(template.getId());
		List<PropertyDefinition> fields = definition.getFields();

		fields.add(createField(TYPE, template.getForType()));
		fields.add(createField(PRIMARY, template.getPrimary()));
		fields.add(createField(TITLE, template.getTitle()));
		fields.add(createField(CONTENT, template.getContent()));
		if (StringUtils.isNotBlank(template.getPurpose())) {
			fields.add(createField(PURPOSE, template.getPurpose()));
		}
		if (StringUtils.isNotBlank(template.getCorrespondingInstance())) {
			fields.add(createField(CORRESPONDING_INSTANCE, template.getCorrespondingInstance()));
		}
		if (StringUtils.isNotBlank(template.getRule())) {
			fields.add(createField(TEMPLATE_RULE, template.getRule()));
		}
		return definition;
	}

	private PropertyDefinition createField(String key, Serializable value) {
		FieldDefinitionImpl impl = new FieldDefinitionImpl();
		impl.setIdentifier(key);
		if (value instanceof String) {
			if (value.toString().length() < 1024) {
				impl.setType("an.." + value.toString().length());
			} else {
				impl.setType("ANY");
			}
		} else if (value instanceof Boolean) {
			impl.setType("boolean");
		} else {
			// this is set not to miss the definition type and later to become an invalid xml
			LOGGER.warn("Unrecognized type when creating definition. Setting it to ANY");
			impl.setType("ANY");
		}

		impl.setDisplayType(DisplayType.SYSTEM);
		impl.setMandatory(Boolean.FALSE);
		// convert values to string depending on the type
		impl.setValue(typeConverter.convert(String.class, value));
		// fill the remaining default properties if any
		impl.setDefaultProperties();
		return impl;
	}

	private static List<String> validateForDuplicateIds(List<Template> templates) {
		Set<String> visitedIds = new HashSet<>();
		List<String> errors = new ArrayList<>();
		for (Template template : templates) {
			String id = template.getId();
			if (visitedIds.contains(id)) {
				errors.add("More than one imported templates have identical ID: " + id);
				LOGGER.debug("More than one imported templates were detected to have identical ID: {}", id);
				continue;
			}
			visitedIds.add(id);
		}
		return errors;
	}
}
