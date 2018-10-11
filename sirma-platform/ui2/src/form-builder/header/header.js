import {View, Component, Inject, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import {EventEmitter} from 'common/event-emitter';
import _ from 'lodash';
import template from './header.html!text';

@Component({
  selector: 'seip-header',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({
  template: template
})
@Inject(NgElement)
class Header extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
    this.eventEmitter = new EventEmitter();
    this.config = this.getHeaderConfig();

    let loadedSubscription = this.eventEmitter.subscribe('headerContainerRendered', () => {
      loadedSubscription.unsubscribe();
      this.formEventEmitter.publish('formControlLoaded',{identifier: this.identifier})
    });

  }

  ngOnInit() {
    this.setRendered(this.$element, this.fieldViewModel.rendered);
  }

  /**
   * Adds the fieldIdentifier for further component use and defaults existing config if any.
   * @returns
   */
  getHeaderConfig() {
    let config = {
      eventEmitter: this.eventEmitter,
      fieldIdentifier: this.identifier,
      loaded: true
    };
    if (this.widgetConfig.results && this.widgetConfig.results.config) {
      config = _.defaults(config, this.widgetConfig.results.config);
    }
    return config;
  }
}