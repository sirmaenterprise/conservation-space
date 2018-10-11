import {View, Component, Inject, NgElement, NgCompile, NgScope, NgTimeout} from 'app/app';
import {Configurable} from 'components/configurable';
import {IconsService} from 'services/icons/icons-service';
import {DialogService} from 'components/dialog/dialog-service';
import {UrlDecorator} from 'layout/url-decorator/url-decorator';
import {SINGLE_SELECTION} from 'search/search-selection-modes';
import 'filters/to-trusted-html';
import _ from 'lodash';

import template from 'instance-header/static-instance-header/static-instance-header.html!text';
import 'instance-header/static-instance-header/static-instance-header.css!css';

@Component({
  selector: 'seip-static-instance-header',
  properties: {
    'config': 'config',
    'headerType': 'header-type',
    'eventEmitter': 'event-emitter',
    'header': 'header'
  }
})
@View({
  template
})
@Inject(NgElement, NgCompile, NgScope, NgTimeout, DialogService)
export class StaticInstanceHeader extends Configurable {

  constructor($element, $compile, $scope, $timeout, dialogService) {
    super({
      disabled: false,
      linkRedirectDialog: false,
      preventLinkRedirect: false
    });
    this.$element = $element;
    this.$compile = $compile;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.dialogService = dialogService;
  }

  ngOnInit() {
    this.iconSize = IconsService.HEADER_ICON_SIZE[this.headerType];
    this.instanceDataElement = this.$element.find('.instance-data');
    this.compileHeader();
    this.registerHeaderWatcher();
    this.publishLoadedEvent();
  }

  publishLoadedEvent() {
    if (this.eventEmitter && this.config.loaded) {
      this.eventEmitter.publish('loaded', this.config.fieldIdentifier);
    }
  }

  /**
   * Registers an angular watcher used to recompile if the header has been changed in the model has been loaded.
   * Uses EventEmitter event if property is part of a form and in print mode.
   */
  registerHeaderWatcher() {
    this.$scope.$watchCollection(() => {
      return [this.header, this.config.loaded];
    }, (newValues, oldValues) => {
      if (newValues[0] !== oldValues[0] || newValues[1] === true) {
        this.compileHeader();
        this.publishLoadedEvent();
      }
    });
  }

  compileHeader() {
    if (this.header) {
      this.clearData();
      this.innerScope = this.$scope.$new();
      let compiledHeader = this.$compile(this.header.replace(/(\r\n|\n|\r)/gm, ''))(this.innerScope);
      this.instanceDataElement.append(compiledHeader);

      this.afterHeaderCompilation();
    }
  }

  afterHeaderCompilation() {
    this.assignHeaderIconClass();
    this.decorateInstanceLinkElement();
  }

  assignHeaderIconClass() {
    let headerIcon = this.$element.find('.instance-data > span:first-child > img');
    headerIcon.parent().addClass('header-icon');
  }

  /**
   * Decorates an instance link to when clicked with a custom behaviour. Note
   * that all links in the header will be decorated with the same behaviour.
   * This behaviour is exhibited when the link is clicked. The link can either:
   *
   * 1) completely prevent and disallow redirection when clicked
   * 2) show a confirmation dialog which prompts the client to decide if he wants to be redirected or not
   */
  decorateInstanceLinkElement() {
    let redirectDialog = this.config.linkRedirectDialog;
    let preventRedirect = this.config.preventLinkRedirect;
    let shouldDecorate = preventRedirect || redirectDialog;

    if (shouldDecorate) {
      let linkElements = this.$element.find('.instance-data > span:last-child').find('a');
      // decorate each and every link element
      _.forEach(linkElements, (element) => {
        let linkElement = $(element);
        if (linkElement.length > 0) {
          linkElement.addClass(UrlDecorator.SUPPRESSED_LINK_CLASS);
          linkElement.click((event) => {
            event.preventDefault();
            if (!preventRedirect && redirectDialog) {
              this.showRedirectConfirmation(linkElement, event);
            }
          });
        }
      });
    }
  }

  showRedirectConfirmation(linkElement, event) {
    let dialogConfig = {
      buttons: [
        this.dialogService.createButton(DialogService.YES, 'dialog.button.yes', true),
        this.dialogService.createButton(DialogService.NO, 'dialog.button.no')
      ],
      onButtonClick: (button, dialogScope, dialogConfig) => {
        if (button === DialogService.YES) {
          // unbind event within itself
          linkElement.unbind(event);
          // timeout digest before re-navigating
          this.$timeout(() => linkElement.click());
        }
        dialogConfig.dismiss();
      }
    };
    let message = 'static.header.confirmation.dialog.message';
    this.dialogService.confirmation(message, undefined, dialogConfig);
  }

  clearData() {
    if (this.innerScope) {
      this.innerScope.$destroy();
    }
    this.instanceDataElement.empty();
  }

  ngOnDestroy() {
    this.clearData();
  }
}