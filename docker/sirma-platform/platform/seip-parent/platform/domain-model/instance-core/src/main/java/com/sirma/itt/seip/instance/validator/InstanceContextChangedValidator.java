package com.sirma.itt.seip.instance.validator;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Checks if the context(parent) of instance is changed. When it is, the validator will check, if that instance is
 * allowed to be child of the newly given context. If the context is unchanged or the new context is not found, the
 * validator will do nothing.
 *
 * @author Boyan Tonchev.
 */
@Extension(target = Validator.TARGET_NAME, order = 2)
public class InstanceContextChangedValidator implements Validator {

	private static final String DESTINATION_PERMISSION_RESTRICTION_ERROR = "validation.error.move.object.destination.permission";
	private static final String ALLOW_CHILDREN_ERROR = "validation.error.move.object.allow.children";

	@Inject
	private InstanceContextService instanceContextService;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private InstanceService instanceService;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private InstancePropertyNameResolver fieldConverter;

	@Override
	public void validate(ValidationContext validationContext) {
		Instance instance = validationContext.getInstance();
		if (!instanceContextService.isContextChanged(instance)) {
			return;
		}

		String newParentId = instance.getAsString(InstanceContextService.HAS_PARENT, fieldConverter);
		if (isBlank(newParentId)) {
			return;
		}

		Instance newParent = domainInstanceService.loadInstance(newParentId);
		if (!newParent.isWriteAllowed()) {
			validationContext.addErrorMessage(labelProvider.getValue(DESTINATION_PERMISSION_RESTRICTION_ERROR));
		}

		if (!isChildAllowed(instance.getIdentifier(), newParent)) {
			validationContext.addErrorMessage(labelProvider.getValue(ALLOW_CHILDREN_ERROR));
		}
	}

	/**
	 * Checks if given destination could be parent of given object. The checks is done by checking the definition
	 * configuration for the destination object, if it is empty (don't contain allowed children configuration, which
	 * means that it allows children of any type) the method will return <code>true</code>, if there is configured
	 * allowed children section, it will check, if the child type matches any of the configured.
	 */
	private boolean isChildAllowed(String childType, Instance destination) {
		Collection<List<DefinitionModel>> allowedChildrenModels = instanceService
				.getAllowedChildren(destination).values();

		// if in definition of destination have not tag allowedChildren instanceService#getAllowedChildren will return
		// empty list, which means there is not constraint for children type.
		if (allowedChildrenModels.isEmpty()) {
			return true;
		}

		return allowedChildrenModels.stream()
					.flatMap(List::stream)
					.anyMatch(model -> model.getIdentifier().equals(childType));
	}
}