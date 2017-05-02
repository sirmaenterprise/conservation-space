package com.sirma.itt.seip.instance.revision.steps;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirmaenterprise.sep.content.idoc.Idoc;

/**
 * Initialize the idoc view in the publish content for the other steps
 *
 * @author BBonev
 */
@Extension(target = PublishStep.EXTENSION_NAME, order = 100)
public class InitialPublishStep implements PublishStep {

	@Inject
	private InstanceContentService contentService;
	@Inject
	private TemplateService templateService;

	@Override
	public void execute(PublishContext publishContext) {
		Instance instanceToPublish = publishContext.getRequest().getInstanceToPublish();
		ContentInfo info = contentService.getContent(instanceToPublish, Content.PRIMARY_VIEW);
		Idoc idoc;
		if (info.exists()) {
			idoc = buildIdocFromContent(instanceToPublish, info);
		} else {
			idoc = buildIdocFromDefaultTemplate(instanceToPublish);
		}
		if (idoc == null) {
			throw new RollbackedRuntimeException("Could not load instance view: " + instanceToPublish.getId());
		}
		publishContext.setView(idoc);
	}

	private static Idoc buildIdocFromContent(Instance instanceToPublish, ContentInfo info) {
		try (InputStream inputStream = info.getInputStream()) {
			return Idoc.parse(inputStream);
		} catch (IOException e) {
			throw new RollbackedRuntimeException("Could not read instance view: " + instanceToPublish.getId(), e);
		}
	}

	private Idoc buildIdocFromDefaultTemplate(Instance instance) {
		TemplateInstance template = templateService.getPrimaryTemplate(instance.getIdentifier());
		if (template != null) {
			template = templateService.loadContent(template);
			if (template.getContent() != null) {
				return Idoc.parse(template.getContent());
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return Steps.INITIAL.getName();
	}

}
