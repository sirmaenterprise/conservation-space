package com.sirma.itt.emf.forum.state;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.state.BaseStateServiceExtension;
import com.sirma.itt.emf.state.GenericInstanceStateServiceExtension;
import com.sirma.itt.emf.state.StateServiceExtension;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * State service extension for {@link CommentInstance}. The actual implementation is in the
 * extension for the topic instance and this here only a proxy to it.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.COMMENT)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 5.1)
public class CommentStateServiceExtension extends
		GenericInstanceStateServiceExtension<CommentInstance> {

	/** The extension. */
	@Inject
	@InstanceType(type = ObjectTypes.TOPIC)
	private BaseStateServiceExtension<TopicInstance> extension;

	@Override
	public boolean changeState(CommentInstance instance, Operation operation) {
		// no state to change
		return false;
	}

	@Override
	public String getPrimaryState(CommentInstance instance) {
		TopicInstance topic = instance.getTopic();
		// return the status of the topic
		if (topic != null) {
			return extension.getPrimaryState(topic);
		}
		return super.getPrimaryState(instance);
	}

	@Override
	public int getPrimaryStateCodelist() {
		return extension.getPrimaryStateCodelist();
	}

	@Override
	public Map<String, String> getStateTypeMapping() {
		return extension.getStateTypeMapping();
	}

	@Override
	protected String getPrimaryStateProperty() {
		return DefaultProperties.STATUS;
	}

	@Override
	protected Class<CommentInstance> getInstanceClass() {
		return CommentInstance.class;
	}

}
