import {Component, View} from 'app/app';
import 'components/concept-picker/concept-picker';

import template from './concept-picker.bootstrap.html!text';

@Component({
  selector: 'seip-concept-picker-bootstrap'
})
@View({
  template: template
})
export class ConceptPickerBootstrap {

  constructor() {
    this.value = ['metal'];
  }

}