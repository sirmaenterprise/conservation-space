package com.sirma.itt.seip.instance.actions.thumbnail;

import java.io.Serializable;
import java.util.Objects;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.sep.content.rendition.RenditionService;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Executes 'add thumbnail' operation. The {@link #perform(AddThumbnailRequest)} method retrieves the instances needed
 * to perform the operation. If the target instance already has thumbnail, it is removed and then added the new one.
 *
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 50)
public class AddThumbnailAction implements Action<AddThumbnailRequest> {

	private static final String KEY_ERROR_MESSAGE_FIELD_THUMBNAIL_NOT_EXIST = "validation.error.field.has.thumbnail.not.existing";

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private ThumbnailService thumbnailService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private InstancePropertyNameResolver fieldConverter;

	@Override
	public String getName() {
		return AddThumbnailRequest.OPERATION_NAME;
	}

	@Override
	public void validate(AddThumbnailRequest request) {
		if (request == null) {
			throw new EmfRuntimeException("The request object is null.");
		}
		Serializable targetId = request.getTargetId();
		Serializable thumbnailObjectId = request.getThumbnailObjectId();
		Objects.requireNonNull(targetId, "Missing target id in the request!");
		Objects.requireNonNull(thumbnailObjectId, "Missing thumbnail object id in the request!");

		InstanceReference targetReference = resolveReference(targetId);
		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(targetReference.toInstance());
		boolean exist = instanceDefinition.findField(PropertyDefinition.hasUri(LinkConstants.HAS_THUMBNAIL)).isPresent();
		if (!exist) {
			throw createMissingFieldException();
		}
		request.setTargetReference(targetReference);
	}

	private BadRequestException createMissingFieldException() {
		return new BadRequestException(
				labelProvider.getLabel(KEY_ERROR_MESSAGE_FIELD_THUMBNAIL_NOT_EXIST));
	}

	@Override
	public Instance perform(AddThumbnailRequest request) {
		InstanceReference targetReference = request.getTargetReference();
		Serializable thumbnailObjectId = request.getThumbnailObjectId();
		InstanceReference thumbnailReference = resolveReference(thumbnailObjectId);

		Instance targetInstance = targetReference.toInstance();

		targetInstance.add(LinkConstants.HAS_THUMBNAIL, thumbnailObjectId, fieldConverter);

		// we remove the old thumbnail, if there is any, before register another
		thumbnailService.removeThumbnail(targetReference.getId(), RenditionService.DEFAULT_PURPOSE);
		thumbnailService.register(targetReference, thumbnailReference.toInstance(), null);

		return domainInstanceService.save(InstanceSaveContext.create(targetInstance, request.toOperation()));
	}

	private InstanceReference resolveReference(Serializable id) {
		return instanceTypeResolver.resolveReference(id).orElseThrow(() -> new InstanceNotFoundException(id));
	}

}
