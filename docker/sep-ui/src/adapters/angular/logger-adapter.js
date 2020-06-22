import {Inject,Injectable} from 'app/app';
import application from 'app/app';
import 'services/logging/extended-exception-handler';

/**
 * Adapter for angular's $log service.
 *
 * @author svelikov
 */
@Injectable()
@Inject('$log')
export class LoggerAdapter {

  constructor($log) {
    this.logger = $log;
  }

  log(message) {
    this.logger.log.apply(this.logger, arguments);
  }

  info(message) {
    this.logger.info.apply(this.logger, arguments);
  }

  warn(message) {
    this.logger.warn.apply(this.logger, arguments);
  }

  debug(message) {
    this.logger.debug.apply(this.logger, arguments);
  }

  error(message) {
    this.logger.error.apply(this.logger, arguments);
  }
}

/**
 * Override the angular's exception handler with a custom one.
 */
application.factory('$exceptionHandler',['$injector', function($injector) {
  return function(exception, cause) {
    var extendedExceptionHandler = $injector.get('ExtendedExceptionHandler');
    extendedExceptionHandler.error(exception, cause);
  };
}]);
