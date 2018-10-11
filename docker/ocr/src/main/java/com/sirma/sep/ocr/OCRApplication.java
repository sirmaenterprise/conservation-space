package com.sirma.sep.ocr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

/**
 * The OCR spring boot micro service entry point.
 *
 * @author bbanchev
 */
@SpringBootApplication
@EnableJms
public class OCRApplication {

	/**
	 * Invokes the {@link SpringApplication} initialization.
	 *
	 * @param args are the additional spring boot arguments.
	 */
	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(OCRApplication.class);
		springApplication.setAdditionalProfiles("service");
		springApplication.run(args);
	}

}
