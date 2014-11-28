/**
 * Copyright (c) 2009 04.01.2009 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * Utility class for specific operations with reflection.
 * 
 * @author Hristo Iliev
 */
public final class ReflectionUtils {

    /**
     * Hide utility constructor
     */
    private ReflectionUtils() {
        // Hide utility constructor
    }

    /**
     * Set field in the specified object. If the field is not in the objects
     * class, it is checked all superclass's fields. If it is not found there
     * again it is checked it's superclass and so on. If setting of field is not
     * possible either because the field does not exist in the class hierarchy
     * of the object or the <code>value</code> is not valid type,
     * {@link ReflectionException} is thrown.
     * 
     * @param object
     *            {@link Object}, the object which field should be set
     * @param fieldName
     *            {@link String}, name of the field to be set
     * @param value
     *            {@link Object}, the new value
     * @throws ReflectionException
     *             thrown if the setting of the field is not possible
     */
    public static void setField(Object object, String fieldName, Object value) {
        try {
            Field field = getClassField(object.getClass(), fieldName);
            boolean accessability = field.isAccessible();
            field.setAccessible(true);
            field.set(object, value);
            field.setAccessible(accessability);
        } catch (NoSuchFieldException e) {
            throw new ReflectionException(e);
        } catch (IllegalArgumentException e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Get field in the specified object. If the field is not in the objects
     * class, it is checked all superclass's fields. If it is not found there
     * again it is checked it's superclass and so on. If getting of field is not
     * possible either because the field does not exist in the class hierarchy
     * of the object {@link ReflectionException} is thrown.
     * 
     * @param object
     *            {@link Object}, the object which field should be gotten
     * @param fieldName
     *            {@link String}, name of the field to be gotten
     * @return {@link Object}, the value of the field
     * @throws ReflectionException
     *             thrown if the setting of the field is not possible
     */
    public static Object getField(Object object, String fieldName) {
        try {
            Field field = getClassField(object.getClass(), fieldName);
            boolean accessability = field.isAccessible();
            field.setAccessible(true);
            Object result = field.get(object);
            field.setAccessible(accessability);
            return result;
        } catch (NoSuchFieldException e) {
            throw new ReflectionException(e);
        } catch (IllegalArgumentException e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Call setter method of specified property. The setter method is called
     * with <code>value</code> as parameter. To construct the name of the
     * setter, the first character of the <code>property</code> is converted to
     * upper case, and <code>set</code> is appended at the beginning. The so
     * created method name is searched in the whole class hierarchy of the
     * object, starting from the object's class.
     * 
     * @param object
     *            {@link Object}, object which setter should be called
     * @param property
     *            {@link String}, name of the property which should be set
     * @param value
     *            {@link Object}, value with which should be called the setter
     */
    public static void setProperty(Object object, String property, Object value) {
        String methodName = "set" + Character.toUpperCase(property.charAt(0)) //$NON-NLS-1$
                + property.substring(1);
        try {
            Method method = getClassMethod(object.getClass(), methodName,
                    value.getClass());
            boolean accessability = method.isAccessible();
            method.setAccessible(true);
            method.invoke(object, value);
            method.setAccessible(accessability);
        } catch (NoSuchMethodException e) {
            throw new ReflectionException(e);
        } catch (IllegalArgumentException e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        } catch (InvocationTargetException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Call getter method of specified property. To construct the name of the
     * getter, the first character of the <code>property</code> is converted to
     * upper case, and <code>get</code> is appended at the beginning. The so
     * created method name is searched in the whole class hierarchy of the
     * object, starting from the object's class.
     * 
     * @param object
     *            {@link Object}, object which getter should be called
     * @param property
     *            {@link String}, name of the property which should be gotten
     * @return value {@link Object}, the retrieved from the getter value
     */
    public static Object getProperty(Object object, String property) {
        String methodName = "get" + Character.toUpperCase(property.charAt(0)) //$NON-NLS-1$
                + property.substring(1);
        try {
            Method method = getClassMethod(object.getClass(), methodName);
            boolean accessability = method.isAccessible();
            method.setAccessible(true);
            Object result = method.invoke(object);
            method.setAccessible(accessability);
            return result;
        } catch (NoSuchMethodException e) {
            throw new ReflectionException(e);
        } catch (IllegalArgumentException e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        } catch (InvocationTargetException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Find specific method in the whole class hierarchy. The method is searched
     * beginning from the <code>objectClass</code> until reaching {@link Object}
     * . The first found method is returned.
     * 
     * @param objectClass
     *            {@link Class}, class of the object which will be searched for
     *            the method
     * @param methodName
     *            {@link String}, name of the method
     * @param parameters
     *            {@link Class}..., class of the parameters
     * @return {@link Method}, founded method
     * @throws NoSuchMethodException
     *             thrown if there is no such method in the class hierarchy
     */
    public static Method getClassMethod(Class<?> objectClass,
            String methodName, Class<?>... parameters)
            throws NoSuchMethodException {
        Class<?> clazz = objectClass;
        if (clazz == null) {
            throw new IllegalArgumentException("Object class must not be null"); //$NON-NLS-1$
        }
        Method method = null;
        do {
            try {
                method = clazz.getDeclaredMethod(methodName, parameters);
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
                if (clazz == null) {
                    throw e;
                }
            }
        } while (method == null);
        return method;
    }

    /**
     * Find specific field in the whole class hierarchy. The field is searched
     * beginning from the <code>objectClass</code> until reaching {@link Object}
     * . The first found field is returned.
     * 
     * @param objectClass
     *            {@link Class}, class of the object which will be searched for
     *            the field
     * @param fieldName
     *            {@link String}, name of the field
     * @return {@link Field}, founded field
     * @throws NoSuchFieldException
     *             thrown if there is no such field in the class hierarchy
     */
    public static Field getClassField(Class<?> objectClass, String fieldName)
            throws NoSuchFieldException {
        Class<?> clazz = objectClass;
        if (clazz == null) {
            throw new IllegalArgumentException("Object class must not be null"); //$NON-NLS-1$
        }
        Field field = null;
        do {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
                if (clazz == null) {
                    throw e;
                }
            }
        } while (field == null);
        return field;
    }

    /**
     * Retrieve list of all methods of the class (including those with private,
     * package and protected visibility).
     * 
     * @param clazz
     *            {@link Class}, class which methods will be retrieved
     * @return {@link List}, list with the methods
     */
    public static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> result = new LinkedList<Method>();
        Class<?> type = clazz;
        while (type != null) {
            Method[] methods = type.getDeclaredMethods();
            for (Method method : methods) {
                result.add(method);
            }
            type = type.getSuperclass();
        }
        return result;
    }

    /**
     * Retrieve list of all fields of the class (including those with private,
     * package and protected visibility).
     * 
     * @param clazz
     *            {@link Class}, class which fields will be retrieved
     * @return {@link List}, list with the fields
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> result = new LinkedList<Field>();
        Class<?> type = clazz;
        while (type != null) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                result.add(field);
            }
            type = type.getSuperclass();
        }
        return result;
    }

    /**
     * Retrieve the getter method of a property. It will be find either getters
     * which start with 'get' or with 'is'.
     * 
     * @param clazz
     *            {@link Class}, class which will be searched for getter
     * @param fieldName
     *            {@link String}, name of the field.
     * @return {@link Method}, the getter method
     */
    public static Method getPropertyGetter(Class<?> clazz, String fieldName) {
        String fieldUpperCase = StringUtils.upperFirstChar(fieldName);
        String getMethodName = "get" + fieldUpperCase;
        Method getter;
        try {
            getter = clazz.getMethod(getMethodName);
        } catch (SecurityException e) {
            throw new ReflectionException(
                    "Error while accessing getter for field: " + fieldName, e);
        } catch (NoSuchMethodException e) {
            try {
                String isMethodName = "is" + fieldUpperCase;
                getter = clazz.getMethod(isMethodName);
            } catch (SecurityException e1) {
                throw new ReflectionException(
                        "Error while accessing getter for field: " + fieldName,
                        e);
            } catch (NoSuchMethodException e1) {
                throw new ReflectionException("No getter for field: "
                        + fieldName);
            }
        }
        return getter;
    }

}
