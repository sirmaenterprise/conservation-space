import {View, Component} from 'app/app';
import {Configurable} from 'components/configurable';
import _ from 'lodash';

import './button.css!css';
import template from './button.html!text';

/**
 * Generic button component which supports ability to be disabled or to visually signal a loading process.
 * Furthermore this button can be specified as either primary or not (by default), button label can also
 * be specified or a default label will be used instead.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-button',
  properties: {
    'config': 'config',
    'enabled': 'enabled',
    'loading': 'loading'
  },
  events: ['onClick']
})
@View({
  template
})
export class Button extends Configurable {

  constructor() {
    super({
      primary: false,
      label: 'dialog.button.confirm'
    });
  }

  ngOnInit() {
    // resolve undefined property
    if (_.isUndefined(this.loading)) {
      this.loading = false;
    }

    // resolve undefined property
    if (_.isUndefined(this.enabled)) {
      this.enabled = true;
    }
  }

  onButtonClick() {
    return this.onClick && this.onClick();
  }
}