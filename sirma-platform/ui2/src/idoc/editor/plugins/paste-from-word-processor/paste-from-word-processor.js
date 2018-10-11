import {Injectable} from 'app/app';
import _ from 'lodash';

const ALL_ELEMENTS_MATCHER = '*';

/**
 * A processor executed after paste from word occur. It is executed after all preliminary filters like
 * CKEditor's advanced content filter and all filters applied from paste from word plugin.
 *
 * Currently it only remove/override/add styles described via configuration.
 * Note that adding styles is applied only to elements which means that if plain text is pasted no styles will be applied to it.
 *
 * Configuration should be in the following JSON format:
 * {
 *  <ELEMENT_NAME>: {
 *    <STYLE_NAME>: <VALUE>
 *  }
 * }
 *
 * where:
 * ELEMENT_NAME is the name of the element which styles to be processed. It could be '*' for all elements.
 * Value could be either an object or empty value ('', null, undefined - use null for consistency) in which case all styles will be removed.
 * STYLE_NAME is the name of the style to be processed.
 * VALUE new value for the particular style. If it is empty value ('', null, undefined - use null for consistency) the style will be removed.
 * Also a function can be passed which will accept old value as parameter and should return the new value.
 *
 * Example configuration:
 * {
 *  '*': {
 *    'color': 'red',
 *    'background-color': null
 *  },
 *  'p': null,
 *  'table': {
 *    'width': (oldValue) => {
 *      if (!oldValue) {
 *        return '100%';
 *      }
 *      return oldValue;
 *    }
 *  }
 * }
 *
 * Given configuration will set all elements color to red and will remove background-color style from all elements.
 * All styles for p elements will be removed.
 * All tables width will be set to 100% if no width is set or will not be changed if it already have width set.
 *
 * Code is loosely based on https://github.com/ckeditor/ckeditor-dev/blob/master/plugins/pastefromword/filter/default.js
 */
@Injectable()
export class PasteFromWordProcessor {
  constructor() {
    CKEDITOR.on('instanceCreated', (instanceCreatedEvent) => {
      let editor = instanceCreatedEvent.editor;
      editor.on('afterPasteFromWord', this.processAfterPasteFromWordEvent.bind(this));
    });
  }

  processAfterPasteFromWordEvent(event) {
    let fragment = CKEDITOR.htmlParser.fragment.fromHtml(event.data.dataValue);

    let filters = event.editor.config.pasteFromWordFilters;

    let filter = new CKEDITOR.htmlParser.filter({
      attributes: {
        'style': (styles, element) => {
          if (filters) {
            return this.normalizeStyles(element, filters);
          }
          return styles;
        },
        // remove width attributes
        'width': () => false
      }
    });
    filter.applyTo(fragment);
    let writer = new CKEDITOR.htmlParser.basicWriter();
    fragment.writeHtml(writer);
    event.data.dataValue = writer.getHtml();
  }

  normalizeStyles(element, filters) {
    let styles = CKEDITOR.tools.parseCssText(element.attributes.style);

    // Apply filters applicable for all elements
    styles = this.applyFilter(styles, filters, ALL_ELEMENTS_MATCHER);

    // Apply specific filters for current element
    styles = this.applyFilter(styles, filters, element.name);

    return CKEDITOR.tools.writeCssText(styles);
  }

  /**
   * Apply filters per element
   * @param styles
   * @param allFilters
   * @param elementName
   * @returns {*}
   */
  applyFilter(styles, allFilters, elementName) {
    if (allFilters.hasOwnProperty(elementName)) {
      let currentFilter = allFilters[elementName];
      // If filter for given element is empty ('', null, undefined) all styles are removed
      if (_.isEmpty(currentFilter)) {
        styles = {};
      } else {
        styles = this.processFilter(styles, currentFilter);
      }
    }
    return styles;
  }

  processFilter(styles, filter) {
    let attributeKeys = Object.keys(filter);
    attributeKeys.forEach((attributeKey) => {
      let attributeFilter = filter[attributeKey];
      if (_.isFunction(attributeFilter)) {
        styles[attributeKey] = attributeFilter(styles[attributeKey]);
      } else if (_.isEmpty(attributeFilter)) {
        delete styles[attributeKey];
      } else if (_.isString(attributeFilter)) {
        styles[attributeKey] = attributeFilter;
      }
    });
    return styles;
  }
}
