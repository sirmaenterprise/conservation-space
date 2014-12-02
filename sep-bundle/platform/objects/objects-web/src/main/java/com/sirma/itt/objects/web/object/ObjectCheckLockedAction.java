package com.sirma.itt.objects.web.object;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityAction;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.rest.model.ViewInstance;

/**
 * Checks if object instance is locked.
 */
@Named
@ViewAccessScoped
public class ObjectCheckLockedAction extends EntityAction implements Serializable {

	private static final long serialVersionUID = 3862657396640559541L;
	
	@Inject
	private transient TypeConverter typeConverter;

	/**
	 * Checks if object is locked.
	 *
	 * @param objectInstance the object instance
	 * @return true, if is locked
	 */
	public boolean isLocked(Instance objectInstance) {
		boolean isLocked = false;
		ViewInstance viewInstance = typeConverter.convert(ViewInstance.class, objectInstance);
		if (viewInstance != null) {
			isLocked = viewInstance.isLocked();
		}
		return isLocked;
	}
}
