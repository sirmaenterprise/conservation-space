package com.sirma.sep.content.preview.mimetype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service resolving mime types to corresponding {@link MimeType} mapping.
 * <p>
 * This can be considered to be the source of truth of what mime type is and what is not supported by {@link
 * com.sirma.sep.content.preview.ContentPreviewApplication}.
 *
 * @author Mihail Radkov
 */
@Service
public class MimeTypesResolver {

	private final Collection<MimeType> mimeTypes;

	/**
	 * Constructs the resolver with the given {@link MimeTypesConfiguration} containing the available {@link MimeType}
	 * mapping.
	 * <p>
	 * The available {@link MimeType}s are validated and reversed to achieve easy overriding for specific {@link
	 * MimeType}.
	 *
	 * @param mimeTypesConfiguration
	 * 		- configuration containing the available {@link MimeType}s
	 */
	@Autowired
	public MimeTypesResolver(MimeTypesConfiguration mimeTypesConfiguration) {
		List<MimeType> loadedMimeTypes = new LinkedList<>(mimeTypesConfiguration.getMimeTypes());
		validateMimeTypes(loadedMimeTypes);
		Collections.reverse(loadedMimeTypes);
		this.mimeTypes = Collections.unmodifiableCollection(loadedMimeTypes);
	}

	/**
	 * Checks a provided mimetype against the mimetype mapping. The first matched {@link MimeType} will be returned or
	 * an empty {@link Optional} in case no one was found.
	 *
	 * @param mimeType
	 * 		the mime type to resolve
	 * @return a resolved {@link MimeType} or an empty {@link Optional}.
	 */
	public Optional<MimeType> resolve(String mimeType) {
		if (StringUtils.hasText(mimeType)) {
			return mimeTypes.stream().sequential().filter(m -> mimeType.matches(m.getName())).findFirst();
		}
		throw new IllegalArgumentException("Cannot accept null or empty mimetype!");
	}

	private static void validateMimeTypes(List<MimeType> mimeTypes) {
		mimeTypes.forEach(mimeType -> {
			String mimeTypeName = mimeType.getName();
			if (!StringUtils.hasText(mimeTypeName)) {
				throw new IllegalArgumentException("Mimetype mapping without value!");
			}
			Objects.requireNonNull(mimeType.getPreview(),
					"Mimetype " + mimeTypeName + " is with missing preview support value!");
			Objects.requireNonNull(mimeType.getThumbnail(),
					"Mimetype " + mimeTypeName + " is with missing thumbnail support value!");
		});
	}
}
