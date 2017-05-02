package com.sirma.itt.seip.instance.content;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.PURPOSE_IDOC;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Handles primary instance content assigning, if there is any. If there is value for the instance property
 * {@link DefaultProperties#PRIMARY_CONTENT_ID} it is used to assign the associated content to the current instance.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 20)
public class AssignPrimaryContentStep implements InstanceSaveStep {

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		Instance instance = saveContext.getInstance();
		Serializable contentId = instance.get(DefaultProperties.PRIMARY_CONTENT_ID);
		String purpose = PURPOSE_IDOC;
		if (contentId instanceof String) {
			instanceContentService.assignContentToInstance((String) contentId, instance.getId());
			// purpose uploaded
			purpose = null;
		}

		if (instance instanceof Purposable) {
			((Purposable) instance).setPurpose(purpose);
		}
	}

	@Override
	public void rollbackBeforeSave(InstanceSaveContext saveContext, Throwable cause) {
		// TODO revoke assignment maybe? Content service need to be extended
	}

	@Override
	public String getName() {
		return "assignPrimaryContent";
	}

}
