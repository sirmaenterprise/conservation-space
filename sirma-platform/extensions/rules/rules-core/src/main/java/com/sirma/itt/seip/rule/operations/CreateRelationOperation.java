package com.sirma.itt.seip.rule.operations;

import java.io.Serializable;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.relation.RelationCreateEvent;

/**
 * Rule operation that can create relations between the current instance and the matched instance. The operation could
 * be configured to create simple (object property) relation or complex.
 *
 * @author BBonev
 */
@Named(CreateRelationOperation.NAME)
public class CreateRelationOperation extends BaseRuleOperation {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateRelationOperation.class);

	public static final String NAME = "createRelation";

	public static final String RELATION_ID = "relationId";
	public static final String REVERSE_RELATION_ID = "reverseRelationId";
	public static final String SIMPLE_ONLY = "simpleOnly";
	public static final String PROPERTIES = "properties";

	private String relationId;
	private String reverseRelationId;
	private boolean simpleOnly = false;
	private String eventId = null;

	private Map<String, Serializable> properties;

	@Inject
	private LinkService linkService;

	@Inject
	private EventService eventService;

	private boolean isDisabled = false;

	@Override
	public String getPrimaryOperation() {
		return NAME;
	}

	/**
	 * Parses the.
	 *
	 * @param map
	 *            the map
	 * @return the creates the relation
	 */
	@Override
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			isDisabled = true;
			return false;
		}
		String mainRelation = configuration.getIfSameType(RELATION_ID, String.class);
		String reverseId = configuration.getIfSameType(REVERSE_RELATION_ID, String.class);
		if (StringUtils.isEmpty(mainRelation) && StringUtils.isEmpty(reverseId)) {
			LOGGER.warn(
					"{} will be disabled because no properties to match for are found in the configuration. Configuration is {}",
					NAME, configuration);
			isDisabled = true;
			return false;
		}
		isDisabled = false;
		eventId = configuration.getIfSameType(EVENT_ID, String.class);
		relationId = mainRelation;
		reverseRelationId = reverseId;
		if (configuration.containsKey(PROPERTIES)) {
			Map<String, Serializable> map = configuration.getIfSameType(PROPERTIES, Map.class);
			if (!map.isEmpty()) {
				properties = map;
			} else {
				properties = LinkConstants.getDefaultSystemProperties();
			}
		}
		if (configuration.containsKey(SIMPLE_ONLY)) {
			simpleOnly = configuration.getIfSameType(SIMPLE_ONLY, Boolean.class, Boolean.FALSE);
		}
		return true;
	}

	@Override
	public void execute(Context<String, Object> context, Instance matchedInstance,
			Context<String, Object> processingContext) {
		Instance processedInstance = getProcessedInstance(context);
		Pair<Serializable, Serializable> link = Pair.NULL_PAIR;
		if (simpleOnly) {
			linkService.linkSimple(processedInstance.toReference(), matchedInstance.toReference(), relationId,
					reverseRelationId);
		} else {
			link = linkService.link(processedInstance, matchedInstance, relationId, reverseRelationId, properties);
		}
		if (eventId != null) {
			eventService
					.fire(new RelationCreateEvent(processedInstance.getIdentifier(), matchedInstance.getIdentifier(),
							relationId, eventId));
		}
	}

	@Override
	public boolean isApplicable(Context<String, Object> context) {
		if (isDisabled) {
			return false;
		}
		return super.isApplicable(context);
	}

	@Override
	public String getName() {
		return NAME;
	}
}