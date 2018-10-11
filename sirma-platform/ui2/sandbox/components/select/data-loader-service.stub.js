import {Inject, Injectable} from 'app/app';

@Injectable()
@Inject('$timeout', '$q')
export class DataLoaderService {
  constructor($timeout, $q) {
    this.$timeout = $timeout;
    this.$q = $q;
  }

  getData() {
    let deferred = this.$q.defer();
    let data = [];
    for (let i=0; i<10; i++) {
      data[`key${i}`] = `Value ${i}`;
    }
    // Simulate ajax request
    this.$timeout(function () {
      deferred.resolve(data);
    }, 10);
    return deferred.promise;
  }
}