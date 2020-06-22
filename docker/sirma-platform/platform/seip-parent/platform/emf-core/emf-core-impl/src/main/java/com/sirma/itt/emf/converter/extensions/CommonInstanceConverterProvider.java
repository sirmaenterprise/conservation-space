package com.sirma.itt.emf.converter.extensions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Converter class that manages conversion of {@link CommonInstance} objects to other types.
 * <p>
 * <b>NOTE</b> Updated common instance converter to change the converter implementation to be consistent with the other
 * implementations. Now the ids will be generated as json object with id and type. The old implementation is kept in
 * order for backward compatibility.<br>
 * This is done because of the new type converters of instance to string that use the same format and it's possible to
 * override the current implementation and break everything.
 *
 * @author BBonev
 */
@ApplicationScoped
public class CommonInstanceConverterProvider implements TypeConverterProvider {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonInstanceConverterProvider.class);
	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypes.INSTANCE)
	private InstanceDao instanceDao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(final TypeConverter converter) {
		// Add Common instance converter
		converter.addConverter(CommonInstance.class, String.class, source -> {
			// save the instance just in case
			instanceDao.persistChanges(source);
			// see class comments
			JSONObject object = new JSONObject();
			InstanceReference reference = source.toReference();
			if (reference != null) {
				JsonUtil.addToJson(object, "instanceId", reference.getId());
				JsonUtil.addToJson(object, "instanceType", reference.getReferenceType().getName());
				return object.toString();
			}
			return source.getId().toString();
		});
		converter.addConverter(String.class, CommonInstance.class, source -> {
			// see class comments
			if (source.startsWith("{")) {
				InstanceReference reference = converter.convert(InstanceReference.class, source);
				if (reference != null) {
					return (CommonInstance) instanceDao.loadInstance(reference.getId(), null, true);
				}
				LOGGER.warn("No common instance found for {}", source);
			}
			return (CommonInstance) instanceDao.loadInstance(source, null, true);
		});
		// END common instance converter
	}

}
