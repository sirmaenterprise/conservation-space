import {View, Component} from 'app/app';
import 'components/button/button';

import './model-select.css!css';
import template from './model-select.html!text';

/**
 * Simple action button component which performs a selection operation by executing a given component event.
 * This component provides a generic selection button look and feel.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-select',
  events: ['onSelect']
})
@View({
  template
})
export class ModelSelect {

  onSelectButton(event) {
    this.onSelect && this.onSelect({event});
  }
}