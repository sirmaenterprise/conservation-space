package com.sirma.itt.emf.solr.remote;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * The SolrMaster is qualifier for accessing master node
 *
 * @author bbanchev
 */
@Qualifier
@Retention(RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
public @interface SolrMaster {

}
