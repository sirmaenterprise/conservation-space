import {Injectable} from 'app/app';

@Injectable()
export class AuthenticationService {

  getUsername() {
    return 'johndoe@tenant.domain';
  }

  isAuthenticated() {
    return true;
  }

  authenticate() {
    return true;
  }

  getToken() {
    return 'token';
  }

  getToken() {

  }

}