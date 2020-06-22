package com.sirma.sep.export.converter;

import java.util.Date;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.time.FormattedDate;
import com.sirma.itt.seip.time.FormattedDateTime;

/**
 * Converter for dates. This converter is used mainly in widgets when rendering idoc in the back-end.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @see com.sirma.sep.export.renders.BaseRenderer
 * @since 07/11/2017
 */
@ApplicationScoped
public class DateToFormattedDateConverter implements TypeConverterProvider {

	@Inject
	private DateConverter dateConverter;

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(Date.class, FormattedDate.class, source -> new FormattedDate(Optional.ofNullable
				(source).map(date -> dateConverter.formatDate(date)).orElseGet(null)));
		converter.addConverter(Date.class, FormattedDateTime.class,
				source -> new FormattedDateTime(Optional.ofNullable(source).map(date -> dateConverter.formatDateTime
						(date)).orElseGet(null)));
	}
}
