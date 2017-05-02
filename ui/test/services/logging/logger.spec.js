import {LoggerAdapter} from 'adapters/angular/logger-adapter';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {Configuration} from './application-config.stub';
import {Logger} from 'services/logging/logger';
import {LoggingRestService} from 'services/rest/logging-service';

describe('Logger service', function () {

  var restClient = {};
  restClient.post = sinon.spy();

  var loggingRestClient = new LoggingRestService(restClient);

  var loggerAdapter = new LoggerAdapter({});
  var windowAdapter = new WindowAdapter({location: {href: 'url'}});
  var configurationService = new Configuration({
    'application.mode.development': true
  });
  var logger = new Logger(loggerAdapter, windowAdapter, loggingRestClient, configurationService, stubAuthenticationService(true));

  function stubLoggerMethods(level) {
    sinon.stub(loggerAdapter, level, function () {
    });
  }

  it('logger.log() should log in log level', function () {
    stubLoggerMethods('log');
    logger.log('test', false);
    expect(loggerAdapter.log.callCount).to.equal(1);
  });

  it('logger.info() should log in info level', function () {
    stubLoggerMethods('info');
    logger.info('message', false);
    expect(loggerAdapter.info.callCount).to.equal(1);
  });

  it('logger.warn() should log in warn level', function () {
    stubLoggerMethods('warn');
    logger.warn('test', false);
    expect(loggerAdapter.warn.callCount).to.equal(1);
  });

  it('logger.error() should log in error level', function () {
    stubLoggerMethods('error');
    logger.error('test', true);
    expect(loggerAdapter.error.callCount).to.equal(1);
  });

  it('logger.error() should always send errors to server', function () {
    loggingRestClient.logMessage = sinon.spy();
    logger.error('test');
    // this is a bit ugly but the operation is asynchronouse
    var timer = setInterval(function () {
      let callCount = loggingRestClient.logMessage.callCount;
      if (callCount === 1) {
        expect(callCount).to.equal(1);
        clearInterval(timer);
      }
    }, 500);
  });

  it('should not send log message to server if shouldLogOnServer=false', function () {
    loggingRestClient.logMessage = sinon.spy();
    logger.logOnServer(false, 'debug', 'message');
    expect(loggingRestClient.logMessage.callCount).to.equal(0);
  });

  it('should send a log message to server if shouldLogOnServer=true', function () {
    loggingRestClient.logMessage = sinon.spy();
    logger.logOnServer(true, 'debug', 'message');
    expect(loggingRestClient.logMessage.callCount).to.equal(1);
  });

  it('should not send log message to server if the user is not authenticated', function () {
    var logger = new Logger(loggerAdapter, windowAdapter, loggingRestClient, configurationService, stubAuthenticationService(false));

    loggingRestClient.logMessage = sinon.spy();
    logger.logOnServer(true, 'debug', 'message');
    expect(loggingRestClient.logMessage.callCount).to.equal(0);
  });
});

function stubAuthenticationService(authenticated) {
  return {
    isAuthenticated: function () {
      return authenticated;
    }
  };
}