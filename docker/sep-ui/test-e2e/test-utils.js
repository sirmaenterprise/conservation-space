'use strict';

let hasClass = (element, clazz) => {
  return element.getAttribute('class').then((classes) => classes.split(/\s+/gm).indexOf(clazz) !== -1);
};

/**
 * Returns true if element has requested inline css style
 * @param element
 * @param css - css style (ex. 'width: 250px;')
 * @returns {Promise<R>} which resolves with a boolean
 */
let hasCss = (element, css) => {
  return element.getAttribute('style').then((cssValue) => cssValue.indexOf(css) !== -1);
};

let isCheckboxSelected = (checkboxElement) => {
  return checkboxElement.getAttribute('checked').then((checked) => {
    return !!checked;
  });
};

let isInViewPort = element => {
  let script = `
    let rect = arguments[0].getBoundingClientRect();
    let viewHeight = Math.max(document.documentElement.clientHeight, window.innerHeight);
    return !(rect.bottom < 0 || rect.top - viewHeight >= 0);
  `;
  return browser.executeScript(script, element);
};

/**
 * Expected condition for existence of multiple elements selected with element.all
 * @param elements
 * @returns {Function}
 */
let existenceOf = elements => elements.count().then(count => count > 0);

module.exports = {
  hasClass,
  hasCss,
  isCheckboxSelected,
  isInViewPort,
  existenceOf
};