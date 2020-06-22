/**
 * Abstract class for defining a service for bootstrapping specific functionalities on application startup.
 *
 * Must override initialize() function to return a promise which should be resolved when the service is
 * fully initialized.
 *
 * Bear in mind that implementations should not perform heavy or slow tasks beacause it slows the startup of
 * the application.
 *
 * @author Mihail Radkov
 */
export class BootstrapService {

  constructor() {
    if (typeof this.initialize !== 'function') {
      throw new TypeError('Must override initialize function');
    }
  }

}