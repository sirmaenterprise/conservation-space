package com.sirma.itt.emf.link.state;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.GenericInstanceStateServiceExtension;
import com.sirma.itt.emf.state.StateServiceExtension;

/**
 * State service extension to handle for {@link LinkReference} instance. The extension uses the
 * default generic state management.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.LINK)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 4.1)
public class LinkReferenceStateServiceExtension extends
		GenericInstanceStateServiceExtension<LinkReference> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<LinkReference> getInstanceClass() {
		return LinkReference.class;
	}

}
