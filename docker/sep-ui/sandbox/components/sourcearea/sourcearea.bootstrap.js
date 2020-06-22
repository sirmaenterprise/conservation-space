import {Component, View} from 'app/app';

import 'components/sourcearea/sourcearea';
import template from './sourcearea.bootstrap.html!text';

@Component({
  selector: 'seip-sourcearea-bootstrap'
})
@View({template})
export class SourceareaBootstrap {

  constructor() {
    this.source = '${eval(<span>(MX1001) Default Header</span>)}';
  }

  onChange() {
    this.value = this.source;
  }
}