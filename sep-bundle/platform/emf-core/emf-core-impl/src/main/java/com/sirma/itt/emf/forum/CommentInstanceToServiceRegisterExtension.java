package com.sirma.itt.emf.forum;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceToServiceRegisterExtension;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.properties.model.PropertyModel;

/**
 * Extension for comments loading.
 * 
 * @author BBonev
 */
@ApplicationScoped
@SuppressWarnings("unchecked")
@InstanceType(type = ObjectTypes.COMMENT)
@Extension(target = InstanceToServiceRegisterExtension.TARGET_NAME, order = 6)
public class CommentInstanceToServiceRegisterExtension implements
		InstanceToServiceRegisterExtension {

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<Class<?>> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = new ArrayList<Class<?>>();
		SUPPORTED_OBJECTS.add(CommentInstance.class);
	}

	@Inject
	private javax.enterprise.inject.Instance<CommentService> commentService;

	@Inject
	@InstanceType(type = ObjectTypes.DEFAULT)
	private PropertyModelCallback<PropertyModel> callback;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <I extends Instance, D extends DefinitionModel> InstanceService<I, D> getInstanceService() {
		if (!commentService.isUnsatisfied()) {
			return (InstanceService<I, D>) commentService.get();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <I extends Instance> InstanceDao<I> getInstanceDao() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <P extends PropertyModel> PropertyModelCallback<P> getModelCallback() {
		return (PropertyModelCallback<P>) callback;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DefinitionAccessor getDefinitionAccessor() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends TopLevelDefinition> DefinitionCompilerCallback<T> getCompilerCallback() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <I extends Instance> InstanceEventProvider<I> getEventProvider() {
		return null;
	}

}
