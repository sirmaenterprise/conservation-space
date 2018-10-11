import {View, Component, Inject} from 'app/app';
import 'administration/audit-log/audit-log';
import template from './audit-log.stub.html!text';

@Component({
  selector: 'seip-audit-log-bootstrap'
})
@View({
  template: template
})
@Inject()
export class AuditLogBootstrap {

  constructor() {
  }

  ngAfterViewInit() {
    this.render = true;
  }
}
