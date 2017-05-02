import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';

const serviceUrl = '/identity';

@Injectable()
@Inject(RestClient)
export class IdentityRestService {

  constructor(restClient) {
    this.restClient = restClient;
  }

  login(username, password) {
    const loginUrl = serviceUrl + `/login?username=${username}&password=${password}`;
    return this.restClient.get(loginUrl);
  }

}