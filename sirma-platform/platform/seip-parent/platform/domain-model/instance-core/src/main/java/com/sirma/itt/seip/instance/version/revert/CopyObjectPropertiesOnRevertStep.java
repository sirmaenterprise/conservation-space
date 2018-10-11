package com.sirma.itt.seip.instance.version.revert;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;

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

	@Inject
	private DefinitionService definitionService;

	@Override
	public String getName() {
		return "copyObjectProperties";
	}

	@Override
	public void invoke(RevertContext context) {
		Instance currentInstance = context.getCurrentInstance();
		definitionService
				.getInstanceObjectProperties(currentInstance)
					.map(PropertyDefinition::getName)
					// remove all object properties from result instance by requirement
					.map(property -> {
						context.getRevertResultInstance().remove(property);
						return property;
					})
					.filter(currentInstance::isValueNotNull)
					// transfer all not null properties from the current to result instance
					.forEach(name -> context.getRevertResultInstance().add(name, currentInstance.get(name)));
	}
}
