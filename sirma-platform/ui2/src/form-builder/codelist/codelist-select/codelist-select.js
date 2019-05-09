import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import {NavigatorAdapter} from 'adapters/navigator-adapter';
import {JsonUtil} from 'common/json-util';

import template from './codelist-select.html!text';

@Component({
  selector: 'codelist-select',
  properties: {
    'validationModel': 'validation-model',
    'config': 'config',
    'form': 'form'
  }
})
@View({template})
@Inject(NgElement, NgScope)
export class CodelistSelect {
  constructor($element, $scope) {
    this.$element = $element.find('select');
    this.$scope = $scope;
    this.createActualConfig();

    this.config.dataLoader(null).then((response) => {
      // If this element is destroyed right after creation,
      // this promise may remain alive, so we must stop it in order to
      // return this object to the browser's GC.
      if (!$.contains(document, this.$element[0])) {
        return;
      }
      this.config.data = [];
      this.config.dataConverter(response).forEach((item) => {
        this.config.data.push(item);
      });
      this.initSelect();
    });
  }

  initSelect() {
    this.$element.select2(this.config);

    if (this.validationModel.value) {
      this.$element.val(this.validationModel.value).trigger('change');
      //current value must be added because in some cases a value is changed
      // but the select component updates its value too slow resulting in an endless loop.
      this.currentValue = this.validationModel.value;
    } else {
      this.$element.val(null).trigger('change');
    }
    this.bindToModel();
    this.preventAutoOpening();

    //fix for CMF-21368 where in Edge, when the select is open or closed, the page is scrolled.
    if (NavigatorAdapter.isEdge()) {
      let idocEditor = $('.idoc-content-container.idoc-editor');
      this.$element.on('select2:open', () => {
        this.$element.focus();
        idocEditor.attr({contenteditable: false});
      });

      this.$element.on('select2:close', () => {
        idocEditor.attr({contenteditable: true});
      });
    }

    // Needed for edited by user flag used to stop suggest in value suggest dropdowns
    this.$element.on('select2:select', this.config.onSelectCallback);
    this.$element.on('select2:unselect', this.config.onSelectCallback);
  }

  createActualConfig() {
    let ajaxConfig = {};
    ajaxConfig.transport = (params, success, failure) => {
      this.config.dataLoader(params).then(success, failure);
    };

    ajaxConfig.processResults = (data, params) => {
      return {results: this.config.dataConverter(data, params)};
    };

    JsonUtil.copyProperty(ajaxConfig, 'delay', this.config);
    this.config.ajax = ajaxConfig;
  }

  bindToModel() {
    this.$element.on('change', () => {
      let newVal = this.$element.val();
      if (!this.compareValues(newVal, this.currentValue)) {
        this.$scope.$evalAsync(() => {
          this.currentValue = newVal ? newVal : this.validationModel.value instanceof Array ? [] : null;
          this.validationModel.value = this.currentValue;
        });
      }
    });

    this.validationModelSubscription = this.validationModel.subscribe('propertyChanged', (propertyChanged) => {
      if (Object.keys(propertyChanged)[0] === 'value' && !this.compareValues(propertyChanged.value, this.$element.val())) {
        this.currentValue = propertyChanged.value;
        if (this.currentValue) {
          this.optionHandler();
        }
        this.$element.val(this.currentValue).trigger('change');
      }
    });

    // manually destroy the element and unsubscribe to prevent memory leak
    this.$element.on('remove', () => {
      if (this.$element.data('select2')) {
        this.$element.select2('destroy');
      }
      this.validationModelSubscription.unsubscribe();
    });
  }

  //convenience method for handling selection changes
  optionHandler() {
    if (this.validationModel.value instanceof Array) {
      // emptied so when the change event is triggered there are no old values being retrieved,
      // which results in unending addition of more properties and blocking the UI.
      this.$element.empty();
      this.validationModel.value.forEach((item) => {
        if (this.validationModel.sharedCodelistData[item]) {
          this.$element.append(new Option(this.validationModel.sharedCodelistData[item], item, true, true));
        }
      });
    } else {
      if (this.validationModel.sharedCodelistData[this.currentValue]) {
        this.$element.append(new Option(this.validationModel.sharedCodelistData[this.currentValue], this.currentValue, true, true));
      }
    }
  }

  /**
   * Prevents auto opening of the select2 menu when an option is deselected.
   *
   * @param element - the select2 element
   */
  preventAutoOpening() {
    this.$element
      .on('select2:opening', this.handleMenuOpeningEvent.bind(this))
      .on('select2:unselecting', this.handleUnselectingEvent.bind(this));
  }

  handleMenuOpeningEvent(event) {
    if (this.keepMenuClosed) {
      this.keepMenuClosed = false;
      event.preventDefault();
    }
  }

  handleUnselectingEvent(event) {
    if (!event.params || !event.params.args || !event.params.args.originalEvent || !event.params.args.originalEvent.target) {
      return;
    }

    var target = event.params.args.originalEvent.target;
    if ($(target).is('.select2-selection__choice__remove')) {
      this.keepMenuClosed = true;
    }
  }

  /**
   * returns true if equal
   * @param firstValue
   * @param secondValue
   * @returns {boolean}
   */
  compareValues(firstValue, secondValue) {
    if (firstValue === secondValue) {
      return true;
    } else if (firstValue instanceof Array && secondValue instanceof Array && firstValue.length === secondValue.length) {
      for (let i = 0; i < firstValue.length; i++) {
        if (firstValue[i] !== secondValue[i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  ngOnDestroy() {
    this.$element.off();
  }

}
