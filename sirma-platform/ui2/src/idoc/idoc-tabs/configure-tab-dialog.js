import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {Configuration} from 'common/application-config';
import configureTabDialogTemplate from './configure-tab-dialog.html!text';

@Component({
  selector: 'seip-configure-tab-dialog',
  properties: {
    'config': 'config'
  }
})
@View({
  template: configureTabDialogTemplate
})

@Inject(Configuration)
export class ConfigureTabDialog extends Configurable {

  constructor(configuration) {
    super({});
    this.tabTitle = this.config.tabTitle;
    this.showNavigation = this.config.showNavigation;
    this.showComments = this.config.showComments;
    this.textfieldMaxCharsLength = configuration.get(Configuration.IDOC_TABS_TITLE_MAX_LENGTH);
    this.revision = this.config.revision;
    this.locked = this.config.locked;
  }
}
