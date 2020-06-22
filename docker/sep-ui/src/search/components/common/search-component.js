import {Configurable} from 'components/configurable';

/**
 * Abstract class for search components containing common logic.
 *
 * @author Mihail Radkov
 */
export class SearchComponent extends Configurable {

  constructor(config) {
    super(config || {});
  }

  /**
   * Determines if the provided component is locked or disabled based on the current configuration.
   */
  isLockedOrDisabled(component) {
    return this.config.locked && this.config.locked.indexOf(component) > -1 || !!this.config.disabled;
  }

}
