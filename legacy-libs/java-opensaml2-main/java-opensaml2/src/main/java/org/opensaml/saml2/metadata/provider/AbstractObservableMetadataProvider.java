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

package org.opensaml.saml2.metadata.provider;

import java.util.ArrayList;
import java.util.List;

/**
 * An observable base implementation of metadata providers. An observer that clears the descriptor index kept by
 * {@link AbstractMetadataProvider} is registered during construction time.
 */
public abstract class AbstractObservableMetadataProvider extends AbstractMetadataProvider implements
        ObservableMetadataProvider {

    /** List of registered observers. */
    private ArrayList<Observer> observers;

    /** Constructor. */
    public AbstractObservableMetadataProvider() {
        super();
        observers = new ArrayList<Observer>();
        observers.add(new DescriptorIndexClearingObserver());
    }

    /** {@inheritDoc} */
    public List<Observer> getObservers() {
        return observers;
    }

    /**
     * Helper method for calling
     * {@link org.opensaml.saml2.metadata.provider.ObservableMetadataProvider.Observer#onEvent(MetadataProvider)} on
     * every registered Observer passing in this provider.
     */
    protected void emitChangeEvent() {
        synchronized (observers) {
            for (Observer observer : observers) {
                if (observer != null) {
                    observer.onEvent(this);
                }
            }
        }
    }

    /**
     * Observer that clears the descriptor index of this provider.
     */
    private class DescriptorIndexClearingObserver implements Observer {

        /** {@inheritDoc} */
        public void onEvent(MetadataProvider provider) {
            ((AbstractMetadataProvider) provider).clearDescriptorIndex();
        }
    }
}