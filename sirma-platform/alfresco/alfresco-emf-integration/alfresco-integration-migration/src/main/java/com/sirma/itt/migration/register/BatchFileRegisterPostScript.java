package com.sirma.itt.migration.register;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.migration.dto.FileRegistryEntry;

/**
 * Web script call back for File register batch operations such as status
 * retrieving many register entries by CRC.
 *
 * @author BBonev
 */
public class BatchFileRegisterPostScript extends DeclarativeWebScript {

	private static final String RESPONSE_RESULT = "result";
	private static final String REQUEST_DATA = "data";
	private static final String MODE_GET = "get";
	private FileRegisterService fileRegisterService;

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status httpStatus, Cache cache) {
		Map<String, Object> model = new LinkedHashMap<String, Object>();
		String extensionPath = req.getServicePath();
		if (extensionPath.endsWith(MODE_GET)) {
			doGetEntries(req, model);
		}
		return model;
	}

	/**
	 * Performs a fetch operation using the given request
	 * 
	 * @param req
	 *            is the web request
	 * @param model
	 *            is the result model
	 */
	private void doGetEntries(WebScriptRequest req, Map<String, Object> model) {
		JSONObject jsonObject = getContent(req);
		if (jsonObject.has(REQUEST_DATA)) {
			try {
				JSONArray jsonArray = jsonObject.getJSONArray(REQUEST_DATA);
				List<String> list = new ArrayList<String>(jsonArray.length());
				for (int i = 0; i < jsonArray.length(); i++) {
					String item = jsonArray.getString(i);
					list.add(item);
				}
				List<FileRegistryEntry> fetchedEntries = fileRegisterService
						.fetchEntries(list);
				model.put(RESPONSE_RESULT, fetchedEntries);
			} catch (JSONException e) {
				e.printStackTrace();
			}
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
