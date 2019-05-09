import {Component, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import {Select} from 'components/select/select';
import {RelationsService} from 'services/rest/relations-service';
import {NavigatorAdapter} from 'adapters/navigator-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {HEADER_COMPACT, HEADER_DEFAULT, HEADER_BREADCRUMB} from 'instance-header/header-constants';

import _ from 'lodash';

export const OBJECT_SELECT_PROPERTIES = ['id', 'title', 'altTitle', HEADER_COMPACT, HEADER_DEFAULT, HEADER_BREADCRUMB];

@Component({
  selector: 'seip-instance-suggest-select',
  properties: {
    'config': 'config'
  }
})

@Inject(NgElement, NgScope, NgTimeout, RelationsService, TranslateService)
export class InstanceSuggestSelect extends Select {

  constructor($element, $scope, $timeout, relationsService, translateService) {
    super($element, $scope, $timeout, translateService);
    this.relationsService = relationsService;
    this.translateService = translateService;
    this.element = $element;
  }

  ngOnInit() {
    this.updatedSelectionHandler = this.config.eventEmitter.subscribe('updatedSelection', this.onUpdatedSelection.bind(this));

    this.$element.on('select2:selecting', (event) => {
      // preventing the default add of selected, because it will be set after updating instance selector's selection pool
      event.preventDefault();
      this.config.eventEmitter.publish('selecting', [{id: event.params.args.data.id, headers: event.params.args.data}]);
      this.$element.select2('close');
    });

    // fix for Hitting backspace on multi-select converts element to text
    // https://github.com/select2/select2/issues/3354
    // removes the html of the removed element, passed trough the event
    this.$element.on('select2:unselecting', function (event) {
      // remove html
      event.params.args.data.text = '';

      let $el = $(this);
      setTimeout(function () {
        $('.select2-search__field', $el.closest('.form-group')).empty();
        $el.select2('close');
      }, 0);
    });

    this.$element.on('select2:unselect', (event) => {
      this.config.eventEmitter.publish('unselecting', event.params.data.id);
    });
  }

  // Sets the selected values in reverse order (newest first), creating a new option if necessary
  onUpdatedSelection([headerType, itemsLimit]) {
    // gets the newest instances - they are sorted from the end of the array to the top
    let selection = _.takeRight(Array.from(this.config.selectionPool.values()), itemsLimit);

    let options = [];
    selection.forEach((selected) => {
      // Create a DOM Option and pre-select by default
      let header = selected.headers && selected.headers[headerType];
      let option = new Option(header, selected.id, true, true);
      option.title = $(header).text();
      options.push(option);
      this.config.eventEmitter.publish('loaded');
    });

    // remove all old options and values from select2 and append the new ones
    this.$element.html('').append(options).trigger('change');

    // This fix is needed because IE looses focus after inserted or deleted selection.
    if (NavigatorAdapter.isInternetExplorer() && this.config.tags === undefined) {
      this.$element.on('select2:selecting select2:unselecting', () => {
        setTimeout(()=> {
          // Ensure the element is focused.
          this.element.find('.select2-search__field').focus();
        }, 500);
      });
    }

    // This fix is needed because EDGE looses focus after deleted selection.
    if (NavigatorAdapter.isEdge()) {
      this.$element.on('select2:unselecting', () => {
        setTimeout(()=> {
          // Ensure the element is focused.
          this.element.find('.select2-search__field').focus();
        }, 500);
      });
    }
  }

  createActualConfig() {
    let converter = (response) => {
      let values = response && response.data && response.data.values;
      if(values) {
        let items = [...this.config.selectionPool.keys()];
        return values.map((item) => {
          let isDisabled = false;
          if (items.indexOf(item.id) !== -1) {
            isDisabled = true;
          }
          return {
            id: item.id,
            text: item.headers.breadcrumb_header || item.headers.compact_header || item.headers.default_header,
            altTitle: item.properties.altTitle,
            disabled: isDisabled
          };
        });
      }
    };

    let loader = (params) => {
      let searchTerm = params && params.data && params.data.q || '';

      // When Safari calls the super method in the constructor, the relation service is not injected jet.
      // Safari throws error and stops instantiation.
      // So we return a promise to allow the instantiate of the supper class and then to build the
      // actual config. Used Promise instead our PromiseAdaprer for the same reason - at this point
      // PromiseAdapter will not be injected jet.
      if (!this.relationsService && NavigatorAdapter.isSafari()) {
        return Promise.resolve();
      }

      return this.relationsService.suggest(this.config.definitionId, this.config.propertyName, searchTerm, OBJECT_SELECT_PROPERTIES);
    };

    let defaultConfig = {
      multiple: true,
      delay: 250,
      dataLoader: loader,
      dataConverter: converter,
      templateResult: (item) => {
        // prevents "undefined" message when searching
        item.altTitle = item.altTitle || this.translateService.translateInstant('search.searching');
        return `<span>${_.escape((item.altTitle))}</span>`;
      },
      formatSelection: (item) => {
        return `<span>${item.text}</span>`;
      },
      dropdownAutoWidth: true,
      width: 'auto'
    };

    this.config = _.defaults(this.config, defaultConfig);
    super.createActualConfig();
  }

  bindToModel() {
    // not implemented
  }

  autoExpandDropdownMenu() {
    // Overrides the parent method. The parent method refers to a component bug as described in CMF-25819.
    // This behavior does not affect the current implementation. On the contrary, it causes interaction with
    // each selector on the page, resulting in the edit mode, the page remains focused on the last selector.
  }

  ngOnDestroy() {
    this.updatedSelectionHandler.unsubscribe();
    super.ngOnDestroy();
  }
}
