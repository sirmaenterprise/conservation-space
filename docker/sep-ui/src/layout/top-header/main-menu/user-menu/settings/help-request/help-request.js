import {View, Component, Inject} from 'app/app';
import {HelpRequestService} from 'user/help-request/help-request-service';

@Component({
  selector: 'help-request'
})

@View({
  template: '<li><a class="help-request-action" href="javascript: void(0)" ng-click="helpRequest.openDialog()">{{"report.issue.action.label" | translate}}</a></li>'
})

@Inject(HelpRequestService)
export class HelpRequest {

  constructor(helpRequestService) {
    this.helpRequestService = helpRequestService;
  }

  /**
   * This method will be call after user clicked on help request link. It will open dialog with
   * subject, type and description fields.
   */
  openDialog() {
    this.helpRequestService.openDialog();
  }
}
