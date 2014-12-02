package com.sirma.itt.pm.schedule.converter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.JsonRepresentable;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.resources.model.EmfResource;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.resources.model.ResourceRole;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.time.ISO8601DateFormat;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.util.PathHelper;
import com.sirma.itt.emf.util.ReflectionUtils;
import com.sirma.itt.pm.schedule.constants.ScheduleConfigProperties;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryMinifiedProperties;
import com.sirma.itt.pm.schedule.model.ScheduleEntryProperties;
import com.sirma.itt.pm.schedule.model.ScheduleResourceRole;
import com.sirma.itt.pm.schedule.util.DateUtil;
import com.sirma.itt.pm.schedule.util.ScheduleEntryUtil;

/**
 * The Class ScheduleToJSONProvider. {@link TypeConverter} provider for converting schedule objects
 * to JSON and vies versa.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ScheduleToJSONConverterProvider implements TypeConverterProvider {

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private CodelistService codelistService;

	@Inject
	@Config(name = ScheduleConfigProperties.CODELIST_PROJECT_DEFINITION, defaultValue = "2")
	private Integer projectDefinitionType;

	@Inject
	private AuthenticationService authenticationService;

	@Inject
	private StateService stateService;

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(ScheduleEntry.class, JSONObject.class,
				new ScheduleEntryToJsonConverter(converter));
		converter.addConverter(JSONObject.class, ScheduleEntry.class,
				new JsonToScheduleEntryConverter(converter));
		converter.addConverter(ScheduleResourceRole.class, JSONObject.class,
				new ScheduleResourceRoleToJsonConverter());

		// converters from JSON to jsonRepresentable
		converter.addConverter(JSONObject.class, ScheduleAssignment.class,
				new JsonToJsonRepresentableConverter<ScheduleAssignment>(ScheduleAssignment.class));
		converter.addConverter(JSONObject.class, ScheduleDependency.class,
				new JsonToJsonRepresentableConverter<ScheduleDependency>(ScheduleDependency.class));
		converter.addConverter(JSONObject.class, EmfUser.class,
				new JsonToJResourceConverter<EmfUser>(EmfUser.class));
		converter.addConverter(JSONObject.class, EmfGroup.class,
				new JsonToJResourceConverter<EmfGroup>(EmfGroup.class));
		converter.addConverter(JSONObject.class, Resource.class,
				new JsonToJResourceConverter<Resource>(Resource.class));

		// converters from JsonRepresentable to JSON
		converter.addConverter(ScheduleDependency.class, JSONObject.class,
				new JsonRepresentativeToJsonConverter<ScheduleDependency>());
		converter.addConverter(ScheduleAssignment.class, JSONObject.class,
				new JsonRepresentativeToJsonConverter<ScheduleAssignment>());
	}

	/**
	 * The Class JsonToJResourceConverter.
	 *
	 * @param <T>
	 *            the generic type
	 * @author BBonev
	 */
	public class JsonToJResourceConverter<T extends Resource> implements Converter<JSONObject, T> {

		/** The target. */
		private final Class<T> target;

		/**
		 * Instantiates a new json to j resource converter.
		 *
		 * @param target
		 *            the target
		 */
		public JsonToJResourceConverter(Class<T> target) {
			this.target = target;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public T convert(JSONObject source) {
			T result = null;
			String id = JsonUtil.getStringValue(source, "Id");
			if (EmfUser.class.equals(target)) {
				EmfUser user = new EmfUser();
				user.setId(id);
				result = (T) user;
			} else if (EmfGroup.class.equals(target)) {
				EmfGroup group = new EmfGroup();
				group.setId(id);
				result = (T) group;
			} else {
				EmfResource resource = new EmfResource();
				resource.setId(id);
				result = (T) resource;
			}
			return result;
		}

	}

	/**
	 * Converter that transforms the given {@link JsonRepresentable} object to a JSON object by
	 * calling the method {@link JsonRepresentable#toJSONObject()}.
	 *
	 * @param <J>
	 *            the concrete type
	 * @author BBonev
	 */
	public class JsonRepresentativeToJsonConverter<J extends JsonRepresentable> implements
			Converter<J, JSONObject> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public JSONObject convert(J source) {
			return source.toJSONObject();
		}
	}

	/**
	 * Converter that transforms the {@link JSONObject} to concrete {@link JsonRepresentable}
	 * implementation. The concrete implementation is instantiated by the given class by reflection
	 * and then is called the method {@link JsonRepresentable#fromJSONObject(JSONObject)} to
	 * initialize the instance.
	 *
	 * @param <J>
	 *            the concrete type
	 * @author BBonev
	 */
	public class JsonToJsonRepresentableConverter<J extends JsonRepresentable> implements
			Converter<JSONObject, J> {

		/** The target class. */
		private final Class<J> targetClass;

		/**
		 * Instantiates a new json to json representable converter.
		 *
		 * @param targetClass
		 *            the target class
		 */
		public JsonToJsonRepresentableConverter(Class<J> targetClass) {
			this.targetClass = targetClass;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public J convert(JSONObject source) {
			J instance = ReflectionUtils.newInstance(targetClass);
			instance.fromJSONObject(source);
			return instance;
		}
	}

	/**
	 * The Class JsonToScheduleEntryConverter. {@link JSONObject} to {@link ScheduleEntry} converter
	 *
	 * @author BBonev
	 */
	public class JsonToScheduleEntryConverter implements Converter<JSONObject, ScheduleEntry> {

		/** The converter. */
		private final TypeConverter converter;

		/**
		 * Instantiates a new json schedule entry converter.
		 *
		 * @param converter
		 *            the converter
		 */
		public JsonToScheduleEntryConverter(TypeConverter converter) {
			this.converter = converter;
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public ScheduleEntry convert(JSONObject source) {
			ScheduleEntry entry = new ScheduleEntry();
			try {

				String string = JsonUtil.getStringValue(source, ScheduleEntryMinifiedProperties.ID);
				if (StringUtils.isNotNullOrEmpty(string)) {
					entry.setId(Long.parseLong(string));
				}
				string = source.getString(ScheduleEntryMinifiedProperties.PARENT_ID);
				if (StringUtils.isNotNullOrEmpty(string) && !string.equals("null")) {
					entry.setParentId(Long.parseLong(string));
				}
				if (source.has(ScheduleEntryMinifiedProperties.LEAF)) {
					entry.setLeaf(Boolean.parseBoolean(source
							.getString(ScheduleEntryMinifiedProperties.LEAF)));
				}
				if (source.has(ScheduleEntryMinifiedProperties.CSS_CLASS)) {
					entry.setCssClass(source.getString(ScheduleEntryMinifiedProperties.CSS_CLASS));
				}
				Map<String, Serializable> properties = entry.getProperties();
				if (source.has(ScheduleEntryMinifiedProperties.SCHEDULE_START_DATE)) {
					properties.put(
							DefaultProperties.PLANNED_START_DATE,
							getDate(source, ScheduleEntryMinifiedProperties.SCHEDULE_START_DATE,
									converter));
				}
				if (source.has(ScheduleEntryMinifiedProperties.SCHEDULE_END_DATE)) {
					properties.put(
							DefaultProperties.PLANNED_END_DATE,
							getDate(source, ScheduleEntryMinifiedProperties.SCHEDULE_END_DATE,
									converter));
				}
				if (source.has(ScheduleEntryMinifiedProperties.DURATION)) {
					properties.put(ScheduleEntryProperties.DURATION, JsonUtil.getDoubleValue(
							source, ScheduleEntryMinifiedProperties.DURATION));
				}
				if (source.has(ScheduleEntryMinifiedProperties.DURATION_UNIT)) {
					properties.put(ScheduleEntryProperties.DURATION_UNIT,
							source.getString(ScheduleEntryMinifiedProperties.DURATION_UNIT));
				}
				if (source.has(ScheduleEntryMinifiedProperties.DESCRIPTION)) {
					properties.put(DefaultProperties.DESCRIPTION,
							source.getString(ScheduleEntryMinifiedProperties.DESCRIPTION));
				}
				if (source.has(ScheduleEntryMinifiedProperties.TITLE)) {
					properties.put(DefaultProperties.TITLE,
							source.getString(ScheduleEntryMinifiedProperties.TITLE));
				}
				if (source.has(ScheduleEntryMinifiedProperties.START_MODE)) {
					properties.put(ScheduleEntryProperties.START_MODE,
							source.getString(ScheduleEntryMinifiedProperties.START_MODE));
				}
				if (source.has(ScheduleEntryMinifiedProperties.HAS_DEPENDENCIES)) {
					properties.put(ScheduleEntryProperties.HAS_DEPENDENCIES, JsonUtil
							.getBooleanValue(source,
									ScheduleEntryMinifiedProperties.HAS_DEPENDENCIES));
				}
				if (source.has(ScheduleEntryProperties.INDEX)) {
					properties.put(ScheduleEntryProperties.INDEX,
							source.getString(ScheduleEntryProperties.INDEX));
				}
				if (source.has(ScheduleEntryMinifiedProperties.COLOR)) {
					properties.put(ScheduleEntryMinifiedProperties.COLOR,
							source.getString(ScheduleEntryMinifiedProperties.COLOR));
				}
				ScheduleEntryUtil.copyBaselineMetadata(properties, entry);
				ScheduleEntryUtil.updateScheduleEntry(entry, properties);
				if (source.has(ScheduleEntryMinifiedProperties.ENTRY_TYPE)) {
					String entityType = source
							.getString(ScheduleEntryMinifiedProperties.ENTRY_TYPE);
					DataTypeDefinition typeDefinition = dictionaryService
							.getDataTypeDefinition(entityType);
					if (typeDefinition != null) {
						String actualId = JsonUtil.getStringValue(source,
								ScheduleEntryMinifiedProperties.ACTUAL_INSTANCE_ID);
						// if (source.has(ScheduleEntryProperties.ACTUAL_INSTANCE_ID)) {
						// actualId = source.getString(ScheduleEntryProperties.ACTUAL_INSTANCE_ID);
						// }
						entry.setInstanceReference(new LinkSourceId(actualId, typeDefinition));
						entry.setActualInstanceClass(converter.convert(Class.class,
								typeDefinition.getJavaClassName()));
						if (StringUtils.isNotNullOrEmpty(actualId)) {
							entry.setActualInstanceId(actualId);
						}
					}
				}
				if (source.has(ScheduleEntryMinifiedProperties.TYPE)) {
					String identifier = source.getString(ScheduleEntryMinifiedProperties.TYPE);
					if (StringUtils.isNotNullOrEmpty(identifier)) {
						entry.setIdentifier(identifier);
					}
				}
				if (source.has(ScheduleEntryMinifiedProperties.UNIQUE_IDENTIFIER)) {
					String id = source.getString(ScheduleEntryMinifiedProperties.UNIQUE_IDENTIFIER);
					if (StringUtils.isNotNullOrEmpty(id)) {
						properties.put(DefaultProperties.UNIQUE_IDENTIFIER, id);
						// probably this should not be copied back because it should not be able to
						// be
						// changed in the web. The property itself is should have immutable
						// behavior.
					}
				}
				if (source.has(ScheduleEntryMinifiedProperties.STATUS)) {
					String status = source.getString(ScheduleEntryMinifiedProperties.STATUS);
					if (StringUtils.isNotNullOrEmpty(status)) {
						if ((entry.getActualInstanceClass() != null)
								&& EqualsHelper.nullSafeEquals(PrimaryStates.SUBMITTED.toString(),
										status, true)) {
							String submitedState = stateService.getState(PrimaryStates.SUBMITTED,
									entry.getActualInstanceClass());
							if (submitedState != null) {
								status = submitedState;
							}
						}
						properties.put(DefaultProperties.STATUS, status);
					}
					// when changing status we should add a proper notification that the
					// status has been changed
					// the status cannot be changed from Schedule any more, so the above comment is
					// invalid
				}
				entry.setPhantomId(JsonUtil.getStringValue(source,
						ScheduleEntryMinifiedProperties.PHANTOM_ID));
				entry.setParentPhantomId(JsonUtil.getStringValue(source,
						ScheduleEntryMinifiedProperties.PHANTOM_PARENT_ID));

				if (source.has(ScheduleEntryMinifiedProperties.CHILDREN)
						&& RuntimeConfiguration
								.isConfigurationSet(RuntimeConfigurationProperties.USE_RECURSIVE_CONVERSION)) {
					Object object = JsonUtil.getValueOrNull(source,
							ScheduleEntryMinifiedProperties.CHILDREN);
					if (object instanceof JSONArray) {
						JSONArray children = (JSONArray) object;
						for (int i = 0; i < children.length(); i++) {
							JSONObject jsonObject = children.getJSONObject(i);

							ScheduleEntry scheduleEntry = converter.convert(ScheduleEntry.class,
									jsonObject);
							entry.getChildren().add(scheduleEntry);
						}
					}
				}
				// TODO: add copying of other properties
			} catch (JSONException e) {
				// REVIEW: probably good idea to throw an exception
				e.printStackTrace();
			}
			return entry;
		}
	}

	/**
	 * Gets the date.
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param converter
	 *            the converter
	 * @return the date
	 */
	Date getDate(JSONObject object, String key, TypeConverter converter) {
		String value = JsonUtil.getStringValue(object, key);

		// CMF-5319: updated the time zone check implementation
		if ((value != null) && (value.length() > 0)) {
			// if not a Zulu time then check for missing type zone and add the current time zone
			// offset
			if (!value.endsWith("Z")) {
				char timezoneIdentificator = value.charAt(value.length() - 6);
				if ((timezoneIdentificator != '+') && (timezoneIdentificator != '-')) {
					int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
					value += ISO8601DateFormat.getTimeZonePadding(offset);
				}
			}
		} else {
			return null;
		}
		return converter.convert(Date.class, value);

	}

	/**
	 * The Class ScheduleEntryToJsonConverter. {@link ScheduleEntry} to {@link JSONObject} converter
	 *
	 * @author BBonev
	 */
	public class ScheduleEntryToJsonConverter implements Converter<ScheduleEntry, JSONObject> {

		public static final String CHILDREN = ScheduleEntryProperties.CHILDREN;

		private final TypeConverter converter;

		/**
		 * Instantiates a new schedule entry to json converter.
		 *
		 * @param converter
		 *            the converter
		 */
		public ScheduleEntryToJsonConverter(TypeConverter converter) {
			this.converter = converter;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public JSONObject convert(ScheduleEntry source) {
			/*
			 * "Id":"14", "parentId":"null", "leaf":"false", "Name":"T1",
			 * "StartDate":"2012-06-27T00:00:00", "EndDate":"2012-06-29T00:00:00", "Duration":"2",
			 * "DurationUnit":"d",
			 */
			JSONObject object = new JSONObject();
			JsonUtil.addToJson(object, ScheduleEntryMinifiedProperties.ID, source.getId());
			JsonUtil.addToJson(object, ScheduleEntryMinifiedProperties.PARENT_ID,
					"" + source.getParentId());
			JsonUtil.addToJson(object, ScheduleEntryMinifiedProperties.LEAF, "" + source.isLeaf());
			JsonUtil.addToJson(object, ScheduleEntryMinifiedProperties.CSS_CLASS,
					StringUtils.isNullOrEmpty(source.getCssClass()) ? "" : source.getCssClass());
			Map<String, Serializable> properties = source.getProperties();

			JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.TITLE, properties,
					DefaultProperties.TITLE);

			JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.SCHEDULE_START_DATE,
					properties, ScheduleEntryProperties.PLANNED_START_DATE, String.class);
			JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.SCHEDULE_END_DATE,
					properties, ScheduleEntryProperties.PLANNED_END_DATE, String.class);
			JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.BASELINE_START_DATE,
					properties, ScheduleEntryProperties.BASELINE_START_DATE, String.class);
			JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.BASELINE_END_DATE,
					properties, ScheduleEntryProperties.BASELINE_END_DATE, String.class);
			JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.START_MODE, properties,
					ScheduleEntryProperties.START_MODE, String.class);
			JsonUtil.copyToJson(object, ScheduleEntryProperties.INDEX, properties,
					ScheduleEntryProperties.INDEX, Integer.class);
			JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.COLOR, properties,
					ScheduleEntryMinifiedProperties.COLOR, String.class);

			Date startDate = (Date) properties.get(ScheduleEntryProperties.PLANNED_START_DATE);
			Date endDate = (Date) properties.get(ScheduleEntryProperties.PLANNED_END_DATE);
			if ((startDate != null) && (endDate != null)) {
				JsonUtil.addToJson(object, ScheduleEntryMinifiedProperties.DURATION,
						DateUtil.daysBetween(startDate, endDate, true));
			}

			JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.DURATION_UNIT, properties,
					ScheduleEntryProperties.DURATION_UNIT);
			JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.DESCRIPTION, properties,
					DefaultProperties.DESCRIPTION);
			JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.BASELINE_PERCENT_DONE,
					properties, ScheduleEntryProperties.BASELINE_PERCENT_DONE);

			String identifier = source.getIdentifier();
			boolean hasDefintionId = StringUtils.isNotNullOrEmpty(identifier);
			if (hasDefintionId) {
				JsonUtil.addToJson(object, ScheduleEntryMinifiedProperties.TYPE, identifier);
				String label = codelistService.getDescription(projectDefinitionType, identifier,
						SecurityContextManager.getCurrentUser(authenticationService).getLanguage());
				JsonUtil.addToJson(object, ScheduleEntryMinifiedProperties.TYPE + "_label", label);
			}
			InstanceReference instanceReference = source.getInstanceReference();
			if (instanceReference != null) {
				DataTypeDefinition referenceType = instanceReference.getReferenceType();
				if (referenceType != null) {
					JsonUtil.addToJson(object, ScheduleEntryMinifiedProperties.ENTRY_TYPE,
							referenceType.getName());
					JsonUtil.addToJson(object, ScheduleEntryMinifiedProperties.ENTRY_TYPE
							+ "_label", referenceType.getTitle());
				}
				if (StringUtils.isNotNullOrEmpty(instanceReference.getIdentifier())) {
					JsonUtil.addToJson(object, ScheduleEntryMinifiedProperties.ACTUAL_INSTANCE_ID,
							instanceReference.getIdentifier());
				}
			}

			addDefinitionType(identifier, source.getActualInstance(), object);

			if (properties.containsKey(DefaultProperties.UNIQUE_IDENTIFIER)) {
				JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.UNIQUE_IDENTIFIER,
						properties, DefaultProperties.UNIQUE_IDENTIFIER);
			}
			if (properties.containsKey(DefaultProperties.STATUS)) {
				JsonUtil.copyToJson(object, ScheduleEntryMinifiedProperties.STATUS, properties,
						DefaultProperties.STATUS);
			}

			if (!source.getChildren().isEmpty()
					&& RuntimeConfiguration
							.isConfigurationSet(RuntimeConfigurationProperties.USE_RECURSIVE_CONVERSION)) {
				for (ScheduleEntry entry : source.getChildren()) {
					JSONObject jsonObject = converter.convert(JSONObject.class, entry);
					JsonUtil.append(object, ScheduleEntryMinifiedProperties.CHILDREN, jsonObject);
				}
			}

			// TODO: add sending other properties
			return object;
		}

		/**
		 * Update entry title.
		 *
		 * @param identifier
		 *            the identifier
		 * @param instance
		 *            the instance reference
		 * @param object
		 *            the object
		 */
		private void addDefinitionType(String identifier, Instance instance, JSONObject object) {
			if ((instance != null) && StringUtils.isNotNullOrEmpty(identifier)) {
				String language = SecurityContextManager.getCurrentUser(authenticationService).getLanguage();
				String definitionId = identifier;
				DefinitionModel instanceDefinition = dictionaryService
						.getInstanceDefinition(instance);
				PathElement rootElement = PathHelper
						.getRootElement((PathElement) instanceDefinition);

				PropertyDefinition property = PathHelper.findProperty(
						(DefinitionModel) rootElement, (PathElement) instanceDefinition,
						DefaultProperties.TYPE);
				if (property != null) {
					// for workflows we have something like this 'activiti$WFTYPE999', so we need to
					// extract the 'WFTYPE999' part
					if (definitionId.contains("$")) {
						String[] split = definitionId.split("\\$");
						String clValue = null;
						if (split.length == 2) {
							clValue = split[1];
						}
						if (clValue != null) {
							definitionId = clValue;
						}
					}
					Integer codelist = property.getCodelist();
					String type = codelistService.getDescription(codelist, definitionId, language);
					if (StringUtils.isNotNullOrEmpty(type)) {
						JsonUtil.addToJson(object, ScheduleEntryMinifiedProperties.DEFINITION_TYPE,
								type);
					}
				}
			}
		}
	}

	/**
	 * The Class ScheduleResourceRoleToJsonConverter. Converts {@link ScheduleResourceRole} to
	 * {@link JSONObject}
	 */
	public class ScheduleResourceRoleToJsonConverter implements
			Converter<ScheduleResourceRole, JSONObject> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public JSONObject convert(ScheduleResourceRole schResourceRole) {
			ResourceRole source = schResourceRole.getResourceRole();
			JSONObject object = new JSONObject();
			Resource resource = source.getResource();

			JsonUtil.addToJson(object, "Id", resource.getId());
			JsonUtil.addToJson(object, "Name",
					StringUtils.isNullOrEmpty(resource.getDisplayName()) ? resource.getIdentifier()
							: resource.getDisplayName());
			if (source.getRole() != null) {
				JsonUtil.addToJson(object, "Role", source.getRole().getIdentifier());
			}
			if (resource instanceof User) {
				Serializable jobTitle = resource.getProperties().get(ResourceProperties.JOB_TITLE);
				if ((jobTitle != null) && !jobTitle.equals("null")) {
					JsonUtil.addToJson(object, "JobTitle", jobTitle);
				}
			}

			return object;
		}
	}

}
