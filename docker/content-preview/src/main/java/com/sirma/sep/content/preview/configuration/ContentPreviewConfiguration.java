package com.sirma.sep.content.preview.configuration;

import com.sirma.sep.content.preview.util.FileUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.File;

/**
 * Global configurations for {@link com.sirma.sep.content.preview.ContentPreviewApplication}.
 *
 * @author Mihail Radkov
 */
@Validated
@Component
@ConfigurationProperties(prefix = "content.preview")
public class ContentPreviewConfiguration {

	@NotNull
	private File tempFolder;

	@NotNull
	private String thumbnailFormat;

	@Min(1000)
	private Long timeout;

	@PostConstruct
	public void verify() {
		if (!tempFolder.exists()) {
			FileUtils.createDirectory(tempFolder);
		}
	}

	public File getTempFolder() {
		return tempFolder;
	}

	public void setTempFolder(File tempFolder) {
		this.tempFolder = tempFolder;
	}

	public String getThumbnailFormat() {
		return thumbnailFormat;
	}

	public void setThumbnailFormat(String thumbnailFormat) {
		this.thumbnailFormat = thumbnailFormat;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}
}
