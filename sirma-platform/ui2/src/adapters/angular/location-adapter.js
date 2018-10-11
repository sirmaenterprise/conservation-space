import {$location} from 'angular-ui-router';
import {Inject,Injectable} from 'app/app';

@Injectable()
@Inject('$location')
export class LocationAdapter {
  constructor($location) {
    this.$location = $location;
  }

  url() {
    return this.$location.$$absUrl;
  }

  /**
   * @returns hash fragment after the very last hash
   */
  hash() {
    return this.$location.hash();
  }
}