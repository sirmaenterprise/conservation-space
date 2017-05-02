import {View, Component, Inject, NgScope, NgElement, NgTimeout} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {IdocEditorFactory} from './editor';
import {IdocEditorListeners} from './idoc-editor-listeners';
import {EditorReadyEvent} from 'idoc/editor/editor-ready-event';
import {EditorResizedEvent} from 'idoc/editor/editor-resized-event';
import {WindowResizedEvent} from 'layout/window-resized/window-resized-event';
import {IdocContentModelUpdateEvent} from 'idoc/events/idoc-content-model-update-event';
import {IdocUpdateUndoManagerEvent} from 'idoc/events/idoc-update-undo-manager-event';
import {IdocTabOpenedEvent} from 'idoc/idoc-tabs/idoc-tab-opened-event';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {PluginsService} from 'services/plugin/plugins-service';
import {ResizeDetectorAdapter} from 'adapters/resize-detector-adapter';
import _ from 'lodash';
import 'idoc/widget/widget';
import editorColumnTemplate from './idoc-editor.html!text';

export const EDITOR_LINKS_SELECTOR = '[data-cke-saved-href]';

@Component({
  selector: 'idoc-editor',
  properties: {
    'editmode': 'editmode',
    'tab': 'tab',
    'context': 'context'
  }
})
@View({
  template: editorColumnTemplate
})
@Inject(NgScope, NgTimeout, NgElement, IdocEditorFactory, Eventbus, TranslateService, PromiseAdapter, PluginsService, ResizeDetectorAdapter)
export class Editor {

  constructor($scope, $timeout, $element, idocEditorFactory, eventbus, translateService, promiseAdapter, pluginsService, resizeDetectorAdapter) {
    this.idocEditorFactory = idocEditorFactory;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
    this.$element = $element;
    this.$scope = $scope;
    this.ngModel = $element.controller('ngModel');
    this.editorClass = Editor.editorClass;
    this.events = [];
    this.eventbus = eventbus;
    this.pluginsService = pluginsService;
    this.resizeDetectorAdapter = resizeDetectorAdapter;
    $element.addClass('idoc-content-container idoc-editor');
    // tab id cannot be used because CKEditor has issues with UUIS
    this.editorId = 'editor' + Math.round(Math.random() * 10000);
    $element.attr('id', this.editorId);

    this.events = [
      eventbus.subscribe(IdocTabOpenedEvent, (tab) => {
        // Timeout is needed to execute editor focus after tab click event
        // otherwise focus is gained by tab.
        if (this.editmode && this.tab.id === tab.id) {
          $timeout(() => {
            this.editor.focus();
          }, 0);
        }
      }),
      eventbus.subscribe(IdocUpdateUndoManagerEvent, (tab) => {
        if (this.editmode && this.tab.id === tab.id) {
          this.editor.undoManager.update();
        }
      }),
      // As part of it's undo mechanism, CKEditor adds a hidden node to the DOM every time a widged is added. Part of the code as follows:
      // http://docs.ckeditor.com/source/selection.html
      //
      // Creates cke_hidden_sel container and puts real selection there.
      // function hideSelection( editor, ariaLabel ) {
      //  var content = ariaLabel || '&nbsp;',
      //    style = CKEDITOR.env.ie && CKEDITOR.env.version < 14 ? 'display:none' : 'position:fixed;top:0;left:-1000px',
      //    hiddenEl = CKEDITOR.dom.element.createFromHtml(
      //      '<div data-cke-hidden-sel="1" data-cke-temp="1" style="' + style + '">' + content + '</div>',
      //      editor.document );
      //    ...
      // The problem occurs by inserting all types of widgets.
      // This subscribe resets the content of variable "content", as is not removed by CKEditor when the node is deleted.
      eventbus.subscribe(WidgetReadyEvent, () => {
        if (this.editor._.hiddenSelectionContainer) {
          this.editor._.hiddenSelectionContainer.$.innerText = "";
        }
      })
    ];

    //The $watch below is a workaround placed here because of the following problem:
    //Each time idoc is switched between edit/preview mode, its child components (navigation,idoc-editor,comments) are destroyed for some reason(?).
    //This implies recreating idoc-editor every time and the need to synchronize its full initialization with the editmode change.
    //Currently, there are too many synchronizations here because of this, which makes this code error-prone.
    $scope.$watch(() => {
      return this.editmode;
    }, () => {
      // editor is recreated because there is no other way to load proper plugins at runtime
      if (this.editor) {
        this._setModelValue(this.editor.getData());
        this.idocEditorFactory.destroy(this.editor);
      }
      idocEditorFactory.init(this.$element[0], this.editmode).then((editor) => {
        this.editor = editor;
        new IdocEditorListeners({
          eventbus: {
            instance: eventbus,
            channel: this.tab.id
          }
        }).listen(this.editor);
        //wait for the editor to be fully initialized
        this.onEditorReady(this.editor).then(() => {
          this.handleEditModeChange();

          // Subscribe after editor is initialized to avoid overriding content with empty value
          this.events.push(eventbus.subscribe(IdocContentModelUpdateEvent, () => {
            this._setModelValue(this.editor.getData());
          }));
          this.calculateMinimumEditorHeight();
          eventbus.publish(new EditorReadyEvent(this.editorId));
        });
      });
    });

    let editorWrapper = this.$element.parent();
    this.editorDimensions = {
      width: editorWrapper.width(),
      height: editorWrapper.height()
    };
    this.editorIdocEditorResizeListener = this.resizeDetectorAdapter.addResizeListener(editorWrapper[0], this.onIdocEditorResize.bind(this, editorWrapper));
  }

  onIdocEditorResize(editorWrapper) {
    this.calculateMinimumEditorHeight();
    let newWidth = editorWrapper.width();
    let newHeight = editorWrapper.height();
    let evtData = {
      editorId: this.editorId,
      width: newWidth,
      height: newHeight,
      widthChanged: this.editorDimensions.width !== newWidth,
      heightChanged: this.editorDimensions.height !== newHeight
    };
    this.editorDimensions.width = evtData.width;
    this.editorDimensions.height = evtData.height;
    this.eventbus.publish(new EditorResizedEvent(evtData));
  }

  handleEditModeChange() {
    this.editor.setReadOnly(!this.editmode);
    let editorElement = $(this.editor.element.$);
    this.toggleContentEditable(editorElement, this.editmode);
    //edit/preview css classes are needed to prevent widgets' drag handlers to appear when the document
    //is in preview mode. They are put inside this watch to avoid concurrency issue in the e2e tests.
    this.$element.removeClass('edit preview');
    this.$element.addClass(this.editmode ? 'edit' : 'preview');
    if (this.editmode) {
      editorElement.find(EDITOR_LINKS_SELECTOR).unbind('click');
    } else {
      this.attachClickHandlersToLinks(editorElement);
    }
  }

  /**
   * Returns a promise indicating when the editor is fully initialized.
   *
   * @param editor is the editor instance
   * @returns {Promise} when the editor is fully initialized
   */
  onEditorReady(editor) {
    return this.promiseAdapter.promise((resolve) => {
      // Fix for http://ckeditor.com/forums/Support/inline-editor-div-contenteditable-when-loaded-hidden-doesnt-work.
      editor.on('instanceReady', (event) => {

        //When layout is inserted we have to wait awhile before adding a new line
        editor.on('layoutmanager:layout-inserted', () => {
          setTimeout(() => {
            editor.execCommand('shiftEnter');
          }, 100);
        });

        // After instating all plugins, invokes their before editor set content function. chainProcessors function ensures "synchronous" execution.
        // After editor.setData() the chainProcessors function is executed again with after editor set content processors' function.
        return this.pluginsService.loadPluginServiceModules('editor-content-processors', 'name', true).then((processors) => {

          return this.chainProcessors(processors, "preprocessContent", this.ngModel.$viewValue).then((result) => {
            editor.fire('lockSnapshot');
            editor.setData(result, {
              callback: () => {
                return this.chainProcessors(processors, "postprocessContent").then(() => {
                  editor.fire('unlockSnapshot');

                  if (this.editmode) {
                    // sets focus over CKEditor editable range and moves to the root position
                    var range = editor.createRange();
                    range.moveToElementEditEnd(range.root);
                    editor.getSelection().selectRanges([range]);
                    editor.focus();
                  }

                  this.$element.addClass('initialized');
                  resolve(editor);
                });
              }
            });
          });
        });
      });
    });
  }

  // Synchronous invokes processors functions.
  chainProcessors(processors, funcName, initialValue, index) {
    if (!index) {
      index = 0;
    }

    if (processors.length < index) {
      return this.promiseAdapter.resolve(initialValue);
    }

    let converterExecutor = this.promiseAdapter.resolve(processors[index][funcName](this, initialValue));

    if (processors[index + 1]) {
      return converterExecutor.then((result) => {
        return this.chainProcessors(processors, result, index + 1);
      });
    }
    return converterExecutor;
  }

  /**
   * Attaches click handlers to the anchor elements within the given parent.
   * This is needed, because CKEditor prevents navigating to external pages when it is in preview mode.
   *
   * @param parentElement is the parent element as jquery object
   */
  attachClickHandlersToLinks(parentElement) {
    parentElement.find(EDITOR_LINKS_SELECTOR).click(function (event) {
      if (this.href) {
        window.open(this.href, 'new' + event.screenX);
      }
    });
  }

  /**
   * Toggles the elements' contenteditable attribute, according to the passed editMode.
   * This is a workaround for the following CkEditor bug: http://dev.ckeditor.com/ticket/12134
   * which is expected to be fixed in CkEditor release 4.6.0
   *
   * @param parentElement
   *                      is the parent element as jquery object
   * @param editMode
   *                is the edit mode of the idoc
   */
  toggleContentEditable(parentElement, editMode) {
    parentElement.find('.cke_widget_editable').each(function () {
      $(this).attr('contenteditable', editMode.toString());
    });
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
    if (this.editor) {
      this.idocEditorFactory.destroy(this.editor);
    }
    // remove the resize listener
    this.editorIdocEditorResizeListener();
  }

  /**
   * Safely sets model value.
   * @param newValue to be set
   */
  _setModelValue(newValue) {
    if (this.ngModel) {
      this.ngModel.$setViewValue(newValue);
    }
  }

  /**
   * Stretches the editor to the visible window bottom if there is no content that would otherwise expand it.
   */
  calculateMinimumEditorHeight() {
    if (this.$element.css('visibility') !== 'hidden') {
      var topOffset = this.$element.offset().top;
      this.$element.css({
        'min-height': `calc(100vh - ${topOffset}px)`
      });
    }
  }
}

Editor.editorClass = 'ck-editor-area';
