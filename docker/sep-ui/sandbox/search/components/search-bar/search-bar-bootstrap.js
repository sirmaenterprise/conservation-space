import {Inject, Component, View} from 'app/app';
import 'search/components/search-bar/search-bar';
import {RecentObjectsService} from 'services/recent/recent-objects-service';
import data from 'sandbox/services/rest/search-service.data.json!';

import template from './search-bar-bootstrap.html!text';

@Component({
  selector: 'search-bar-bootstrap'
})
@View({
  template: template
})
@Inject(RecentObjectsService)
export class SearchBarBootstrap {

  constructor(recentObjectsService) {
    this.recentObjectsService = recentObjectsService;
  }

  ngAfterViewInit() {
    this.render = true;
  }

  onSearch() {
    this.isSearchTriggered = true;
  }

  assignContext() {
    this.selectedContext = data.data.values[1];
  }

  addRecentObject() {
    this.recentObjectsService.addRecentObject(data.data.values[0]);
  }

  removeRecentObject() {
    this.recentObjectsService.removeRecentObject(data.data.values[0]);
  }
}