package com.sirma.itt.migration.register;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.migration.constants.MigrationStatus;
import com.sirma.itt.migration.dto.FileRegisterSearchDTO;
import com.sirma.itt.migration.dto.FileRegistryEntry;

/**
 * Web script call back for File register get operations such as searching and
 * status probing.
 *
 * @author BBonev
 */
public class FileRegisterGetScript extends DeclarativeWebScript {

	private static final String RESPONSE_DATA = "data";
	private static final String MODE_CRC = "crc";
	private static final String MODE_SEARCH = "search";
	private static final String PAGING_PAGING = "paging";
	private static final String PAGING_SKIP_COUNT = "skipCount";
	private static final String PAGING_TOTAL_ITEMS = "totalItems";
	private static final String PARAM_TARGET = "target";
	private static final String PARAM_TOTAL = "total";
	private static final String PARAM_NAME = "name";
	private static final String PARAM_PAGE_SIZE = "pageSize";
	private static final String PARAM_SKIP = "skip";
	private static final String PARAM_STATUS = "status";
	private static final String PARAM_MODIFIED_TO = "modifiedTo";
	private static final String PARAM_MODIFIED_FROM = "modifiedFrom";
	private static final String PARAM_MODIFIED_BY = "modifiedBy";
	private static final String PARAM_SOURCE = "source";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private FileRegisterService fileRegisterService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status status, Cache cache) {
		Map<String, Object> model = new HashMap<String, Object>(4, 1f);
		String extensionPath = req.getExtensionPath();
		// we are just checking the status of the file
		if (extensionPath.startsWith(MODE_CRC)) {
			String crcString = extensionPath.substring(extensionPath
					.indexOf("/") + 1);
			FileRegistryEntry registryEntry = fileRegisterService
					.findByCrc(crcString);
			List<FileRegistryEntry> result = new ArrayList<FileRegistryEntry>(1);
			Map<String, Integer> paging = new HashMap<String, Integer>(6, 1f);
			int count = 0;
			if (registryEntry != null) {
				result.add(registryEntry);
				count = 1;
			}
			model.put(RESPONSE_DATA, result);
			paging.put(PAGING_TOTAL_ITEMS, count);
			paging.put(PARAM_PAGE_SIZE, count);
			paging.put(PAGING_SKIP_COUNT, 0);
			model.put(PAGING_PAGING, paging);
		} else if (extensionPath.startsWith(MODE_SEARCH)) {
			// we are searching for files
			String[] parameterNames = req.getParameterNames();
			Set<String> parameters = new LinkedHashSet<String>(
					Arrays.asList(parameterNames));
			FileRegisterSearchDTO dto = new FileRegisterSearchDTO();
			if (parameters.contains(PARAM_SOURCE)) {
				dto.setSource(decodeParameter(req, PARAM_SOURCE));
			}
			if (parameters.contains(PARAM_MODIFIED_BY)) {
				dto.setModifiedBy(req.getParameter(PARAM_MODIFIED_BY));
			}
			if (parameters.contains(PARAM_MODIFIED_FROM)) {
				dto.setModifiedFrom(convertDate(decodeParameter(req,PARAM_MODIFIED_FROM)));
			}
			if (parameters.contains(PARAM_MODIFIED_TO)) {
				dto.setModifiedTo(convertDate(decodeParameter(req,PARAM_MODIFIED_TO)));
			}
			if (parameters.contains(PARAM_STATUS)) {
				dto.setStatus(MigrationStatus.getStatus(Integer.parseInt(req
						.getParameter(PARAM_STATUS))));
			}
			if (parameters.contains(PARAM_SKIP)) {
				dto.setSkipCount(Integer.parseInt(req.getParameter(PARAM_SKIP)));
			}
			if (parameters.contains(PARAM_PAGE_SIZE)) {
				dto.setPageSize(Integer.parseInt(req.getParameter(PARAM_PAGE_SIZE)));
			}
			if (parameters.contains(PARAM_NAME)) {
				dto.setNameFilter(decodeParameter(req, PARAM_NAME));
			}
			if (parameters.contains(PARAM_TOTAL)) {
				dto.setTotalCount(Integer.parseInt(req.getParameter(PARAM_TOTAL)));
			}
			if (parameters.contains(PARAM_TARGET)) {
				dto.setTarget(decodeParameter(req, PARAM_TARGET));
			}
			if (parameters.contains("include")) {
				dto.getInclude().addAll(Arrays.asList(decodeParameter(req, "include").split(",")));
			}
			if (parameters.contains("exclude")) {
				dto.getExclude().addAll(Arrays.asList(decodeParameter(req, "exclude").split(",")));
			}
			fileRegisterService.search(dto);
			if ((dto.getResult() != null) && !dto.getResult().isEmpty()) {
				model.put(RESPONSE_DATA, dto.getResult());
				Map<String, Integer> paging = new HashMap<String, Integer>(6, 1f);
				paging.put(PAGING_TOTAL_ITEMS, dto.getTotalCount());
				paging.put(PARAM_PAGE_SIZE, dto.getPageSize());
				paging.put(PAGING_SKIP_COUNT, dto.getSkipCount());
				model.put(PAGING_PAGING, paging);
			}
		}

		return model;
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
	 * Converts the given string to {@link Date} object if the source is in the
	 * format <code>yyyy-MM-dd HH:mm:ss</code>
	 *
	 * @param parameter
	 *            is the source to convert
	 * @return the {@link Date} representation of the given argument or
	 *         <code>null</code> if not in valid format.
	 */
	private Date convertDate(String parameter) {
		try {
			return DATE_FORMAT.parse(parameter);
		} catch (ParseException e) {
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
}
