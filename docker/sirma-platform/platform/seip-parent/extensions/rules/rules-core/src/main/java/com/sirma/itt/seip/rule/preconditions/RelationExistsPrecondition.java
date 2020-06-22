package com.sirma.itt.seip.rule.preconditions;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.rule.BaseDynamicInstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.RulePrecondition;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.rule.model.RelationDirection;
import com.sirma.itt.seip.search.SearchService;

/**
 * Check for existing relations of simple or complex to objects of type identified by uri or instance type, in direction
 * of outgoing or incoming.
 *
 * @author BBonev
 */
@Named(RelationExistsPrecondition.RELATION_EXISTS_NAME)
public class RelationExistsPrecondition extends BaseDynamicInstanceRule implements RulePrecondition {

	public static final String DIRECTION = "direction";
	public static final String OTHER_TYPE = "otherType";
	public static final String SIMPLE_ONLY = "simpleOnly";
	public static final String RELATION_ID = "relationId";

	public static final String RELATION_EXISTS_NAME = "relationExists";

	private static final String QUERY_START = "SELECT (COUNT(distinct ?instance) as ?count) WHERE { ";
	private static final String IS_NOT_DELETED = " ?instance emf:isDeleted \"false\"^^xsd:boolean. ";
	private static final String RELATION_START = "?relation a emf:Relation; emf:relationType ";

	private String relationId;
	private boolean simpleOnly;
	private String otherType;
	private RelationDirection relationDirection;

	@Inject
	private SearchService searchService;

	@Override
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			return false;
		}

		relationId = configuration.getIfSameType(RELATION_ID, String.class);
		simpleOnly = configuration.getIfSameType(SIMPLE_ONLY, Boolean.class, Boolean.FALSE);
		otherType = configuration.getIfSameType(OTHER_TYPE, String.class);
		if (StringUtils.isNotBlank(otherType) && otherType.startsWith("http")) {
			otherType = "<" + otherType + ">";
		}
		relationDirection = RelationDirection
				.parse(configuration.getIfSameType(DIRECTION, String.class, RelationDirection.OUTGOING.toString()));
		return StringUtils.isNotBlank(relationId) && relationId.indexOf(':') >= 0;
	}

	@Override
	public String getName() {
		return RELATION_EXISTS_NAME;
	}

	@Override
	public String getPrimaryOperation() {
		return RELATION_EXISTS_NAME;
	}

	@Override
	public boolean isAsyncSupported() {
		return true;
	}

	@Override
	public boolean checkPreconditions(RuleContext processingContext) {
		Instance instance = processingContext.getTriggerInstance();
		String query = buildCountQuery(instance);
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setCountOnly(true);
		arguments.setDialect(SearchDialects.SPARQL);
		arguments.setStringQuery(query);
		searchService.search(Instance.class, arguments);
		return arguments.getTotalItems() > 0;
	}

	private String buildCountQuery(Instance instance) {
		StringBuilder builder = new StringBuilder(256);
		builder.append(QUERY_START);

		Object source = instance.getId();
		Object destination = "?instance";

		if (relationDirection == RelationDirection.INGOING) {
			destination = instance.getId();
			source = "?instance";
		}

		if (simpleOnly) {
			builder.append(source).append(" ").append(relationId).append(" ").append(destination).append(".");
		} else {
			builder.append(RELATION_START).append(relationId).append(";");
			builder.append(" emf:isActive \"true\"^^xsd:boolean;");
			builder.append(" emf:source ").append(source).append(";");
			builder.append(" emf:destination ").append(destination).append(".");
		}
		builder.append(IS_NOT_DELETED);
		addTypeFilter(builder);
		builder.append("}");
		return builder.toString();
	}

	private void addTypeFilter(StringBuilder builder) {
		if (StringUtils.isBlank(otherType)) {
			return;
		}
		if (otherType.indexOf(':') >= 0) {
			builder.append(" ?instance a ").append(otherType).append(".");
		} else {
			builder.append(" ?instance a ?type. ?type emf:definitionId ").append(otherType).append(".");
		}
	}
}
