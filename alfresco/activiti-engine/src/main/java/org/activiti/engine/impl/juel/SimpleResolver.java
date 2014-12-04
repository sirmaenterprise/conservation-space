/*
 * Based on JUEL 2.2.1 code, 2006-2009 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.juel;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.javax.el.ArrayELResolver;
import org.activiti.engine.impl.javax.el.BeanELResolver;
import org.activiti.engine.impl.javax.el.CompositeELResolver;
import org.activiti.engine.impl.javax.el.DynamicBeanPropertyELResolver;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.javax.el.ListELResolver;
import org.activiti.engine.impl.javax.el.MapELResolver;
import org.activiti.engine.impl.javax.el.ResourceBundleELResolver;

// TODO: Auto-generated Javadoc
/**
 * Simple resolver implementation. This resolver handles root properties (top-level identifiers).
 * Resolving "real" properties (<code>base != null</code>) is delegated to a resolver specified at
 * construction time.
 * 
 * @author Christoph Beck
 */
public class SimpleResolver extends ELResolver {
	
	/** The Constant DEFAULT_RESOLVER_READ_ONLY. */
	private static final ELResolver DEFAULT_RESOLVER_READ_ONLY = new CompositeELResolver() {
		{
			add(new ArrayELResolver(true));
			add(new ListELResolver(true));
			add(new MapELResolver(true));
			add(new ResourceBundleELResolver());
      add(new DynamicBeanPropertyELResolver(true, ItemInstance.class, "getFieldValue", "setFieldValue"));
			add(new BeanELResolver(true));
		}
	};
	
	/** The Constant DEFAULT_RESOLVER_READ_WRITE. */
	private static final ELResolver DEFAULT_RESOLVER_READ_WRITE = new CompositeELResolver() {
		{
			add(new ArrayELResolver(false));
			add(new ListELResolver(false));
			add(new MapELResolver(false));
			add(new ResourceBundleELResolver());
      add(new DynamicBeanPropertyELResolver(false, ItemInstance.class, "getFieldValue", "setFieldValue"));
			add(new BeanELResolver(false));
		}
	};

	/** The root. */
	private final RootPropertyResolver root;
	
	/** The delegate. */
	private final CompositeELResolver delegate;

	/**
	 * Create a resolver capable of resolving top-level identifiers. Everything else is passed to
	 * the supplied delegate.
	 *
	 * @param resolver the resolver
	 * @param readOnly the read only
	 */
	public SimpleResolver(ELResolver resolver, boolean readOnly) {
		delegate = new CompositeELResolver();
		delegate.add(root = new RootPropertyResolver(readOnly));
		delegate.add(resolver);
	}

	/**
	 * Create a read/write resolver capable of resolving top-level identifiers. Everything else is
	 * passed to the supplied delegate.
	 *
	 * @param resolver the resolver
	 */
	public SimpleResolver(ELResolver resolver) {
		this(resolver, false);
	}

	/**
	 * Create a resolver capable of resolving top-level identifiers, array values, list values, map
	 * values, resource values and bean properties.
	 *
	 * @param readOnly the read only
	 */
	public SimpleResolver(boolean readOnly) {
		this(readOnly ? DEFAULT_RESOLVER_READ_ONLY : DEFAULT_RESOLVER_READ_WRITE, readOnly);
	}

	/**
	 * Create a read/write resolver capable of resolving top-level identifiers, array values, list
	 * values, map values, resource values and bean properties.
	 */
	public SimpleResolver() {
		this(DEFAULT_RESOLVER_READ_WRITE, false);
	}

	/**
	 * Answer our root resolver which provides an API to access top-level properties.
	 * 
	 * @return root property resolver
	 */
	public RootPropertyResolver getRootPropertyResolver() {
		return root;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.javax.el.ELResolver#getCommonPropertyType(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object)
	 */
	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return delegate.getCommonPropertyType(context, base);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.javax.el.ELResolver#getFeatureDescriptors(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object)
	 */
	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return delegate.getFeatureDescriptors(context, base);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.javax.el.ELResolver#getType(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		return delegate.getType(context, base, property);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.javax.el.ELResolver#getValue(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		return delegate.getValue(context, base, property);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.javax.el.ELResolver#isReadOnly(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return delegate.isReadOnly(context, base, property);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.javax.el.ELResolver#setValue(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		delegate.setValue(context, base, property, value);
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.javax.el.ELResolver#invoke(org.activiti.engine.impl.javax.el.ELContext, java.lang.Object, java.lang.Object, java.lang.Class[], java.lang.Object[])
	 */
	@Override
	public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
		return delegate.invoke(context, base, method, paramTypes, params);
	};
}
