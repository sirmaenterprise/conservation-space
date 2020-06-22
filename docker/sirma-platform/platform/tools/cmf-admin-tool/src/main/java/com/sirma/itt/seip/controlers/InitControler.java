/*
 *
 */
package com.sirma.itt.seip.controlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.seip.PropertyConfigsWrapper;
import com.sirma.itt.seip.alfresco4.ServiceURIRegistry;
import com.sirma.itt.seip.alfresco4.remote.AbstractRESTClient;
import com.sirma.itt.seip.alfresco4.remote.AlfrescoUploader;

/**
 * Init of cmf structures in dms by invoking the corresponding scripts.
 *
 * @author borislav banchev
 */
public class InitControler {

	/** The Constant LOGGER. */
	protected static final Logger LOGGER = Logger.getLogger(InitControler.class);

	/** The http client. */
	private AbstractRESTClient httpClient;

	/** The estimate load. */
	private int estimateLoad;

	/**
	 * Test init.
	 *
	 * @param siteId
	 *            the site id
	 * @param definitionTypes
	 *            current definitions types
	 * @param taskThread
	 *            the task thread
	 * @throws Exception
	 *             the exception
	 */
	public void init(String siteId, List<DefinitionType> definitionTypes, ProgressMonitor taskThread) throws Exception {
		LOGGER.info("Begin Init");

		JSONObject request = new JSONObject();
		request.put("sites", siteId);
		JSONObject properties = new JSONObject();
		// def config
		JSONObject def = new JSONObject();
		properties.put("cm:name", "Case Definitions");
		properties.put("cm:title", "Stored Case Definitions");
		properties.put("cm:description", "Stored Case Definitions");
		def.put("properties", properties);
		def.put("lock", Boolean.TRUE);

		request.put("case", def);

		JSONObject instances = new JSONObject();
		properties = new JSONObject();
		properties.put("cm:name", "Case Instances");
		properties.put("cm:title", "Stored CMF Cases");
		properties.put("cm:description", "Stored CMF Cases");
		instances.put("properties", properties);
		instances.put("lock", Boolean.TRUE);
		request.put("instance", instances);

		JSONObject document = new JSONObject();
		properties = new JSONObject();
		properties.put("cm:name", "Document Definitions");
		properties.put("cm:title", "Stored Document Definitions");
		properties.put("cm:description", "Stored Document Definitions");
		document.put("properties", properties);
		document.put("lock", Boolean.TRUE);
		request.put("document", document);

		JSONObject workflow = new JSONObject();
		properties = new JSONObject();
		properties.put("cm:name", "Workflow Definitions");
		properties.put("cm:title", "Stored Workfklow Definitions");
		properties.put("cm:description", "Stored Workfklow Definitions");
		workflow.put("properties", properties);
		workflow.put("lock", Boolean.TRUE);
		request.put("workflow", workflow);

		JSONObject task = new JSONObject();
		properties = new JSONObject();
		properties.put("cm:name", "Tasks Definitions");
		properties.put("cm:title", "Stored Task Definitions");
		properties.put("cm:description", "Stored Task Definitions");
		task.put("properties", properties);
		task.put("lock", Boolean.TRUE);
		request.put("task", task);

		JSONObject idoctemplate = new JSONObject();
		properties = new JSONObject();
		properties.put("cm:name", "Template Definitions");
		properties.put("cm:title", "Stored Template Definitions");
		properties.put("cm:description", "Stored Template Definitions");
		idoctemplate.put("properties", properties);
		idoctemplate.put("lock", Boolean.TRUE);
		request.put("template", idoctemplate);

		JSONObject generic = new JSONObject();
		properties = new JSONObject();
		properties.put("cm:name", "Generic Definitions");
		properties.put("cm:title", "Stored Generic Definitions");
		properties.put("cm:description", "Stored Generic Definitions");
		generic.put("properties", properties);
		generic.put("lock", Boolean.TRUE);
		request.put("generic", generic);

		JSONObject permission = new JSONObject();
		properties = new JSONObject();
		properties.put("cm:name", "Permission Definitions");
		properties.put("cm:title", "Stored Permission Definitions");
		properties.put("cm:description", "Stored Permission Definitions");
		permission.put("properties", properties);
		permission.put("lock", Boolean.TRUE);
		request.put("permission", permission);

		PropertyConfigsWrapper configsWrapper = PropertyConfigsWrapper.getInstance();
		boolean domEnabled = Boolean.TRUE.equals(Boolean.valueOf(configsWrapper.getProperty("dom.enabled")));
		String initURI = null;
		// either cmf only or dom
		boolean firstPassEnabled = false;
		if (domEnabled) {
			JSONObject objects = new JSONObject();
			properties = new JSONObject();
			properties.put("cm:name", "Objects Instances");
			properties.put("cm:title", "Stored Domain Objects");
			properties.put("cm:description", "Stored Domain Objects");
			objects.put("properties", properties);
			objects.put("lock", Boolean.TRUE);
			request.put("objectinstance", objects);

			JSONObject objectsDef = new JSONObject();
			properties = new JSONObject();
			properties.put("cm:name", "Object Definitions");
			properties.put("cm:title", "Stored Object Definitions");
			properties.put("cm:description", "Stored Object Definitions");
			objectsDef.put("properties", properties);
			objectsDef.put("lock", Boolean.TRUE);
			request.put("objects", objectsDef);
			initURI = "/dom/init";
			firstPassEnabled = true;
		} else {
			initURI = "/cmf/init";
		}

		boolean pmEnabled = Boolean.TRUE.equals(Boolean.valueOf(configsWrapper.getProperty("pm.enabled")));
		if (pmEnabled) {
			JSONObject projects = new JSONObject();
			properties = new JSONObject();
			properties.put("cm:name", "Project Instances");
			properties.put("cm:title", "Stored PM Projects");
			properties.put("cm:description", "Stored PM Projects");
			projects.put("properties", properties);
			projects.put("lock", Boolean.TRUE);
			request.put("projectinstance", projects);

			JSONObject projectDef = new JSONObject();
			properties = new JSONObject();
			properties.put("cm:name", "Project Definitions");
			properties.put("cm:title", "Stored Project Definitions");
			properties.put("cm:description", "Stored Project Definitions");
			projectDef.put("properties", properties);
			projectDef.put("lock", Boolean.TRUE);
			request.put("project", projectDef);
			if (firstPassEnabled == true) {
				HttpMethod createMethod = createMethod(request.toString(), new PostMethod());
				String callWebScript = httpClient.invokeWithResponse("/pm/init", createMethod);
				LOGGER.info("Finish first pass init " + callWebScript);
			}
		}
		HttpMethod createMethod = createMethod(request.toString(), new PostMethod());
		String callWebScript = httpClient.invokeWithResponse(initURI, createMethod);
		// init step is processed
		taskThread.setProgressInfo(++processed);
		LOGGER.info("Finish structure init " + callWebScript);
		assertNotNull(callWebScript, "Result should be json object");
		LOGGER.info("Begin definitions init!");
		// upload the definitions after init
		AlfrescoUploader alfrescoUploader = new AlfrescoUploader();
		alfrescoUploader.setRestClient(httpClient);
		estimateLoad = estimateLoad(definitionTypes);
		for (DefinitionType type : definitionTypes) {
			scanAndUploadByType(alfrescoUploader, type.type, siteId, taskThread);
		}
		LOGGER.info("Finish definitions init!");
	}

	/**
	 * The Enum ModuleType.
	 */
	public enum ModuleType {

		/** The cmf. */
		CMF, /** The dom. */
		DOM, /** The pm. */
		PM;
	}

	/**
	 * The Enum DefinitionType.
	 */
	public enum DefinitionType implements Comparable<DefinitionType> {

		/** The bpmn definitions. */
		BPMN("bpmn", ModuleType.CMF, ModuleType.PM, ModuleType.DOM), /** The workflow. */
		WORKFLOW("workflow", ModuleType.CMF, ModuleType.PM, ModuleType.DOM), /** The task. */
		TASK("task", ModuleType.CMF, ModuleType.PM, ModuleType.DOM), /** The case. */
		CASE("case", ModuleType.CMF, ModuleType.PM, ModuleType.DOM), /** The document. */
		DOCUMENT("document", ModuleType.CMF, ModuleType.PM, ModuleType.DOM), /** The idoctemplate. */
		TEMPLATE("template", ModuleType.CMF, ModuleType.PM, ModuleType.DOM), /** The idoctemplate. */
		GENERIC("generic", ModuleType.CMF, ModuleType.PM, ModuleType.DOM), /** The permission. */
		PERMISSION("permission", ModuleType.CMF, ModuleType.PM, ModuleType.DOM), /** The object. */
		OBJECT("object", ModuleType.DOM), /** The project. */
		PROJECT("project", ModuleType.PM);

		/** The type. */
		private String type = null;

		/** The module types. */
		private ModuleType[] moduleTypes;

		/**
		 * Instantiates a new definition type.
		 *
		 * @param type
		 *            the type
		 * @param forModule
		 *            the for module
		 */
		private DefinitionType(String type, ModuleType... forModule) {
			this.type = type;
			moduleTypes = forModule;
		}

		/**
		 * Type.
		 *
		 * @param type
		 *            the type
		 * @return the definition type
		 */
		public static DefinitionType type(String type) {
			DefinitionType[] values = values();
			for (DefinitionType definitionType : values) {
				if (type.equals(definitionType.type)) {
					return definitionType;
				}
			}
			return null;
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * Gets the for module.
		 *
		 * @param moduleType
		 *            the module type
		 * @return the for module
		 */
		public static Set<DefinitionType> getForModule(ModuleType moduleType) {
			Set<DefinitionType> values = new TreeSet<>();
			for (DefinitionType definitionType : values()) {
				if (definitionType.moduleTypes != null) {
					for (ModuleType module : definitionType.moduleTypes) {
						if (module == moduleType) {
							values.add(definitionType);
						}
					}
				}
			}
			return values;
		}
	}

	/**
	 * Scan by type.
	 *
	 * @param definitionDir
	 *            the definition dir
	 * @return the int
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private int scanByType(String definitionDir) throws FileNotFoundException {
		File dir = new File(definitionDir);
		if (!dir.canRead()) {
			throw new RuntimeException("Missing dir: " + dir.getAbsolutePath());
		}
		return dir.listFiles().length;

	}

	/** The processed. */
	int processed = 0;

	/**
	 * Scan and upload by type.
	 *
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param definitionDir
	 *            the definition dir
	 * @param siteId
	 *            the site id
	 * @param taskThread
	 *            the task thread
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private void scanAndUploadByType(AlfrescoUploader alfrescoUploader, String definitionDir, String siteId,
			ProgressMonitor taskThread) throws FileNotFoundException {
		File dir = new File(definitionDir);
		DefinitionType defType = DefinitionType.type(definitionDir);
		if (defType == null || !dir.canRead()) {
			throw new RuntimeException("Missing type or directory: " + definitionDir);
		}
		File[] list = dir.listFiles();
		LOGGER.info("For " + defType.toString() + " files: " + Arrays.toString(list));

		Queue<File> dirsQueue = new LinkedList<>(Arrays.asList(list));

		while (dirsQueue.size() > 0) {
			File currentFile = dirsQueue.remove();
			if (!currentFile.exists()) {
				LOGGER.error(currentFile + " is not valid entry! Skipping it!");
				continue;
			}
			if (doesFileExists(dirsQueue, currentFile)) {
				LOGGER.error(currentFile + " already exists! Skipping it!");
				continue;
			}

			if (currentFile.isDirectory()) {
				File[] children = currentFile.listFiles();
				if (children != null) {
					dirsQueue.addAll(Arrays.asList(children));
				}
			} else if (currentFile.isFile()) {
				uploadDefinition(defType, currentFile, alfrescoUploader, siteId);
				processed++;
				taskThread.setProgressInfo((int) ((double) processed / estimateLoad * 100));
			}
		}
	}

	/**
	 * Checks if given file exists in given collection.
	 *
	 * @param fileList
	 *            - the collection in which to check if the given file exists
	 * @param file
	 *            - the file who to check if it exists in the collection
	 * @return - true if the given file exists in the given collection, otherwise false
	 */
	private static boolean doesFileExists(Collection<File> fileList, File file) {
		for (File currentfile : fileList) {
			if (currentfile.getName().equals(file.getName())) {
				return true;
			}
		}
		return false;
	}

	private void uploadDefinition(DefinitionType definitionType, File definitionFile, AlfrescoUploader alfrescoUploader,
			String siteId) throws FileNotFoundException {
		switch (definitionType) {
			case WORKFLOW:
				uploadWorkflowDef(definitionFile, alfrescoUploader, siteId);
				break;
			case TASK:
				uploadTaskDef(definitionFile, alfrescoUploader, siteId);
				break;
			case CASE:
				uploadCaseDef(definitionFile, alfrescoUploader, siteId);
				break;
			case DOCUMENT:
				uploadDocDef(definitionFile, alfrescoUploader, siteId);
				break;
			case TEMPLATE:
				uploadTemplateDef(definitionFile, alfrescoUploader, siteId);
				break;
			case GENERIC:
				uploadGenericDef(definitionFile, alfrescoUploader, siteId);
				break;
			case PERMISSION:
				uploadPermissionDef(definitionFile, alfrescoUploader, siteId);
				break;
			case OBJECT:
				uploadObjectDef(definitionFile, alfrescoUploader, siteId);
				break;
			case PROJECT:
				uploadProjectDef(definitionFile, alfrescoUploader, siteId);
				break;
			case BPMN:
				LOGGER.info(uploadBpmnAndDeploy(definitionFile, alfrescoUploader,
						"/app:company_home/app:dictionary/app:workflow_defs"));
			default:
				break;
		}
	}

	/**
	 * Upload project def.
	 *
	 * @param definitionFile
	 *            the definition file
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param siteId
	 *            the site id to use
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private void uploadProjectDef(File definitionFile, AlfrescoUploader alfrescoUploader, String siteId)
			throws FileNotFoundException {
		File file = definitionFile;
		if (file.exists()) {
			HashSet<String> aspectsProp = new HashSet<>();
			HashMap<String, Serializable> props = new HashMap<>();
			aspectsProp.add("pm:projectDefinition");
			props.put("cm:title", "Project definition");
			uploadFile(alfrescoUploader, file.getAbsolutePath(), siteId, "Project Definitions", "cm:content", props,
					aspectsProp, Boolean.TRUE, "New Project defintion");
		}

	}

	/**
	 * Upload a generic definition.
	 *
	 * @param nextDefinition
	 *            the generic definition to upload
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param siteId
	 *            the site id
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private void uploadPermissionDef(File nextDefinition, AlfrescoUploader alfrescoUploader, String siteId)
			throws FileNotFoundException {
		HashSet<String> aspectsProp = new HashSet<>();
		HashMap<String, Serializable> props = new HashMap<>();
		aspectsProp.add("cmf:permissionDefinition");

		String templateType = nextDefinition.getName();
		props.put("cm:title", "Definition (" + templateType + ")");
		props.put("emf:type", templateType);
		uploadFile(alfrescoUploader, nextDefinition.getAbsolutePath(), siteId, "Permission Definitions", "cm:content",
				props, aspectsProp, Boolean.TRUE, "New definition");

	}

	/**
	 * Upload a generic definition.
	 *
	 * @param nextDefinition
	 *            the generic definition to upload
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param siteId
	 *            the site id
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private void uploadGenericDef(File nextDefinition, AlfrescoUploader alfrescoUploader, String siteId)
			throws FileNotFoundException {
		HashSet<String> aspectsProp = new HashSet<>();
		HashMap<String, Serializable> props = new HashMap<>();
		aspectsProp.add("cmf:genericDefinition");

		String templateType = nextDefinition.getName();
		props.put("cm:title", "Definition (" + templateType + ")");
		props.put("emf:type", templateType);
		uploadFile(alfrescoUploader, nextDefinition.getAbsolutePath(), siteId, "Generic Definitions", "cm:content",
				props, aspectsProp, Boolean.TRUE, "New definition");

	}

	/**
	 * Upload a template to dms.
	 *
	 * @param nextTemplate
	 *            the definitions template
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param siteId
	 *            the site id
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private void uploadTemplateDef(File nextTemplate, AlfrescoUploader alfrescoUploader, String siteId)
			throws FileNotFoundException {
		HashSet<String> aspectsProp = new HashSet<>();
		HashMap<String, Serializable> props = new HashMap<>();
		aspectsProp.add("cmf:templateDefinition");

		if (nextTemplate.getName().contains("primary")) {
			props.put("cmf:primarytemplate", Boolean.TRUE);
		} else {
			props.put("cmf:primarytemplate", Boolean.FALSE);
		}
		String templateType = nextTemplate.getName();
		props.put("cm:title", "Template (" + templateType + ")");
		props.put("emf:type", templateType);
		uploadFile(alfrescoUploader, nextTemplate.getAbsolutePath(), siteId, "Template Definitions", "cm:content",
				props, aspectsProp, Boolean.TRUE, "New template");

	}

	/**
	 * Upload doc def.
	 *
	 * @param definitionFile
	 *            the definition file
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param siteId
	 *            the site id
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private void uploadDocDef(File definitionFile, AlfrescoUploader alfrescoUploader, String siteId)
			throws FileNotFoundException {
		File file = definitionFile;
		if (file.exists()) {
			HashSet<String> aspectsProp = new HashSet<>();
			HashMap<String, Serializable> props = new HashMap<>();
			aspectsProp.add("cmf:documentDefinition");
			props.put("cm:title", "Document definition");
			uploadFile(alfrescoUploader, file.getAbsolutePath(), siteId, "Document Definitions", "cm:content", props,
					aspectsProp, Boolean.TRUE, "New document defintion");
		}
	}

	/**
	 * Upload object def.
	 *
	 * @param definitionFile
	 *            the definition file
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param siteId
	 *            the site id
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private void uploadObjectDef(File definitionFile, AlfrescoUploader alfrescoUploader, String siteId)
			throws FileNotFoundException {
		File file = definitionFile;
		if (file.exists()) {
			HashSet<String> aspectsProp = new HashSet<>();
			HashMap<String, Serializable> props = new HashMap<>();
			aspectsProp.add("dom:objectDefinition");
			props.put("cm:title", "Object definition");
			uploadFile(alfrescoUploader, file.getAbsolutePath(), siteId, "Object Definitions", "cm:content", props,
					aspectsProp, Boolean.TRUE, "New object defintion");
		}
	}

	/**
	 * Upload file.
	 *
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param absolutePath
	 *            the absolute path
	 * @param siteId
	 *            the site id
	 * @param folder
	 *            the string
	 * @param contentType
	 *            the content type
	 * @param props
	 *            the props
	 * @param aspectsProp
	 *            the aspects prop
	 * @param overWrite
	 *            the over write
	 * @param versionDescription
	 *            the version description
	 * @return the string
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private String uploadFile(AlfrescoUploader alfrescoUploader, String absolutePath, String siteId, String folder,
			String contentType, Map<String, Serializable> props, Set<String> aspectsProp, Boolean overWrite,
			String versionDescription) throws FileNotFoundException {
		File file = new File(absolutePath);

		if (file.exists()) {
			String uploadFile = alfrescoUploader.uploadFile(ServiceURIRegistry.UPLOAD_SERVICE_URI,
					new FilePartSource(file), siteId, folder, null, contentType, props, aspectsProp, overWrite,
					Boolean.FALSE, versionDescription);
			LOGGER.info("Upload result " + uploadFile);
			assertNotNull(uploadFile, "Result should be json object");
			return uploadFile;
		}
		return file.getAbsolutePath();
	}

	/**
	 * Assert not null.
	 *
	 * @param callWebScript
	 *            the call web script
	 * @param string
	 *            the string
	 */
	private void assertNotNull(String callWebScript, String string) {
		// nothing to do now
	}

	/**
	 * Creates the method.
	 *
	 * @param string
	 *            the string
	 * @param postMethod
	 *            the post method
	 * @return the http method
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	private HttpMethod createMethod(String string, PostMethod postMethod) throws UnsupportedEncodingException {
		return httpClient.createMethod(postMethod, string, true);
	}

	/**
	 * Upload workflow definition.
	 *
	 * @param file
	 *            the def
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param siteId
	 *            the site id
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private void uploadWorkflowDef(File file, AlfrescoUploader alfrescoUploader, String siteId)
			throws FileNotFoundException {
		HashSet<String> aspectsProp = new HashSet<>();
		aspectsProp.add("cmf:workflowDefinition");
		HashMap<String, Serializable> props = new HashMap<>();
		props.put("cm:title", "The workflow definitions");
		uploadFile(alfrescoUploader, file.getAbsolutePath(), siteId, "Workflow Definitions", "cm:content", props,
				aspectsProp, Boolean.TRUE, "");
	}

	/**
	 * Upload workflow definition.
	 *
	 * @param file
	 *            the definition file
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param siteId
	 *            the site id
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private void uploadTaskDef(File file, AlfrescoUploader alfrescoUploader, String siteId)
			throws FileNotFoundException {
		HashSet<String> aspectsProp = new HashSet<>();
		aspectsProp.add("cmf:tasksDefinition");
		HashMap<String, Serializable> props = new HashMap<>();
		props.put("cm:title", "The tasks definitions");
		uploadFile(alfrescoUploader, file.getAbsolutePath(), siteId, "Tasks Definitions", "cm:content", props,
				aspectsProp, Boolean.TRUE, "");
	}

	/**
	 * Upload case def.
	 *
	 * @param file
	 *            the def
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param siteId
	 *            the site id
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private void uploadCaseDef(File file, AlfrescoUploader alfrescoUploader, String siteId)
			throws FileNotFoundException {
		HashSet<String> aspectsProp = new HashSet<>();
		HashMap<String, Serializable> props = new HashMap<>();
		aspectsProp.add("cmf:caseDefinition");
		props.put("cm:title", "Case definition");
		uploadFile(alfrescoUploader, file.getAbsolutePath(), siteId, "Case Definitions", "cm:content", props,
				aspectsProp, Boolean.TRUE, "");
	}

	/** The site to instances qname. */
	private static Map<String, String> siteToInstancesQname = new HashMap<>();

	static {
		siteToInstancesQname.put("cmf", "cmf:caseinstancesspace");
		siteToInstancesQname.put("pm", "pm:projectinstancesspace");
		siteToInstancesQname.put("dom", "dom:objectinstancesspace");
	}

	/**
	 * Clear site.
	 *
	 * @param siteId
	 *            the site id
	 * @param containers
	 *            - cmf, pm instances
	 * @throws Exception
	 *             the exception
	 */
	public void clearSite(String siteId, List<String> containers) throws Exception {
		JSONObject request = new JSONObject();
		request.put("sites", siteId);
		request.put("all", Boolean.TRUE);
		JSONArray containersArray = new JSONArray();
		// set instance mapping
		for (String string : containers) {
			containersArray.put(siteToInstancesQname.get(string));
		}
		request.put("containers", containersArray);
		HttpMethod createMethod = createMethod(request.toString(), new PostMethod());
		String callWebScript = httpClient.invokeWithResponse("case/instance/obsolete/delete", createMethod);

		assertNotNull(callWebScript, "Result should be json object");

	}

	/**
	 * Clear specific.
	 *
	 * @param node
	 *            the site
	 * @throws Exception
	 *             the exception
	 */
	public void clearSpecific(String node) throws Exception {
		JSONObject request = new JSONObject();
		request.put("node", node);
		request.put("all", Boolean.TRUE);

		HttpMethod createMethod = createMethod(request.toString(), new PostMethod());
		String callWebScript = httpClient.invokeWithResponse("case/instance/obsolete/delete", createMethod);

		assertNotNull(callWebScript, "Result should be json object");

	}

	/**
	 * Upload bpmn and deploy.
	 *
	 * @param definitionFile
	 *            the definition file
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param basePath
	 *            the base path
	 * @return the string
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private String uploadBpmnAndDeploy(File definitionFile, AlfrescoUploader alfrescoUploader, String basePath)
			throws FileNotFoundException {
		File file = definitionFile;
		HashSet<String> aspectsProp = new HashSet<>();
		HashMap<String, Serializable> props = new HashMap<>();
		String uploadFile = uploadFile(alfrescoUploader, file.getAbsolutePath(), null, basePath, "cm:content", props,
				aspectsProp, Boolean.TRUE, "");
		if (uploadFile != null) {
			try {
				String nodeId = new JSONObject(uploadFile).getString("nodeRef");
				HttpMethod deployRequest = httpClient.createMethod(new GetMethod(), "", true);
				String result = httpClient
						.invokeWithResponse(
								"workflow/deploy?node=" + nodeId + "&redeployIfExists="
										+ Boolean.valueOf(PropertyConfigsWrapper
												.getInstance()
													.getProperty("init.bpmn.redeployIfExists")),
								deployRequest);
				if (result != null) {
					JSONObject wf = new JSONObject(result);
					if (wf.getString("id").length() > 0) {
						return "Deployed: " + definitionFile.getName() + " as " + wf.getString("id");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return "Not Deployed: " + definitionFile.getName();
	}

	/**
	 * Sets the http client.
	 *
	 * @param httpClient
	 *            the new http client
	 */
	public void setHttpClient(AbstractRESTClient httpClient) {
		this.httpClient = httpClient;

	}

	/**
	 * Clear child specific.
	 *
	 * @param node
	 *            the node
	 * @throws JSONException
	 *             the jSON exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	public void clearChildSpecific(String node) throws JSONException, UnsupportedEncodingException {
		JSONObject request = new JSONObject();
		request.put("parentNode", node);
		request.put("all", Boolean.TRUE);

		HttpMethod createMethod = createMethod(request.toString(), new PostMethod());
		String callWebScript = httpClient.invokeWithResponse("case/instance/obsolete/delete", createMethod);

		assertNotNull(callWebScript, "Result should be json object");
	}

	/**
	 * Estimate load.
	 *
	 * @param definitionTypes
	 *            the definition types
	 * @return the int
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	public int estimateLoad(List<DefinitionType> definitionTypes) throws FileNotFoundException {
		int total = 1;
		for (DefinitionType type : definitionTypes) {
			total += scanByType(type.type);
		}
		return total;
	}

	/**
	 * Post request.
	 *
	 * @param url
	 *            the url
	 * @param data
	 *            the data
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	public void postRequest(String url, String data) throws UnsupportedEncodingException {

		HttpMethod createMethod = createMethod(data, new PostMethod());
		String callWebScript = httpClient.invokeWithResponse(url, createMethod);
		LOGGER.debug("postRequest: " + callWebScript);
		assertNotNull(callWebScript, "Result should be json object");
	}

	/**
	 * Gets the request.
	 *
	 * @param string
	 *            the string
	 */
	public void getRequest(String string) {
		HttpMethod createMethod = new GetMethod(string);
		httpClient.invokeWithResponse(string, createMethod);
	}
}
