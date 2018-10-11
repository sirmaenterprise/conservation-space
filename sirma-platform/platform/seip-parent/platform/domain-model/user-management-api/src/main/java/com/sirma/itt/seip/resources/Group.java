package com.sirma.itt.seip.resources;

import java.io.Serializable;
import java.security.Principal;

/**
 * Resource that represents a group of users.
 *
 * @author BBonev
 */
public interface Group extends Principal, Resource, Serializable {

}
