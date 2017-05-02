package com.sirma.cmf.web.actionsmanager;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * When actions are requested for given instance, its context is first evaluated and restored if possible. Then the
 * target instance is stored here for later access.
 *
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class ActionContext extends AbstractMap<String, Serializable>implements Serializable {

	private static final long serialVersionUID = -1501700937129982984L;

	private final Map<String, Serializable> context = new HashMap<String, Serializable>();

	/**
	 * Sets the action traget.
	 *
	 * @param instance
	 *            the new traget instance
	 */
	public void setActionTraget(Instance instance) {
		put("targetInstance", instance);
	}

	/**
	 * Gets the action target.
	 *
	 * @return the target instance
	 */
	public Instance getActionTarget() {
		return (Instance) get("targetInstance");
	}

	// Overriden AbstractMap methods

	@Override
	public boolean containsKey(Object key) {
		return context.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return context.containsValue(value);
	}

	@Override
	public Serializable get(Object key) {
		return context.get(key);
	}

	@Override
	public Serializable put(String key, Serializable value) {
		return context.put(key, value);
	}

	@Override
	public Serializable remove(Object key) {
		return context.remove(key);
	}

	@Override
	public void clear() {
		context.clear();
	}

	@Override
	public Set<String> keySet() {
		return context.keySet();
	}

	@Override
	public Collection<Serializable> values() {
		return context.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Serializable>> entrySet() {
		return context.entrySet();
	}

}
