import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';

const SERVICE_PATH = '/image/manifest';

@Injectable()
@Inject(RestClient)
export class ImageService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {
      headers: {
        'Content-Type': 'application/json',
        'Cache-Control': 'no-cache, no-store, must-revalidate',
        'Pragma': 'no-cache'
      }
    };

  }

  createManifest(imageIds) {
    return this.restClient.post(SERVICE_PATH + '/create', imageIds, this.config);
  }

  getManifest(manifestId) {
    return this.restClient.get(SERVICE_PATH + '/' + manifestId, this.config);
  }

  updateManifest(imageIds) {
    return this.restClient.post(SERVICE_PATH + '/update', imageIds, this.config);
  }
}