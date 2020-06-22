import {Component,Inject} from 'app/app';
import {Router} from 'adapters/router/router';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {UserService} from 'security/user-service';
import {STATE_PARAM_ID} from 'idoc/idoc-constants';

@Component({
  selector: 'seip-user-dashboard'
})
@Inject(Router, StateParamsAdapter, UserService)
export class UserDashboard {

  constructor(router, stateParamsAdapter, userService) {
    userService.getCurrentUser().then((user) => {
      stateParamsAdapter.setStateParam(STATE_PARAM_ID, user.id);
      router.navigate('idoc', stateParamsAdapter.getStateParams(), {notify: true});
    });
  }

}