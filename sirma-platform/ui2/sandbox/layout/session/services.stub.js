import {Injectable} from 'app/app';

@Injectable()
export class Configuration {

  get(key) {
    if (key === 'session.timeout.period') {
      return 0.07;
    }
    return true;
  }
}

Configuration.SESSION_TIMEOUT = 'session.timeout';
Configuration.SESSION_TIMEOUT_PERIOD = 'session.timeout.period';

@Injectable()
export class AuthenticationService {

  logout() {
    $('.status').val('logout-called');
  }
}