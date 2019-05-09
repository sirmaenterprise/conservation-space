import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(PromiseAdapter)
export class AuthenticationService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  getUsername() {
    return 'johndoe@tenant.domain';
  }

  isAuthenticated() {
    return false;
  }

  authenticate() {
    return true;
  }

  getToken() {
    return this.promiseAdapter.resolve('token');
  }

  init() {

  }

  buildAuthHeader() {
    return this.getToken().then(token => `Bearer ${token}`);
  }

}

AuthenticationService.TOKEN_REQUEST_PARAM = 'jwt';