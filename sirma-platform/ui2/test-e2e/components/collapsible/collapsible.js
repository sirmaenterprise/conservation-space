'use strict';

const hasClass = (element, cls) => {
  return element.getAttribute('class').then(function (classes) {
    return classes.split(' ').indexOf(cls) !== -1;
  });
};

class Collapsible {
  constructor(selector) {
    this.collapseElement = $(selector);
  }

  isCollapsed() {
    var iconElement = this.collapseElement.element(by.tagName('i'));
    return protractor.promise.all([hasClass(iconElement, 'fa-caret-down'), hasClass(iconElement, 'fa-caret-right')]).then((result) => {
      return this.collapseElement.getAttribute('target').then((targetSelector) => {
        // Evaluate target attribute expression from collapsible element
        return this.collapseElement.evaluate(targetSelector);
      }).then((evaluatedTarget) => {
        return $(evaluatedTarget).isDisplayed();
      }).then((isTargetVisible) => {
        return !result[0] && result[1] && !isTargetVisible;
      });
    });
  }

  isElementPresent(){
    return this.collapseElement.isPresent();
  }
  isCollapsible() {
    return this.collapseElement.isDisplayed();
  }

  toggleCollapse() {
    this.collapseElement.click();
  }

  isCollapsedAndEmpty() {
    var iconElement = this.collapseElement.element(by.tagName('i'));
    return protractor.promise.all([hasClass(iconElement, 'fa-caret-down'), hasClass(iconElement, 'fa-caret-right')]).then((result) => {
      return this.collapseElement.getAttribute('target').then((targetSelector) => {
        return this.collapseElement.evaluate(targetSelector);
      }).then((evaluatedTarget) => {
        return $(evaluatedTarget).isPresent();
      }).then((isTargetPresent) => {
        return !result[0] && result[1] && !isTargetPresent;
      });
    });
  }
}

module.exports.Collapsible = Collapsible;
