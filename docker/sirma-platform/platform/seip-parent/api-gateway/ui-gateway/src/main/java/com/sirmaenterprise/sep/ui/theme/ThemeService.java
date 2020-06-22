package com.sirmaenterprise.sep.ui.theme;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Theme service which queries the {@link DefinitionService} to retrieve theme related properties on ui bootstrap.
 *
 * @author g.tsankov
 */
public class ThemeService {

	private static Logger LOGGER = LoggerFactory.getLogger(ThemeService.class);

	@Inject
	private DefinitionService definitionService;

	/**
	 * Queries the {@link DefinitionService} and gets the `uiTheme` definition id, serialized as a {@link HashMap}.
	 *
	 * @return {@link HashMap} of defined theme styles.
	 * Returns empty map if a problem with definition serialization occurs.
	 */
	public Map<String, String> getUiTheme() {
		DefinitionModel model = definitionService.find("uiTheme");
		if (model != null) {
			Optional<PropertyDefinition> themeDefinition = model.getField("styles");
			if (themeDefinition.isPresent()) {
				Map<String, String> propertiesMap = new HashMap<>();
				try {
					JSONObject definitionProperties = new JSONObject(themeDefinition.get().getDefaultValue());
					Iterator<String> keysItr = definitionProperties.keys();

					while (keysItr.hasNext()) {
						String key = keysItr.next();
						String value = (String) definitionProperties.get(key);

						if (StringUtils.isNotEmpty(value)) {
							propertiesMap.put(key, value);
						}
					}
					return propertiesMap;
				} catch (JSONException e) {
					LOGGER.error("A problem occured with theme definition serialization.", e);
				}
			}
		}

		return Collections.emptyMap();
	}
}
