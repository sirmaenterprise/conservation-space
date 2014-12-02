package com.sirma.itt.objects.web.object;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.instance.landingpage.InstanceLandingPage;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.objects.domain.ObjectTypesObject;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.event.ObjectOpenEvent;
import com.sirma.itt.objects.services.ObjectService;

/**
 * Object landing page backing bean.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class ObjectLandingPage extends InstanceLandingPage<ObjectInstance, ObjectDefinition>
		implements Serializable {

	private static final long serialVersionUID = 6254229319006057509L;

	@Inject
	private ObjectService objectService;

	@Override
	public void initPage() {
		Class<ObjectInstance> instanceClass = getInstanceClass();
		ObjectInstance instance = getDocumentContext().getInstance(instanceClass);
		// if instance is not new one, then call definition reader to render the form
		if ((instance != null) && SequenceEntityGenerator.isPersisted(instance)) {
			Instance owningInstance = instance.getOwningInstance();
			if (owningInstance != null) {
				getDocumentContext().addContextInstance(owningInstance);
			}
		} else {
			initForNewInstance(instanceClass, instance);
		}
	}

	@Override
	public void itemSelectedAction() {
		if (log.isDebugEnabled()) {
			log.debug("ObjectsWeb: InstanceLandingPage.itemSelectedAction selected definition: ["
					+ getSelectedType() + "]");
		}

		if (StringUtils.isNullOrEmpty(getSelectedType())) {
			return;
		}

		// - get selected definition type
		ObjectDefinition selectedDefinition = dictionaryService.getDefinition(
				getInstanceDefinitionClass(), getSelectedType());
		if (selectedDefinition == null) {
			log.error("ObjectsWeb: InstanceLandingPage.itemSelectedAction cann't create new instance with null definition");
			return;
		}

		// For every new instance except project we should have the context inside which it
		// should be created.
		Instance contextInstance = getDocumentContext().getContextInstance();

		ObjectInstance newInstance = getNewInstance(selectedDefinition, contextInstance);

		// in order to have the chain to parent instances needed for headers
		newInstance.setOwningInstance(contextInstance);

		fireInstanceOpenEvent(newInstance);

		getDocumentContext().populateContext(newInstance, getInstanceDefinitionClass(),
				selectedDefinition);
	}

	@Override
	protected Class<ObjectDefinition> getInstanceDefinitionClass() {
		return ObjectDefinition.class;
	}

	@Override
	protected ObjectInstance getNewInstance(ObjectDefinition selectedDefinition, Instance context) {
		return objectService.createInstance(selectedDefinition, null);
	}

	@Override
	protected Class<ObjectInstance> getInstanceClass() {
		return ObjectInstance.class;
	}

	@Override
	protected InstanceReference getParentReference() {
		// not used
		return null;
	}

	@Override
	protected String saveInstance(ObjectInstance instance) {
		eventService.fire(new ObjectOpenEvent(instance));
		return null;
	}

	@Override
	protected String cancelEditInstance(ObjectInstance instance) {
		// not used
		return null;
	}

	@Override
	protected void onExistingInstanceInitPage(ObjectInstance instance) {
		// not used

	}

	@Override
	protected void onNewInstanceInitPage(ObjectInstance instance) {
		// not used

	}

	@Override
	protected FormViewMode getFormViewModeExternal(ObjectInstance instance) {
		// not used
		return null;
	}

	@Override
	protected String getNavigationString() {
		// not used
		return null;
	}

	@Override
	protected String getDefinitionFilterType() {
		return ObjectTypesObject.OBJECT;
	}

	@Override
	protected InstanceService<ObjectInstance, ObjectDefinition> getInstanceService() {
		return objectService;
	}

}
