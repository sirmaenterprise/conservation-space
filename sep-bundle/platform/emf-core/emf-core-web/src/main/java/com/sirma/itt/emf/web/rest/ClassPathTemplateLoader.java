package com.sirma.itt.emf.web.rest;

import java.net.URL;

import freemarker.cache.URLTemplateLoader;

/**
 * The Class ClassPathTemplateLoader.
 */
public class ClassPathTemplateLoader extends URLTemplateLoader
{
    
    /**
     * Gets the url.
     *
     * @param name the name
     * @return the url
     * @see freemarker.cache.URLTemplateLoader#getURL(java.lang.String)
     */
    protected URL getURL(String name)
    {
        return this.getClass().getClassLoader().getResource(name);
    }
}