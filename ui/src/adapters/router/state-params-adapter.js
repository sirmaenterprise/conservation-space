import {$stateParams} from 'angular-ui-router';
import {Inject,Injectable} from 'app/app';

@Injectable()
@Inject('$stateParams')
export class StateParamsAdapter {
  constructor($stateParams) {
    this.$stateParams = $stateParams;
  }

  getStateParams() {
    return this.$stateParams;
  }

  getStateParam(name) {
    return this.$stateParams[name];
  }

  setStateParam(name, value) {
    this.$stateParams[name] = value;
  }
}