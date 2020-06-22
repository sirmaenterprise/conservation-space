import _ from 'lodash';
import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {Configuration} from 'common/application-config';
import './resizable-input.css!';
import template from './resizable-input.html!text';

/**
 * An input field which auto resizes itself in order to contain the whole tab title in one line.
 *
 * Configuration:
 * config: [string] - the tab title if already provided
 * editmode: [true|false]  - if the document is in edit mode or not
 */
@Component({
  selector: 'seip-resizable-input',
  properties: {
    'config': 'config',
    'editmode': 'editmode'
  }
})
@View({
  template
})
@Inject(NgElement, NgScope, '$attrs', '$document', WindowAdapter, Configuration)
export class ResizableInput {

  constructor($element, $scope, $attrs, $document, windowAdapter, configuration) {
    this.windowAdapter = windowAdapter;
    this.$document = $document;
    this.$element = $element;
    this.$scope = $scope;
    this.$attrs = $attrs;

    this.textfieldMaxCharsLength = configuration.get(Configuration.IDOC_TABS_TITLE_MAX_LENGTH);
  };

  ngOnInit() {
    this.wrapper = angular.element('<div style="position:fixed; top:-999px; left:0;"></div>');
    angular.element(this.$document[0].body).append(this.wrapper);

    // Disable trimming inputs by default
    this.$attrs.$set('ngTrim', this.$attrs.ngTrim === 'true' ? 'true' : 'false');

    let span = angular.element('<span style="white-space:pre;"></span>');
    this.applyStyleToSpan(span, this.$element);
    this.wrapper.append(span);

    // Debounces the functionality in order to prevent too often resize when text is typed really fast.
    let resize = _.debounce((element, attrs, span) => {
      span.text(element.val() || attrs.placeholder || '');
      element.css('width', span.prop('offsetWidth') + 2 + 'px');
    }, 100, false);

    this.$scope.$watch(this.$attrs.ngModel, () => {
      resize(this.$element, this.$attrs, span);
    });

    // IE shows caret even if input is readonly. Blur prevent this behaviour
    this.$scope.$watch(() => {
      if (!this.editmode) {
        this.$element.blur();
      }
    });

    this.$element.val(this.config);
    this.$scope.$on('$destroy', function () {
      span.remove();
    });
  }

  /**
   * Apply styles and borders to be sure that if font size is different resize will be done correctly
   * @param span - span element containing entered text
   * @param element - original element
   */
  applyStyleToSpan(span, element) {
    let style = this.windowAdapter.window.getComputedStyle(element[0]);

    ['fontFamily', 'fontSize', 'fontWeight', 'fontStyle', 'letterSpacing', 'textTransform', 'wordSpacing'].forEach((value) => {
      span.css(value, style[value]);
    });
  }

  ngOnDestroy() {
    this.wrapper.remove();
  }
}
