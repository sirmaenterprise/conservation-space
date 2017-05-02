package com.sirma.itt.seip.plugin;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import com.sirma.itt.seip.Named;

/**
 * The Class MockPlugins.
 *
 * @author Adrian Mitev
 */
public class MockPlugins {

	/** The Constant EXTENSION_POINT. */
	static final String EXTENSION_POINT = "mockExtensionPoint";

	/**
	 * Plugin with order 1 and default priority.
	 */
	@Extension(target = EXTENSION_POINT, order = 1)
	public static class Plugin1 implements Plugin {

	}

	/**
	 * Plugin with order 2 and default priority.
	 */
	@Extension(target = EXTENSION_POINT, order = 2)
	public static class Plugin2 implements Plugin {

	}

	/**
	 * Another Plugin with order 2 and default priority.
	 */
	@Extension(target = EXTENSION_POINT, order = 2)
	public static class Plugin2DefaultPriority implements Plugin {

	}

	/**
	 * Plugin with order 2 and higher priority.
	 */
	@Extension(target = EXTENSION_POINT, order = 2, priority = 2)
	public static class Plugin2Priority2 implements Plugin {

	}

	/**
	 * Another Plugin with order 2 and default priority but disabled.
	 */
	@Extension(target = EXTENSION_POINT, order = 2, priority = 2, enabled = false)
	public static class Plugin2Priority2Disabled implements Plugin {

	}

	/**
	 * The Interface MyQualifier.
	 */
	@Qualifier
	@Target({ TYPE, METHOD, PARAMETER, FIELD })
	@Retention(RUNTIME)
	@Documented
	public @interface MyQualifier {

	}

	/**
	 * The Interface MyQualifier2.
	 */
	@Qualifier
	@Target({ TYPE, METHOD, PARAMETER, FIELD })
	@Retention(RUNTIME)
	@Documented
	public @interface MyQualifier2 {

	}

	/**
	 * Plugin annotated with MyQualifier.
	 */
	@MyQualifier
	@Extension(target = EXTENSION_POINT, order = 1)
	public static class PluginWithQualifier implements Plugin {

	}

	/**
	 * Plugin annotated with MyQualifier2.
	 */
	@MyQualifier2
	@Extension(target = EXTENSION_POINT, order = 3)
	public static class PluginWithQualifier2 implements Plugin {

	}

	/**
	 * Plugin annotated with @MyQualifier and MyQualifier2.
	 */
	@MyQualifier
	@MyQualifier2
	@Extension(target = EXTENSION_POINT, order = 4)
	public static class PluginWith2Qualifiers implements Plugin {

	}

	/**
	 * Plugin not annotated with MyQualifier.
	 */
	@Extension(target = EXTENSION_POINT, order = 2)
	public static class PluginWithoutQualifier implements Plugin {

	}

	/**
	 * The Interface MyBindingQualifier.
	 */
	@Qualifier
	@Target({ TYPE, METHOD, PARAMETER, FIELD })
	@Retention(RUNTIME)
	@Documented
	public @interface MyBindingQualifier {

		/**
		 * Value.
		 */
		String value() default "";

		/**
		 * Non binding method.
		 */
		@Nonbinding
		String nonBindingMethod() default "";
	}

	/**
	 * Plugin annotated with MyBindingQualifier.
	 */
	@MyBindingQualifier("1")
	@Extension(target = EXTENSION_POINT, order = 1)
	public static class PluginWithBindingQualifier1 implements Plugin {

	}

	/**
	 * Plugin annotated with MyBindingQualifier.
	 */
	@MyBindingQualifier(value = "2", nonBindingMethod = "test")
	@Extension(target = EXTENSION_POINT, order = 2)
	public static class PluginWithBindingQualifier2 implements Plugin {

	}

	/**
	 * The Interface PluginWithName.
	 */
	public static interface PluginWithName extends Plugin, Named {

	}

	/**
	 * The Class PluginWithName1.
	 */
	public static class PluginWithName1 implements PluginWithName {

		@Override
		public String getName() {
			return "PluginWithName1";
		}
	}

	/**
	 * The Class PluginWithName2.
	 */
	public static class PluginWithName2 implements PluginWithName {

		@Override
		public String getName() {
			return "PluginWithName2";
		}
	}
}
