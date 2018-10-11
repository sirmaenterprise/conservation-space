import {Injectable} from 'app/app';

@Injectable()
export class AuthenticationService {

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
    return 'token';
  }

  getToken() {

  }

}