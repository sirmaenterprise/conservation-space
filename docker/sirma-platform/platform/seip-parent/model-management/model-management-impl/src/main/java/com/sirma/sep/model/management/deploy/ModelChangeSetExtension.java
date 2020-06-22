package com.sirma.sep.model.management.deploy;

import com.sirma.sep.model.management.operation.ModelChangeSet;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * During the initial process of reading the changes we inflate the changes by introducing change instances for each
 * map key-value entry in the map changes. This class is used to represent such changes and include the original
 * change as well in order to deflate them before returning the results
 *
 * @author B. Bonev
 */
public final class ModelChangeSetExtension extends ModelChangeSet {
	private final ModelChangeSetInfo delegate;

	public ModelChangeSetExtension(ModelChangeSetInfo delegate) {
		this.delegate = delegate;
	}

	public static ModelChangeSetExtension copyFrom(ModelChangeSetInfo info) {
		ModelChangeSetExtension extension = new ModelChangeSetExtension(info);
		ModelChangeSet change = info.getChangeSet();
		extension.setSelector(change.getSelector())
				.setNewValue(change.getNewValue())
				.setOldValue(change.getOldValue());
		return extension;
	}

	public ModelChangeSetInfo getDelegate() {
		return delegate;
	}

	@Override
	public String getOperation() {
		return delegate.getChangeSet().getOperation();
	}

}
