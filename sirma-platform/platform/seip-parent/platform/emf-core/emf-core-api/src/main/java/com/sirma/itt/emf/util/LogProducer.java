package com.sirma.itt.emf.util;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.log4j.Logger;

/**
 * Producer class for log4j logging.
 * <p>
 * TODO: remove this class!
 *
 * @author BBonev
 */
@ApplicationScoped
public class LogProducer implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8707506342327947559L;

	/**
	 * Creates the logger.
	 *
	 * @param ip
	 *            the ip
	 * @return the logger
	 */
	@Produces
	public Logger createLogger(InjectionPoint ip) {
		Bean<?> bean = ip.getBean();
		String category;
		if (bean != null) {
			category = bean.getBeanClass().getName();
		} else {
			category = ip.getMember().getDeclaringClass().getName();
		}
		return Logger.getLogger(category);
	}

}
