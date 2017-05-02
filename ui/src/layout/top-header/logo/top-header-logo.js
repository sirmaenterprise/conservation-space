import {View, Component, Inject} from 'app/app';
import {Configuration} from 'common/application-config';
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
  }
}
