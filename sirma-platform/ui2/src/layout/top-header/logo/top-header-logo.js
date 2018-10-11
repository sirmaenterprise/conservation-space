import {View, Component, Inject} from 'app/app';
import {Configuration} from 'common/application-config';
import './top-header-logo.css!css';
import logoTemplate from './top-header-logo.html!text';

@Component({
  selector: 'seip-top-header-logo'
})
@View({
  template: logoTemplate
})
@Inject(Configuration)
export class TopHeaderLogo {
  constructor(configuration) {
    this.state = 'userDashboard';
    this.logo = configuration.get(Configuration.LOGO_LOCATION);
    this.applicationName = configuration.get(Configuration.APPLICATION_NAME);
  }
}
