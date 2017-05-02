/*
 *
 */
package com.sirma.itt.cmf.alfresco4.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.remote.AlfrescoUploader;
import com.sirma.itt.cmf.alfresco4.remote.ContentUploadContext;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.UploadMode;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.seip.adapters.AdapterService;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.content.descriptor.LocalFileDescriptor;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * The {@link InitAlfresco4Controller} is initialization tool for alfresco.
 */
@ApplicationScoped
public class InitAlfresco4Controller implements AdapterService, AlfrescoCommunicationConstants {
	private static final long serialVersionUID = -16044292938437373L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String EMF_TYPE = "emf:type";
	private static final String DEFINITION_TYPE_GENERIC = "cmf:genericDefinition";
	@Inject
	private RESTClient restClient;
	@Inject
	private AlfrescoUploader alfrescoUploader;

	/** The site to instances qname. */
	private static Map<String, String> siteToInstancesQname = new HashMap<>(5);

	static {
		siteToInstancesQname.put("cmf", "cmf:caseinstancesspace");
		siteToInstancesQname.put("dom", "dom:objectinstancesspace");
	}

	/**
	 * Indicates a progress as number of sub operations completed
	 */
	@FunctionalInterface
	public interface ProgressMonitorBean {

		/**
		 * Sets the progress info.
		 *
		 * @param progreess
		 *            the new progress info
		 */
		void setProgressInfo(int progreess);
	}

	/**
	 * Supported modules in DMS
	 */
	public enum ModuleType {

		/** The cmf. */
		CMF,

		/** The dom. */
		DOM,

		/** The pm. */
		PM;
	}

	/**
	 * Supported model types that could be processed by the {@link InitAlfresco4Controller}
	 */
	public enum DefinitionType {

		/** The bpmn. */
		BPMN("bpmn", ModuleType.CMF, ModuleType.PM, ModuleType.DOM),

		/** The workflow. */
		WORKFLOW("workflow", ModuleType.CMF, ModuleType.PM, ModuleType.DOM),

		/** The task. */
		TASK("task", ModuleType.CMF, ModuleType.PM, ModuleType.DOM),

		/** The case. */
		CASE("case", ModuleType.CMF, ModuleType.PM, ModuleType.DOM),

		/** The document. */
		DOCUMENT("document", ModuleType.CMF, ModuleType.PM, ModuleType.DOM),

		/** The template. */
		TEMPLATE("template", ModuleType.CMF, ModuleType.PM, ModuleType.DOM),

		/** The generic. */
		GENERIC("generic", ModuleType.CMF, ModuleType.PM, ModuleType.DOM),

		/** The permission. */
		PERMISSION("permission", ModuleType.CMF, ModuleType.PM, ModuleType.DOM),

		/** The object. */
		OBJECT("object", ModuleType.DOM);

		/** The type. */
		private String type = null;

		/** The module types. */
		private ModuleType[] moduleTypes;

		/**
		 * Instantiates a new definition type.
		 *
		 * @param type
		 *            the type (the folder name)
		 * @param modules
		 *            the supported modules fot that type
		 */
		private DefinitionType(String type, ModuleType... modules) {
			this.type = type;
			moduleTypes = modules;
		}

		static DefinitionType type(String type) {
			DefinitionType[] values = values();
			for (DefinitionType definitionType : values) {
				if (type.equals(definitionType.type)) {
					return definitionType;
				}
			}
			return null;
		}

		String getType() {
			return type;
		}

		/**
		 * List of modules.
		 *
		 * @return the module type[]
		 */
		ModuleType[] modules() {
			return moduleTypes;
		}
	}

	/**
	 * The InitConfiguration <br>
	 * Supported values are:
	 * <ul>
	 * <li>storeLocation = string path to root directory containing definitions.</li>
	 * <li>pm.enabled = true/false</li>
	 * <li>dom.enabled = true/false</li>
	 * </ul>
	 */
	public static class InitConfiguration {
		private String definitionsLocation = "";
		private boolean failOnMissing = true;
		private String siteId = "seip";
		private boolean pmEnabled = true;
		private boolean domEnabled = true;
		private List<DefinitionType> definitionTypes = new LinkedList<>();
		private String adminUser;

		/**
		 * Getter method for definitionStorage.
		 *
		 * @return the definitionStorage
		 */
		public String getDefinitionsLocation() {
			return definitionsLocation;
		}

		/**
		 * Setter method for definitionStorage.
		 *
		 * @param definitionsLocation
		 *            the definitionsLocation to set
		 */
		public void setDefinitionsLocation(String definitionsLocation) {
			this.definitionsLocation = definitionsLocation;
		}

		/**
		 * Setter method for siteId.
		 *
		 * @param siteId
		 *            the siteId to set
		 */
		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}

		/**
		 * Gets the site id.
		 *
		 * @return the site id
		 */
		public String getSiteId() {
			return siteId;
		}

		/**
		 * Checks if is dom enabled.
		 *
		 * @return true, if is dom enabled
		 */
		public boolean isDomEnabled() {
			return domEnabled;
		}

		/**
		 * Setter method for domEnabled.
		 *
		 * @param domEnabled
		 *            the domEnabled to set
		 */
		public void setDomEnabled(boolean domEnabled) {
			this.domEnabled = domEnabled;
		}

		/**
		 * Checks if is pm enabled.
		 *
		 * @return true, if is pm enabled
		 */
		public boolean isPmEnabled() {
			return pmEnabled;
		}

		/**
		 * Setter method for pmEnabled.
		 *
		 * @param pmEnabled
		 *            the pmEnabled to set
		 */
		public void setPmEnabled(boolean pmEnabled) {
			this.pmEnabled = pmEnabled;
		}

		/**
		 * Adds the definition type.
		 *
		 * @param definitions
		 *            the definitions
		 */
		public void addDefinitionType(DefinitionType... definitions) {

			if (definitions == null) {
				return;
			}
			for (DefinitionType definitionType : definitions) {
				definitionTypes.add(definitionType);
			}
		}

		/**
		 * Gets the definition types.
		 *
		 * @return the definition types
		 */
		public List<DefinitionType> getDefinitionTypes() {
			return definitionTypes;
		}

		/**
		 * Set the admin user
		 *
		 * @param adminUser
		 */
		public void setAdminUser(String adminUser) {
			this.adminUser = adminUser;
		}

		/**
		 * Get the admin user
		 *
		 * @return the admin user
		 */
		public String getAdminUser() {
			return adminUser;
		}

		/**
		 * Setter method for fail on missing definitions but should be uploaded.
		 *
		 * @param failOnMissing
		 *            the failOnMissing to set
		 */
		public void setFailOnMissing(boolean failOnMissing) {
			this.failOnMissing = failOnMissing;
		}

		/**
		 * Check if {@link #failOnMissing} is set
		 *
		 * @return true if operation should fail on missing data
		 */
		public boolean isFailOnMissing() {
			return failOnMissing;
		}
	}

	/**
	 * Initialize the alfresco storage and optionally upload the selected definition types.
	 *
	 * @param configuration
	 *            the configuration properties.
	 * @param taskThread
	 *            the task thread
	 * @throws Exception
	 *             the exception
	 */
	public void initialize(InitConfiguration configuration, ProgressMonitorBean taskThread) throws Exception { // NOSONAR
		LOGGER.info("Begin Init");
		/** The processed. */
		int processed = 0;
		File baseDir = configuration.getDefinitionsLocation() != null ? new File(configuration.getDefinitionsLocation())
				: new File("");
		// Handles the case where all definition directories are packed into a single parent directory.
		if (baseDir.listFiles().length == 1) {
			baseDir = baseDir.listFiles()[0];
		}
		String siteId = configuration.getSiteId();
		JSONObject request = new JSONObject();
		request.put("sites", siteId);
		JSONObject properties = new JSONObject();
		// def config
		JSONObject def = new JSONObject();
		properties.put(KEY_DMS_NAME, "Case Definitions");
		properties.put(KEY_DMS_TITLE, "Stored Case Definitions");
		properties.put(KEY_DMS_DESCRIPTION, "Stored Case Definitions");
		def.put(KEY_PROPERTIES, properties);
		def.put("lock", Boolean.TRUE);

		request.put("case", def);

		JSONObject instances = new JSONObject();
		properties = new JSONObject();
		properties.put(KEY_DMS_NAME, "Case Instances");
		properties.put(KEY_DMS_TITLE, "Stored CMF Cases");
		properties.put(KEY_DMS_DESCRIPTION, "Stored CMF Cases");
		instances.put(KEY_PROPERTIES, properties);
		instances.put("lock", Boolean.TRUE);
		request.put("instance", instances);

		JSONObject document = new JSONObject();
		properties = new JSONObject();
		properties.put(KEY_DMS_NAME, "Document Definitions");
		properties.put(KEY_DMS_TITLE, "Stored Document Definitions");
		properties.put(KEY_DMS_DESCRIPTION, "Stored Document Definitions");
		document.put(KEY_PROPERTIES, properties);
		document.put("lock", Boolean.TRUE);
		request.put("document", document);

		JSONObject workflow = new JSONObject();
		properties = new JSONObject();
		properties.put(KEY_DMS_NAME, "Workflow Definitions");
		properties.put(KEY_DMS_TITLE, "Stored Workfklow Definitions");
		properties.put(KEY_DMS_DESCRIPTION, "Stored Workfklow Definitions");
		workflow.put(KEY_PROPERTIES, properties);
		workflow.put("lock", Boolean.TRUE);
		request.put("workflow", workflow);

		JSONObject task = new JSONObject();
		properties = new JSONObject();
		properties.put(KEY_DMS_NAME, "Tasks Definitions");
		properties.put(KEY_DMS_TITLE, "Stored Task Definitions");
		properties.put(KEY_DMS_DESCRIPTION, "Stored Task Definitions");
		task.put(KEY_PROPERTIES, properties);
		task.put("lock", Boolean.TRUE);
		request.put("task", task);

		JSONObject idoctemplate = new JSONObject();
		properties = new JSONObject();
		properties.put(KEY_DMS_NAME, "Template Definitions");
		properties.put(KEY_DMS_TITLE, "Stored Template Definitions");
		properties.put(KEY_DMS_DESCRIPTION, "Stored Template Definitions");
		idoctemplate.put(KEY_PROPERTIES, properties);
		idoctemplate.put("lock", Boolean.TRUE);
		request.put("template", idoctemplate);

		JSONObject generic = new JSONObject();
		properties = new JSONObject();
		properties.put(KEY_DMS_NAME, "Generic Definitions");
		properties.put(KEY_DMS_TITLE, "Stored Generic Definitions");
		properties.put(KEY_DMS_DESCRIPTION, "Stored Generic Definitions");
		generic.put(KEY_PROPERTIES, properties);
		generic.put("lock", Boolean.TRUE);
		request.put("generic", generic);

		JSONObject permission = new JSONObject();
		properties = new JSONObject();
		properties.put(KEY_DMS_NAME, "Permission Definitions");
		properties.put(KEY_DMS_TITLE, "Stored Permission Definitions");
		properties.put(KEY_DMS_DESCRIPTION, "Stored Permission Definitions");
		permission.put(KEY_PROPERTIES, properties);
		permission.put("lock", Boolean.TRUE);
		request.put("permission", permission);

		boolean domEnabled = configuration.isDomEnabled();
		String initURI;
		// either cmf only or dom
		if (domEnabled) {
			JSONObject objects = new JSONObject();
			properties = new JSONObject();
			properties.put(KEY_DMS_NAME, "Objects Instances");
			properties.put(KEY_DMS_TITLE, "Stored Domain Objects");
			properties.put(KEY_DMS_DESCRIPTION, "Stored Domain Objects");
			objects.put(KEY_PROPERTIES, properties);
			objects.put("lock", Boolean.TRUE);
			request.put("objectinstance", objects);

			JSONObject objectsDef = new JSONObject();
			properties = new JSONObject();
			properties.put(KEY_DMS_NAME, "Object Definitions");
			properties.put(KEY_DMS_TITLE, "Stored Object Definitions");
			properties.put(KEY_DMS_DESCRIPTION, "Stored Object Definitions");
			objectsDef.put(KEY_PROPERTIES, properties);
			objectsDef.put("lock", Boolean.TRUE);
			request.put("objects", objectsDef);
			initURI = "/dom/init";
		} else {
			initURI = "/cmf/init";
		}

		HttpMethod createdMethod = restClient.createMethod(new PostMethod(), request.toString(), true);
		String callWebScript = restClient.request(initURI, createdMethod);
		// init step is processed
		taskThread.setProgressInfo(++processed);
		LOGGER.info("Finish structure init " + callWebScript);
		LOGGER.info("Begin definitions init!");
		// upload the definitions after init
		float estimatedLoad = estimateLoad(baseDir, configuration.getDefinitionTypes());
		float[] counters = new float[] { processed, estimatedLoad };
		for (DefinitionType type : configuration.getDefinitionTypes()) {
			scanAndUploadByType(counters, new File(baseDir, type.getType()), siteId, configuration.isFailOnMissing(),
					taskThread);
		}
		LOGGER.info("Finish definitions init!");
	}

	private void scanAndUploadByType(float[] counters, File dir, String siteId, boolean failOnMissing,
			ProgressMonitorBean taskThread) throws FileNotFoundException {
		DefinitionType defType = DefinitionType.type(dir.getName());
		if (defType == null || !dir.isDirectory()) {
			if (failOnMissing) {
				throw new EmfRuntimeException("Missing type or directory: " + dir);
			}
			return;
		}
		File[] list = dir.listFiles();
		if (list == null) {
			throw new EmfRuntimeException("Error during directory scan: " + dir);
		}
		LOGGER.info("For {} files: {}", defType, Arrays.toString(list));
		List<File> files = Stream
				.of(list)
					.filter(InitAlfresco4Controller::isFileValid)
					.flatMap(InitAlfresco4Controller::expandDir)
					.collect(Collectors.toCollection(LinkedList::new));

		Set<String> distinctNames = files.stream().map(File::getName).collect(Collectors.toCollection(HashSet::new));

		files.stream().filter(file -> distinctNames.contains(file.getName())).forEach(current -> {
			try {
				uploadDefinition(defType, current, siteId);
				counters[0]++;
				float progreess = Math.min(1, counters[0] / counters[1]);
				taskThread.setProgressInfo((int) (progreess * 100));
			} catch (Exception e) {
				LOGGER.warn("Could not upload file {}", current, e);
			}
		});
	}

	private static Stream<File> expandDir(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			if (children != null) {
				return Stream.of(children);
			}
			return Stream.empty();
		}
		return Stream.of(dir);
	}

	private static boolean isFileValid(File currentFile) {
		if (!currentFile.exists()) {
			LOGGER.error(currentFile + " is not valid entry! Skipping it!");
			return false;
		}
		return true;
	}

	private void uploadDefinition(DefinitionType definitionType, File definitionFile, String siteId) // NOSONAR
			throws FileNotFoundException {
		switch (definitionType) {
			case WORKFLOW:
				uploadWorkflowDefinitions(definitionFile, siteId);
				break;
			case TASK:
				uploadTaskDefinitions(definitionFile, siteId);
				break;
			case CASE:
				uploadCaseDefinitions(definitionFile, siteId);
				break;
			case DOCUMENT:
				uploadDocumentDefinitions(definitionFile, siteId);
				break;
			case TEMPLATE:
				uploadTemplateDefinitions(definitionFile, siteId);
				break;
			case GENERIC:
				uploadGenericDefinitions(definitionFile, siteId);
				break;
			case PERMISSION:
				uploadPermissionDefinitions(definitionFile, siteId);
				break;
			case OBJECT:
				uploadObjectDefinitions(definitionFile, siteId);
				break;
			case BPMN:
				LOGGER.info(uploadBpmnAndDeploy(definitionFile, "/app:company_home/app:dictionary/app:workflow_defs",
						false));
				break;
			default:
				break;
		}
	}

	private void uploadWorkflowDefinitions(File file, String siteId) throws FileNotFoundException {
		uploadDefinition(file, siteId, "Workflow Definitions", DEFINITION_TYPE_GENERIC, null);
	}

	private void uploadTaskDefinitions(File file, String siteId) throws FileNotFoundException {
		uploadDefinition(file, siteId, "Tasks Definitions", DEFINITION_TYPE_GENERIC, null);
	}

	private void uploadCaseDefinitions(File file, String siteId) throws FileNotFoundException {
		uploadDefinition(file, siteId, "Case Definitions", DEFINITION_TYPE_GENERIC, null);
	}

	private void uploadPermissionDefinitions(File file, String siteId) throws FileNotFoundException {
		Map<String, Serializable> props = CollectionUtils.createHashMap(2);
		String templateType = file.getName();
		props.put(KEY_DMS_TITLE, "Definition (" + templateType + ")");
		props.put(EMF_TYPE, templateType);
		uploadDefinition(file, siteId, "Permission Definitions", "cmf:permissionDefinition", props);
	}

	private void uploadGenericDefinitions(File file, String siteId) throws FileNotFoundException {
		Map<String, Serializable> props = CollectionUtils.createHashMap(2);
		String templateType = file.getName();
		props.put(KEY_DMS_TITLE, "Definition (" + templateType + ")");
		props.put(EMF_TYPE, templateType);
		uploadDefinition(file, siteId, "Generic Definitions", DEFINITION_TYPE_GENERIC, props);

	}

	private void uploadTemplateDefinitions(File file, String siteId) throws FileNotFoundException {
		Map<String, Serializable> props = CollectionUtils.createHashMap(3);
		if (file.getName().contains("primary")) {
			props.put("cmf:primarytemplate", Boolean.TRUE);
		} else {
			props.put("cmf:primarytemplate", Boolean.FALSE);
		}
		String templateType = file.getName();
		props.put(KEY_DMS_TITLE, "Template (" + templateType + ")");
		props.put(EMF_TYPE, templateType);

		uploadDefinition(file, siteId, "Template Definitions", "cmf:templateDefinition", props);
	}

	private void uploadDocumentDefinitions(File file, String siteId) throws FileNotFoundException {
		uploadDefinition(file, siteId, "Document Definitions", DEFINITION_TYPE_GENERIC, null);
	}

	private void uploadObjectDefinitions(File file, String siteId) throws FileNotFoundException {
		uploadDefinition(file, siteId, "Object Definitions", DEFINITION_TYPE_GENERIC, null);
	}

	private String uploadBpmnAndDeploy(File definitionFile, String basePath, boolean deploy) {
		File file = definitionFile;
		Set<String> aspectsProp = CollectionUtils.createHashSet(2);
		Map<String, Serializable> props = CollectionUtils.createHashMap(1);
		String uploadFile = uploadFile(alfrescoUploader, file.getAbsolutePath(), null, basePath, KEY_DMS_CONTENT, props,
				aspectsProp);
		if (uploadFile == null) {
			return "Not Deployed: " + definitionFile.getName();
		}
		try {
			String nodeId = new JSONObject(uploadFile).getString(KEY_NODEREF);
			if (!deploy) {
				return "Uploaded: " + definitionFile.getName() + " as " + nodeId;
			}
			HttpMethod deployRequest = restClient.createMethod(new GetMethod(), "", true);
			String result = restClient.request("workflow/deploy?node=" + nodeId + "&redeployIfExists=" + Boolean.TRUE,
					deployRequest);
			if (result != null) {
				JSONObject wf = new JSONObject(result);
				if (wf.getString(KEY_ID).length() > 0) {
					return "Deployed: " + definitionFile.getName() + " as " + wf.getString("id");
				}
			}
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
		return "Not Deployed: " + definitionFile.getName();
	}

	private void uploadDefinition(File file, String siteId, String folder, String aspect,
			Map<String, Serializable> additionalProperties) {
		Set<String> aspectsProp = new HashSet<>();
		aspectsProp.add(aspect);
		Map<String, Serializable> props = new HashMap<>();
		props.put(KEY_DMS_TITLE, folder);
		if (additionalProperties != null) {
			props.putAll(additionalProperties);
		}
		uploadFile(alfrescoUploader, file.getAbsolutePath(), siteId, folder, KEY_DMS_CONTENT, props, aspectsProp);
	}

	/**
	 * Actual work on upload. See
	 * {@link AlfrescoUploader#uploadFile(String, com.sirma.itt.emf.adapter.FileDescriptor, String, String, String, String, Map, Set, String, String)}
	 */
	private static String uploadFile(AlfrescoUploader alfrescoUploader, String absolutePath, String siteId,
			String folder, String contentType, Map<String, Serializable> props, Set<String> aspectsProp) {
		File file = new File(absolutePath);
		String uploadFile;
		if (file.exists()) {
			try {
				ContentUploadContext uploadContext = ContentUploadContext
						.create(ServiceURIRegistry.UPLOAD_SERVICE_URI, UploadMode.DIRECT)
							.setFilePart(alfrescoUploader.getPartSource(new LocalFileDescriptor(file)))
							.setSiteId(siteId)
							.setFolder(folder)
							.setContentType(contentType)
							.setProperties(props)
							.setAspectProperties(aspectsProp)
							.setOverwrite(Boolean.TRUE)
							.setMajorVersion(Boolean.FALSE)
							.setVersionDescription("Uplodaded definition")
							.setThumbnailMode(ThumbnailGenerationMode.NONE.toString());
				uploadFile = alfrescoUploader.uploadFile(uploadContext);
			} catch (DMSClientException e) {
				LOGGER.info("Upload failure " + e.getMessage(), e);
				return null;
			}
			LOGGER.info("Upload result " + uploadFile);
			return uploadFile;
		}
		return file.getAbsolutePath();
	}

	/**
	 * Estimate the total work.
	 *
	 * @param baseDir
	 *            is the root store dir
	 * @param definitionTypes
	 *            the definition types
	 * @return the total estimated work count
	 */
	private static int estimateLoad(File baseDir, List<DefinitionType> definitionTypes) {
		int total = 1;
		for (DefinitionType type : definitionTypes) {
			total += scanDirectorySize(new File(baseDir, type.getType()));
		}
		return total;
	}

	/**
	 * Scan directory size.
	 *
	 * @param dir
	 *            the definition dir
	 * @return the directory size (file count)
	 */
	private static int scanDirectorySize(File dir) {
		if (!dir.isDirectory()) {
			throw new EmfRuntimeException("Missing dir: " + dir.getAbsolutePath());
		}
		File[] listFiles = dir.listFiles();
		return listFiles != null ? listFiles.length : 0;

	}

}
