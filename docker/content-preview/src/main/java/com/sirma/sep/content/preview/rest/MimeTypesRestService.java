package com.sirma.sep.content.preview.rest;

import com.sirma.sep.content.preview.mimetype.MimeType;
import com.sirma.sep.content.preview.mimetype.MimeTypesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * End point for filtering available mime types in the {@link com.sirma.sep.content.preview.ContentPreviewApplication}.
 *
 * @author Mihail Radkov
 */
@RestController
@RequestMapping("/content/mimetypes")
public class MimeTypesRestService {

	private final MimeTypesResolver mimeTypesService;

	@Autowired
	public MimeTypesRestService(MimeTypesResolver mimeTypesService) {
		this.mimeTypesService = mimeTypesService;
	}

	/**
	 * Filters the available {@link MimeType}s with the provided mime type filter.
	 * <p>
	 * If no one was found the service will respond with {@link HttpStatus#NOT_FOUND}.
	 * <p>
	 * If the provided mime type filter is not a valid string, the service will return {@link HttpStatus#BAD_REQUEST}
	 *
	 * @param mimeType
	 * 		- the mime type filter
	 * @return a {@link ResponseEntity} carrying either the resolved mime type or the error code
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity filterMimetype(@RequestParam(name = "filter") String mimeType) {
		if (!StringUtils.hasText(mimeType)) {
			return ResponseEntity.badRequest().build();
		}

		Optional<MimeType> supportedMimeType = mimeTypesService.resolve(mimeType);
		if (supportedMimeType.isPresent()) {
			return ResponseEntity.ok(supportedMimeType.get());
		}

		return ResponseEntity.notFound().build();
	}

}
