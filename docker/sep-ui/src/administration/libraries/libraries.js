import {Component, View, Inject} from 'app/app';
import _ from 'lodash';
import {ModelsService} from 'services/rest/models-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {DCTERMS_TITLE} from 'instance/instance-properties';
import {SearchService} from 'services/rest/search-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NO_SELECTION} from 'search/search-selection-modes';
import {ORDER_ASC} from 'search/order-constants';
import 'components/select/select';
import 'instance/instance-list';
import 'header-container/header-container';

import './libraries.css!css';
import template from './libraries.html!text';

export const CLASS_OBJECT_TYPE = 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#ClassDescription';
export const CLASS_OBJECT_DEFINITION = 'classDefinition';

const PART_OF_ONTOLOGY_RELATIONSHIP = 'emf:partOfOntology';

/**
 * Displays list of all ontology classes and provides filtering capabilities.
 */
@Component({
  selector: 'seip-libraries'
})
@View({
  template
})
@Inject(ModelsService, SearchService, TranslateService)
export class Libraries {

  constructor(modelsService, searchService, translateService) {
    this.modelsService = modelsService;
    this.searchService = searchService;
    this.translateService = translateService;
  }

  ngOnInit() {
    this.libraries = [];
    this.selectedOntologies = [];

    this.instanceListConfig = {
      selection: NO_SELECTION,
      renderMenu: true
    };

    this.modelsService.getOntologies().then(ontologies => {
      this.constructOntologySelectorConfig(ontologies);
    });

    // fetchLibraries is not fired on init because the seip-select component fires onchange event
    // on init
  }

  fetchLibraries() {
    let searchRequest = {};

    searchRequest.arguments = {
      orderBy: DCTERMS_TITLE,
      orderDirection: ORDER_ASC,
      properties: ['id', HEADER_DEFAULT],
      pageSize: 0
    };

    let rules = [];

    rules.push(SearchCriteriaUtils.getDefaultObjectTypeRule([CLASS_OBJECT_TYPE]));

    if (this.selectedOntologies && this.selectedOntologies.length) {
      let ontologyFilterRules = this.selectedOntologies.map(ontology => {
        return SearchCriteriaUtils.buildRule(PART_OF_ONTOLOGY_RELATIONSHIP, 'string', AdvancedSearchCriteriaOperators.CONTAINS.id, ontology);
      });

      rules.push(SearchCriteriaUtils.buildCondition(SearchCriteriaUtils.OR_CONDITION, ontologyFilterRules));
    }

    if (this.titleFilter) {
      rules.push(SearchCriteriaUtils.buildRule(DCTERMS_TITLE, 'string', AdvancedSearchCriteriaOperators.CONTAINS.id, this.titleFilter));
    }

    let searchCriteria = SearchCriteriaUtils.buildCondition('', rules);

    searchRequest.query = {
      tree: searchCriteria
    };

    this.searchService.search(searchRequest).promise.then(response => {
      this.libraries = response.data.values;
      // When sorting by title, the backend returns duplicate records for classes that have title on multiple languages
      this.libraries = _.uniq(this.libraries, 'id');
      this.libraries = this.libraries.filter(function (library) {
        return library.definitionId === CLASS_OBJECT_DEFINITION;
      });
    });
  }

  constructOntologySelectorConfig(ontologies) {
    let options = ontologies.map(ontology => {
      return {
        id: ontology.id,
        text: ontology.title
      };
    });

    this.ontologySelectorConfig = {
      data: options,
      multiple: true,
      placeholder: this.translateService.translateInstant('libraries.filter.ontology.placeholder')
    };
  }

}