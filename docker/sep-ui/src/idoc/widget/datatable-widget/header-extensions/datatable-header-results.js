import {View, Component, Inject, NgScope} from 'app/app';
import {SELECT_OBJECT_MANUALLY, SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {DialogService} from 'components/dialog/dialog-service';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import './datatable-header-results.css!';
import template from './datatable-header-results.html!text';

@Component({
  selector: 'seip-datatable-header-results',
  properties: {
    context: 'context',
    control: 'control',
    config: 'config'
  }
})
@View({
  template
})
@Inject(ObjectSelectorHelper, DialogService, NgScope)
export class DatatableHeaderResults {
  constructor(objectSelectorHelper, dialogService, $scope) {
    this.objectSelectorHelper = objectSelectorHelper;
    this.dialogService = dialogService;
    this.$scope = $scope;

    if(this.config.expanded) {
      this.dataloadedEvent = this.control.subscribe('dataLoaded', (results) => {
        if(results.size > 0){
          this.totalResults = results.size;
        } else {
          this.totalResults = '';
        }
      });
    } else {
      this.assignConfigChangeWatchers();
    }
  }

  ngOnInit() {
    this.orderChangedEvent = this.control.subscribe('orderChanged', (payload) => {
      this.orderBy = payload.orderBy;
      this.orderDirection = payload.orderDirection;
      this.orderByCodelistNumbers = payload.orderByCodelistNumbers;
    });
  }

  showDetails() {
    let control = this.control;
    let widgetConfig = {
      modalCls: control.getConfigSelector(),
      config: this.createConfig(),
      definition: control.getDefinition(),
      context: this.context
    };
    let dialogConfig = {
      header: this.config.title,
      buttons: [
        {
          id: DialogService.CLOSE,
          label: 'dialog.button.close'
        }],
      onButtonClick(buttonId, componentScope, dialogConfig) {
        dialogConfig.dismiss();
      }
    };

    //This CSS class is located in the search-results.scss file
    dialogConfig.customStyles = 'fullscreen';
    this.dialogService.create(control.getConfigSelector(), widgetConfig, dialogConfig);
  }

  createConfig() {
    let filterCriteria = this.control.getDataFromAttribute('data-filter-criteria');
    let fullscreenConfig = ObjectSelectorHelper.getFilteringConfiguration(this.config, filterCriteria);

    //Passes properties to disable selected components in the dialog.
    fullscreenConfig.renderOptions = false;
    fullscreenConfig.renderCriteria = false;
    fullscreenConfig.hideWidgerToolbar = false;
    fullscreenConfig.instanceHeaderType = HEADER_DEFAULT;
    if (this.orderBy) {
      fullscreenConfig.orderBy = this.orderBy;
      fullscreenConfig.orderDirection = this.orderDirection;
      fullscreenConfig.orderByCodelistNumbers = this.orderByCodelistNumbers;
    }

    return fullscreenConfig;
  }

  getTotalResults() {
    if(this.config.selectObjectMode === SELECT_OBJECT_MANUALLY){
      if (this.config.selectedObjects.length > 0){
        this.totalResults = this.config.selectedObjects.length;
      } else {
        this.totalResults = '';
      }
    } else {
      let searchArguments = {
        pageNumber: 1,
        countOnly: true
      };
      this.objectSelectorHelper.getAutomaticSelection(this.config, this.context, searchArguments).then((result) => {
        this.totalResults = result.total;
      });
    }
  }

  assignConfigChangeWatchers() {
    this.$scope.$watchCollection(() => {
      let watchConditions = [this.config.selectObjectMode, this.config.selectedObjects];
      if (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
        watchConditions.push(this.config.criteria);
      }
      return watchConditions;
    }, () => {
      this.getTotalResults();
    });
  }

  ngOnDestroy() {
    this.dataloadedEvent && this.dataloadedEvent.unsubscribe();
    this.orderChangedEvent && this.orderChangedEvent.unsubscribe();
  }
}
