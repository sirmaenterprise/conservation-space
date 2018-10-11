package com.sirma.itt.emf.link.converters;

import javax.enterprise.context.ApplicationScoped;

import org.json.JSONObject;

import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.model.LinkSourceId;

/**
 * Converter provider class to enable conversion from {@link LinkSourceId} to concrete. {@link Instance}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class LinkSourceToInstanceConverterProvider implements TypeConverterProvider {

	/**
	 * Converter class to support conversion from {@link LinkSourceId} to concrete {@link Instance} implementation.
	 *
	 * @author BBonev
	 */
	public class LinkSourceToInstanceConverter implements Converter<LinkSourceId, Instance> {

		@Override
		public Instance convert(LinkSourceId source) {
			return source.toInstance();
		}

	}

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(LinkSourceId.class, Instance.class, new LinkSourceToInstanceConverter());
		converter.addConverter(LinkSourceId.class, String.class, linkSource -> linkSource.toJSONObject().toString());
		converter.addConverter(LinkSourceId.class, JSONObject.class, LinkSourceId::toJSONObject);
	}

}
