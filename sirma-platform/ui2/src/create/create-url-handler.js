import {Component, Inject} from 'app/app';
import {CreatePanelService} from 'services/create/create-panel-service';
import {UrlUtils} from 'common/url-utils';
import {WindowAdapter} from 'adapters/angular/window-adapter';

/**
 * This is an intermediate view that is responsible for proper dispatch according to the request parameters.
 */
@Component({
  selector: 'create-url-handler'
})
@Inject(CreatePanelService, '$scope', WindowAdapter)
export class CreateUrlHandler {

  constructor(createPanelService, $scope, windowAdapter) {
    var url = windowAdapter.location.href;

    var parentId = UrlUtils.getParameter(url, 'parentId');

    var params = {
      parentId,
      returnUrl: UrlUtils.getParameter(url, 'return-url'),
      operation: UrlUtils.getParameter(url, 'operation'),
      scope: $scope
    };
    createPanelService.openCreateInstanceDialog(params);
  }
}
