package com.sirma.itt.emf.security.model;

import java.io.Serializable;
import java.security.Principal;

import com.sirma.itt.emf.resources.model.Resource;

/**
 * Resource that represents a group of users.
 * 
 * @author BBonev
 */
public interface Group extends Principal, Resource, Serializable {

}
