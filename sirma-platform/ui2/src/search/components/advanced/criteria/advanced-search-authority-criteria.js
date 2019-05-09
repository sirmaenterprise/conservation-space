import {Component, View} from 'app/app';
import {Configurable} from 'components/configurable';
import 'components/select/resource/resource-select';

import template from './advanced-search-authority-criteria.html!text';

/**
 * Component for choosing user/group objects as values in the advanced search criteria form.
 *
 * The component relies heavily on providing a criteria object and the property for which will display user/group values.
 *
 * The component can be configured, example configuration:
 *  config: {
 *    disabled: false
 *  }
 *
 * @author Hristo Lungov
 */
@Component({
  selector: 'seip-advanced-search-authority-criteria',
  properties: {
    'config': 'config',
    'property': 'property',
    'criteria': 'criteria'
  }
})
@View({template})
export class AdvancedSearchAuthorityCriteria extends Configurable {
  constructor() {
    super({
      disabled: false
    });
  }

  /**
   * Creates select configuration for resource select component.
   */
  ngOnInit() {
    this.selectConfig = {
      includeUsers: true,
      includeGroups: false,
      isDisabled: () => this.config.disabled,
      resourceConverter: this.convertResource.bind(this)
    };
  }

  convertResource(resource) {
    return {
      id: resource.value,
      text: resource.label,
      type: resource.type,
      value: resource.value
    };
  }
}
