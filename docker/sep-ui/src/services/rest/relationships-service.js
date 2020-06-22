import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';

const RELATIONSHIPS_URL = '/relationships';

@Injectable()
@Inject(RestClient)
export class RelationshipsService {

  constructor(client) {
    this.client = client;
  }

  find(opts) {
    let config = {
      params: {
        q: opts.q || ''
      }
    };
    return this.client.get(RELATIONSHIPS_URL, config);
  }

  getRelationInfo(relationShipId) {
    return this.client.get(`${RELATIONSHIPS_URL}/${relationShipId}/info`);
  }
}
