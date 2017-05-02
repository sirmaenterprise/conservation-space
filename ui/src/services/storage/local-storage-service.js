import {Injectable, Inject} from 'app/app';
import {Storage} from 'services/storage/storage';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import _ from 'lodash';

@Injectable()
@Inject(WindowAdapter)
export class LocalStorageService extends Storage {

  constructor(windowAdapter) {
    super(windowAdapter.window.localStorage);
  }

  mergeValues(storage, object) {
    let store = this.getJson(storage, {});
    _.merge(store, object);
    this.set(storage, store);
  }

}

LocalStorageService.SESSION_TIMEOUT = 'session.timeout';
LocalStorageService.LAST_USER_ACTIVITY = 'session.lastUserActivity';
LocalStorageService.RECENT_OBJECTS = 'user.recent.objects';
LocalStorageService.WIDGET_PREFERENCES = 'widget.preferences';