package com.sirma.itt.pm.schedule.converter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.pm.schedule.security.ScheduleActions;

/**
 * Type converter provider for different Action implementations to JSONObject.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ActionToJsonConverterProvider implements TypeConverterProvider {

	/** The label provider. */
	@Inject
	LabelProvider labelProvider;
	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_LANGUAGE, defaultValue = "en")
	private String defaultLanguage;
	@Inject
	private AuthenticationService authenticationService;

	/**
	 * The Class TransitionActionToJsonConverter.
	 *
	 * @author BBonev
	 */
	public class TransitionActionToJsonConverter implements
			Converter<TransitionDefinitionImpl, JSONObject> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public JSONObject convert(TransitionDefinitionImpl source) {
			JSONObject data = new JSONObject();
			String label = labelProvider.getLabel(source.getLabelId(), getLanguage());
			JsonUtil.addToJson(data, "label", label);

			JSONObject jsonObject = new JSONObject();
			// note if ID is changed not to be the action id, this should be updated in the
			// ModelConverter.buildActions method
			JsonUtil.addToJson(jsonObject, source.getActionId(), data);
			return jsonObject;
		}
	}

	/**
	 * The Class ScheduleActionToJsonConverter.
	 *
	 * @author BBonev
	 */
	public class ScheduleActionToJsonConverter implements Converter<ScheduleActions, JSONObject> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public JSONObject convert(ScheduleActions source) {
			JSONObject data = new JSONObject();
			String label = labelProvider.getLabel(source.getLabel(), getLanguage());
			JsonUtil.addToJson(data, "label", label);

			JSONObject jsonObject = new JSONObject();
			// note if ID is changed not to be the action id, this should be updated in the
			// ModelConverter.buildActions method
			JsonUtil.addToJson(jsonObject, source.getActionId(), data);
			return jsonObject;
		}

	}

	/**
	 * Gets the language.
	 *
	 * @return the language
	 */
	String getLanguage() {
		User user = null;
		try {
			user = authenticationService.getCurrentUser();
		} catch (ContextNotActiveException e) {
			// TODO: handle exception
		}
		if (user == null) {
			user = SecurityContextManager.getFullAuthentication();
		}

		if (user == null) {
			return defaultLanguage;
		}
		String language = user.getLanguage();
		if (StringUtils.isNullOrEmpty(language)) {
			language = defaultLanguage;
		}
		return language;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(ScheduleActions.class, JSONObject.class,
				new ScheduleActionToJsonConverter());
		converter.addConverter(TransitionDefinitionImpl.class, JSONObject.class,
				new TransitionActionToJsonConverter());
	}

}
