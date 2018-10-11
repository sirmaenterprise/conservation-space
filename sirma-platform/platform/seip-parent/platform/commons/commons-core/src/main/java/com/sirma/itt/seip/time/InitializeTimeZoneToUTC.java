package com.sirma.itt.seip.time;

import java.lang.invoke.MethodHandles;
import java.util.TimeZone;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * Initializer for the JVM to set the time zone to UTC on deployment. This enforces the default time zone regardless of
 * the OS locale and zone setting <br>
 * If this is not desired override the method {@link #setTimeZone(TimeZone)}
 *
 * @author BBonev
 */
@Singleton
public class InitializeTimeZoneToUTC {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Called first on startup will set the default time zone to UTC
	 */
	@Startup(phase = StartupPhase.DEPLOYMENT, order = Double.NEGATIVE_INFINITY, transactionMode = TransactionMode.NOT_SUPPORTED)
	public void setTimeZoneToUTC() {
		setTimeZone(TimeZone.getTimeZone("UTC"));

		LOGGER.info("Initialized default time zone to: {}({})", TimeZone.getDefault().getID(),
				TimeZone.getDefault().getDisplayName());
	}

	/**
	 * Sets the given time zone as default
	 *
	 * @param timeZone
	 *            the time zone to default
	 * @see TimeZone#setDefault(TimeZone)
	 */
	@SuppressWarnings("static-method")
	protected void setTimeZone(TimeZone timeZone) {
		TimeZone.setDefault(timeZone);
	}
}
