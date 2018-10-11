import {Inject, Injectable, NgCompile, NgDocument, NgRootScope} from 'app/app';
import 'filters/to-trusted-html';
import {GenericDialog} from 'components/dialog/generic-dialog';
import {TranslateService} from 'services/i18n/translate-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {RouterStateChangeStartEvent} from 'common/router/router-state-change-start-event';
import _ from 'lodash';
import 'common/lib/jquery-ui/jquery-ui.min';
import 'common/lib/jquery-ui/jquery-ui.min.css!';
import genericDialogTemplate from 'components/dialog/generic-dialog.html!text';

/**
 * Service for creating and displaying modal dialogs.
 */
@Injectable()
@Inject(NgCompile, NgDocument, NgRootScope, Eventbus, TranslateService)
export class DialogService {

  constructor($compile, $document, $rootScope, eventbus, translateService) {
    this.$compile = $compile;
    this.$document = $document;
    this.$rootScope = $rootScope;

    this.translateService = translateService;
    eventbus.subscribe(RouterStateChangeStartEvent, this.closeExistingDialogs);

    this.hideInProgress = false;
    this.queuedModal = '';
  }

  /**
   * Creates and displays an error dialog.
   *
   * @param message a message to be displayed
   * @param header dialog title (header)
   * @param opts additional configurations for the modal dialog
   */
  error(message, header, opts) {
    opts = opts || {};
    let config = {
      header: '<i class="fa fa-exclamation-triangle"> ' + (header || this.translateService.translateInstant('dialog.header.error')) + '</i>',
      headerCls: 'error',
      showClose: true,
      buttons: [this.createButton(DialogService.CLOSE, 'dialog.button.close', true)],
      onButtonClick: function (buttonId, componentScope, dialogConfig) {
        dialogConfig.dismiss();
      }
    };
    _.defaults(opts, config);
    return this.create(GenericDialog, {content: message || ''}, opts);
  }

  /**
   * Creates and displays a notification dialog.
   *
   * @param message a message to be displayed
   * @param header dialog title (header)
   * @param opts additional configurations for the modal dialog
   */
  notification(message, header, opts) {
    opts = opts || {};
    let config = {
      header: '<i class="fa fa-info-circle"> ' + (header || this.translateService.translateInstant('dialog.header.notification')) + '</i>',
      headerCls: 'notification',
      showClose: true,
      buttons: [this.createButton(DialogService.OK, 'dialog.button.ok', true)],
      onButtonClick: function (buttonId, componentScope, dialogConfig) {
        dialogConfig.dismiss();
      }
    };
    _.defaults(opts, config);
    return this.create(GenericDialog, {content: message || ''}, opts);
  }

  /**
   * Creates and displays a confirmation dialog.
   *
   * @param message a message to be displayed
   * @param header dialog title (header)
   * @param opts additional configurations for the modal dialog
   */
  confirmation(message, header, opts) {
    opts = opts || {};
    let config = {
      header: '<i class="fa fa-question-circle"> ' + (header || this.translateService.translateInstant('dialog.header.confirm')) + '</i>',
      headerCls: 'confirmation',
      showClose: true,
      buttons: [
        this.createButton(DialogService.YES, 'dialog.button.yes', true),
        this.createButton(DialogService.NO, 'dialog.button.no'),
        this.createButton(DialogService.CANCEL, 'dialog.button.cancel')
      ],
      onButtonClick: function (buttonId, componentScope, dialogConfig) {
        dialogConfig.dismiss();
      }
    };
    _.defaults(opts, config);
    return this.create(GenericDialog, {content: message || ''}, opts);
  }

  /**
   * Creates and displays a modal dialog.
   *
   * @param component component class or selector to be visualized in the body of the modal dialog
   * @param componentProperties an object with properties (attributes) to be added to the component directive. Their values will be passed via scope variables
   * @param dialogConfig an object with configuration for the modal dialog like header, buttons, etc. See generic-dialog.html
   * @param domAttributes an object with DOM attributes to be added to component's root element
   */
  create(component, componentProperties, dialogConfig, domAttributes) {
    let selector;

    if (_.isString(component)) {
      selector = component;
    } else {
      selector = component.COMPONENT_SELECTOR;
    }

    const body = this.$document.find('body').eq(0);
    let scope = this.$rootScope.$new(true);

    dialogConfig = dialogConfig || {};
    dialogConfig.showHeader = !!dialogConfig.showClose || !_.isEmpty(dialogConfig.header);

    if (dialogConfig.modeless) {
      // don't allow stacking of modless dialogs
      // trigger the event instead of calling .modal('hide') because the hide is async which is not desired here
      $('.modal.modeless').trigger('hidden.bs.modal');

      dialogConfig.modalCls = ((dialogConfig.modalCls || '') + ' modeless').trim();
    }

    scope.dialogConfig = dialogConfig;
    dialogConfig.dialogScope = scope;

    // Add all component properties needed for the component to the scope
    _.defaults(scope, componentProperties);
    let dialogBody = '<' + selector;
    // append all component properties to component selector (they are passed via scope variables)
    _.forIn(componentProperties, (value, key) => dialogBody += ' ' + _.kebabCase(key) + '="' + key + '" ');
    // append all dom attributes directly to component selector
    _.forIn(domAttributes, (value, key) => dialogBody += ' ' + key + '="' + value + '" ');
    dialogBody += '></' + selector + '>';
    let template = genericDialogTemplate.replace('{body}', dialogBody);

    let dialogEl = angular.element(template);
    let compiled = this.$compile(dialogEl)(scope);
    body.append(compiled);

    dialogConfig.componentScope = scope.$$childHead;

    let removeOnHide = scope.dialogConfig.removeOnHide !== undefined ? scope.dialogConfig.removeOnHide : true;

    compiled.on('shown.bs.modal', (evt) => {
      // Ctrl+A events should not propagate away from the modal
      $(evt.currentTarget).on('keydown', (e) => {
        if (e.ctrlKey && e.keyCode === 65) {
          e.stopPropagation()
        }
      });

      var zIndex = 1040 + (10 * $('.modal:visible').length);
      compiled.css('z-index', zIndex);

      if (_.isFunction(dialogConfig.onShow)) {
        dialogConfig.onShow();
      }

      $('.modal-backdrop').not('.modal-stack').css('z-index', zIndex - 1).addClass('modal-stack');

      if (dialogConfig.modeless) {
        $(document).off('focusin.bs.modal');
        $('body').removeClass('modal-open');
      }

      //Applies custom CSS styles to the main modal dialog
      if (dialogConfig.customStyles) {
        $('.seip-modal').addClass(dialogConfig.customStyles);
      }

      //Lazily initialize the bootstrap popover
      if (dialogConfig.warningPopover) {
        let popover = DialogService.getPopover(dialogEl);
        popover.on('show.bs.popover', () => {
          let style = dialogConfig.warningPopover.style;
          if (style) { //override default popover style
            popover.data('bs.popover').tip().css(style);
          }
        });
      }

      this.makeDraggable(compiled);
    });

    compiled.on('hidden.bs.modal', function () {
      $('.modal:visible').length && $(document.body).addClass('modal-open');

      if (removeOnHide) {
        let dialog = $(this);
        // remove angular comment
        compiled.remove();
        // remove actual modal
        dialog.remove();
        DialogService.onClose(dialogConfig);
        DialogService.getPopover(dialog, 'destroy');
        scope.$destroy();
      } else {
        DialogService.onClose(dialogConfig);
      }
    });

    scope.dialogConfig.dismiss = (closeButtonClicked) => {
      // should be covered because of CMF-21668, the state change is not handled correctly.
      // also checks if there are buttons because it can be configured to not have.
      if (closeButtonClicked && dialogConfig.buttons) {
        // execute onButtonClick function if the close button is pressed.
        let cancelButton = dialogConfig.buttons.find((button) => {
          return button.id === DialogService.CANCEL;
        });

        if (dialogConfig.onButtonClick instanceof Function && !!cancelButton) {
          dialogConfig.onButtonClick(cancelButton.id, scope, dialogConfig);
        } else {
          this.hideModal(compiled);
        }
      } else {
        this.hideModal(compiled);
      }
    };

    // the backdrop config options: 'static', true|false
    compiled.backdrop = DialogService.configureBackdrop(scope.dialogConfig);

    this.showModal(compiled);
  }

  static onClose(dialogConfig) {
    if (_.isFunction(dialogConfig.onClose)) {
      dialogConfig.onClose(dialogConfig.componentScope, dialogConfig);
    }
  }

  makeDraggable(dialogElement) {
    let modalDialogElement = dialogElement.find('.modal-dialog');
    modalDialogElement.draggable({
      handle: '.modal-header',
      cursor: 'move',
      containment: 'document body',
      start: function () {
        // prevent the transition because it lags the movement
        $(this).css('transition', 'none');
        //When clicked on the draggable element, the opened ckeditor's dropdowns aren't closed. Focusing something else closes them.
        $(window).focus();
      },
      stop: function (event, ui) {
        DialogService.dragStop(modalDialogElement, ui.offset);
      },
      scroll: false
    });
  }

  //If the element is dragged under the viewport, it returns the dialog into it.
  static dragStop(modalDialogElement, offset) {
    let modalContentElement = modalDialogElement.find('.modal-content');
    let modalTop = offset.top;
    let modalLeft = offset.left;
    let modalRight = modalLeft + modalContentElement.width();
    let modalBottom = modalTop + modalContentElement.height();
    let screenPosition = DialogService.getWindowPositionOnScreen();

    if (modalBottom > screenPosition.bottom) {
      modalTop = screenPosition.bottom - modalContentElement.height();
    }
    if (modalTop < screenPosition.top) {
      modalTop = screenPosition.top;
    }
    if (modalLeft < 0) {
      modalLeft = 0;
    }
    if (modalRight > DialogService.getWindowWidth()) {
      modalLeft = modalLeft - (modalRight - DialogService.getWindowWidth());
    }

    modalDialogElement.offset({
      top: modalTop,
      left: modalLeft
    });
  }

  /**
   * Gets the bottom pixel screen position taking into account if the user has scrolled down.
   * @returns {*}
   */
  static getWindowPositionOnScreen() {
    return {top: window.pageYOffset, bottom: window.pageYOffset + DialogService.getWindowHeight()};
  }

  static getWindowHeight() {
    return $(window).height();
  }

  static getWindowWidth() {
    return $(window).width();
  }

  static getPopover(parent, action) {
    return parent.find('.alert-message').popover(action);
  }

  closeExistingDialogs() {
    $('.modal').modal('hide');
  }

  showModal(modal) {
    if (this.hideInProgress) {
      this.queuedModal = modal;
    } else {
      modal.modal({backdrop: modal.backdrop});
    }
  }

  hideModal(modal) {
    this.hideInProgress = true;
    modal.on('hidden.bs.modal', () => {
      this.hideInProgress = false;
      if (this.queuedModal) {
        this.showModal(this.queuedModal);
      }
      this.queuedModal = '';
      modal.off('hidden.bs.modal');
    });
    modal.modal('hide');
  }

  static configureBackdrop(dialogConfig) {
    var backdrop = dialogConfig.modeless ? false : dialogConfig.backdrop;
    return backdrop !== undefined ? backdrop : 'static';
  }

  createButton(id, labelId, primary) {
    var btn = {
      id: id,
      label: this.translateService.translateInstant(labelId)
    };

    if (primary) {
      btn.cls = 'btn-primary';
    }
    return btn;
  }
}

DialogService.CANCEL = 'CANCEL';
DialogService.CLOSE = 'CLOSE';
DialogService.CONFIRM = 'CONFIRM';
DialogService.NO = 'NO';
DialogService.OK = 'OK';
DialogService.YES = 'YES';
