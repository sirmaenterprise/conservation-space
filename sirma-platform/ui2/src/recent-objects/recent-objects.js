import {Component, View} from 'app/app';
import './recent-objects-list';

import template from './recent-objects.html!text';
import './recent-objects.css!css';

@Component({
  selector: 'seip-recent-objects'
})
@View({
  template: template
})
export class RecentObjects {

  constructor() {
    this.recentObjectsListConfig = {
      renderMenu: true,
      placeholder: 'recent-objects'
    };
  }
}