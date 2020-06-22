package com.sirma.itt.sep.content.extraction.patch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.cmf.content.extract.TikaContentExtractor;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Patch to update value of configuration {@link TikaContentExtractor#MIMETYPE_PATTERN_ID}.<br>
 * It fetch oldValue of configuration and update it as:
 * <ol>
 * <li>if change set parameter addMimetypes is set the mimetypes in it will be added.</li>
 * <li>if change set parameter removeMimetypes is set the mimetypes in it will be removed.</li>
 * </ol>
 *
 * Change set parameters {@link #addMimetypes} and {@link #removeMimetypes} have to be comma separated lists of mimetypes.
 *
 * @author Boyan Tonchev.
 */
public class UpdateMimeTypePatternTask implements CustomTaskChange {

	private static final String PREFIX = "^(?!";
	private static final String SUFFIX = ").+";

	private static final String COMMA_SEPARATOR = ",";

	private List<String> addMimetypes = Collections.emptyList();
	private List<String> removeMimetypes = Collections.emptyList();

	private TransactionSupport transactionSupport;
	private ConfigurationManagement configurationManagement;

	@Override
	public void setUp() throws SetupException {
		configurationManagement = CDI.instantiateBean(ConfigurationManagement.class, CDI.getCachedBeanManager(),
													  CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
												 CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		if (addMimetypes.isEmpty() && removeMimetypes.isEmpty()) {
			// there are nothing to update.
			return;
		}
		transactionSupport.invokeInNewTx(this::updateConfiguration);

	}

	private void updateConfiguration() {
		Configuration configuration = fetchOldValue();
		if (configuration == null) {
			// there is nothing to be updated.
			return;
		}
		String oldPattern = configuration.getRawValue();
		if (oldPattern == null) {
			//there is not configuration of mimetype pattern so we can't add or remove.
			return;
		}

		oldPattern = oldPattern.replace(PREFIX, "");
		oldPattern = oldPattern.replace(SUFFIX, "");
		List<String> oldMimetypes = toList(oldPattern, "\\|");
		oldMimetypes.removeAll(removeMimetypes);
		oldMimetypes.addAll(addMimetypes);
		String newPattern = PREFIX + oldMimetypes.stream().collect(Collectors.joining("|")) + SUFFIX;
		configuration.setValue(newPattern);
		configurationManagement.updateSystemConfiguration(configuration);
	}

	private Configuration fetchOldValue() {
		return configurationManagement.getSystemConfigurations()
				.stream()
				.filter(configuration -> configuration.getConfigurationKey()
						.equals(TikaContentExtractor.MIMETYPE_PATTERN_ID))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Sets mimetypes which have to be added from "content.extract.tika.mimetype.pattern" configuration.
	 *
	 * @param mimetypesToBeAdded - comma separated mimetypes.
	 */
	public void setAddMimetypes(String mimetypesToBeAdded) {
		this.addMimetypes = toList(mimetypesToBeAdded, COMMA_SEPARATOR);
	}

	/**
	 * Sets mimetypes which have to be removed from "content.extract.tika.mimetype.pattern" configuration.
	 *
	 * @param mimetypesToBeRemuved - comma separated mimetypes.
	 */
	public void setRemoveMimetypes(String mimetypesToBeRemuved) {
		this.removeMimetypes = toList(mimetypesToBeRemuved, COMMA_SEPARATOR);
	}

	public List<String> toList(String values, String separator) {
		if (values == null || StringUtils.isBlank(values)) {
			return Collections.emptyList();
		}
		return Arrays.stream(values.split(separator))
				.map(String::trim)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toList());
	}

	@Override
	public String getConfirmationMessage() {
		return null;
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
}
