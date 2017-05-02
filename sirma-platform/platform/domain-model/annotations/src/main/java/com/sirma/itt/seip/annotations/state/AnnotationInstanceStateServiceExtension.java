package com.sirma.itt.seip.annotations.state;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.state.GenericInstanceStateServiceExtension;
import com.sirma.itt.seip.instance.state.StateServiceExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * State service extension to handle for {@link Annotation} instance. The extension uses the default generic state
 * management.
 *
 * @author tdossev *
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.ANNOTATION)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 51)
public class AnnotationInstanceStateServiceExtension extends GenericInstanceStateServiceExtension<Annotation> {

	@Override
	public Class<Annotation> getInstanceClass() {
		return Annotation.class;
	}

	@Override
	public Set<String> getActiveStates() {
		return Collections.emptySet();
	}

	@Override
	public String getPrimaryStateProperty() {
		return DefaultProperties.EMF_STATUS;
	}

}
