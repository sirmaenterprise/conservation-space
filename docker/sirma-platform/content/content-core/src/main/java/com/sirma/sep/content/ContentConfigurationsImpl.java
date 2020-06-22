package com.sirma.sep.content;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.content.ContentConfigurations;

/**
 * Default content configurations definitions
 *
 * @author BBonev
 */
@Singleton
public class ContentConfigurationsImpl implements ContentConfigurations {
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "file.upload.maxsize", type = Long.class, defaultValue = "10485760", label = "Max file size allowed for upload in bytes.")
	private ConfigurationProperty<Long> maxUploadSize;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "file.upload.max.concurrent.requests", type = Integer.class, defaultValue = "3", label = "Limits the number of concurrent uploads per user when uploading multiple files via the UI. IMPORTANT It is not recommended to set value above 5!")
	private ConfigurationProperty<Integer> maxConcurrentUploads;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "file.upload.max.simultaneous.files.count", type = Integer.class, defaultValue = "10", label = "Limits the number of files that are allowed to be uploaded at once per user via the UI.")
	private ConfigurationProperty<Integer> maxFilesToUploadAtOnce;

	@ConfigurationPropertyDefinition(password = true, system = true, sensitive = true, label = "Secret key used when constructing and deconstructing share codes added as an extra measurement of security")
	private static final String SHARE_CODE_SECRET_KEY = "content.sharecode.secret.key";

	@Inject
	@Configuration(SHARE_CODE_SECRET_KEY)
	private ConfigurationProperty<String> shareCodeSecretKey;

	@SuppressWarnings("unused")
	@ConfigurationConverter(SHARE_CODE_SECRET_KEY)
	static String buildShareCodeSecretKey(ConverterContext context, SecurityContext securityContext,
			ConfigurationManagement configurationManagement) {
		if (StringUtils.isEmpty(context.getRawValue())) {
			String key = RandomStringUtils.randomAlphanumeric(64);
			com.sirma.itt.seip.configuration.db.Configuration config = new com.sirma.itt.seip.configuration.db.Configuration(
					SHARE_CODE_SECRET_KEY, key);
			configurationManagement.addConfigurations(Arrays.asList(config));
			return key;
		}
		return context.getRawValue();
	}

	@Override
	public ConfigurationProperty<Long> getMaxFileSize() {
		return maxUploadSize;
	}

	@Override
	public ConfigurationProperty<Integer> getMaxConcurrentUploads() {
		return maxConcurrentUploads;
	}

	@Override
	public ConfigurationProperty<Integer> getMaxFilesToUploadAtOnce() {
		return maxFilesToUploadAtOnce;
	}

	@Override
	public ConfigurationProperty<String> getShareCodeSecretKey() {
		return shareCodeSecretKey;
	}
}
