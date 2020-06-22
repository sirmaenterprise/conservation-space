import $ from 'jquery';

export class ToCUtils {
  /**
   * Constructs a string with tag names of the headings
   * with equal or smaller number for a given heading
   * @param headingSize {String}
   * @returns {String} The generated string.
   */
  static calcSmallerOrEqualHeadings(headingSize) {
    //collect a list with all elements
    let smallerOrEqualHeadings = '';
    for (let i = 1; i <= headingSize; i++) {
      smallerOrEqualHeadings = smallerOrEqualHeadings + 'H' + i + ((i) < headingSize ? ',' : '');
    }
    return smallerOrEqualHeadings;
  }

  static getTagName(element) {
    if (element) {
      return $(element).prop('tagName');
    }
  }

  static getHeadingSize(heading) {
    let tagName = ToCUtils.getTagName(heading);
    if (tagName) {
      return tagName.substring(1);
    }
  }

  static getSmallerOrEqualHeadings(headingElement) {
    let headingSize = ToCUtils.getHeadingSize(headingElement);
    if (headingSize) {
      return ToCUtils.calcSmallerOrEqualHeadings(headingSize);
    }
  }

}