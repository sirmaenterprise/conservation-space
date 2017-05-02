package com.sirma.itt.objects.security.provider;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.permissions.BaseDefinitionActionProvider;
import com.sirma.itt.seip.permissions.action.ActionProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Producer implementation for object definition operations.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ActionProvider.TARGET_NAME, order = 200)
public class ObjectsActionProvider extends BaseDefinitionActionProvider {

	@Override
	protected Class<?> getInstanceClass() {
		return ObjectInstance.class;
	}

	@Override
	protected Class<? extends DefinitionModel> getDefinitionClass() {
		return GenericDefinition.class;
	}
}
