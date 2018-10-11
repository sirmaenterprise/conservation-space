package com.sirma.itt.sep.instance.unique.persistence;

/**
 * Marks implemented classes as holder for unique field information.
 *
 * @author Boyan Tonchev.
 */
public interface UniqueField {

    /**
     * Getter for field uri.
     * @return field uri.
     */
    String getFieldUri();

    /**
     * Getter for definition id.
     * @return definition id.
     */
    String getDefinitionId();
}