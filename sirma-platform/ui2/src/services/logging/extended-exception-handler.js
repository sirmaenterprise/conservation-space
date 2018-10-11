import printStackTrace from 'stacktrace-js';
import {Inject,Injectable} from 'app/app';
import {Logger} from 'services/logging/logger';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {NavigatorAdapter} from 'adapters/navigator-adapter';

/**
 * Replacement for the angular's default exception handler. This exception handler
 * can send caught errors to the server where to be logged.
 *
 * @author svelikov
 */
@Injectable()
@Inject('Logger', WindowAdapter)
export class ExtendedExceptionHandler {

  constructor(logger, windowAdapter) {
    this.logger = logger;
    this.windowAdapter = windowAdapter;
  }

  error(exception, cause) {
    try {
      var errorMessage = exception.toString();
      var stackTrace = printStackTrace({
        e: exception
      });
      var data = JSON.stringify({
        url: this.windowAdapter.location.href,
        type: 'error',
        message: errorMessage,
        stackTrace: stackTrace,
        cause: (cause || ''),
        userId: this.getUser(),
        browser: NavigatorAdapter.getNavigator(),
        os: NavigatorAdapter.getOS() || '',
        resolution: this.getResolution(),
        mobile: NavigatorAdapter.mobile()
      });
      this.logger.error(data);
    } catch (loggingError) {
      this.logger.warn('Error server-side logging failed');
      this.logger.log(loggingError);
    }
  }

  getUser() {
    return 'userId';
  }

  getResolution() {
    var screenSize = '';
    if (screen.width) {
        var width = (screen.width) ? screen.width : '';
        var height = (screen.height) ? screen.height : '';
        screenSize += '' + width + " x " + height;
    }
    return screenSize;
  }
}