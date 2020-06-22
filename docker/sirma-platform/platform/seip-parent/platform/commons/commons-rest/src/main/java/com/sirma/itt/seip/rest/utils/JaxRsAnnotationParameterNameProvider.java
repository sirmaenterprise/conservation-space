package com.sirma.itt.seip.rest.utils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.validation.ParameterNameProvider;
import javax.websocket.server.PathParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JaxRsAnnotationParameterNameProvider implements ParameterNameProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public List<String> getParameterNames(Constructor<?> constructor) {
		// this is called on each request and floods the log
		LOGGER.trace("Pameter name lookup not supported on constructor");
		return Collections.emptyList();
	}

	@Override
	public List<String> getParameterNames(Method method) {
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		if (paramAnnotations.length == 0) {
			return Collections.emptyList();
		}

		List<String> names = new ArrayList<>(paramAnnotations.length);
		for (int index = 0; index < paramAnnotations.length; index++) {
			Annotation[] annotations = paramAnnotations[index];
			names.add(JaxRsAnnotationParameterNameProvider.resolveParamName(index, annotations));
		}
		return names;
	}

	private static String resolveParamName(int index, Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			String name = JaxRsAnnotationParameterNameProvider.getNameFromAnnotation(annotation);
			if (name != null) {
				return name;
			}
		}
		return "param" + index;
	}

	private static String getNameFromAnnotation(Annotation annotation) {
		if (annotation instanceof HeaderParam) {
			return ((HeaderParam) annotation).value();
		} else if (annotation instanceof PathParam) {
			return ((PathParam) annotation).value();
		} else if (annotation instanceof QueryParam) {
			return ((QueryParam) annotation).value();
		}
		return null;
	}
}
