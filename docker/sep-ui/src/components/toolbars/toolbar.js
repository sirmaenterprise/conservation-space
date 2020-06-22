import {View, Component} from 'app/app';
import template from './toolbar.html!text';
import './toolbar.css!';

@Component({
  selector: 'seip-toolbar',
  properties: {
    config: 'config'
  }
})
@View({template})
export class Toolbar{

}
