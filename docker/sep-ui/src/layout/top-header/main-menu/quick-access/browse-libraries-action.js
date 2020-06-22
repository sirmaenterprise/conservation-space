import {Inject, Injectable} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {Router} from 'adapters/router/router';

const BROWSE_STATE = 'libraries';

@Injectable()
@Inject(Router)
export class BrowseLibrariesAction extends ActionHandler {

  constructor(router) {
    super();
    this.router = router;
  }

  execute(action) {
    this.router.navigate(BROWSE_STATE, action.params);
  }
}