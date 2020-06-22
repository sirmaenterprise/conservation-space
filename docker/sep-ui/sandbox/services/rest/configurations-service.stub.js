import {Injectable,Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import data from 'sandbox/services/rest/configuration-service.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class ConfigurationRestService {
  constructor(promise) {
    this.promise = promise;
  }

  loadConfigurations() {
    return this.promise.resolve({
      status: 200,
      data: data
    });
  }

  updateConfigurations() {
    return this.promise.resolve({
      status: 200,
      data: data
    });
  }

  reloadConfigurations() {
    return this.promise.resolve({
      status: 200
    });
  }

}