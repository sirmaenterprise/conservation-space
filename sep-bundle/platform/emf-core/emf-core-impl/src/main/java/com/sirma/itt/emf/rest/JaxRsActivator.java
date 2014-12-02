package com.sirma.itt.emf.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS activator class. All REST services URL will begin with "/service".
 * 
 * @author Adrian Mitev
 */
@ApplicationPath("/service")
public class JaxRsActivator extends Application {

}