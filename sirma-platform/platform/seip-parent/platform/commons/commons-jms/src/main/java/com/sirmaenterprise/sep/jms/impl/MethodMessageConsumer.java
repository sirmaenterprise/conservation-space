package com.sirmaenterprise.sep.jms.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.jms.JMSContext;
import javax.jms.Message;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirmaenterprise.sep.jms.api.MessageConsumer;

/**
 * Message consumer implementation that calls the specified method on message arrival
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 15/05/2017
 */
public class MethodMessageConsumer implements MessageConsumer {

	private final Method method;

	/**
	 * Instantiate new message consumer that corresponds to the given method.
	 *
	 * @param method the method to be called upon message arrival
	 */
	public MethodMessageConsumer(Method method) {
		this.method = method;
		if (!isAcceptable(method)) {
			throw new IllegalArgumentException("The provided method should have argument that accepts a "
					+ Message.class);
		}
	}

	@Override
	public void accept(Message message, JMSContext context) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		Object[] args = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			if (parameterTypes[i].isAssignableFrom(Message.class)) {
				args[i] = message;
			} else if (parameterTypes[i].isAssignableFrom(JMSContext.class)) {
				args[i] = context;
			}
		}
		Instance<?> instance = CDI.current().select(method.getDeclaringClass());
		try {
			method.setAccessible(true);
			method.invoke(instance.get(), args);
		} catch (RollbackedRuntimeException e) {
			throw e;
		} catch (InvocationTargetException e) { // NOSONAR
			// unwrap the exception from the called method
			throw new RollbackedRuntimeException(e.getTargetException());
		} catch (Exception e) {
			throw new RollbackedRuntimeException(e);
		}
	}

	@Override
	public Class<? extends Message> getExpectedType() {
		Class<?>[] types = method.getParameterTypes();
		for (int i = 0; i < types.length; i++) {
			if (Message.class.isAssignableFrom(types[i])) {
				return (Class<? extends Message>) types[i];
			}
		}
		return null;
	}

	/**
	 * Checks if the given method is acceptable by the implementation. It checks if the method has at least one
	 * argument that is of type {@link Message} or one of it's sub types.
	 *
	 * @param method the method to check
	 * @return true if the method could be used to construct new instances and false if it's invalid
	 */
	public static boolean isAcceptable(Method method) {
		Class<?>[] types = method.getParameterTypes();
		for (int i = 0; i < types.length; i++) {
			if (Message.class.isAssignableFrom(types[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return new StringBuilder(128)
				.append("MethodMessageConsumer{")
				.append(method.getDeclaringClass())
				.append('.')
				.append(method.getName())
				.append('}')
				.toString();
	}
}
