/*
 *
 */
package com.sirma.cmf.web.upload;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.content.UploadPostProcessor;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Identity;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.io.TempFileProvider;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rest.EmfApplicationException;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.FileUtil;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.util.PropertyModelComparator;

/**
 * Handles file uploads.
 * <p>
 * REVIEW: What about changing the scope to Application? The methods does not require scope and are
 * designed to handle parallel requests.
 * 
 * @author Adrian Mitev
 */
@Stateless
@Path("/upload")
public class UploadRestService extends EmfRestService {

	/** The Constant ITEMS_COUNT. */
	private static final String ITEMS_COUNT = "itemsCount";

	/** The Constant TYPE. */
	private static final String TYPE = "type";

	/** The Constant FILE. */
	private static final String FILE = "file";

	/** The Constant PATH. */
	private static final String PATH = "path";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(UploadRestService.class);

	/** The upload operation. */
	private static Operation UPLOAD_OPERATION = new Operation(ActionTypeConstants.UPLOAD);

	/** temp file provider. */
	@Inject
	private TempFileProvider tempFileProvider;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The codelist service. */
	@Inject
	private CodelistService codelistService;

	/** The document service. */
	@Inject
	private DocumentService documentService;

	/** The documet type codelist. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_DOCUMENT_TITLE, defaultValue = "210")
	private Integer documetTypeCodelist;

	/** The default language. */
	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_LANGUAGE, defaultValue = "bg")
	private String defaultLanguage;

	/** The max allowed file size. */
	@Inject
	@Config(name = EmfConfigurationProperties.FILE_UPLOAD_MAXSIZE, defaultValue = "10485760")
	private Integer maxSize;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The label provider. */
	@Inject
	private LabelProvider labelProvider;

	/** The authentication service. */
	@Inject
	protected javax.enterprise.inject.Instance<AuthenticationService> authenticationService;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The instance service. */
	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	/** The file item factory. */
	@Inject
	private javax.enterprise.inject.Instance<EmfFileItemFactory> fileItemFactory;

	/** The post processors. */
	@Inject
	@ExtensionPoint(value = UploadPostProcessor.TARGET_NAME)
	private Iterable<UploadPostProcessor> postProcessors;

	/**
	 * Retrieve document types allowed to be upload in specified section.
	 * 
	 * @param instanceType
	 *            Document type - objectinstance or documentinstance.
	 * @param instanceId
	 *            Section id.
	 * @return JSON object as string containing an array of types .
	 */
	@GET
	@Path("/{instanceType}/{instanceId}")
	@Produces(MediaType.APPLICATION_JSON)
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String retrieveAllowedTypes(@PathParam("instanceType") String instanceType,
			@PathParam("instanceId") String instanceId) {

		if ((instanceType == null) || (instanceId == null)) {
			return null;
		}

		// load the section if needed
		Instance sectionInstance = fetchInstance(instanceId, instanceType);

		Map<String, CodeValue> codeValues = codelistService.getCodeValues(documetTypeCodelist);

		// collect all code values for available document definitions
		Map<String, CodeValue> filteredCodevalues = new HashMap<>();
		// the number of upload times for each document
		Map<String, Pair<? extends Identity, Integer>> allowedDocuments;

		// if section is present that is part of a case as parent we should check the possible
		// documents for the section
		if (isSectionInCase(sectionInstance)
				&& SectionInstance.class.getSimpleName().equalsIgnoreCase(instanceType)) {
			// get allowed documents for selected section
			Map<String, Pair<DocumentDefinitionRef, Integer>> documents = documentService
					.getAllowedDocuments((SectionInstance) sectionInstance);

			allowedDocuments = CollectionUtils.createLinkedHashMap(documents.size());
			// filter only default documents
			for (Entry<String, Pair<DocumentDefinitionRef, Integer>> entry : documents.entrySet()) {
				allowedDocuments.put(entry.getKey(), entry.getValue());
			}

			// filter the documents by codelist
			CollectionUtils.copyValuesIfExist(codeValues, filteredCodevalues,
					allowedDocuments.keySet());
		} else {
			// otherwise we should get all possible documents and return them
			List<DocumentDefinitionTemplate> allDocuments = dictionaryService
					.getAllDefinitions(DocumentDefinitionTemplate.class);

			allowedDocuments = CollectionUtils.createLinkedHashMap(allDocuments.size());

			// all documents from this list could be uploaded unlimited number of times to we do
			// that for all
			Integer count = Integer.valueOf(-1);
			for (DocumentDefinitionTemplate template : allDocuments) {

				// only return documents that a part of the codelist - same as above the 'else'
				if (CollectionUtils.copyValueIfExist(codeValues, filteredCodevalues,
						template.getIdentifier())) {
					allowedDocuments.put(template.getIdentifier(), new Pair<Identity, Integer>(
							template, count));
				}
			}
		}

		JSONArray result = new JSONArray();
		String language = getLanguage();
		String unlimited = labelProvider
				.getValue(LabelConstants.DOCUMENT_UPLOAD_NUMBER_OF_COPIES_UNLIMITED);

		List<CodeValue> list = new ArrayList<>(filteredCodevalues.values());
		// sort the values by the user locale
		String locale = getLanguage();
		Collections.sort(list, new PropertyModelComparator(true, locale));
		// copy sorted list to the map
		Map<String, CodeValue> map = CollectionUtils.createLinkedHashMap(list.size());
		for (CodeValue codeValue : list) {
			map.put(codeValue.getValue(), codeValue);
		}
		filteredCodevalues = map;

		// build result for the allowed documents
		for (Map.Entry<String, CodeValue> entry : filteredCodevalues.entrySet()) {
			Pair<? extends Identity, Integer> pair = allowedDocuments.get(entry.getKey());

			String numberOfCopies = Integer.toString(pair.getSecond());

			// unlimited number of documents can be uploaded
			if ("-1".equals(numberOfCopies)) {
				numberOfCopies = unlimited;
			}

			JSONObject value = new JSONObject();
			JsonUtil.addToJson(value, "value", entry.getKey());

			StringBuilder builder = new StringBuilder(100);
			builder.append(entry.getValue().getProperties().get(language)).append(" (")
					.append(numberOfCopies).append(")");

			JsonUtil.addToJson(value, "label", builder.toString());
			result.put(value);
		}

		return result.toString();
	}

	/**
	 * Retrieve document mandatory fields depending on definition.
	 * 
	 * @param type
	 *            Document type.
	 * @return JSON object as string containing an array of mandatory fields.
	 */
	@GET
	@Path("/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String retrieveMandatoryFields(@PathParam("type") String type) {

		JSONArray result = new JSONArray();
		DocumentDefinitionTemplate documentDefinition = dictionaryService.getDefinition(
				DocumentDefinitionTemplate.class, type);
		addProperties(result, documentDefinition);

		// return result.toString();
		return null;
	}

	/**
	 * Adds the properties.
	 * 
	 * @param result
	 *            the result
	 * @param regionDefinitionModel
	 *            the region definition model
	 */
	private void addProperties(JSONArray result, RegionDefinitionModel regionDefinitionModel) {
		addProperties(result, (DefinitionModel) regionDefinitionModel);
		for (RegionDefinition regionDefinition : regionDefinitionModel.getRegions()) {
			addProperties(result, regionDefinition);
		}
	}

	/**
	 * Adds the properties.
	 * 
	 * @param result
	 *            the result
	 * @param documentDefinition
	 *            the document definition
	 */
	private void addProperties(JSONArray result, DefinitionModel documentDefinition) {
		List<PropertyDefinition> properties = documentDefinition.getFields();

		for (PropertyDefinition property : properties) {
			if (property.isMandatory() && !DefaultProperties.TYPE.equals(property.getIdentifier())) {
				JSONObject value = new JSONObject();
				JsonUtil.addToJson(value, "fieldName", property.getIdentifier());
				JsonUtil.addToJson(value, "codelistNumber", property.getCodelist());
				JsonUtil.addToJson(value, "fieldType", property.getDataType().getName());
				JsonUtil.addToJson(value, "displayType", property.getDisplayType().toString());
				result.put(value);
			}
		}
	}

	/**
	 * Gets the current language for user or system defined.
	 * 
	 * @return the language
	 */
	private String getLanguage() {
		try {
			User user = authenticationService.get().getCurrentUser();
			if (user != null) {
				return user.getLanguage();
			}
		} catch (ContextNotActiveException e) {
			LOGGER.info("", e);
			User loggedUser = SecurityContextManager.getFullAuthentication();
			if (loggedUser != null) {
				return loggedUser.getLanguage();
			}
		}
		return defaultLanguage;
	}

	/**
	 * Consumes a file upload POST request. Call doUpload and return Id's of newly created
	 * (uploaded) documents.
	 * 
	 * @param request
	 *            http request containing posted data.
	 * @return the id's of uploaded files
	 */
	@POST
	@Path("/upload")
	@Consumes("multipart/form-data")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String upload(@Context HttpServletRequest request) {
		List<DocumentInstance> instances = doUpload(request);
		JSONArray result = new JSONArray();

		for (DocumentInstance instance : instances) {
			JSONObject value = new JSONObject();
			JsonUtil.addToJson(value, "dbId", instance.getId());
			JsonUtil.addToJson(value, "uuid", instance.getId().toString().substring(4));
			JsonUtil.addToJson(value, "success", true);
			JsonUtil.addToJson(value, "type", "documentinstance");
			JsonUtil.addToJson(value, DefaultProperties.HEADER_DEFAULT, instance.getProperties()
					.get(DefaultProperties.HEADER_DEFAULT));
			JsonUtil.addToJson(value, DefaultProperties.HEADER_COMPACT, instance.getProperties()
					.get(DefaultProperties.HEADER_COMPACT));
			result.put(value);
		}
		return result.toString();
	}

	/**
	 * Consumes a file upload POST request, creates a document within the specified context (parent)
	 * and attach it to a parent.
	 * 
	 * @param request
	 *            http request containing posted data.
	 * @return the list
	 */
	private List<DocumentInstance> doUpload(HttpServletRequest request) {
		File uploadFolder = null;
		try {
			EmfFileItemFactory itemFactory = fileItemFactory.get();
			uploadFolder = initializeUploadFolder(itemFactory);

			Map<String, Serializable> args = parseRequest(request, itemFactory);
			Instance targetInstance = getTargetInstance(args);

			if (targetInstance == null) {
				LOGGER.warn("Failed to load target instance for the upload of a document! "
						+ "The request is: {}", args);
				return Collections.emptyList();
			}

			// build document request for parallel upload to DMS
			List<DocumentInstance> documentsToUpload = buildInstancesForUpload(args,
					targetInstance, itemFactory);
			if (documentsToUpload.isEmpty()) {
				LOGGER.warn("NO documents to upload after processing the upload request!");
				return Collections.emptyList();
			}

			return uploadToDms(targetInstance, documentsToUpload);
		} finally {
			clearUploadFolder(uploadFolder);
		}
	}

	/**
	 * Initialize upload folder.
	 * 
	 * @param fileFactory
	 *            the file factory
	 * @return the file
	 */
	private File initializeUploadFolder(EmfFileItemFactory fileFactory) {
		File uploadFolder = fileFactory.getRepository();
		if (uploadFolder == null) {
			throw new EmfApplicationException(
					"No write permissions to upload files on the server: Cannot create file/folder on the file system");
		}
		RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.UPLOAD_SESSION_FOLDER,
				uploadFolder);
		return uploadFolder;
	}

	/**
	 * Clear upload folder.
	 * 
	 * @param uploadFolder
	 *            the upload folder
	 */
	private void clearUploadFolder(File uploadFolder) {
		// clear all information about the folder
		RuntimeConfiguration.disable(RuntimeConfigurationProperties.UPLOAD_SESSION_FOLDER);
		tempFileProvider.deleteFile(uploadFolder);
	}

	/**
	 * Parses the request.
	 * 
	 * @param request
	 *            the request
	 * @param itemFactory
	 *            the item factory
	 * @return the map
	 */
	private Map<String, Serializable> parseRequest(HttpServletRequest request,
			EmfFileItemFactory itemFactory) {
		ServletFileUpload servletUpload = new ServletFileUpload(itemFactory);

		List<FileItem> items = null;
		try {
			// perform upload from client to server
			items = servletUpload.parseRequest(request);
		} catch (FileUploadException e) {
			throw new EmfRuntimeException("Failed to parse upload request due to: ", e);
		}

		return parseRequestItems(items);
	}

	/**
	 * Parses the request items.
	 * 
	 * @param items
	 *            the items
	 * @return the map
	 */
	private Map<String, Serializable> parseRequestItems(List<FileItem> items) {
		Map<String, Serializable> map = CollectionUtils.createLinkedHashMap(items.size());
		int itemIndex = 0;
		for (FileItem item : items) {
			if (item.isFormField()) {
				if ("docPath".equals(item.getFieldName())
						&& StringUtils.isNotNullOrEmpty(item.getString())) {
					try {
						JSONObject jsonObj = new JSONObject(item.getString());
						JSONArray jsonArray = JsonUtil.getJsonArray(jsonObj, PATH);
						List<Pair<String, String>> path = new ArrayList<>(jsonArray.length());
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject object = jsonArray.getJSONObject(i);
							String instanceId = JsonUtil.getStringValue(object, "id");
							String instanceType = JsonUtil.getStringValue(object, TYPE);
							path.add(new Pair<>(instanceType, instanceId));
						}
						map.put(PATH, (Serializable) path);
					} catch (JSONException e) {
						LOGGER.warn("Failed to parse json for docPath: " + item.getString(), e);
					}
				} else {
					if (StringUtils.isNotNullOrEmpty(item.getString())) {
						map.put(item.getFieldName(), item.getString());
					}
				}
			} else {
				map.put(FILE + Integer.toString(itemIndex), item);
				itemIndex++;
			}
		}
		map.put(ITEMS_COUNT, Integer.valueOf(itemIndex));
		return map;
	}

	/**
	 * Builds the instances for upload.
	 * 
	 * @param args
	 *            the args
	 * @param targetInstance
	 *            the target instance
	 * @param itemFactory
	 *            the item factory
	 * @return the list
	 */
	private List<DocumentInstance> buildInstancesForUpload(Map<String, Serializable> args,
			Instance targetInstance, FileItemFactory itemFactory) {

		JSONArray fileNames = JsonUtil.createArrayFromString((String) args.get("fileName"));
		JSONArray types = JsonUtil.createArrayFromString((String) args.get("type"));
		JSONArray descriptions = JsonUtil.createArrayFromString((String) args.get("description"));
		JSONArray uuids = JsonUtil.createArrayFromString((String) args.get("uuid"));

		int itemsCount = Integer.parseInt(args.get(ITEMS_COUNT).toString());
		// note when uploading mail with attachments this list will not be enough
		List<DocumentInstance> documentsToUpload = new ArrayList<>(itemsCount);

		for (int i = 0; i < itemsCount; i++) {
			String fileIndex = FILE + Integer.toString(i);
			FileItem fileItem = (FileItem) args.get(fileIndex);

			// check max allowed file size server side and skip file if it's bigger
			if (types.isNull(i) || (fileItem.getSize() > maxSize)) {
				LOGGER.warn(
						"Recieved file with size {} bigger than the allowed {} or missing document type!",
						FileUtil.humanReadableByteCount(fileItem.getSize()),
						FileUtil.humanReadableByteCount(maxSize));
			} else {
				createAndUpdateDocumentInstance(targetInstance, uuids, fileNames, types,
						descriptions, fileItem, documentsToUpload, i, itemFactory);
			}
		}
		return documentsToUpload;
	}

	/**
	 * Creates the and update document instance.
	 * 
	 * @param targetInstance
	 *            the target instance
	 * @param uuids
	 *            the uuids
	 * @param fileNames
	 *            the file names
	 * @param types
	 *            the types
	 * @param descriptions
	 *            the descriptions
	 * @param fileItem
	 *            the file item
	 * @param documentsToUpload
	 *            the documents to upload
	 * @param index
	 *            the sequence index
	 * @param itemFactory
	 *            the item factory
	 */
	private void createAndUpdateDocumentInstance(Instance targetInstance, JSONArray uuids,
			JSONArray fileNames, JSONArray types, JSONArray descriptions, FileItem fileItem,
			List<DocumentInstance> documentsToUpload, int index, FileItemFactory itemFactory) {
		try {
			String documentType;
			String documentTitle = null;
			String documentDescription = null;
			if (!descriptions.isNull(index)) {
				documentDescription = convertEncoding(descriptions.getString(index));
			}
			if (!fileNames.isNull(index)) {
				documentTitle = convertEncoding(fileNames.getString(index));
			}
			documentType = types.getString(index);

			DocumentInstance uploadedDocument = createDocumentInstance(targetInstance, documentType);
			if (uploadedDocument == null) {
				return;
			}
			// override the DB id if send from web
			if (!uuids.isNull(index)) {
				SequenceEntityGenerator.unregister(uploadedDocument.getId());
				uploadedDocument.setId(buildDbIdFromUUID(uuids.getString(index)));
				SequenceEntityGenerator.registerId(uploadedDocument.getId());
			}

			uploadedDocument.setPurpose(null);
			uploadedDocument.getProperties().put(DocumentProperties.DESCRIPTION,
					documentDescription);
			uploadedDocument.getProperties().put(DocumentProperties.TYPE, documentType);
			uploadedDocument.getProperties().put(DocumentProperties.TITLE, documentTitle);

			// FIXME for upload new version

			updateDocumentModelOnUpload(fileItem, uploadedDocument, itemFactory);

			applyPostProcessing(documentsToUpload, uploadedDocument);
		} catch (Exception e) {
			LOGGER.error("Failed to create document instance due to", e);
		}
	}

	/**
	 * Builds the db id from uuid.
	 * 
	 * @param string
	 *            the string
	 * @return the serializable
	 */
	private Serializable buildDbIdFromUUID(String string) {
		if (StringUtils.isNullOrEmpty(string)) {
			// if invalid generate new ID
			return SequenceEntityGenerator.generateId();
		}
		if (string.contains(":")) {
			return string;
		}
		// the prefix could be from configuration or utility
		// there is such in semantic module
		return "emf:" + string;
	}

	/**
	 * Method to update the document properties before upload.
	 * 
	 * @param item
	 *            is the bean holding uploaded data
	 * @param documentInstance
	 *            is the currently uploaded instance
	 * @param itemFactory
	 *            the item factory
	 */
	private void updateDocumentModelOnUpload(FileItem item, DocumentInstance documentInstance,
			FileItemFactory itemFactory) {

		long lenght = item.getSize();
		String finalFileName = item.getName();
		Map<String, Serializable> properties = documentInstance.getProperties();

		// send the input stream directly to DMS, the content is already on the server in a file
		properties.put(DocumentProperties.FILE_LOCATOR, new FileItemDescriptor(finalFileName, item,
				itemFactory));

		// No empty title is allowed - CMF-6612
		if (org.apache.commons.lang.StringUtils.isBlank((String) properties
				.get(DocumentProperties.TITLE))) {
			properties.put(DocumentProperties.TITLE, finalFileName);
		}
		properties.put(DocumentProperties.FILE_SIZE, FileUtil.humanReadableByteCount(lenght));
		// remove any old thumbnail if document version is updated
		// will wait for the new thumbnail to be generated
		properties.remove(DocumentProperties.THUMBNAIL_IMAGE);
	}

	/**
	 * Apply post processing for document before upload.
	 * 
	 * @param documentsToUpload
	 *            the documents to upload
	 * @param uploadedDocument
	 *            the uploaded document
	 * @throws Exception
	 *             the exception
	 */
	private void applyPostProcessing(List<DocumentInstance> documentsToUpload,
			DocumentInstance uploadedDocument) throws Exception {
		for (UploadPostProcessor nextProcessor : postProcessors) {
			documentsToUpload.addAll(nextProcessor.proccess(uploadedDocument));
		}
	}

	/**
	 * Upload to DMS.
	 * 
	 * @param targetInstance
	 *            the target instance
	 * @param documentsToUpload
	 *            the documents to upload
	 * @return the list
	 */
	private List<DocumentInstance> uploadToDms(Instance targetInstance,
			List<DocumentInstance> documentsToUpload) {
		DocumentInstance[] instances = documentsToUpload
				.toArray(new DocumentInstance[documentsToUpload.size()]);

		// upload documents to DMS in parallel
		if (documentService.upload(targetInstance, true, instances)) {
			// on success attach all documents to the parent instance depending on the type
			// for documents there is a special requirement to add the uploaded sub documents to
			// the document's section
			if (targetInstance instanceof DocumentInstance) {
				SectionInstance sectionInstance = InstanceUtil.getParent(SectionInstance.class,
						targetInstance);
				if (sectionInstance != null) {
					instanceService.attach(sectionInstance, UPLOAD_OPERATION, instances);
				} else {
					LOGGER.warn("Trying to upload documents to other document that is not part of a section. The documents will be uploaded to the original document!");
				}
			}

			updateParentInstanceForUploadedDocuments(targetInstance);
		}
		return documentsToUpload;
	}

	/**
	 * Update parent instance for uploaded documents.
	 * 
	 * @param targetInstance
	 *            the target instance
	 */
	private void updateParentInstanceForUploadedDocuments(Instance targetInstance) {
		try {
			// no need to update the instance tree when only need to touch it
			RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			if (isSectionInCase(targetInstance) || (targetInstance instanceof DocumentInstance)) {
				CaseInstance caseInstance = InstanceUtil.getParent(CaseInstance.class,
						targetInstance);
				// if document instance is part of an object then we will not have a case
				if (caseInstance != null) {
					instanceService.save(caseInstance, UPLOAD_OPERATION);
				}
			}
		} finally {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		}
	}

	/**
	 * Creates the document instance.
	 * 
	 * @param targetInstance
	 *            the target instance
	 * @param documentType
	 *            the document type
	 * @return the document instance
	 */
	private DocumentInstance createDocumentInstance(Instance targetInstance, String documentType) {
		DocumentInstance uploadedDocument = null;
		if (isSectionInCase(targetInstance)) {
			// we have selected a section that is part of a case on root level so we could
			// create the target document as a part of the case/section
			SectionInstance sectionInstance = (SectionInstance) targetInstance;
			if (documentService.canUploadDocumentInSection(sectionInstance, documentType)) {
				uploadedDocument = documentService.createDocumentInstance(sectionInstance,
						documentType);
			}
			// if not allowed we could try to upload it as any random document
		}

		if (uploadedDocument == null) {
			DocumentDefinitionTemplate template = dictionaryService.getDefinition(
					DocumentDefinitionTemplate.class, documentType);
			if (template == null) {
				// we can't upload a document for a type that is not recognized by the
				// system
				LOGGER.warn("Can't upload a document for a type '" + documentType
						+ "' because is not recognized by the system");
				return null;
			}

			DocumentInstance createInstance = documentService.createInstance(
					new DocumentDefinitionRefProxy(template), targetInstance);
			if (createInstance == null) {
				// something is wrong and we couldn't create the instance
				LOGGER.warn("Failed to create document instance for document with type '"
						+ documentType + "'");
			}
			uploadedDocument = createInstance;
		}
		return uploadedDocument;
	}

	/**
	 * Convert encoding from ISO-8859-1 to UTF-8. The method will remove also any extra white spaces
	 * from the beginning ot the end.
	 * 
	 * @param string
	 *            the string to convert
	 * @return the string
	 */
	private String convertEncoding(String string) {
		if (string == null) {
			return null;
		}
		String documentDescription = null;
		try {
			byte[] strToBytes = string.getBytes("ISO-8859-1");
			documentDescription = new String(strToBytes, "UTF-8").trim();
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Invalid field encoding {}", string, e);
		}
		return documentDescription;
	}

	/**
	 * Gets the target instance.
	 * 
	 * @param args
	 *            the args
	 * @return the target instance
	 */
	@SuppressWarnings("unchecked")
	private Instance getTargetInstance(Map<String, Serializable> args) {
		Serializable id = args.get("id");
		Serializable type = args.get("instance");
		if ((type != null) && (id != null)) {
			return fetchInstance(id.toString(), type.toString());
		}
		Serializable serializable = args.get(PATH);
		if (serializable instanceof List) {
			List<Pair<String, String>> path = (List<Pair<String, String>>) serializable;
			if (!path.isEmpty()) {
				Pair<String, String> lastElement = path.get(path.size() - 1);
				return fetchInstance(lastElement.getSecond(), lastElement.getFirst());
			}
		}
		return null;
	}

	/**
	 * Checks if is section in case.
	 * 
	 * @param targetInstance
	 *            the target instance
	 * @return true, if is section in case
	 */
	private boolean isSectionInCase(Instance targetInstance) {
		return (targetInstance instanceof SectionInstance)
				&& (InstanceUtil.getDirectParent(targetInstance, true) instanceof CaseInstance);
	}

}