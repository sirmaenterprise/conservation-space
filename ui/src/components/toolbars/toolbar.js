import {View, Component} from 'app/app';
import template from './toolbar.html!text';
import './toolbar.css!';

const registry = 'toolbar-section';

@Component({
  selector: 'seip-toolbar',
  properties: {
    config: 'config'
  }
})
@View({
  template: template
})
export class Toolbar{

}