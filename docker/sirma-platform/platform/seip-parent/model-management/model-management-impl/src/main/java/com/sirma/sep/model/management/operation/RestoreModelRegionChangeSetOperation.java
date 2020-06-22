package com.sirma.sep.model.management.operation;

import java.util.ArrayList;
import java.util.stream.Stream;

import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.ModelRegion;
import com.sirma.sep.model.management.Models;

/**
 * Model change set operation for restoring the inheritance of a {@link ModelRegion}. The region is deleted and all of
 * the fields registered in the region will moved outside of the removed region
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/11/2018
 */
public class RestoreModelRegionChangeSetOperation extends RestoreModelNodeChangeSetOperation<ModelRegion> {

	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelRegion;
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelRegion targetNode, ModelChangeSet changeSet) {
		ModelDefinition parentDefinition = targetNode.getContext();
		String regionId = targetNode.getId();

		// notify the owning definition for the region removal
		ModelRegion region = parentDefinition.getRegionsMap().get(regionId);

		if (region == null) {
			return Stream.empty();
		}
		// notify all region fields that they are no longer part of that region
		Stream<ModelChangeSetInfo> unAssignFieldsFromRegion = new ArrayList<>(parentDefinition.getFields())
				.stream()
				.filter(field -> EqualsHelper.nullSafeEquals(field.getRegionId(), regionId))
				.flatMap(field -> AssignToRegionChangeSetOperation.createChange(field.getPath(), null));

		Stream<ModelChangeSetInfo> removeAttributesAndRegion = Stream.concat(restoreModelNodeAttributes(region),
				Stream.of(RemoveNodeChangeSetOperation.createChange(region.getPath())));

		return Stream.concat(unAssignFieldsFromRegion, removeAttributesAndRegion);
	}
}
