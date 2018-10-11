/**
 * The package contains events fired during instance view and generic content persist. Observers could be annotated with
 * {@link javax.ejb.Asynchronous} for parallel processing. All of the events observers should thread the data provided
 * by the events as read only. If modifications are needed then extension implementation of
 * {@link com.sirma.sep.content.InstanceViewPreProcessor} is needed.
 *
 * @author BBonev
 */
package com.sirma.sep.content.event;