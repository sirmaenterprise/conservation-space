package com.sirma.itt.seip.template;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.definition.model.BaseDefinition;
import com.sirma.itt.seip.domain.PathElement;

/**
 * Default implementation for the template definition.
 *
 * @author BBonev
 */
public class TemplateDefinitionImpl extends BaseDefinition<TemplateDefinitionImpl>
		implements TemplateDefinition, PathElement {

	private static final long serialVersionUID = -3015343782613271098L;

	@Tag(1)
	protected String dmsId;

	@Tag(2)
	protected String container;

	@Override
	public String getParentDefinitionId() {
		return null;
	}

	@Override
	public String getDmsId() {
		return dmsId;
	}

	@Override
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
	}

	@Override
	public String getContainer() {
		return container;
	}

	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public void setRevision(Long revision) {
		// no revision support
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

}
