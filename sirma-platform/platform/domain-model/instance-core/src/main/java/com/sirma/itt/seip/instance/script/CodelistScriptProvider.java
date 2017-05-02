package com.sirma.itt.seip.instance.script;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;

/**
 * Codelist service script API provider
 *
 * @author Valeri Tishev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 15.006)
public class CodelistScriptProvider implements GlobalBindingsExtension {

	@Inject
	private CodelistService codelistService;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("codelist", this);
	}

	@Override
	public Collection<String> getScripts() {
		return Collections.emptyList();
	}

	/**
	 * Gets the description for the given codelist and code value based on the provided locale id.
	 *
	 * @param codelist the codelist number
	 * @param value the codelist value
	 * @param language the language to use for fetching the description
	 * @return The description according to locale. This may return null in cases where the codelist is null or there is
	 *         not such codelist in cache or there is no value found in the codelist for the key.
	 * @throws NullPointerException in case some of the passed parameters are {@code null}
	 */
	public String getDescription(Integer codelist, String value, String language) {
		Objects.requireNonNull(codelist, "Undefined codelist number.");
		Objects.requireNonNull(value, "Undefined codelist value.");
		Objects.requireNonNull(language, "Undefined language.");

		return codelistService.getDescription(codelist, value, language);
	}

}
