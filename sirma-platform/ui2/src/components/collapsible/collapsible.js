import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import 'filters/to-trusted-html';
import 'components/collapsible/collapsible.css!';
import 'font-awesome/css/font-awesome.css!';

@Component({
  selector: 'seip-collapsible',
  properties: {
    // Target css selector
    target: 'target',
    // Label for the collapse trigger
    label: 'label'
  }
})
@View({
  template: '<div class="collapsible" ng-click="collapsible.onClick()"><i class="fa"></i> <span ng-bind-html="::collapsible.label | toTrustedHtml"></span></div>'
})
@Inject(NgScope, NgElement, '$timeout')
/**
 * Component used to collapse/expand DOM elements. Require target css selector.
 */
export class Collapsible {
  constructor($scope, $element, $timeout) {
    this.$element = $element;
    if (!this.target) {
      throw new TypeError('Must provide target property.');
    } else {
      // added timeout because if collapsible and its target are resolved by the same directive the watcher doesn't trigger until next digest
      $timeout(()=> {
        let targetWatcher = $scope.$watch(() => {
          if ($(this.target)[0]) {
            return $(this.target);
          }
        }, (targetElement) => {
          this.targetElement = targetElement;
          Collapsible.toggleCaret(this.$element.find('>:first-child'), !Collapsible.isElementVisible(this.targetElement));
          targetWatcher();
        });
      }, 0);
    }
  }

  onClick() {
    if (this.targetElement) {
      let expanded = Collapsible.isElementVisible(this.targetElement);
      Collapsible.toggleCaret(this.$element.find('>:first-child').eq(0), expanded);
      this.targetElement.toggle(!expanded);
    }
  }

  /**
   * Toggle collapsible element caret down/right depending on its target visibility
   * @param expanded - true if target is visible, false otherwise
   */
  static toggleCaret(iconElement, expanded) {
    if (!expanded) {
      iconElement.removeClass('fa-caret-right');
      iconElement.addClass('fa-caret-down');
    } else {
      iconElement.removeClass('fa-caret-down');
      iconElement.addClass('fa-caret-right');
    }
  }

  static isElementVisible(element) {
    return element && element.css('display') !== 'none' && element.css('visibility') !== 'hidden';
  }
}
