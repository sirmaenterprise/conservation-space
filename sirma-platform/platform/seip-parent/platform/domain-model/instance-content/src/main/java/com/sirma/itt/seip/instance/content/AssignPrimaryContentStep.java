package com.sirma.itt.seip.instance.content;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.PURPOSE_IDOC;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.InstanceContentService;

/**
 * Handles primary instance content assigning, if there is any. If there is value for the instance property
 * {@link DefaultProperties#PRIMARY_CONTENT_ID} it is used to assign the associated content to the current instance.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 1.15)
public class AssignPrimaryContentStep implements InstanceSaveStep {

	public static final String CONTENT_ASSIGNED_KEY = "contentAssigned";

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		Instance instance = saveContext.getInstance();
		String contentId = instance.getString(DefaultProperties.PRIMARY_CONTENT_ID);
		String purpose = PURPOSE_IDOC;
		if (StringUtils.isNotBlank(contentId)) {
			boolean assigned = instanceContentService.assignContentToInstance(contentId, instance.getId(),
																			  Content.PRIMARY_CONTENT);
			saveContext.setPropertyIfNotNull(CONTENT_ASSIGNED_KEY, assigned);
			// purpose uploaded
			purpose = null;
		}

		if (instance instanceof Purposable) {
			((Purposable) instance).setPurpose(purpose);
		}
	}

	@Override
	public void rollbackBeforeSave(InstanceSaveContext saveContext) {
		Instance instance = saveContext.getInstance();
		String contentId = instance.getString(DefaultProperties.PRIMARY_CONTENT_ID);
		if (saveContext.getIfSameType(CONTENT_ASSIGNED_KEY, Boolean.class, Boolean.FALSE)) {
			instanceContentService.deleteContent(contentId, "any");
		}
	}

	@Override
	public String getName() {
		return "assignPrimaryContent";
	}

}
