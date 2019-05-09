package com.sirma.itt.seip.instance.actions.clone;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Idoc;

/**
 * Extension point that executes the persisting phase of the 'clone' action. Here is assumed that the
 * {@link InstanceCloneRequest} already contains a cloned {@link com.sirma.itt.seip.domain.instance.Instance} object.
 * The only thing that has to be handled is the content and persisting the cloned instance and its data.
 *
 * @author Ivo Rusev on 11.12.2016
 */
@Extension(target = Action.TARGET_NAME, order = 160)
public class InstanceCloneAction implements Action<InstanceCloneRequest> {

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private EventService eventService;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	public boolean shouldLockInstanceBeforeAction(InstanceCloneRequest request) {
		// new instance, no need to lock it
		return false;
	}

	@Override
	public Object perform(InstanceCloneRequest request) {
		Instance clonedInstance = request.getClonedInstance();
		if (clonedInstance == null) {
			throw new EmfRuntimeException("The instance to be cloned is null.");
		}

		clonePrimaryContentIfAny(request);
		Idoc idoc = processViewContent(request);
		clonedInstance.add(DefaultProperties.TEMP_CONTENT_VIEW, idoc.asHtml());

		// CMF-22293 The normal behaviour of the audit operation is to log 'clone' operation for the new instance.
		// However the default audit logic should be overridden because there's an improvement that states that only
		// create operation for the cloned object should be logged in the audit. See the comment in the Jira task for
		// further information.
		eventService.fire(new AuditableEvent(clonedInstance, ActionTypeConstants.CREATE));
		return Options.DISABLE_AUDIT_LOG.wrap(() -> domainInstanceService
						.save(InstanceSaveContext.create(clonedInstance, request.toOperation()))).get();
	}

	private void clonePrimaryContentIfAny(InstanceCloneRequest request) {
		ContentInfo content = instanceContentService.getContent(request.getTargetId(), Content.PRIMARY_CONTENT);
		if (!content.exists()) {
			return;
		}

		Content contentToClone = Content
				.createFrom(content)
					.setContent(content)
					.setContentLength(content.getLength())
					.setVersionable(true);

		// save with dummy instance because the ID of the instance can be changed later
		EmfInstance dummy = new EmfInstance();
		dummy.setProperties(request.getClonedInstance().getProperties());
		ContentInfo clonedContent = instanceContentService.saveContent(dummy, contentToClone);
		request.getClonedInstance().add(DefaultProperties.PRIMARY_CONTENT_ID, clonedContent.getContentId());
	}

	/**
	 * Retrieves the content for the instance that should be cloned. Generates new ids for all the content nodes and
	 * returns {@link Idoc} object with the changes.
	 * <p>
	 * Idoc comments are linked with the idoc tab ids that are located in the DOM. Therefore in order to hide the
	 * comments from the original instance we need to parse the iDoc and generate new identifiers for its sections. That
	 * will cause properties like comments to be untied from the newly generated instance clone.
	 * </p>
	 */
	private Idoc processViewContent(InstanceCloneRequest request) {
		String content = getContent(request);
		Idoc idoc = Idoc.parse(content);
		idoc.getSections().forEach(ContentNode::generateNewId);
		idoc.widgets().forEach(ContentNode::generateNewId);
		return idoc;
	}

	/**
	 * Downloads and returns the content if it is not attached to the instance.
	 *
	 * @param request the data object that holds the data from the rest request
	 * @return the primary view view as string.
	 */
	private String getContent(InstanceCloneRequest request) {
		if (request.getClonedInstance().isValueNotNull(DefaultProperties.TEMP_CONTENT_VIEW)) {
			// TODO when the service for clone content is created(next sprint maybe) we don't need to generate new ids
			// for this content, because it will be done already
			return request.getClonedInstance().getString(DefaultProperties.TEMP_CONTENT_VIEW);
		}

		try {
			return instanceContentService
					.getContent(request.getTargetId(), Content.PRIMARY_VIEW)
						.asString(StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new EmfApplicationException(
					"Error while downloading the view content for instance with id - " + request.getTargetId(), e);
		}
	}

	@Override
	public String getName() {
		return InstanceCloneRequest.OPERATION_NAME;
	}
}
