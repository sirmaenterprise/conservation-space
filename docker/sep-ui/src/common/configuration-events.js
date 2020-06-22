import {Event} from 'app/app';

/**
 * Event carrying array of configurations that are updated somehow.
 *
 * Designed for components that need to be notified when the configurations are changed.
 *
 * @author Mihail Radkov
 */
@Event()
export class ConfigurationsUpdateEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}

@Event()
export class ConfigurationsLoadedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}