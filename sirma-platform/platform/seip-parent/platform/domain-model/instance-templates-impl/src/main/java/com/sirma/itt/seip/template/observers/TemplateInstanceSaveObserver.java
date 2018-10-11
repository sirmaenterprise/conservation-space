package com.sirma.itt.seip.template.observers;

import static com.sirma.itt.seip.domain.ObjectTypes.TEMPLATE;
import static com.sirma.itt.seip.template.TemplateProperties.FOR_OBJECT_TYPE;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.save.event.BeforeInstanceSaveEvent;
import com.sirma.itt.seip.template.TemplateProperties;

/**
 * Observers save events of instances of type "template" and performs enrichment operations: <br/>
 * - Gets the value of the 'forObjectType' property, fetches its label and stores it the 'forObjectTypeLabel'
 * field.<br/>
 *
 * @author Adrian Mitev
 */
@Singleton
public class TemplateInstanceSaveObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DefinitionService definitionService;

	@Inject
	private CodelistService codelistService;

	void onInstanceSave(@Observes BeforeInstanceSaveEvent event) {
		Instance savedInstance = event.getInstanceToSave();
		if (savedInstance.type().is(TEMPLATE)) {
			// The field is initialized on each template change because the definition label may have been changed
			setForObjectTypeLabel(savedInstance);
		}
	}

	/**
	 * Sets the 'forObjectTypeLabel' property of an instance by fetching it from the definition label.
	 *
	 * @param templateInstance
	 *            template instance to update.
	 */
	public void setForObjectTypeLabel(Instance templateInstance) {
		String forType = templateInstance.getAsString(FOR_OBJECT_TYPE);
		String forTypeLabel = getDefinitionLabel(forType);
		templateInstance.add(TemplateProperties.FOR_OBJECT_TYPE_LABEL, forTypeLabel);
	}

	private String getDefinitionLabel(String definitionId) {
		DefinitionModel definition = definitionService.find(definitionId);

		Optional<PropertyDefinition> typeFiledOptional = definition.getField(DefaultProperties.TYPE);

		String label = typeFiledOptional.map(property -> {
			Integer codelist = property.getCodelist();

			if (codelist != null && property.getDefaultValue() != null) {
				return codelistService.getDescription(codelist, property.getDefaultValue());
			}

			return null;
		}).orElse(null);

		if (label == null) {
			LOGGER.warn("Label cannot be fetched for definition '{}'", definitionId);
		}

		return label;
	}
}
