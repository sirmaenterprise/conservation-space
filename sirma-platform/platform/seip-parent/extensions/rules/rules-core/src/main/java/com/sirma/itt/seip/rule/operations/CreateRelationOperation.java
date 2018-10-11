package com.sirma.itt.seip.rule.operations;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
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

	static final String RELATION_ID = "relationId";

	private String relationId;
	private String eventId = null;

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
	 * @param configuration
	 *            the configuration
	 * @return the creates the relation
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			isDisabled = true;
			return false;
		}
		String mainRelation = configuration.getIfSameType(RELATION_ID, String.class);
		if (StringUtils.isEmpty(mainRelation)) {
			LOGGER.warn(
					"{} will be disabled because no properties to match for are found in the configuration. Configuration is {}",
					NAME, configuration);
			isDisabled = true;
			return false;
		}
		isDisabled = false;
		eventId = configuration.getIfSameType(EVENT_ID, String.class);
		relationId = mainRelation;
		return true;
	}

	@Override
	public void execute(Context<String, Object> context, Instance matchedInstance,
			Context<String, Object> processingContext) {
		if (matchedInstance == null) {
			return;
		}
		Instance processedInstance = getProcessedInstance(context);
		processedInstance.append(relationId, matchedInstance.getId());
		if (eventId != null) {
			eventService
					.fire(new RelationCreateEvent(processedInstance.getIdentifier(), matchedInstance.getIdentifier(),
							relationId, eventId));
		}
	}

	@Override
	public boolean isApplicable(Context<String, Object> context) {
		return !isDisabled && super.isApplicable(context);
	}

	@Override
	public String getName() {
		return NAME;
	}
}
