package com.sirma.itt.seip.eai.util;

import java.util.Map;

import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonUtil;

public class XlsxModelParserTestUtil {
	public static Map<String, Integer> provideXlsxPropertyMapping() {
		JSONObject jsonObject = JsonUtil.createObjectFromString(
				"{\"TITLE\":0,\"URI\":12,\"PPROPERTY_ID\":13,\"MAPPING_DATA_CONVERT\":14,\"MAPPING_CRITERIA_CONVERT\":15,\"MANDATORY_SEIP\":2,\"DATA_TYPE\":6,\"CODELIST_ID\":7, \"THIN_REQUEST_USAGE\":16}");
		Map<String, Integer> model = JsonUtil.toMap(jsonObject);
		return model;
	}
}
