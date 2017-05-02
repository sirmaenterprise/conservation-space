import {Component, View, Inject} from 'app/app';
import {BreadcrumbEntryManager} from 'layout/breadcrumb/breadcrumb-entry-manager';
import template from './recent-objects.html!text';
import './recent-objects-list';
import './recent-objects.css!css';

@Component({
  selector: 'seip-recent-objects'
})
@View({
  template: template
})
@Inject(BreadcrumbEntryManager)
export class RecentObjects {

  constructor(breadcrumbEntryManager) {
    this.breadcrumbEntryManager = breadcrumbEntryManager;
    this.recentObjectsListConfig = {
      renderMenu: true,
      placeholder: 'recent-objects'
    };
  }

  ngOnInit() {
    this.breadcrumbEntryManager.clear();
  }
}