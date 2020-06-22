import {Component, View, Inject, NgFilter} from 'app/app';
import 'filters/time-ago';
import {Configuration} from 'common/application-config';
import {MomentAdapter} from 'adapters/moment-adapter';
import {EventEmitter} from 'common/event-emitter';
import 'user/avatar/user-avatar';
import template from './user-activity-entry.html!text';
import './user-activity-entry.css!';

const ONE_HOUR = 60 * 60 * 1000;

@Component({
  selector: 'seip-user-activity-entry',
  properties: {
    activity: 'activity',
    control: 'control'
  }
})
@View({
  template
})
@Inject(NgFilter, Configuration, MomentAdapter)
export class UserActivityEntry {

  constructor($filter, configuration, momentAdapter) {
    this.$filter = $filter;
    this.momentAdapter = momentAdapter;
    this.datePattern = configuration.get(Configuration.UI_DATE_FORMAT) + ' ' + configuration.get(Configuration.UI_TIME_FORMAT);
    this.config = {loaded:true};
    this.eventEmitter = new EventEmitter();
  }

  ngOnInit() {
    this.activitySentence = `<span>${this.activity.text}</span>`;
    let loadedSubscription = this.eventEmitter.subscribe('loaded', () => {
      loadedSubscription.unsubscribe();
      this.control.publish('userActivityRendered');
    });
  }

  /**
   * Returns string representation of activity timestamp
   * @returns Either a "time ago" string if event happened less that one hour ago or full formatted date otherwise
   */
  getFormattedTimestamp() {
    if (new Date() - new Date(this.activity.timestamp) < ONE_HOUR) {
      return this.$filter('timeAgo')(this.activity.timestamp);
    }
    return this.momentAdapter.format(this.activity.timestamp, this.datePattern);
  }
}
