package com.sirma.itt.migration.register;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.migration.constants.MigrationStatus;
import com.sirma.itt.migration.dto.FileRegistryEntry;
import com.sirma.itt.service.BaseService;

/**
 * Web script call back for File register get operations such as searching and
 * status probing.
 *
 * @author BBonev
 */
public class FileRegisterFixGetScript extends DeclarativeWebScript {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(FileRegisterFixGetScript.class);

	/** The Constant RESPONSE_DATA. */
	private static final String RESPONSE_DATA = "data";

	/** The Constant PARAM_DEST_PATH. */
	private static final String PARAM_DEST_PATH = "destPath";

	/** The Constant PARAM_SITE_ID. */
	private static final String PARAM_SITE_ID = "siteId";

	/** The Constant PARAM_SOURCE_BASE. */
	private static final String PARAM_SOURCE_BASE = "srcBase";

	/** The file register service. */
	private FileRegisterService fileRegisterService;

	/** The node service. */
	private NodeService nodeService;

	/** The check builder. */
	private CheckBuilder checkBuilder;

	/** The base service. */
	private BaseService baseService;

	/** The folder filter. */
	private static Set<QName> folderFilter = new HashSet<QName>();

	/** The file filter. */
	private static Set<QName> fileFilter = new HashSet<QName>();

	static {
		folderFilter.add(ContentModel.TYPE_FOLDER);
		// TODO: RM support
//		folderFilter.add(RecordsManagementModel.TYPE_RECORD_FOLDER);
//		folderFilter.add(DOD5015Model.TYPE_RECORD_SERIES);
//		folderFilter.add(DOD5015Model.TYPE_RECORD_CATEGORY);

		fileFilter.add(ContentModel.TYPE_CONTENT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status status, Cache cache) {
		Map<String, Object> model = new HashMap<String, Object>(4, 1f);
		String extensionPath = req.getServiceMatch().toString();
		// we are just checking the status of the file
		if (extensionPath.endsWith("/fix")) {
			// we are searching for files
			String[] parameterNames = req.getParameterNames();
			Set<String> parameters = new LinkedHashSet<String>(
					Arrays.asList(parameterNames));
			String baseSource = "";
			String siteId = null;
			String destPath = "/";
			if (parameters.contains(PARAM_DEST_PATH)) {
				destPath = decodeParameter(req, PARAM_DEST_PATH);
			}
			if (parameters.contains(PARAM_SITE_ID)) {
				siteId = req.getParameter(PARAM_SITE_ID);
			}
			if (parameters.contains(PARAM_SOURCE_BASE)) {
				baseSource = decodeParameter(req, PARAM_SOURCE_BASE);
			}
			if (siteId == null) {
				model.put("error", "Destination site is required");
				return model;
			}

			LOGGER.info("Starting file register maintanance...");

			NodeRef siteRef = baseService.getDocLibraryOfSite(siteId);
			List<FileRegistryEntry> result = new LinkedList<FileRegistryEntry>();
			String localDestPath = stripExtraSeparators(destPath);
			traversePath(getNodeRefForPath(siteRef, localDestPath), siteId
					+ "://" + localDestPath, baseSource, result);

			for (FileRegistryEntry fileRegistryEntry : result) {
				fileRegisterService.save(fileRegistryEntry);
			}

			if (!result.isEmpty()) {
				LOGGER.info("Created " + result.size() + " new entries");
				model.put(RESPONSE_DATA, result);
			}
		}
		LOGGER.info("End of file register maintanance...");
		return model;
	}

	/**
	 * Strip extra separators.
	 *
	 * @param destPath
	 *            the destination path
	 * @return the string
	 */
	private String stripExtraSeparators(String destPath) {
		String local = destPath;
		if ("/".equals(local)) {
			return local;
		}
		if (local.startsWith("/")) {
			local = local.substring(1);
		}
		if (local.endsWith("/")) {
			local = local.substring(0, local.length() - 1);
		}
		return local;
	}

	/**
	 * Gets the node reference for path.
	 *
	 * @param doclibrary
	 *            the document library reference
	 * @param destPath
	 *            the destination path
	 * @return the node reference for path
	 */
	private NodeRef getNodeRefForPath(NodeRef doclibrary, String destPath) {
		String local = destPath;
		if ("/".equals(local)) {
			return doclibrary;
		}
		String[] split = local.split("\\s*/\\s*");
		NodeRef ref = doclibrary;

		for (int i = 0; i < split.length; i++) {
			String string = split[i];
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(ref, folderFilter);
			for (ChildAssociationRef associationRef : childAssocs) {
				Serializable property = nodeService.getProperty(associationRef.getChildRef(), ContentModel.PROP_NAME);
				if (string.equalsIgnoreCase(property.toString())) {
					ref = associationRef.getChildRef();
					break;
				}
			}
		}
		return ref;
	}

	/**
	 * Traverse the given path and creates missing file register entries.
	 *
	 * @param nodeRef
	 *            the node reference to list
	 * @param destPath
	 *            the destination path to set
	 * @param baseSource
	 *            the base source is the source path to set
	 * @param result
	 *            the result list that will contain all generated entries if any
	 */
	private void traversePath(NodeRef nodeRef, String destPath,
			String baseSource, List<FileRegistryEntry> result) {
		// list all files from the given directory
		List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
				nodeRef, fileFilter);
		LOGGER.info("Found " + childAssocs.size() + " files in " + destPath);
		int index = 0;
		for (ChildAssociationRef associationRef : childAssocs) {
			NodeRef ref = associationRef.getChildRef();
			if (!fileRegisterService.existsNodeRefEntry(ref)) {
				index++;
				if (index % 100 == 0) {
					String msg = "Created : (" + index + "/" + childAssocs.size() + ")";
					LOGGER.info(msg);
				}
				String checkCode = checkBuilder.createCheckCode(ref);
				if (checkCode == null) {
					LOGGER.warn("Skipping due to: failed to generate check code for node " + ref);
					continue;
				}
				FileRegistryEntry registryEntry = fileRegisterService.findByCrc(checkCode);
				String name = nodeService.getProperty(ref, ContentModel.PROP_NAME).toString();
				if (registryEntry == null) {
					registryEntry = new FileRegistryEntry();
					registryEntry.setCrc(checkCode);
					registryEntry.setDestFileName(name);
					registryEntry.setFileName(name);
					registryEntry.setNodeId(ref.getId());
					registryEntry.setSourcePath(baseSource);
					registryEntry.setStatus(MigrationStatus.MIGRATED.getStatusCode());
					registryEntry.setTargetPath(destPath);
					result.add(registryEntry);
				} else {
					registryEntry.setDestFileName(name);
					registryEntry.setTargetPath(destPath);
					registryEntry.setStatus(MigrationStatus.MIGRATED.getStatusCode());
					registryEntry.setNodeId(ref.getId());
					result.add(registryEntry);
				}
			}
		}
		// schedule for gc
		childAssocs.clear();
		childAssocs = null;

		// traverse all sub folders
		childAssocs = nodeService.getChildAssocs(nodeRef, folderFilter);
		for (int i = 0; i < childAssocs.size(); i++) {
			ChildAssociationRef associationRef = childAssocs.get(i);
			NodeRef ref = associationRef.getChildRef();
			String name = nodeService.getProperty(ref, ContentModel.PROP_NAME).toString();
			traversePath(ref, destPath + "/" + name, baseSource + "\\" + name, result);
		}
	}

	/**
	 * Decodes the given parameter using the {@link URLDecoder}.
	 *
	 * @param req
	 *            is the web script request
	 * @param param
	 *            is the parameter to return
	 * @return the decoded parameter value or <code>null</code> if error occur.
	 */
	private String decodeParameter(WebScriptRequest req, String param){
		try {
			return URLDecoder.decode(req.getParameter(param), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Getter method for fileRegisterService.
	 *
	 * @return the fileRegisterService
	 */
	public FileRegisterService getFileRegisterService() {
		return fileRegisterService;
	}

	/**
	 * Setter method for fileRegisterService.
	 *
	 * @param fileRegisterService
	 *            the fileRegisterService to set
	 */
	public void setFileRegisterService(FileRegisterService fileRegisterService) {
		this.fileRegisterService = fileRegisterService;
	}

	/**
	 * Getter method for nodeService.
	 *
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * Setter method for nodeService.
	 *
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Getter method for checkBuilder.
	 *
	 * @return the checkBuilder
	 */
	public CheckBuilder getCheckBuilder() {
		return checkBuilder;
	}

	/**
	 * Setter method for checkBuilder.
	 *
	 * @param checkBuilder
	 *            the checkBuilder to set
	 */
	public void setCheckBuilder(CheckBuilder checkBuilder) {
		this.checkBuilder = checkBuilder;
	}

	/**
	 * Getter method for baseService.
	 *
	 * @return the baseService
	 */
	public BaseService getBaseService() {
		return baseService;
	}

	/**
	 * Setter method for baseService.
	 *
	 * @param baseService
	 *            the baseService to set
	 */
	public void setBaseService(BaseService baseService) {
		this.baseService = baseService;
	}
}
