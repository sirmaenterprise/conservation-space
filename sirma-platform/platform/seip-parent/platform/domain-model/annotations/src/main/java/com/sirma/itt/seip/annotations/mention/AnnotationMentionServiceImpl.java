package com.sirma.itt.seip.annotations.mention;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailNotificationService;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Annotation mention service implementation.
 *
 * @author tdossev
 */
public class AnnotationMentionServiceImpl implements AnnotationMentionService {

	private static final String MENTIONED_USERS = EMF.MENTIONED_USERS.getLocalName();
	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationMentionServiceImpl.class);

	/**
	 * Query that fetches all mentioned users for annotation id
	 * <p>
	 *
	 * <pre>
	 * SELECT ?instanceType ?mentionedUsers WHERE  {
	 *   	emf:7f6bacc3-dc5e-43ad-8160-908a07119656 emf:mentionedUsers ?mentionedUsers;
	 *         emf:instanceType ?instanceType .
	 * }
	 * </pre>
	 */
	private static final String LOAD_ANNOTATION_PROPERTIES = ResourceLoadUtil
			.loadResource(AnnotationMentionServiceImpl.class, "LoadMentionedUsers.sparql");

	@Inject
	private MailNotificationService mailNotificationService;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private InstanceTypeResolver typeResolver;

	@Inject
	private SearchService searchService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private Statistics statistics;

	@Inject
	private ResourceService resourceService;

	@Override
	public void sendNotifications(Collection<Serializable> mentionedUsers, String commentedInstanceId,
			Serializable commentsOn, String commentedBy) {

		Instance commentedInstance = getInstance(commentedInstanceId);
		if (commentedInstance == null) {
			LOGGER.warn("Could not load the instance ({}) on which a comment had occurred. "
							+ "Probably deleted or not created, yet", commentedInstanceId);
			return;
		}
		Instance commenterInstance = getInstance(commentedBy);
		if (commenterInstance == null) {
			LOGGER.warn("Could not find the commenter: {}. Probably deleted", commentedBy);
			return;
		}
		String commentsOnTabId = getCommentOnTabId(commentsOn);

		for(Serializable user : mentionedUsers){
			Resource resource = resourceService.findResource(user);
			if (resource != null && resource.isActive() && resource.getEmail() != null) {
				MailNotificationContext context = new AnnotationMentionNotificationContext(resource, commentedInstance,
						commenterInstance, commentsOnTabId, systemConfiguration.getApplicationName().get(),
						systemConfiguration.getUi2Url().get());
				mailNotificationService.sendEmail(context);
			}
		}

	}

	@SuppressWarnings("boxing")
	@Override
	public Collection<Serializable> loadMentionedUsers(String annotationId) {
		LOGGER.debug("Executing search for mentioned users for annotation id: {}", annotationId);
		TimeTracker tracker = statistics.createTimeStatistics(getClass(), "semanticSearch").begin();

		SearchArguments<Annotation> arguments = new SearchArguments<>();
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		arguments.setDialect(SearchDialects.SPARQL);
		arguments.setStringQuery(
				String.format(LOAD_ANNOTATION_PROPERTIES, namespaceRegistryService.getShortUri(annotationId)));
		arguments.setFaceted(false);
		arguments.setPageSize(0);

		try {
			searchService.search(Annotation.class, arguments);

			Set<Serializable> mentioned = new HashSet<>();
			for (Annotation annotation : arguments.getResult()) {
				mentioned.add(annotation.getProperties().get(MENTIONED_USERS));
			}
			return mentioned;
		} catch (Exception e) {
			LOGGER.error("Error executing search for mentioned users for annotation id: {}", annotationId,
					e.getMessage(), e);
			return Collections.emptyList();
		} finally {
			LOGGER.debug("Annotation search took {} s", tracker.stopInSeconds());
		}
	}

	/**
	 * Instance getter.
	 *
	 * @param instanceId
	 *            id of the instance
	 * @return instance
	 */
	private Instance getInstance(String instanceId) {
		return typeResolver.resolveReference(instanceId).map(InstanceReference::toInstance).orElse(null);
	}

	/**
	 * Substrings commentsOn Id
	 *
	 * @param commentsOn
	 *            Id
	 * @return short Id
	 */
	private static String getCommentOnTabId(Serializable commentsOn) {
		if (commentsOn == null) {
			return StringUtils.EMPTY;
		}
		return commentsOn.toString().substring(commentsOn.toString().indexOf('#') + 1, commentsOn.toString().length());
	}

}
