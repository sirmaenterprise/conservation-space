package com.sirma.itt.seip.template;

import java.io.Serializable;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.seip.instance.dao.InstanceLoader;
import com.sirma.itt.seip.instance.dao.InstanceType;

/**
 * Instance dao implementation for template instance
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = TemplateProperties.TEMPLATE_TYPE)
public class TemplateInstanceDao extends BaseInstanceDaoImpl<TemplateEntity, Long, String, TemplateDefinition> {

	@Inject
	@InstanceType(type = TemplateProperties.TEMPLATE_TYPE)
	private InstanceLoader instanceLoader;

	@Override
	protected void onInstanceUpdated(Instance instance) {
		// ensure the content will not be saved
		instance.getProperties().remove(DefaultProperties.CONTENT);
		instance.getProperties().remove(TemplateProperties.IS_CONTENT_LOADED);
	}

	@Override
	protected void updateModifierInfo(Instance instance, Date currentDate) {
		// not needed to update it
	}

	@Override
	protected String getOwningInstanceQuery() {
		return null;
	}

	@Override
	protected void populateInstanceForModel(Instance instance, TemplateDefinition model) {
		populateProperties(instance, model);

		// ensure the default properties are set
		instance.setIdentifier(model.getIdentifier());

		TemplateInstance template = (TemplateInstance) instance;

		template.setForType(model
				.getField(DefaultProperties.TYPE)
					.map(PropertyDefinition::getDefaultValue)
					.filter(StringUtils::isNotNullOrEmpty)
					.orElse(TemplateProperties.DEFAULT_GROUP));

		template.setPrimary(model
				.getField(TemplateProperties.PRIMARY)
					.map(PropertyDefinition::getDefaultValue)
					.map(Boolean::valueOf)
					.orElse(Boolean.FALSE));

		template.setPublicTemplate(model
				.getField(TemplateProperties.PUBLIC)
					.map(PropertyDefinition::getDefaultValue)
					.map(Boolean::valueOf)
					.orElse(Boolean.TRUE));
	}

	@Override
	public void saveProperties(Instance instance, boolean addOnly) {
		Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.enable();
		try {
			super.saveProperties(instance, addOnly);
		} finally {
			Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.disable();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<TemplateInstance> getInstanceClass() {
		return TemplateInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<TemplateEntity> getEntityClass() {
		return TemplateEntity.class;
	}

	@Override
	public <I extends Serializable> Class<I> getPrimaryIdType() {
		return (Class<I>) Long.class;
	}

	@Override
	protected InstanceLoader getInstanceLoader() {
		return instanceLoader;
	}

}
