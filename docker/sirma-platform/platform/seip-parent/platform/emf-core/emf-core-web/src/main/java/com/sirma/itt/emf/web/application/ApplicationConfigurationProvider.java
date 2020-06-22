package com.sirma.itt.emf.web.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URLConnection;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.search.SearchConfiguration;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.content.ContentConfigurations;

/**
 * Exposes some application configurations in one place usable for referencing in web templates.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class ApplicationConfigurationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** default location for the resources folder */
	private static final String RESOURCES_LOCATION = "META-INF/resources/";
	private static final String BASE64_PART = ";base64,";

	private String contextPath;

	/**
	 * User help module access link.
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "help.module.link", system = true, label = "User help module access link.")
	private ConfigurationProperty<String> helpModuleLink;

	/** If footer should be visible. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "application.footer.enable", type = Boolean.class, defaultValue = "false", system = true, label = "If a footer should be visible on page.")
	private ConfigurationProperty<Boolean> applicationFooterEnable;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "javascript.debug", type = Boolean.class, defaultValue = "false", system = true, label = "Enables javascript debug mode where available.")
	private ConfigurationProperty<Boolean> javascriptDebug;

	@ConfigurationPropertyDefinition(defaultValue = "images/logo.png", label = "Path to an image to be used as logo in SEIP")
	private static final String APPLICATION_LOGO_IMAGE = "application.logo.image.path";

	@Inject
	@Configuration(APPLICATION_LOGO_IMAGE)
	private ConfigurationProperty<String> logoImageName;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "application.favicon.image.path", defaultValue = "images:favicon.png", label = "Path to an image to be used as favicon.")
	private ConfigurationProperty<String> faviconImageName;

	@Inject
	private ContentConfigurations contentConfigurations;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private SearchConfiguration searchConfiguration;

	@Inject
	private UserPreferences userPreferences;

	private ServletContext servletContext;

	@ConfigurationConverter(APPLICATION_LOGO_IMAGE)
	static String buildLogo(ConverterContext context, SystemConfiguration systemConfiguration) {
		String raw = context.getRawValue();
		String baseUrl = systemConfiguration.getSystemAccessUrl().get() + "/";
		if (isBase64Encoded(raw)) {
			if (isValidBase64Encoded(raw)) {
				return raw;
			}
			LOGGER.warn("Specified logo is not a valid base64 content. Will use the default logo!");
			return getLogoAsInternalResource(context.getDefaultValue(), baseUrl);
		}

		if (EqualsHelper.nullSafeEquals(context.getDefaultValue(), raw)) {
			return getLogoAsInternalResource(raw, baseUrl);
		}

		File external = new File(raw);
		if (external.isFile()) {
			if (external.length() > 524288L) {
				LOGGER.warn("External logo image is bigger than 512K and will not be loaded!");
				return getLogoAsInternalResource(context.getDefaultValue(), baseUrl);
			}
			return tryLoadingAsExternalFile(context, baseUrl, raw, external);
		}
		return tryLoadingAsLink(baseUrl, raw);
	}

	private static String tryLoadingAsExternalFile(ConverterContext context, String baseURL,
			String raw, File external) {
		Pair<String, String> nameAndExtension = FileUtil.splitNameAndExtension(raw);
		String extension = nameAndExtension.getSecond() == null ? "png" : nameAndExtension.getSecond();
		try (InputStream input = new FileInputStream(external)) {
			return readImageAsBase64("image/" + extension, input);
		} catch (IOException e) {
			LOGGER.warn("Could not load logo {}. Using default!", external, e);
		}
		return getLogoAsInternalResource(context.getDefaultValue(), baseURL);
	}

	private static String tryLoadingAsLink(String baseURL, String raw) {
		try {
			URI uri = URI.create(raw);
			URLConnection connection = uri.toURL().openConnection();
			String type = connection.getHeaderField("Content-Type");
			if (type == null || type.startsWith("image")) {
				try (InputStream input = connection.getInputStream()) {
					return readImageAsBase64(type, input);
				}
			}
		} catch (IllegalArgumentException | IOException e) {
			LOGGER.warn("Configuration value {} is not a link or could not open connection", raw, e);
		}
		return getLogoAsInternalResource(raw, baseURL);
	}

	private static String readImageAsBase64(String contentType, InputStream input) throws IOException {
		StringBuilder builder = new StringBuilder(4096);
		builder.append("data:");
		builder.append(contentType != null ? contentType : "image/png");
		builder.append(BASE64_PART);
		builder.append(Base64.getEncoder().encodeToString(IOUtils.toByteArray(input)));
		return builder.toString();
	}

	private static String getLogoAsInternalResource(String name, String baseURL) {
		StringBuilder builder = new StringBuilder();
		builder.append(RESOURCES_LOCATION);
		builder.append(name);
		InputStream imageStream = ApplicationConfigurationProvider.class
				.getClassLoader()
					.getResourceAsStream(builder.toString());
		String base64;
		try {
			base64 = readImageAsBase64(null, imageStream);
		} catch (NullPointerException | IOException e) {
			LOGGER.error("Could not convert image to base64.Returning the path to the image.", e);
			return builder.toString().replace(RESOURCES_LOCATION, baseURL);
		}
		return base64;
	}

	/**
	 * Checks if raw is base64 encoded string.
	 *
	 * @param raw
	 *            string to be tested
	 * @return true if is a valid base64 encoded string.
	 */
	static boolean isBase64Encoded(String raw) {
		return raw.contains(BASE64_PART);
	}

	/**
	 * Validates a string against a regex to check if it is a valid base64 string.
	 *
	 * @param raw
	 *            string that will be matched.
	 * @return true if whole string matches as a valid base64 string.
	 */
	private static boolean isValidBase64Encoded(String raw) {
		String base64 = raw.split(BASE64_PART)[1];
		Pattern pattern = Pattern.compile("^(?:[A-Za-z0-9+//]{4})*(?:[A-Za-z0-9+//]{2}==|[A-Za-z0-9+//]{3}=)?$");
		Matcher matcher = pattern.matcher(base64);
		if (matcher.matches()) {
			return true;
		}
		LOGGER.warn("String raw is not a valid base64 encoded string. Returning the default value");
		return false;
	}

	/**
	 * Captures the ServletContext for injection providing.
	 *
	 * @param event
	 *            ServletContextEvent.
	 */
	public void onApplicationStarted(@Observes ServletContextEvent event) {
		servletContext = event.getServletContext();
	}

	/**
	 * CDI producer for the ServletContext.
	 *
	 * @return available ServletContext.
	 */
	@Produces
	@ApplicationScoped
	public ServletContext produceServletContext() {
		return servletContext;
	}

	/**
	 * Getter method for contextPath.
	 *
	 * @return the contextPath
	 */
	public String getContextPath() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext == null) {
			return contextPath;
		}
		return facesContext.getExternalContext().getRequestContextPath();
	}

	/**
	 * Setter method for contextPath.
	 *
	 * @param contextPath
	 *            the contextPath to set
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Getter method for applicationModeDevelopement.
	 *
	 * @return the applicationModeDevelopement
	 */
	public Boolean getApplicationModeDevelopement() {
		return systemConfiguration.getApplicationMode().get();
	}

	/**
	 * Gets the application mode.
	 *
	 * @return the application mode
	 */
	public ConfigurationProperty<Boolean> getApplicationMode() {
		return systemConfiguration.getApplicationMode();
	}

	/**
	 * Getter method for applicationFooterEnable.
	 *
	 * @return the applicationFooterEnable
	 */
	public Boolean getApplicationFooterEnable() {
		return applicationFooterEnable.get();
	}

	/**
	 * Getter method for helpModuleLink.
	 *
	 * @return the helpModuleLink
	 */
	public String getHelpModuleLink() {
		return helpModuleLink.get();
	}

	/**
	 * Getter method for javascriptDebug.
	 *
	 * @return the javascriptDebug
	 */
	public Boolean getJavascriptDebug() {
		return javascriptDebug.get();
	}

	/**
	 * Getter method for sessionTimeoutPeriod.
	 *
	 * @return the sessionTimeoutPeriod
	 */
	public Integer getSessionTimeoutPeriod() {
		return userPreferences.getSessionTimeout();
	}

	/**
	 * Getter method for pagerPageSize.
	 *
	 * @return the pagerPageSize
	 */
	public int getPagerPageSize() {
		return searchConfiguration.getPagerPageSize();
	}

	/**
	 * Getter method for pagerMaxPages.
	 *
	 * @return the pagerMaxPages
	 */
	public int getPagerMaxPages() {
		return searchConfiguration.getPagerMaxPages();
	}

	/**
	 * Gets the max upload size.
	 *
	 * @return the max upload size
	 */
	public long getMaxUploadSize() {
		return contentConfigurations.getMaxFileSize().get().longValue();
	}

	/**
	 * @return the searchResultMaxSize
	 */
	public Integer getSearchResultMaxSize() {
		return searchConfiguration.getSearchResultMaxSize();
	}

	/**
	 * @return the searchFacetResultExceedDisable
	 */
	public Boolean getSearchFacetResultExceedDisable() {
		return searchConfiguration.getSearchFacetResultExceedDisable();
	}

	/**
	 * Getter method for baseURL.
	 *
	 * @return the baseURL
	 */
	public String getBaseURL() {
		return systemConfiguration.getSystemAccessUrl().get() + "/";
	}

	/**
	 * Gets the server address.
	 *
	 * @return the server address
	 */
	public String getServerAddress() {
		return systemConfiguration.getSystemAccessUrl().get().toString();
	}

	/**
	 * Gets the logo image name.
	 *
	 * @return the logo image name
	 */
	public String getLogoImageName() {
		return logoImageName.get();
	}

	/**
	 * Gets the favicon image name.
	 *
	 * @return the favicon image name
	 */
	public String getFaviconImageName() {
		return faviconImageName.get();
	}

	/**
	 * Gets the ui2 url.
	 *
	 * @return the ui2 url
	 */
	public String getUi2Url() {
		return systemConfiguration.getUi2Url().requireConfigured("UI2 address is not configured!").get();
	}

	/**
	 * Gets the ui2 url.
	 *
	 * @return the ui2 url
	 */
	public String getUi2EntityOpenUrl() {
		return getUi2Url() + "#/idoc";
	}

	/**
	 * Getter for ui2 main search location
	 *
	 * @return url
	 */
	public String getUi2SearchOpenUrl() {
		return getUi2Url() + "#/search";
	}
}
