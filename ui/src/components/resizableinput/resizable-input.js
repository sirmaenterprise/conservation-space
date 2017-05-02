import _ from 'lodash';
import {View,Component,Inject,NgElement,NgScope} from 'app/app';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {Configuration} from 'common/application-config';
import './resizable-input.css!';
import resizableInputTemplate from './resizable-input.html!text';

@Component({
  selector: 'seip-resizable-input',
  properties: {
    'config': 'config',
    'editmode': 'editmode'
  }
})
@View({
  template: resizableInputTemplate
})
@Inject(NgElement, NgScope, '$attrs', '$document', WindowAdapter, Configuration)
export class ResizableInput {

  constructor($element, $scope, $attrs, $document, windowAdapter, configuration) {
    this.wrapper = angular.element('<div style="position:fixed; top:-999px; left:0;"></div>');
    angular.element($document[0].body).append(this.wrapper);
    this.$scope = $scope;
    this.ngModel = $element.controller('ngModel');
    this.config = this.config || '';
    this.windowAdapter = windowAdapter;
    this.textfieldMaxCharsLength = configuration.get(Configuration.IDOC_TABS_TITLE_MAX_LENGTH);

    let attrs = $attrs;
    let element = $element;

    // Disable trimming inputs by default
    attrs.$set('ngTrim', attrs.ngTrim === 'true' ? 'true' : 'false');

    let span = angular.element('<span style="white-space:pre;"></span>');
    this.applyStyleToSpan(span, element);
    this.wrapper.append(span);

    /**
     * Debounces the functionality in order to prevent too often resize when text is typed really fast.
     */
    let resize  = _.debounce((element, attrs, span) => {
      span.text(element.val() || attrs.placeholder || '');
      element.css('width', span.prop('offsetWidth') + 2 + 'px');
    }, 100, false);

    this.$scope.$watch(attrs.ngModel, () => {
      resize(element, attrs, span);
    });

    // IE shows caret even if input is readonly. Blur prevent this behaviour
    this.$scope.$watch(()=> {
      if(!this.editmode) {
        $element.blur();
      }
    });

    element.val(this.config);
    this.$scope.$on('$destroy', function () {
      span.remove();
    });
  };

  /**
   * Apply styles and borders to be sure that if font size is different resize will be done correctly
   * @param span - span element containing entered text
   * @param element - original element
   */
  applyStyleToSpan(span, element) {
    let style = this.windowAdapter.window.getComputedStyle(element[0]);

    ['fontFamily', 'fontSize', 'fontWeight', 'fontStyle',
     'letterSpacing', 'textTransform', 'wordSpacing'].forEach(function (value) {
      span.css(value, style[value]);
    });
  }

  ngOnDestroy() {
    this.wrapper.remove();
  }
}
