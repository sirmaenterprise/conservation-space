import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';

import CodeMirror from 'codemirror';
import 'codemirror/mode/xml/xml';
import 'codemirror/mode/css/css';
import 'codemirror/mode/htmlmixed/htmlmixed';
import 'codemirror/mode/javascript/javascript';
import 'codemirror/addon/hint/show-hint';
import 'codemirror/addon/hint/xml-hint';
import 'codemirror/addon/hint/html-hint';
import 'codemirror/addon/edit/matchtags';
import 'codemirror/addon/edit/closebrackets';
import 'codemirror/addon/edit/closetag';
import 'codemirror/addon/edit/matchbrackets';
import 'codemirror/addon/edit/trailingspace';
import 'codemirror/addon/fold/xml-fold';
import 'codemirror/addon/search/match-highlighter';
import 'codemirror/addon/selection/active-line';

import 'codemirror/lib/codemirror.css!';
import 'codemirror/addon/hint/show-hint.css!';

import './sourcearea.css!css';
import template from './sourcearea.html!text';

/**
 * A reusable component which is capable to render text value containing various types of source code like: xml, html,
 * json, programming languages and so on. It is a simple angular directive wrapping the CodeMirror library
 * <a>https://codemirror.net/</a>.
 *
 * This component requires its value to be passed through the ngModel directive. The changes in the model value can then
 * be observed in the standard way by simply implementing an ng-change handler (see /sandbox/components/sourcearea for
 * an example).
 *
 * The configuration object is completely optional. If provided it must contains only options specific to the CodeMirror
 * library and should not be used for anything else. If in the future this component appears to need specific
 * configurations, then they might be passed in separate config object for example.
 *
 * CodeMirror library is pre-configured by default with some additional add-ons in order to suit the most likely needs.
 * If additional configuration or add-on is needed, then the library documentation must be consulted for a guideline,
 * because many configurations and add-ons needs additional source files and stylesheets to be pre-loaded in order to
 * work properly.
 *
 * @author svelikov
 */
@Component({
  selector: 'seip-sourcearea',
  properties: {
    config: 'config'
  }
})
@View({template})
@Inject(NgElement, NgScope)
export class Sourcearea extends Configurable {

  constructor($element, $scope) {
    super({
      // Along with css height=auto, this is the recommended way to force the editor to auto expand
      // and wrap whole content
      viewportMargin: Infinity,
      // possible mode options: text/html, javascript, json
      mode: 'htmlmixed',
      readOnly: false,
      autoRefresh: true,
      lineNumbers: true,
      lineWrapping: true,
      matchBrackets: true,
      matchTags: {bothTags: true},
      autoCloseBrackets: true,
      autoCloseTags: true,
      styleActiveLine: true,
      showTrailingSpace: true,
      highlightSelectionMatches: true,
      extraKeys: {
        'Ctrl-Space': 'autocomplete',
        'Ctrl-J': 'toMatchingTag'
      }
    });
    this.$element = $element;
    this.$scope = $scope;
    this.ngModel = $element.controller('ngModel');
  }

  ngAfterViewInit() {
    let textarea = this.$element.find('textarea')[0];
    this.editor = CodeMirror.fromTextArea(textarea, this.config);
    this.setEditorValue(this.ngModel.$viewValue);
    this.bindToModel();
  }

  bindToModel() {
    if (this.ngModel) {
      this.editor.on('change', () => {
        let value = this.editor.getValue();
        // prevent updating the model if value is not changed
        if (this.ngModel.$viewValue === null && value.length || this.ngModel.$viewValue !== null) {
          this.ngModel.$setViewValue(value);
        }
      });

      this.$scope.$watch(() => this.ngModel.$viewValue, (newValue) => {
        if (newValue !== this.editor.getValue()) {
          this.setEditorValue(newValue);
        }
      });
    }
  }

  setEditorValue(value) {
    // the editor don't respect null values
    this.editor.setValue(value || '');
  }

  ngOnDestroy() {
    this.editor.off('change');
  }
}