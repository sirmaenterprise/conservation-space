package com.sirma.itt.emf.link.state;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.state.GenericInstanceStateServiceExtension;
import com.sirma.itt.seip.instance.state.StateServiceExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * State service extension to handle for {@link LinkReference} instance. The extension uses the default generic state
 * management.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.LINK)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 4.1)
public class LinkReferenceStateServiceExtension extends GenericInstanceStateServiceExtension<LinkReference> {

	@Override
	protected Class<LinkReference> getInstanceClass() {
		return LinkReference.class;
	}

	@Override
	protected Set<String> getActiveStates() {
		return Collections.emptySet();
	}

}
