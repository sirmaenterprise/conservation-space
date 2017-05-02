import {Filter} from 'app/app';

/**
 * A filter to remove all whitespaces from a string
 */
@Filter
export class RemoveWhitespaces {
  filter(text) {
    return text.replace(/\s/g, '');
  }
}
