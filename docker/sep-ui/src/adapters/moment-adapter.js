import {Injectable} from 'app/app';
import 'moment';

@Injectable()
export class MomentAdapter {

  /**
   * Converts date string to moment date object
   * @param dateString
   * @returns {*} moment date object
   */
  parse(dateString) {
    return moment(dateString);
  }

  format(date, pattern) {
    if (!date) {
      return null;
    }
    return moment(date).format(pattern);
  }

  isBefore(date1, date2) {
    if (!date1 || !date2) {
      return null;
    }
    return moment(date1).isBefore(date2);
  }

  isAfter(date1, date2) {
    if (!date1 || !date2) {
      return null;
    }
    return moment(date1).isAfter(date2);
  }

}