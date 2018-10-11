import {HttpInterceptor} from 'app/app';
import uuid from 'common/uuid';

/**
 * Sets x-correlation-id http header to all requests. The header is used in backend to track down the requests for debugging.
 * @author svelikov
 */
@HttpInterceptor
export class RequestHeadersUpdateInterceptor {

  request(config) {
    config.headers['x-correlation-id'] = uuid(true);
    config.startTime = performance.now();
    return config;
  }

}