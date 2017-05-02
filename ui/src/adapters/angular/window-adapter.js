import {$window, $document} from 'angular';
import {Inject,Injectable} from 'app/app';

export const TARGET_BLANK = '_blank';

/**
 * Adapter for angular's $window service.
 *
 * @author svelikov
 */
@Injectable()
@Inject('$window', '$document')
export class WindowAdapter {

  constructor($window, $document) {
    this.window = $window;
    this.document = $document && $document[0];
    this.location = $window.location;
  }

  navigate(url) {
    this.location.href = url;
  }

  openInNewTab(url) {
    this.window.open(url, TARGET_BLANK);
  }
}