'use strict';

var hasClass = (element, clazz) => {
  return element.getAttribute('class').then((classes) => classes.split(/\s+/gm).indexOf(clazz) !== -1);
};

/**
 * Returns true if element has requested inline css style
 * @param element
 * @param css - css style (ex. 'width: 250px;')
 * @returns {Promise<R>} which resolves with a boolean
 */
var hasCss = (element, css) => {
  return element.getAttribute('style').then((cssValue) => cssValue.indexOf(css) !== -1);
};

module.exports = {
  hasClass: hasClass,
  hasCss: hasCss
};