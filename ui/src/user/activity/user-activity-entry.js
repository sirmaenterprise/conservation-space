import {Component, View} from 'app/app';
import 'filters/to-trusted-html';
import 'filters/time-ago';
import '../avatar/user-avatar';

import template from './user-activity-entry.html!text';
import './user-activity-entry.css!';

@Component({
  selector: 'seip-user-activity-entry',
  properties: {
    activity: 'activity'
  }
})
@View({
  template
})
export class UserActivityEntry {

}