package com.sirma.itt.emf.definition.load;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Base template definition qualifier. The qualifier is used to identify
 * {@link DefinitionCompilerCallback} implementation that handles a template definitions.
 * 
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER })
public @interface TemplateDefinition {

}
