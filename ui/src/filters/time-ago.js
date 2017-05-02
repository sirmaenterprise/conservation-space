import {Filter} from 'app/app';
import moment from 'moment';

@Filter
export class TimeAgo {

  filter(date) {
    if (!date) {
      return '';
    }

    return moment(date).from(moment.utc());
  }
}
