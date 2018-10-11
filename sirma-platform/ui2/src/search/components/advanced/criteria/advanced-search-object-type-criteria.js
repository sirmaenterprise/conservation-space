import {Component, View} from 'app/app';
import {Configurable} from 'components/configurable';
import 'components/select/object/object-type-select';

import template from './advanced-search-object-type-criteria.html!text';

/**
 * Component for choosing object type in the advanced search criteria form.
 * It's applied when the field control type in model is OBJECT_TYPE_SELECT.
 *
 * The component can be configured, example configuration:
 *  config: {
 *    disabled: false
 *  }
 *
 * @author Adrian Mitev
 */
@Component({
  selector: 'seip-advanced-search-object-type-criteria',
  properties: {
    'config': 'config',
    'property': 'property',
    'criteria': 'criteria'
  }
})
@View({
  template: template
})
export class AdvancedSearchObjectTypeCriteria extends Configurable {
  
  constructor() {
    super({
      disabled: false
    });
  }
  
  ngOnInit() {
    this.selectConfig = {
      isDisabled: () => this.config.disabled,
      multiple: false,
      preferDefinitionType: true
    };
  }
}