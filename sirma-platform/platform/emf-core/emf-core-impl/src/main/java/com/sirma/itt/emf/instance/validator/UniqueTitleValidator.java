package com.sirma.itt.emf.instance.validator;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.inject.Inject;

import org.openrdf.model.vocabulary.DCTERMS;

import com.sirma.itt.emf.instance.UniquePropertiesService;
import com.sirma.itt.seip.MessageType;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Validates instances (tags & saved filters) when they are being created or updated. The validity rule is that they
 * must have unique titles.
 *
 * @author nvelkov
 */
@Extension(target = Validator.TARGET_NAME, order = 0)
public class UniqueTitleValidator implements Validator {

	private static final String INSTANCE_VALIDATION_TITLE = "instance.validation.title";

	private static final String TITLE = DCTERMS.PREFIX + ":" + DCTERMS.TITLE.getLocalName();
	private static final String CREATED_BY = EMF.PREFIX + ":" + EMF.CREATED_BY.getLocalName();
	private static final String SAVED_SEARCH = EMF.SAVED_SEARCH.getLocalName();
	private static final String SAVED_SEARCH_RDFTYPE = EMF.SAVED_SEARCH.toString();

	private static final String TAG = EMF.SAVED_SEARCH.getLocalName();
	private static final String TAG_RDFTYPE = EMF.TAG.toString();

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private UniquePropertiesService uniquePropertiesService;

	@Override
	public void validate(ValidationContext validationContext) {
		Instance instance = validationContext.getInstance();
		String instanceRdfType = instance.getAsString(SEMANTIC_TYPE);

		if (nullSafeEquals(instanceRdfType, TAG_RDFTYPE) || nullSafeEquals(instanceRdfType, SAVED_SEARCH_RDFTYPE)) {
			String instanceType = null;
			if (nullSafeEquals(instanceRdfType, TAG_RDFTYPE)) {
				instanceType = TAG;
			} else if (nullSafeEquals(instanceRdfType, SAVED_SEARCH_RDFTYPE)) {
				instanceType = SAVED_SEARCH;
			}
			Map<String, Serializable> properties = CollectionUtils.createHashMap(2);
			// Escape the : symbol because the semantic query visitor will think it's an uri -
			// SemanticQueryVisitor#buildStringSearchClause
			String escapedTitle = instance.getProperties().get(DefaultProperties.TITLE).toString().replaceAll(":",
					"\\\\:");
			properties.put(TITLE, "^\\Q" + escapedTitle + "\\E$");
			properties.put(CREATED_BY, instance.get(DefaultProperties.CREATED_BY));
			properties.put(DefaultProperties.SEMANTIC_TYPE, instanceRdfType);

			if (uniquePropertiesService.objectExists(properties, instance.getId().toString())) {
				String rawMessage = labelProvider.getValue(INSTANCE_VALIDATION_TITLE);
				String message = MessageFormat.format(rawMessage, instanceType,
						instance.getProperties().get(DefaultProperties.TITLE));
				validationContext.addMessage(MessageType.ERROR, message);
			}
		}
	}

}
