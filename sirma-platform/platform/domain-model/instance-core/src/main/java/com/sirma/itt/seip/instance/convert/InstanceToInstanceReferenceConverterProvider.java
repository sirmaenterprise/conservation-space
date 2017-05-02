package com.sirma.itt.seip.instance.convert;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.json.JSONObject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.model.LinkSourceId;

/**
 * Provider class that registers the converters for the default implementations of.
 * {@link com.sirma.itt.seip.domain.instance.Instance} interface to {@link LinkSourceId}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class InstanceToInstanceReferenceConverterProvider extends AbstractInstanceToInstanceReferenceConverterProvider {

	/** The entity classes. */
	private static List<Class<? extends Instance>> entityClasses = new java.util.LinkedList<>();

	static {
		entityClasses.add(CommonInstance.class);
		entityClasses.add(LinkInstance.class);
		entityClasses.add(ObjectInstance.class);
	}

	@Override
	public void register(TypeConverter converter) {
		for (Class<? extends Instance> c : entityClasses) {
			addEntityConverter(converter, c, LinkSourceId.class);
			addEntityConverter(converter, c, InstanceReference.class);
		}

		converter.addDynamicTwoStageConverter(LinkSourceId.class, JSONObject.class, String.class);
	}

}
