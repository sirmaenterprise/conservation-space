/**
 * Copyright (c) 2008 13.08.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.archives;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * ClassLoader for loading classes from jar file. The jar file is iterated and
 * every instance with <code>.class</code> extension is tried to be loaded. If
 * the loading fails or the jar entry is corrupt(invalid class format), the file
 * is skipped.
 * 
 * @author Hristo Iliev
 */
public final class JarFileClassLoader extends ClassLoader {
    /** Jar file which contains the classes to be loaded. */
    private final JarFile jarFile;

    /** list with failed to be loaded entries. */
    private List<String> failedEntries = new ArrayList<String>();

    /**
     * Create an jar class loader instance for specified jar file. The parent
     * class loader is the class loader used to load this class.
     * 
     * @param jarFile
     *                {@link JarFile}, jar file from where to load the classes
     */
    public JarFileClassLoader(JarFile jarFile) {
	super(JarFileClassLoader.class.getClassLoader());
	this.jarFile = jarFile;
    }

    /**
     * Create an jar class loader instance for specified jar file.
     * 
     * @param jarFile
     *                {@link JarFile}, jar file from where to load the classes
     * @param parent
     *                {@link ClassLoader}, parent class loader
     */
    public JarFileClassLoader(JarFile jarFile, ClassLoader parent) {
	super(parent);
	this.jarFile = jarFile;
    }

    /**
     * Create an jar class loader instance for specified jar file. The parent
     * class loader is the class loader used to load this class.
     * 
     * @param jarFile2
     *                {@link String}, name of the jar file from where to load
     *                the classes
     * @throws IOException
     *                 thrown if cannot create {@link JarFile} with specified
     *                 name
     */
    public JarFileClassLoader(String jarFile2) throws IOException {
	this(new JarFile(jarFile2));
    }

    /**
     * Create an jar class loader instance for specified jar file.
     * 
     * @param jarFile2
     *                {@link String}, name of the jar file from where to load
     *                the classes
     * @param parent
     *                {@link ClassLoader}, parent class loader
     * @throws IOException
     *                 thrown if cannot create {@link JarFile} with specified
     *                 name
     */
    public JarFileClassLoader(String jarFile2, ClassLoader parent)
	    throws IOException {
	this(new JarFile(jarFile2), parent);
    }

    @Override
    protected Class<?> findClass(String name) {
	try {
	    JarEntry entry = jarFile.getJarEntry(name.replace('.', '/')
		    + ".class"); //$NON-NLS-1$
	    InputStream input = jarFile.getInputStream(entry);
	    byte[] buffer = new byte[1024];
	    ByteArrayOutputStream out = new ByteArrayOutputStream(buffer.length);
	    int length = input.read(buffer);
	    while (length > 0) {
		out.write(buffer, 0, length);
		length = input.read(buffer);
	    }
	    buffer = out.toByteArray();
	    return super.defineClass(name, buffer, 0, buffer.length);
	} catch (ClassFormatError e) {
	    return null;
	} catch (IOException e) {
	    return null;
	}
    }

    /**
     * Load all classes from the jar file.
     * 
     * @return list of classes in the jar file
     */
    public List<Class<?>> loadClasses() {
	Enumeration<JarEntry> entries = jarFile.entries();
	List<Class<?>> result = new ArrayList<Class<?>>();
	JarEntry entry;
	String entryName = null;
	while (entries.hasMoreElements()) {
	    try {
		entry = entries.nextElement();
		entryName = entry.getName();
		if (entryName.endsWith(".class")) { //$NON-NLS-1$
		    result.add(loadClass(entryName.substring(0,
			    entryName.length() - 6).replace('/', '.')));
		}
	    } catch (ClassNotFoundException e) {
		if (entryName != null) {
		    failedEntries.add(entryName);
		}
	    } catch (NoClassDefFoundError e) {
		if (entryName != null) {
		    failedEntries.add(entryName);
		}
	    }
	}
	return result;
    }

    /**
     * Retrieve the list of names of entries which was not be loaded. This
     * method is depends from {@link #loadClasses()}. The returned list
     * contains all entries which was not be loaded.
     * 
     * @return list with failed to be loaded entries
     * @see #loadClasses()
     */
    public List<String> getFailedEntries() {
	return failedEntries;
    }

    /**
     * Retrieve the name of the jar file from which classes are loaded.
     * 
     * @return {@link JarFile}, used jar file
     */
    public JarFile getJarFile() {
	return jarFile;
    }

}
