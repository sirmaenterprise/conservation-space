import {Component, View, Inject} from 'app/app';
import {HelpRequestService} from 'user/help-request/help-request-service';
import template from 'help-request-template!text';

@Component({
  selector: 'help-request-stub'
})
@View({
  template: template
})
@Inject(HelpRequestService)
export class HelpRequestServiceStub {

  constructor(helpRequestService) {
    this.helpRequestService = helpRequestService;
  }

  openDialog() {
    this.helpRequestService.openDialog();
  }

}