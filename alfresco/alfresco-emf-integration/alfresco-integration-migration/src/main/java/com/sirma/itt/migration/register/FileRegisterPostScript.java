package com.sirma.itt.migration.register;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.migration.constants.MigrationStatus;
import com.sirma.itt.migration.dto.FileRegistryEntry;

/**
 * Web script call back for File register post operations such as status
 * changing or adding to the register.
 *
 * @author BBonev
 */
public class FileRegisterPostScript extends DeclarativeWebScript {

	private static final String PROP_NODE_ID = "nodeId";
	private static final String RESPONSE_RESULT = "result";
	private static final String PROP_FILE_NAME = "fileName";
	private static final String PROP_DEST_FILE_NAME = "destFileName";
	private static final String PROP_TARGET_PATH = "targetPath";
	private static final String PROP_SOURCE_PATH = "sourcePath";
	private static final String PROP_STATUS = "status";
	private static final String PROP_CRC = "crc";
	private static final String REQUEST_DATA = "data";
	private static final String MODE_UPDATE = "update";
	private static final String MODE_ADD = "add";
	private static final String MODE_CHANGE_STATUS = "changestatus";
	private FileRegisterService fileRegisterService;

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status httpStatus, Cache cache) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String extensionPath = req.getServicePath();
		// to change a status of a given list of files
		if (extensionPath.endsWith(MODE_CHANGE_STATUS)) {
			doChangeStatus(req, model);
			// to add new entries to the register
		} else if (extensionPath.endsWith(MODE_ADD)) {
			doAddToRegister(req, model);
		} else if (extensionPath.endsWith(MODE_UPDATE)) {
			doUpdateRegister(req, model);
		}
		return model;
	}

	/**
	 * Performs updating of the given file register entries
	 *
	 * @param req
	 *            is the script request
	 * @param model
	 *            is the result model to populate
	 */
	private void doUpdateRegister(WebScriptRequest req,
			Map<String, Object> model) {
		JSONObject jsonObject = getContent(req);
		try {
			if ((jsonObject != null) && jsonObject.has(REQUEST_DATA)) {
				JSONArray array = jsonObject.getJSONArray(REQUEST_DATA);
				List<Pair<String, Boolean>> result = new ArrayList<Pair<String, Boolean>>(
						array.length());
				for (int i = 0; i < array.length(); i++) {
					FileRegistryEntry entry = null;
					String crc = null;
					try {
						JSONObject item = array.getJSONObject(i);
						crc = item.getString(PROP_CRC);
						entry = fileRegisterService.findByCrc(crc);
						if (entry == null) {
							result.add(new Pair<String, Boolean>(crc, false));
							continue;
						}

						entry.setSourcePath(item.getString(PROP_SOURCE_PATH));
						if (item.has(PROP_TARGET_PATH)) {
							entry.setTargetPath(item.getString(PROP_TARGET_PATH));
						}
						if ((entry.getNodeId() == null)
								&& item.has(PROP_NODE_ID)) {
							entry.setNodeId(new NodeRef(item
									.getString(PROP_NODE_ID)).getId());
						}
						entry.setFileName(item.getString(PROP_FILE_NAME));
						if (item.has(PROP_DEST_FILE_NAME)) {
							entry.setDestFileName(item.getString(PROP_DEST_FILE_NAME));
						}
						MigrationStatus status = MigrationStatus.getStatus(item
								.getInt(PROP_STATUS));
						entry.setStatus(status.getStatusCode());
					} catch (JSONException e) {
						// if we can't extract the data from the given entry of
						// the array then we mark it as invalid and continue
						e.printStackTrace();
						result.add(new Pair<String, Boolean>(crc, false));
						continue;
					} catch (AlfrescoRuntimeException e) {
						// invalid node reference
						e.printStackTrace();
						result.add(new Pair<String, Boolean>(crc, false));
						continue;
					}
					// here the 'resultStatus' should not come null
					FileRegistryEntry saved = fileRegisterService.save(entry);
					if (saved != null) {
						result.add(new Pair<String, Boolean>(crc, true));
					} else {
						result.add(new Pair<String, Boolean>(crc, false));
					}
				}
				model.put(RESPONSE_RESULT, result);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs adding entries to the file register
	 *
	 * @param req
	 *            is the script request
	 * @param model
	 *            is the result model to populate
	 */
	private void doAddToRegister(WebScriptRequest req, Map<String, Object> model) {
		JSONObject jsonObject = getContent(req);
		try {
			if (jsonObject.has(REQUEST_DATA)) {
				JSONArray array = jsonObject.getJSONArray(REQUEST_DATA);
				List<Pair<String, Boolean>> result = new ArrayList<Pair<String, Boolean>>(
						array.length());
				for (int i = 0; i < array.length(); i++) {
					FileRegistryEntry entry = new FileRegistryEntry();
					try {
						JSONObject item = array.getJSONObject(i);
						entry.setCrc(item.getString(PROP_CRC));
						entry.setSourcePath(item.getString(PROP_SOURCE_PATH));
						if (item.has(PROP_TARGET_PATH)) {
							entry.setTargetPath(item.getString(PROP_TARGET_PATH));
						}
						entry.setFileName(item.getString(PROP_FILE_NAME));
						if (item.has(PROP_DEST_FILE_NAME)) {
							entry.setDestFileName(item.getString(PROP_DEST_FILE_NAME));
						}
						MigrationStatus status = MigrationStatus.getStatus(item
								.getInt(PROP_STATUS));
						if (item.has(PROP_NODE_ID)) {
							entry.setNodeId(new NodeRef(item
									.getString(PROP_NODE_ID)).getId());
						}
						entry.setStatus(status.getStatusCode());
					} catch (JSONException e) {
						// if we can't extract the data from the given entry of
						// the array then we mark it as invalid and continue
						e.printStackTrace();
						result.add(new Pair<String, Boolean>(entry.getCrc(),
								false));
						continue;
					} catch (AlfrescoRuntimeException e) {
						// invalid node reference
						e.printStackTrace();
						result.add(new Pair<String, Boolean>(entry.getCrc(), false));
						continue;
					}
					// here the 'entry' should not come null
					FileRegistryEntry resultStatus = fileRegisterService
							.save(entry);
					if (resultStatus != null) {
						result.add(new Pair<String, Boolean>(resultStatus
								.getCrc(), true));
					} else {
						result.add(new Pair<String, Boolean>(entry.getCrc(),
								false));
					}
				}
				model.put(RESPONSE_RESULT, result);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs an update of the status for the given CRC codes
	 *
	 * @param req
	 *            is the script request
	 * @param model
	 *            is the result model to populate
	 */
	private void doChangeStatus(WebScriptRequest req, Map<String, Object> model) {
		try {
			JSONObject jsonObject = getContent(req);
			if (jsonObject.has(REQUEST_DATA)) {
				JSONArray array = jsonObject.getJSONArray(REQUEST_DATA);
				List<Pair<String, Boolean>> result = new ArrayList<Pair<String, Boolean>>(
						array.length());
				for (int i = 0; i < array.length(); i++) {
					JSONObject item = array.getJSONObject(i);
					String crc = item.getString(PROP_CRC);
					MigrationStatus status = MigrationStatus.getStatus(item
							.getInt(PROP_STATUS));
					boolean resultStatus = fileRegisterService.changeStatus(
							crc, status);
					result.add(new Pair<String, Boolean>(crc, resultStatus));
				}
				model.put(RESPONSE_RESULT, result);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Extracts the content from the request
	 *
	 * @param req
	 *            is the source request
	 * @return the extracted content. If error occur the default content will be
	 *         returned <code>{}</code>>
	 */
	private JSONObject getContent(WebScriptRequest req) {
		String jsonCallback = "{}";
		try {
			jsonCallback = req.getContent().getContent();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(jsonCallback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
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
}
