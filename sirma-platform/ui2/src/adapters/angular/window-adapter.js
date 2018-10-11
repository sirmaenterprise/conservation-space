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
    // Fix for IE. Some older versions of IE11 can't access window.location.origin
    if (!this.location.origin) {
      this.location.origin = `${this.location.protocol}//${this.location.hostname}${this.location.port ? ':' + this.location.port: ''}`;
    }
  }

  navigate(url) {
    this.location.href = url;
  }

  openInNewTab(url) {
    this.window.open(url, TARGET_BLANK);
  }
}