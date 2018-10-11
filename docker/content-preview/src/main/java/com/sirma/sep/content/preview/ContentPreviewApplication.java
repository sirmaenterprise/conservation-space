package com.sirma.sep.content.preview;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jms.annotation.EnableJms;

/**
 * SpringBoot service for generating previews and thumbnails of given documents.
 * <p>
 * See {@link com.sirma.sep.content.preview.messaging.JMSContentReceiver} about receiving & dispatching content preview
 * requests See {@link com.sirma.sep.content.preview.service.ContentPreviewService} about handling content preview
 * requests
 *
 * @author Mihail Radkov
 * @see com.sirma.sep.content.preview.messaging.JMSContentReceiver
 * @see com.sirma.sep.content.preview.service.ContentPreviewService
 */
@SpringBootApplication
@EnableJms
public class ContentPreviewApplication {

	/**
	 * Entry point of the content preview application. Uses any provided arguments to instantiate the {@link
	 * SpringApplication}.
	 *
	 * @param args
	 * 		- provided command line arguments to the executable
	 */
	public static void main(String[] args) {
		SpringApplication previewApplication = new SpringApplicationBuilder(ContentPreviewApplication.class).bannerMode(
				Banner.Mode.OFF).properties("spring.config.name=application,mimetypes").build();
		previewApplication.run(args);
	}
}
