package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

import javax.inject.Inject;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Integration step for users and groups that intercepts instance creation and overrides the instance id if the created
 * instance is user or group
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/09/2017
 */
@Extension(target = InstanceSaveStep.NAME, order = 0.9)
public class UserAndGroupIntegrationStep implements InstanceSaveStep {

	@Inject
	private DatabaseIdManager idManager;
	@Inject
	private ResourceService resourceService;
	@Inject
	private InstanceContextService contextService;

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		Instance instance = saveContext.getInstance();
		if (instance.type().is(ObjectTypes.USER)) {
			onUserSave(instance);
			return;
		}

		if (instance.type().is(ObjectTypes.GROUP)) {
			onGroupSave(instance);
		}
	}

	private void onUserSave(Instance instance) {
		versifyContextNotPresent(instance);
		// sync user title if displayed fields are modified
		instance.add(TITLE, EmfResourcesUtil.buildDisplayName(instance.getProperties()));

		setCorrectResourceId(instance, ResourceProperties.USER_ID, resourceService::buildUser);
		addToEveryoneGroup(instance);
	}

	private void addToEveryoneGroup(Instance instance) {
		// add the user to the Everyone group so it's updated in the semantic DB immediate on save
		Collection<Serializable> members = instance.getAsCollection(ResourceProperties.IS_MEMBER_OF, LinkedList::new);
		members.add(resourceService.getAllOtherUsers().getId());
		instance.add(ResourceProperties.IS_MEMBER_OF, (Serializable) members);
	}

	private void onGroupSave(Instance instance) {
		versifyContextNotPresent(instance);

		// add title if its not already set in the instance
		instance.addIfNullMapping(TITLE,
				EmfResourcesUtil.cleanGroupId(instance.getString(ResourceProperties.GROUP_ID)));

		setCorrectResourceId(instance, ResourceProperties.GROUP_ID, resourceService::buildGroup);
	}

	private void setCorrectResourceId(Instance instance, String resourcePropertyName, Function<Instance, ? extends Resource> resourceBuilder) {
		// set proper resource identifier before the database id is used for anything
		if (idManager.isPersisted(instance)) {
			return;
		}

		// fetch the entered value by the user before update
		String resourceName = instance.getString(resourcePropertyName);
		// build proper resource name and system id
		Resource resource = resourceBuilder.apply(instance);
		if (resourceService.resourceExists(resource.getName())) {
			String type = instance.type().getCategory();
			type = type.toUpperCase().charAt(0) + type.substring(1);
			// this probably should be from a bundle, as this message is displayed to the user
			throw new RollbackedRuntimeException(type + " with " + type + " ID '" + resourceName + "' already exists");
		}

		PropertiesUtil.copyValue(resource, instance, resourcePropertyName);
		idManager.unregister(instance);
		instance.setId(resource.getId());
		idManager.register(instance);
	}

	private void versifyContextNotPresent(Instance instance) {
		if (contextService.getContext(instance).isPresent()) {
			throw new RollbackedRuntimeException("Cannot create an " + instance.type().getCategory() + " in a context");
		}
	}

	@Override
	public String getName() {
		return "userAndGroupIntegration";
	}
}
