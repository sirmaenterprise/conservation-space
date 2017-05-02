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

Configuration.SESSION_TIMEOUT = 'session.timeout.period';
Configuration.SESSION_TIMEOUT_REDIRECT = 'session.timeout.redirect';

@Injectable()
export class AuthenticationService {

  logout() {
    $('.status').val('logout-called');
  }
}