import _ from 'lodash';
import {Inject,Injectable} from 'app/app';
import {LoggerAdapter} from 'adapters/angular/logger-adapter';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {LoggingRestService} from 'services/rest/logging-service';
import {Configuration} from 'common/application-config';
import {AuthenticationService} from 'security/authentication-service';

const LOGGING_DEBOUNCE_DELAY = 300;
const LEVELS = {
  log: 'log',
  info: 'info',
  warn: 'warn',
  debug: 'debug',
  error: 'error'
};

/**
 * An injectable logger that allows some client side logging and also an option the logs to be sent
 * and logged to the server too. All log methods provide argument that tells if the log message
 * should be sent to the server or not. <b>The logger.error(message, logOnServer) method always
 * sends the log to the server.</b>
 *
 * @author svelikov
 */
@Injectable()
@Inject(LoggerAdapter, WindowAdapter, LoggingRestService, Configuration, AuthenticationService)
export class Logger {

  constructor(loggerAdapter, windowAdapter, loggingRestService, configuration, authenticationService) {
    this.loggerAdapter = loggerAdapter;
    this.windowAdapter = windowAdapter;
    this.loggingRestService = loggingRestService;
    // the logging to the server is debounced in order to prevent server flood
    // with requests in case of cyclic invocation
    this._debouncedLogger = _.debounce(this.logOnServer, LOGGING_DEBOUNCE_DELAY, false);
    this.configuration = configuration;
    this.authenticationService = authenticationService;
  }

  log(message, logOnServer) {
    this.executeLogging(LEVELS.log, message, logOnServer);
  }

  info(message, logOnServer) {
    this.executeLogging(LEVELS.info, message, logOnServer);
  }

  warn(message, logOnServer) {
    this.executeLogging(LEVELS.warn, message, logOnServer);
  }

  debug(message, logOnServer) {
    this.executeLogging(LEVELS.debug, message, logOnServer);
  }

  error(message) {
    this.executeLogging(LEVELS.error, message, true);
  }

  executeLogging(level, message, shouldLogOnServer) {
    let developmentMode = this.configuration.get(Configuration.APPLICATION_MODE_DEVELOPMENT);
    let logInProduction = (developmentMode === false && (level === LEVELS.error || level === LEVELS.warn));
    let prefixed = `[SEP]: ${message}`;
    if (developmentMode === true || logInProduction) {
      this.loggerAdapter[level](prefixed);
      this._debouncedLogger(shouldLogOnServer, level, prefixed);
    }
  }

  /**
   * Private function that is not invoked directly but is debounced and the
   * debounced version is called instead.
   */
  logOnServer(shouldLogOnServer, type, message) {
    if (shouldLogOnServer && this.authenticationService.isAuthenticated()) {
      this.loggingRestService.logMessage(JSON.stringify({
        url: this.windowAdapter.location.href,
        message,
        type
      }));
    }
  }
}
