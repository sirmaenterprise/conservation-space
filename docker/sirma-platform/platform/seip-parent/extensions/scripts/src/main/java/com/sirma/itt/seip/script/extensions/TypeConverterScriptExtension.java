package com.sirma.itt.seip.script.extensions;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;

/**
 * Extension to provide access to {@link TypeConverter} functions in JavaScript.
 *
 * @author BBonev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 5.1)
public class TypeConverterScriptExtension implements GlobalBindingsExtension {
	private static final String DEFAULT_CONVERTER_JS = "type-converter.js";
	@Inject
	private TypeConverter typeConverter;

	@Override
	public Map<String, Object> getBindings() {
		Map<String, Object> bindings = CollectionUtils.createHashMap(2);
		bindings.put("converter", typeConverter);
		return bindings;
	}

	@Override
	public Collection<String> getScripts() {
		return ResourceLoadUtil.loadResources(getClass(), DEFAULT_CONVERTER_JS);
	}

}
