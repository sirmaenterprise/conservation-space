import {View, Inject} from 'app/app';
import {WidgetConfig} from 'idoc/widget/widget';
import {Configurable} from 'components/configurable';
import {SELECT_OBJECT_CURRENT} from 'idoc/widget/object-selector/object-selector';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {TranslateService} from 'services/i18n/translate-service';
import 'components/select/select';
import 'components/tabs/tabs';
import template from './config.html!text';
import './config.css!';

@WidgetConfig
@View({template})
@Inject(TranslateService)
export class RecentActivitiesConfig extends Configurable {

  constructor(translateService) {
    super({
      selection: MULTIPLE_SELECTION,
      selectObjectMode: SELECT_OBJECT_CURRENT,
      showIncludeCurrent: true,
      pageSize: 10,
      onObjectSelectorChanged: (config) => {
        this.config.criteria = config.searchCriteria;
      }
    });

    // Trigger search if there is previously configured non empty criteria
    this.config.triggerSearch = !!this.config.criteria && !!this.config.criteria.rules;

    this.createTabsConfiguration();
    this.createPageSizeSelectConfig(translateService);
  }

  createTabsConfiguration() {
    this.tabsConfig = {
      tabs: [{
        id: 'select-object',
        classes: 'select-object-tab',
        label: 'widget.config.tab.select.objects'
      }, {
        id: 'display-options',
        classes: 'display-options-tab',
        label: 'widget.config.tab.display.options'
      }],
      activeTab: 'select-object'
    };
  }

  createPageSizeSelectConfig(translateService) {
    translateService.translate('common.results.all').then((allLabel) => {
      this.pageSizeSelectConfig = {
        data: [
          {id: 'all', text: allLabel},
          {id: 5, text: '5'},
          {id: 10, text: '10'},
          {id: 20, text: '20'},
          {id: 50, text: '50'},
          {id: 100, text: '100'}
        ]
      };
    });
  }
}
