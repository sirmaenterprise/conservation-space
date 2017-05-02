import { Inject, Injectable } from 'app/app';

@Injectable()
@Inject('$q')
export class PromiseAdapter {
  constructor($q) {
    this.$q = $q;
  }

  promise(executor) {
    return this.$q(executor);
  }

  reject(reason) {
    return this.$q.reject(reason);
  }

  resolve(result) {
    return this.$q.resolve(result);
  }

  defer() {
    return this.$q.defer();
  }

  /**
   * Delegates to $q.all(..).
   * @see https://docs.angularjs.org/api/ng/service/$q
   */
  all(promises) {
    return this.$q.all(promises);
  }
}
