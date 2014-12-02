/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.util.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.joda.time.DateTime;
import org.opensaml.xml.util.DatatypeHelper;

/**
 * A resource representing a file on the local filesystem.
 */
public class FilesystemResource implements Resource {
    
    /** The file represented by this resource. */
    private File resource;
    
    /**
     * Constructor.
     *
     * @param resourcePath the path to the file for this resource
     * 
     * @throws ResourceException thrown if the resource path is null or empty
     */
    public FilesystemResource(String resourcePath) throws ResourceException{
        if(DatatypeHelper.isEmpty(resourcePath)){
            throw new ResourceException("Resource path may not be null or empty");
        }
        
        resource = new File(resourcePath);
    }

    /** {@inheritDoc} */
    public boolean exists() throws ResourceException {
        return resource.exists();
    }

    /** {@inheritDoc} */
    public InputStream getInputStream() throws ResourceException {
        try{
            return new FileInputStream(resource);
        }catch(FileNotFoundException e){
            throw new ResourceException("Resource file does not exist: " + resource.getAbsolutePath());
        }
    }

    /** {@inheritDoc} */
    public DateTime getLastModifiedTime() throws ResourceException {
        if(!resource.exists()){
            throw new ResourceException("Resource file does not exist: " + resource.getAbsolutePath());
        }
        
        return new DateTime(resource.lastModified());
    }

    /** {@inheritDoc} */
    public String getLocation() {
        return resource.getAbsolutePath();
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return getLocation();
    }
    
    /** {@inheritDoc} */
    public int hashCode() {
        return getLocation().hashCode();
    }
    
    /** {@inheritDoc} */
    public boolean equals(Object o) {
        if(o == this){
            return true;
        }
        
        if(o instanceof FilesystemResource){
            return getLocation().equals(((ClasspathResource)o).getLocation());
        }
        
        return false;
    }
}