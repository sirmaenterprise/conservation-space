import {Component, View} from 'app/app';
import 'components/ui-preference/ui-preference';
import template from './ui-preference-bootstrap.html!text';
import './ui-preference-bootstrap.css!css';

@Component({
  selector: 'ui-preference-bootstrap'
})
@View({
  template: template
})
class UIPreferenceBootstrap {
  constructor() {
    this.testElementUIConfig = {
      sourceElements: {
        top: '#mockAlignElement'
      },
      copyElementWidth: '#mockParentElement',
      noDebounce: true
    };

    this.testElement2UIConfig = {
      sourceElements: {
        top: '#testElement',
        left: '#testElement'
      },
      fillAvailableHeight: true,
      copyParentWidth: '#mockParentElement',
      noDebounce: true
    };
  }
}
