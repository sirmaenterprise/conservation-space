package com.sirma.itt.seip.instance.content.view;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplatePurposes;
import com.sirmaenterprise.sep.content.idoc.sanitizer.IdocSanitizer;

/**
 * Handles the saving of instance view content. Checks if there is a content send with the target instance as property.
 * If there is one it is converted to {@link Content} and saved as {@link Content#PRIMARY_VIEW} for the target instance.
 * If there is no content the default template for the definition with which the instance is saved is retrieved and used
 * as default content for the instance view. <br />
 * The rollback for this step will delete the saved content, if any error occurs while the saving process is running.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 10)
public class SaveInstanceViewStep implements InstanceSaveStep {

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private TemplateService documentTemplateService;

	@Inject
	private IdocSanitizer idocSanitizer;

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		Instance instance = saveContext.getInstance();
		Serializable contentAsString = instance.remove(DefaultProperties.TEMP_CONTENT_VIEW);
		String viewId = null;
		if (contentAsString instanceof String) {
			viewId = saveContent(instance, createInstanceView(contentAsString.toString(), instance));
		} else if (!instanceContentService.getContent(instance, Content.PRIMARY_VIEW).exists()) {
			// when the instance doesn't have a view assigned, it is retrieved based on the default template. It is
			// executed for instances that are created without being opened in the web
			viewId = saveContent(instance, getContentFromTemplate(instance));
		}

		saveContext.setViewId(Optional.ofNullable(viewId));
	}

	private String saveContent(Instance instance, Content content) {
		return instanceContentService.saveContent(instance, content).getContentId();
	}

	private static Content createInstanceView(String contentAsString, Instance instance) {
		return Content
				.createEmpty()
					.setContent(contentAsString, StandardCharsets.UTF_8)
					.setName(UUID.randomUUID() + "-instanceView.html")
					.setMimeType(MediaType.TEXT_HTML)
					.setPurpose(Content.PRIMARY_VIEW)
					.setVersionable(instance.type().isVersionable())
					.setView(true)
					.setIndexable(true);
	}

	/**
	 * Retrieve the instance's creatable or uploadable template content if possible. Used when the instance doesn't have
	 * a primary content on it's own.
	 *
	 * @param instance
	 *            the instance
	 * @return the instance's template content
	 */
	private Content getContentFromTemplate(Instance instance) {
		String templatePurpose = TemplatePurposes.CREATABLE;
		if (instance.isUploaded() && instance.type().isUploadable()) {
			templatePurpose = TemplatePurposes.UPLOADABLE;
		}

		TemplateInstance template = documentTemplateService.getPrimaryTemplate(instance.getIdentifier(),
				templatePurpose);

		String content = null;
		if (template != null) {
			content = documentTemplateService.loadContent(template).getContent();
		}

		// If the template isn't found, load the default template content
		if (StringUtils.isNullOrEmpty(content)) {
			content = documentTemplateService.getDefaultTemplateContent();
		}

		return createInstanceView(idocSanitizer.sanitize(content), instance);
	}

	@Override
	public void rollbackBeforeSave(InstanceSaveContext saveContext, Throwable cause) {
		Optional<String> viewId = saveContext.getViewId();
		if (viewId.isPresent()) {
			instanceContentService.deleteContent(viewId.get(), null);
		}
	}

	@Override
	public String getName() {
		return "saveInstanceView";
	}
}
