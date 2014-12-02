package com.sirma.itt.emf.web.converter;

import java.text.MessageFormat;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.DateTimeConverter;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * The Class DateWithTimeConverter. <br />
 * <b>NOTE: Because its not possible to inject inside FacesConverter we should use a workaround. We
 * make the convertor a managed bean and it is binded trough EL expression instead trough setter.
 * https://java.net/jira/browse/JAVASERVERFACES_SPEC_PUBLIC-763 http://stackoverflow.com/questions
 * /13156671/how-can-i-inject-in-facesconverter/13156834#13156834</b>
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class EmfDateWithTimeConverter extends DateTimeConverter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/** The converter datetime format pattern. */
	@Inject
	@Config(name = EmfConfigurationProperties.CONVERTER_DATETIME_FORMAT, defaultValue = "dd.MM.yyyy, HH:mm")
	private String converterDatetimeFormatPattern;

	/** The label provider. */
	@Inject
	private LabelProvider labelProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAsObject(final FacesContext context, final UIComponent component,
			final String value) {
		Date date = null;
		try {
			this.setPattern(converterDatetimeFormatPattern);
			setTimeZone(null);
			date = (Date) super.getAsObject(context, component, value);
		} catch (final Exception e) {
			final String errorMessage = MessageFormat.format(
					labelProvider.getValue("date.converter.error"), converterDatetimeFormatPattern);
			log.error("Datetime conversion error: ", errorMessage);
			throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, errorMessage));
		}
		return date;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAsString(final FacesContext context, final UIComponent component,
			final Object obj) {
		String formattedDate = null;
		setTimeZone(null);
		this.setPattern(converterDatetimeFormatPattern);
		formattedDate = super.getAsString(context, component, obj);
		return formattedDate;
	}

}