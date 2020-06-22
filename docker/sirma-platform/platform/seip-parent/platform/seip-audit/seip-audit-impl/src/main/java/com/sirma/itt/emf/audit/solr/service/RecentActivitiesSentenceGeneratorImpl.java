package com.sirma.itt.emf.audit.solr.service;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_BREADCRUMB;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.emf.audit.processor.RecentActivity;
import com.sirma.itt.emf.audit.processor.StoredAuditActivity;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Generates sentences from recent activities.
 *
 * @author nvelkov
 */
public class RecentActivitiesSentenceGeneratorImpl implements RecentActivitiesSentenceGenerator {

	private static final String RECENT_ACTIVITIES_DEFAULT_SENTENCE_LABEL_ID = "recent.activities.default.sentence";

	private static final Instance NO_INSTANCE = new EmfInstance();

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@Inject
	private InstanceService objectService;

	@Inject
	private AuditConfiguration auditConfiguration;

	@Inject
	private CodelistService codeListService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private UserPreferences userPreferences;

	@Override
	public List<RecentActivity> generateSentences(List<StoredAuditActivity> activities) {
		Map<Serializable, Instance> instanceIdtoInstanceMapping = getInstanceIdToInstanceMapping(activities);
		List<RecentActivity> recentActivities = new ArrayList<>(activities.size());
		for (StoredAuditActivity activity : activities) {
			String sentence = generateSentence(instanceIdtoInstanceMapping, activity);
			Instance user = instanceIdtoInstanceMapping.get(activity.getUserId());
			recentActivities.add(new RecentActivity(user, activity.getTimestamp(), sentence));
		}
		return recentActivities;
	}

	private Map<Serializable, Instance> getInstanceIdToInstanceMapping(List<StoredAuditActivity> activities) {
		Set<Serializable> ids = new HashSet<>();
		for (StoredAuditActivity activity : activities) {
			ids.add(activity.getUserId());
			CollectionUtils.addNonNullValue(ids, activity.getInstanceId());
			ids.addAll(activity.getAddedTargetProperties());
			ids.addAll(activity.getRemovedTargetProperties());
		}

		Function<List<Serializable>, List<Instance>> function = objectService::loadByDbId;
		Collection<Instance> instances = Options.ALLOW_LOADING_OF_DELETED_INSTANCES
				.wrapFunction(function)
					.apply(new ArrayList<>(ids));

		instanceLoadDecorator.decorateResult(instances);
		return instances.stream().collect(Collectors.toMap(Instance::getId, Function.identity()));
	}

	private String generateSentence(Map<Serializable, Instance> instanceIdToHeaderMapping,
			StoredAuditActivity activity) {
		String label = null;
		if (auditConfiguration.getRecentActivitiesLabelPrefix().isSet()) {
			String action = auditConfiguration.getRecentActivitiesLabelPrefix().get() + activity.getAction();
			label = labelProvider.getLabel(action);
			if (label.equals(action)) {
				label = labelProvider.getValue(RECENT_ACTIVITIES_DEFAULT_SENTENCE_LABEL_ID);
			}
		} else {
			label = labelProvider.getValue(RECENT_ACTIVITIES_DEFAULT_SENTENCE_LABEL_ID);
		}
		String addedSubjects = getSubjectHeaders(instanceIdToHeaderMapping, activity.getAddedTargetProperties());
		String removedSubjects = getSubjectHeaders(instanceIdToHeaderMapping, activity.getRemovedTargetProperties());

		Instance instance = instanceIdToHeaderMapping.get(activity.getInstanceId());
		String state = "";
		if (instance != null) {
			state = getStateLabel(instance.getIdentifier(), activity.getState());
		}
		String subject = StringUtils.isBlank(addedSubjects) ? removedSubjects : addedSubjects;

		String object = getHeader(instanceIdToHeaderMapping, activity.getInstanceId());
		String user = getHeader(instanceIdToHeaderMapping, activity.getUserId());

		return label
				.replaceAll("\\{object\\}", object)
					.replaceAll("\\{subject\\}", subject)
					.replaceAll("\\{addedSubjects\\}", addedSubjects)
					.replaceAll("\\{removedSubjects\\}", removedSubjects)
					.replaceAll("\\{User\\}", user)
					.replaceAll("\\{state\\}", state)
					.trim();
	}

	/**
	 * Retrieve the state label from the state code and definition id. If no state is found, '(Unknown state)' is going
	 * to be returned.
	 *
	 * @param definitionId
	 *            the definitionId
	 * @param state
	 *            the state code
	 * @return the state of the instance
	 */
	private String getStateLabel(String definitionId, String state) {
		return definitionService.find(definitionId)
				.getField(DefaultProperties.STATUS)
				.filter(property2 -> property2.getCodelist() != null)
				.map(property -> codeListService.getCodeValue(property.getCodelist(), state))
				.filter(Objects::nonNull)
				.map(value -> value.getProperties().get(userPreferences.getLanguage()))
				.orElse("(Unknown state)")
				.toString();
	}

	private static String getSubjectHeaders(Map<Serializable, Instance> instanceIdToHeaderMapping,
			Collection<Serializable> ids) {
		return ids.stream().map(id -> getHeader(instanceIdToHeaderMapping, id)).collect(Collectors.joining(", "));
	}

	private static String getHeader(Map<Serializable, Instance> instanceIdToHeaderMapping, Serializable id) {
		return instanceIdToHeaderMapping.getOrDefault(id, NO_INSTANCE)
				.getString(HEADER_BREADCRUMB, "(Unknown instance)").trim();
	}
}
