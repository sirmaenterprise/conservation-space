import {View, Component, Inject, NgScope} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {IdocToC} from 'idoc/idoc-toc/idoc-toc';
import navigationTemplate from './navigation.html!text';
import './navigation.css!css';

@Component({
  selector: 'seip-idoc-navigation',
  properties: {
    'tab': 'tab',
    'eventbusChannel': 'eventbus-channel',
    'sourceSelector': 'source-selector',
    'editMode': 'edit-mode',
    'tocHolderSelector': 'toc-holder-selector'
  }
})
@View({
  template: navigationTemplate
})
@Inject(NgScope, Eventbus)
export class IdocNavigation {

  constructor($scope, eventbus) {
    this.tocHolderClass = IdocNavigation.tocHolderClass;
    this.$scope = $scope;
    this.eventbus = eventbus;
  }

  ngAfterViewInit() {

    this.initializeToc();

    // Watch when the idoc switches modes and set the idoc toc to proper mode
    // Manually trigger tree rebuild in order to have the changes
    this.$scope.$watch(() => {
      return this.editMode;
    }, () => {
      this.idocToc.setPreviewMode(!this.editMode);
      this.idocToc.refresh();
    });
  }

  initializeToc() {
    this.idocToc = new IdocToC({
      navigation: this.tocHolderSelector,
      previewMode: this.editMode,
      source: this.sourceSelector,
      scrollTime: 'slow',
      eventbus: {
        channel: this.eventbusChannel,
        instance: this.eventbus
      },
      tab: this.tab
    });
  }

  ngOnDestroy() {
    if (this.idocToc) {
      this.idocToc.destroy();
    }
  }
}
IdocNavigation.tocHolderClass = 'idoc-toc-holder';
