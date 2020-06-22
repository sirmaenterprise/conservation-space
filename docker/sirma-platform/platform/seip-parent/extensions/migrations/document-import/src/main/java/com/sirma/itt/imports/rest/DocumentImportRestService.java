package com.sirma.itt.imports.rest;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.dao.InstanceServiceProvider;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.io.TempFileProvider;
import com.sirma.itt.emf.io.descriptors.ByteArrayFileDescriptor;
import com.sirma.itt.emf.io.descriptors.LocalProxyFileDescriptor;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.FileUtil;
import com.sirma.itt.imports.AnnotationEntry;
import com.sirma.itt.imports.CsvImportParser;
import com.sirma.itt.imports.DocumentTransformer;
import com.sirma.itt.imports.HtmlAnnotationParser;
import com.sirma.itt.imports.Template;
import com.sirma.itt.imports.TemplateBuilder;

/**
 * Rest service for importing document and objects data via CSV file format.
 *
 * @author BBonev
 */
@Path("/document-import")
@Produces(MediaType.APPLICATION_JSON)
public class DocumentImportRestService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentImportRestService.class);

	/** The index. */
	private static AtomicLong index = new AtomicLong(0);

	/** The temp file provider. */
	@Inject
	private javax.enterprise.inject.Instance<TempFileProvider> tempFileProvider;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The instance service. */
	@Inject
	@Proxy
	private InstanceServiceProvider instanceService;

	/** The expressions manager. */
	@Inject
	private ExpressionsManager expressionsManager;

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The csv import parser. */
	@Inject
	private CsvImportParser csvImportParser;

	/** The html annotation parser. */
	@Inject
	private HtmlAnnotationParser htmlAnnotationParser;

	/** The search service. */
	@Inject
	private SearchService searchService;

	@Inject
	private EventService eventService;


	/**
	 * Import document.
	 *
	 * @param request
	 *            the request
	 */
	@POST
	@Path("/importDocument/")
	@Consumes("multipart/form-data")
	@Secure(runAsSystem = true)
	public void importDocument(@Context HttpServletRequest request) {
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = null;
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			LOGGER.error("Failed to parse upload request due to: ", e);
			return;
		}

		File uploadFolder = createTempFolder();

		Map<String, Serializable> htmlProperties = null;
		Map<String, Serializable> csvProperties = null;

		for (FileItem fileItem : items) {
			try {
				Map<String, Serializable> properties = readFileItem(fileItem, uploadFolder);
				String filename = fileItem.getName();
				if (properties.isEmpty()) {
					LOGGER.warn("No metadata information for file {}", filename);
					continue;
				}

				String mimetype = getMimeType(properties, filename);
				if (mimetype.contains("htm")) {
					htmlProperties = properties;
				} else {
					csvProperties = properties;
				}
			} catch (IOException e) {
				LOGGER.error("Failed to ", e);
			}
		}

		if (csvProperties == null) {
			LOGGER.error("No input file found in the request");
			return;
		}

		if (htmlProperties == null) {
			LOGGER.warn("No html file found in the request! No file annotation will be done");
		}

		importInSystem(htmlProperties, csvProperties);
	}

	/**
	 * Import in system.
	 *
	 * @param htmlProperties
	 *            the html properties
	 * @param csvProperties
	 *            the csv properties
	 */
	private void importInSystem(Map<String, Serializable> htmlProperties,
			Map<String, Serializable> csvProperties) {
		Map<String, Instance> instances;
		Map<String, AnnotationEntry> pathToValueMapping = new LinkedHashMap<>();
		Map<String, Instance> relations = new LinkedHashMap<>();
		try {
			instances = csvImportParser.parseFile(csvProperties, pathToValueMapping, relations);

			Map<String, Instance> updatedInstances = save(instances, relations);

			for (Entry<String, AnnotationEntry> mapEntry : pathToValueMapping
					.entrySet()) {
				AnnotationEntry entry = mapEntry.getValue();
				Instance instance = entry.getOwningInstance();
				Instance updated = updatedInstances.get(instance.getId());
				entry.setOwning(updated);
				if (entry.getUpdatedValue() instanceof Instance) {
					updated = updatedInstances.get(((Instance) entry.getUpdatedValue()).getId());
					entry.setUpdatedValue(updated);
				} else if (entry.getUpdatedValue() instanceof Collection) {
					LinkedList<Serializable> updatedlist = new LinkedList<>();
					for (Object serializable : (Collection<Object>) entry.getUpdatedValue()) {
						if (serializable instanceof Instance) {
							updated = updatedInstances.get(((Instance) serializable).getId());
							if (updated!=null) {
								updatedlist.add(updated);
							} else {
								LOGGER.warn("No updated instance found for " + serializable);
								updatedlist.add((Serializable) serializable);
							}
						} else {
							updatedlist.add((Serializable) serializable);
						}
					}
					entry.setUpdatedValue(updatedlist);
				}
			}
			Instance originalInstance = getInstance(updatedInstances, DocumentInstance.class);

			Instance annotatedInstance = htmlAnnotationParser.createAnnotation(originalInstance,
					pathToValueMapping, htmlProperties);

			if ((annotatedInstance != null) && (originalInstance != null)) {
				// this should be the information for the first/base file
				Instance instance = updatedInstances.get(originalInstance.getId());
				if (!linkService.isLinked(instance.toReference(), annotatedInstance.toReference(),
						LinkConstants.REFERENCES_URI)) {
					linkService.link(instance, annotatedInstance, LinkConstants.REFERENCES_URI,
							LinkConstants.REFERENCES_URI, LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
				}

				// create relations between the annotated document and the parts
				for (Instance relation : relations.values()) {
					Serializable fromId = relation.getProperties().get("from");
					Serializable toId = relation.getProperties().get("to");

					Instance from = null;
					Instance to = null;
					if (originalInstance.getId().equals(fromId)) {
						from = annotatedInstance;
						to = updatedInstances.get(toId);
					} else if (originalInstance.getId().equals(toId)) {
						from = updatedInstances.get(fromId);
						to = annotatedInstance;
					}
					String mainLinkId = (String) relation.getProperties().get("emf:relationType");
					if ((from != null) && (to != null) && (mainLinkId != null)) {
						if (!linkService.isLinked(from.toReference(), to.toReference(), mainLinkId)) {
							linkService.link(from, to, mainLinkId, mainLinkId,
									LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to parse and import files from request due to", e);
		}
	}

	/**
	 * Gets the single instance of DocumentImportRestService.
	 *
	 * @param updatedInstances
	 *            the updated instances
	 * @param class1
	 *            the class1
	 * @return single instance of DocumentImportRestService
	 */
	private Instance getInstance(Map<String, Instance> updatedInstances,
			Class<DocumentInstance> class1) {
		for (Instance instance : updatedInstances.values()) {
			if (class1.isInstance(instance)) {
				return instance;
			}
		}
		return null;
	}

	/**
	 * Save.
	 * 
	 * @param instances
	 *            the instances
	 * @param relations
	 *            the relations
	 * @return the map
	 */
	private Map<String, Instance> save(Map<String, Instance> instances,
			Map<String, Instance> relations) {
		Map<String, Instance> converted = CollectionUtils.createLinkedHashMap(instances.size());
		Map<String, Instance> persisted = CollectionUtils.createLinkedHashMap(instances.size());

		// remove all relations from the list not to create graph cycle
		instances.keySet().removeAll(relations.keySet());

		List<Instance> list = new ArrayList<>(instances.values());

		for (Instance instance : list) {
			// removed by when converting properties
			if (!instances.containsKey(instance.getId())) {
				continue;
			}
			convertProperties(instance, instances, persisted);

			Instance convert = convertAndSave(instance, persisted);
			if (convert != null) {
				converted.put(typeConverter.convert(String.class, instance.getId()), convert);
			}
		}

		for (Instance instance : relations.values()) {
			Serializable fromId = instance.getProperties().get("from");
			Serializable toId = instance.getProperties().get("to");

			Instance from = persisted.get(fromId);
			Instance to = persisted.get(toId);
			String mainLinkId = (String) instance.getProperties().get("emf:relationType");
			if ((from != null) && (to != null) && (mainLinkId != null)) {
				if (!linkService.isLinked(from.toReference(), to.toReference(), mainLinkId)) {
					linkService.link(from, to, mainLinkId, mainLinkId,
							LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
				}
			}
		}
		return persisted;
	}

	/**
	 * Convert properties.
	 *
	 * @param instance
	 *            the instance
	 * @param instances
	 *            the instances
	 * @param persisted
	 *            the persisted
	 */
	private void convertProperties(Instance instance,
			Map<String, Instance> instances, Map<String, Instance> persisted) {
		Map<String, Serializable> convertedInstance = new LinkedHashMap<>();
		// convert properties
		for (Entry<String, Serializable> entry : instance.getProperties().entrySet()) {
			if (entry.getValue() instanceof Instance) {
				Instance value = (Instance) entry.getValue();
				// remove from the original map the old instance, we will replace is with new one
				// but we are removing it not to save it
				Instance old = instances.remove(value.getId());
				// if already processed then we use the processed instance if any
				if (old == null) {
					Instance instance2 = persisted.get(value.getId());
					convertedInstance.put(entry.getKey(), instance2);
					continue;
				}
				convertProperties(value, instances, persisted);

				Instance convert = convertAndSave(value, persisted);
				convertedInstance.put(entry.getKey(), convert);
			} else if (entry.getValue() instanceof Collection) {
				LinkedList<Object> converted = new LinkedList<>();
				for (Object object : (Collection<?>)entry.getValue()) {
					if (object instanceof Instance) {
						Instance value = (Instance) object;
						// remove from the original map the old instance, we will replace is with
						// new one but we are removing it not to save it
						Instance old = instances.remove(value.getId());
						if (old == null) {
							Instance instance2 = persisted.get(value.getId());
							converted.add(instance2);
							continue;
						}
						convertProperties(value, instances, persisted);

						Instance convert = convertAndSave(value, persisted);
						if (convert != null) {
							converted.add(convert);
						}
					} else {
						converted.add(object);
					}
				}
				convertedInstance.put(entry.getKey(), converted);
			}
		}
		instance.getProperties().putAll(convertedInstance);
		PropertiesUtil.cleanNullProperties(instance);
	}

	/**
	 * Convert.
	 *
	 * @param instance
	 *            the instance
	 * @param persisted
	 *            the save
	 * @return the instance
	 */
	private Instance convertAndSave(Instance instance, Map<String, Instance> persisted) {
		Uri rdfType = (Uri) instance.getProperties().get("rdf:type");
		if (rdfType != null) {
			DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(rdfType
					.toString());
			if (typeDefinition != null) {
				Instance concreteInstance = (Instance) typeConverter.convert(
						typeDefinition.getJavaClass(), instance);

				convertPropertyNames(concreteInstance);

				if (!persisted.containsKey(concreteInstance.getId())
						&& (instanceService.getService(concreteInstance.getClass()) != null)) {
					instanceService.save(concreteInstance, new Operation("initialImport"));
					// if the ID has been changed add the old id also
					if (!EqualsHelper.nullSafeEquals(concreteInstance.getId(), instance.getId())) {
						persisted.put(instance.getId().toString(), concreteInstance);
					}
					persisted.put(concreteInstance.getId().toString(), concreteInstance);
				}
				return concreteInstance;
			}
			LOGGER.error("No Type definition for rdf:type -> {} for instance: {}",
					rdfType.toString(), instance);
		} else {
			// we can handle instance without need of rdf:type
			if (instance instanceof Resource ) {
				return instance;
			}
			LOGGER.error("No rdf:type defined for instance and will be skipped! Instance: {}",
					instance);
		}
		return null;
	}

	/**
	 * Convert property names.
	 *
	 * @param concreteInstance
	 *            the concrete instance
	 */
	private void convertPropertyNames(Instance concreteInstance) {
		DefinitionModel model = dictionaryService.getInstanceDefinition(concreteInstance);
		if (model == null) {
			return;
		}
		Map<String, String> fields = new LinkedHashMap<>(32);
		collectFieldsMapping(model, fields);
		Map<String, Serializable> properties = concreteInstance.getProperties();
		Map<String, Serializable> converteredNames = CollectionUtils.createLinkedHashMap(properties
				.size());
		for (Entry<String, String> entry : fields.entrySet()) {
			Serializable value = properties.remove(entry.getKey());
			if (value != null) {
				converteredNames.put(entry.getValue(), value);
			}
		}
		properties.putAll(converteredNames);
	}

	/**
	 * Collect fields mapping.
	 *
	 * @param model
	 *            the model
	 * @param fields
	 *            the fields
	 */
	private void collectFieldsMapping(DefinitionModel model, Map<String, String> fields) {
		if (model instanceof RegionDefinitionModel) {
			for (RegionDefinition regionDefinition : ((RegionDefinitionModel) model).getRegions()) {
				collectFieldsMapping(regionDefinition, fields);
			}
		}
		for (PropertyDefinition definition : model.getFields()) {
			if ((definition.getUri() != null)
					&& !DefaultProperties.NOT_USED_PROPERTY_VALUE.equals(definition.getUri())) {
				fields.put(definition.getUri(), definition.getIdentifier());
			}
		}
	}

	/**
	 * Transforms the document data file according to the given templates file.
	 *
	 * @param request
	 *            the request
	 * @return The content of the transformed file
	 */
	@POST
	@Path("/transformDocument/")
	@Consumes("multipart/form-data")
	@Secure(runAsSystem = true)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response transformDocument(@Context HttpServletRequest request) {
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);

		List<FileItem> items = null;
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			LOGGER.error("Failed to parse upload request due to: ", e);
			return Response.serverError().build();
		}

		File uploadFolder = createTempFolder();

		if (items.size() != 2) {
			LOGGER.error("Expected two files: html and csv!");
			ResponseBuilder response = Response.status(Status.BAD_REQUEST);
			return response.build();
		}

		Map<String, Serializable> htmlProperties = null;
		Map<String, Serializable> dataFileProperties = null;

		for (FileItem fileItem : items) {
			try {
				Map<String, Serializable> properties = readFileItem(fileItem, uploadFolder);
				String filename = fileItem.getName();
				if (properties.isEmpty()) {
					LOGGER.warn("No metadata information for file {}", filename);
					continue;
				}

				String mimetype = getMimeType(properties, filename);
				if (mimetype.contains("htm")) {
					htmlProperties = properties;
				} else {
					dataFileProperties = properties;
				}
			} catch (IOException e) {
				LOGGER.error("Failed to ", e);
				return Response.serverError().build();
			}
		}

		if (dataFileProperties == null) {
			LOGGER.error("No input file found in the request");
			return Response.serverError().build();
		}

		if (htmlProperties == null) {
			LOGGER.warn("No html file found in the request! No file annotation will be done");
		}

		try {
			// build the templates
			TemplateBuilder templateBuilder = new TemplateBuilder();
			List<Template> templates = templateBuilder.buildTemplates();

			if (templates.isEmpty()) {
				LOGGER.warn("No valid templates!");
				ResponseBuilder response = Response.status(Status.BAD_REQUEST);
				return response.build();
			}

			DocumentTransformer transformer = new DocumentTransformer(searchService);
			final String transformedFile = transformer.transformDataFile(dataFileProperties,
					templates);

			if (transformedFile == null) {
				LOGGER.error("Error transforming the data file!");
				return Response.serverError().build();
			}

			Map<String, Serializable> csvProperties = CollectionUtils.createLinkedHashMap(5);
			String name = (String) dataFileProperties.get(DocumentProperties.NAME);
			csvProperties.put(DocumentProperties.NAME, name);
			csvProperties.put(DocumentProperties.FILE_LOCATOR, new ByteArrayFileDescriptor(name,
					transformedFile.getBytes("utf-8")));
			importInSystem(htmlProperties, csvProperties);

			ResponseBuilder response = Response.ok(transformedFile);
			String originalName = (String) dataFileProperties.get(DocumentProperties.NAME);
			response.header("Content-Disposition", "attachment; filename=\"transformed_"
					+ originalName + "\"");

			return response.build();

		} catch (Exception e) {
			LOGGER.error("Failed to parse and import files from request due to", e);
			return Response.serverError().build();
		}
	}

	/**
	 * Gets the mime type.
	 *
	 * @param properties
	 *            the properties
	 * @param filename
	 *            the filename
	 * @return the mime type
	 */
	private String getMimeType(Map<String, Serializable> properties, String filename) {
		String mimetype = (String) properties.get(DocumentProperties.MIMETYPE);

		if (StringUtils.isBlank(mimetype)
				|| mimetype.contains(MediaType.APPLICATION_OCTET_STREAM)) {
			LOGGER.warn("No mimetype information for file {}", filename);
			// will try with extension
			mimetype = filename.substring(filename.lastIndexOf("."));
		}
		return mimetype;
	}

	/**
	 * Creates the temp folder.
	 *
	 * @return the file
	 */
	private File createTempFolder() {
		return tempFileProvider.get().createTempDir(
				"Upload-" + System.currentTimeMillis() + '-' + index.getAndIncrement());
	}

	/**
	 * Method to update the document properties before upload.
	 *
	 * @param item
	 *            is the bean holding uploaded data
	 * @param parentFolder
	 *            is the dir where to store instance
	 * @return read properties
	 * @throws IOException
	 *             if any error occurs
	 */
	private Map<String, Serializable> readFileItem(FileItem item, File parentFolder)
			throws IOException {

		// check if created
		if (parentFolder == null) {
			LOGGER.error("\n==================================\n\tDocument will not be uploaded because: No temporary directory"
					+ "\n==================================");
			return Collections.emptyMap();
		}

		long lenght = item.getSize();
		String name = item.getName();
		String mimetype = item.getContentType();
		byte[] data = item.get();

		String finalFileName = new File(name).getName();
		int lastSeparator = finalFileName.lastIndexOf("\\");
		if (lastSeparator > 0) {
			finalFileName = finalFileName.substring(lastSeparator + 1, finalFileName.length());
		}
		File localFile = null;
		if (finalFileName.getBytes().length > 255) {
			String extension = FileUtil.splitNameAndExtension(finalFileName).getSecond();
			if (StringUtils.isBlank(extension)) {
				extension = ".tmp";
			}
			localFile = new File(parentFolder, System.nanoTime() + "." + extension);
		} else {
			localFile = new File(parentFolder, finalFileName);
		}
		try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(localFile))) {
			// copy the file now
			IOUtils.copyLarge(new ByteArrayInputStream(data), output);
			IOUtils.closeQuietly(output);

			Map<String, Serializable> properties = CollectionUtils.createLinkedHashMap(4);

			properties.put(DocumentProperties.FILE_LOCATOR, new LocalProxyFileDescriptor(
					finalFileName, localFile.getAbsolutePath()));
			properties.put(DocumentProperties.NAME, name);
			properties.put(DocumentProperties.MIMETYPE, mimetype);
			// place non converted value, will convert it later
			properties.put(DocumentProperties.FILE_SIZE, humanReadableByteCount(lenght));

			return properties;
		}
	}

	/**
	 * Converts bytes into human readable format.
	 *
	 * @param bytes
	 *            bytes to convert.
	 * @return human readable string.
	 */
	public String humanReadableByteCount(long bytes) {
		return FileUtil.humanReadableByteCount(bytes);
	}
}
