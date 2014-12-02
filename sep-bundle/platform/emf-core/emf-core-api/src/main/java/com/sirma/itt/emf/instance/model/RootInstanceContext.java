package com.sirma.itt.emf.instance.model;

/**
 * Marker interface only. The instances that implement the interface will be considered as root
 * contextual instance. This means that they will be considered as a root level objects. They will
 * be rendered as specific way. The root context cannot have a child of other root context.
 * 
 * @author BBonev
 */
public interface RootInstanceContext extends InstanceContext {

}
