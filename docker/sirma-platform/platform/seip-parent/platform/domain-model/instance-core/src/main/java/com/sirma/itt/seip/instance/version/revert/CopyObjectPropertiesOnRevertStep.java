package com.sirma.itt.seip.instance.version.revert;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Handles the transferring of the object properties from the current instance to the result instance. By requirement
 * all of the object properties that the version contains should be removed and the ones that the current instance
 * contains should be copied to the result instance.<br>
 * The step first will get all of the properties that are object from the definition model. Then removed all from the
 * result instance and then set the ones extracted from the current to as the properties form the result instance.<br>
 * If there are no object properties described in the definition model this step will do nothing.
 *
 * @author A. Kunchev
 */
@Extension(target = RevertStep.EXTENSION_NAME, enabled = true, order = 20)
public class CopyObjectPropertiesOnRevertStep implements RevertStep {

	private static final Set<String> SKIP_PROPERTIES = Stream.of(LinkConstants.HAS_TEMPLATE, DefaultProperties.SEMANTIC_TYPE).collect(
			Collectors.toSet());

	@Inject
	private DefinitionService definitionService;

	@Inject
	private InstancePropertyNameResolver fieldConverter;

	@Inject
	private InstanceService instanceService;

	@Override
	public String getName() {
		return "copyObjectProperties";
	}

	@Override
	public void invoke(RevertContext context) {
		Instance currentInstance = context.getCurrentInstance();
		Instance revertResultInstance = context.getRevertResultInstance();

		// process template revert in case of type change the template will be reverted
		// otherwise it will be kept the same
		revertTemplateIfNecessary(currentInstance, revertResultInstance);

		definitionService.getInstanceObjectProperties(currentInstance)
				.filter(systemPropertiesThatShouldNotBeCopied())
				.map(PropertyDefinition::getName)
				// remove all object properties from result instance by requirement
				.peek(revertResultInstance::remove)
				.filter(currentInstance::isValueNotNull)
				// transfer all not null properties from the current to result instance
				.forEach(name -> revertResultInstance.add(name, currentInstance.get(name)));

		// if this is done for instance that changed types, some of the relation will not be processed
		// because the current definition and the new one are different, so we restore the relations from the original
		// version
		definitionService.getInstanceObjectProperties(revertResultInstance)
				.map(PropertyDefinition::getName)
				.forEach(name -> {
					Serializable value = revertResultInstance.get(name);
					if (value instanceof Collection) {
						Collection<Serializable> collection = (Collection) value;
						value = (Serializable) collection.stream()
								.map(convertToNonVersionId())
								.collect(Collectors.toList());
						revertResultInstance.add(name, value);
					} else if (value instanceof String) {
						value = convertToNonVersionId().apply(value);
						revertResultInstance.add(name, value);
					}
				});
	}

	private Predicate<PropertyDefinition> systemPropertiesThatShouldNotBeCopied() {
		return property -> {
			String uri = PropertyDefinition.resolveUri().apply(property);
			return uri == null || !SKIP_PROPERTIES.contains(uri);
		};
	}

	private static Function<Serializable, Serializable> convertToNonVersionId() {
		return id -> {
			if (id != null && InstanceVersionService.isVersion(id)) {
				return InstanceVersionService.getIdFromVersionId(id);
			}
			return id;
		};
	}

	private void revertTemplateIfNecessary(Instance current, Instance revertResultInstance) {
		Serializable templateId = current.get(LinkConstants.HAS_TEMPLATE, fieldConverter);
		Instance template = instanceService.loadByDbId(templateId);
		if (template != null) {
			String forObjectType = template.getString(TemplateProperties.EMF_FOR_OBJECT_TYPE, fieldConverter);
			if (!EqualsHelper.nullSafeEquals(revertResultInstance.getIdentifier(), forObjectType)) {
				Serializable previousTemplateId = revertResultInstance.get(LinkConstants.HAS_TEMPLATE, fieldConverter);
				templateId = convertToNonVersionId().apply(previousTemplateId);
			}
			revertResultInstance.add(LinkConstants.HAS_TEMPLATE, templateId, fieldConverter);
		}
	}
}
