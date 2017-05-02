package com.sirma.itt.objects.script;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.script.InstanceToScriptNodeConverterProvider;

/**
 * Cmf converter register for specific converters for {@link com.sirma.itt.seip.domain.instance.Instance} to
 * {@link com.sirma.itt.seip.instance.script.ScriptNode}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ObjectInstanceToScritpNodeConverterProvider extends InstanceToScriptNodeConverterProvider {

	@Inject
	@InstanceType(type = ObjectTypes.OBJECT)
	private Instance<ObjectScript> objectNodes;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		addConverter(converter, ObjectInstance.class, objectNodes);
	}
}
