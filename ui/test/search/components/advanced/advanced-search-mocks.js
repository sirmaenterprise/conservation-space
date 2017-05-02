import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';

export class AdvancedSearchMocks {
  static getCriteria() {
    return {
      id: '1',
      condition: SearchCriteriaUtils.OR_CONDITION,
      rules: [
        {
          id: '1-1',
          field: 'title',
          operator: 'in',
          value: ['1', '2']
        },
        {
          id: '1-2',
          condition: SearchCriteriaUtils.AND_CONDITION,
          rules: [
            {
              id: '1-2-1',
              field: 'status',
              operator: 'in',
              value: ['APPROVED']
            }
          ]
        }
      ]
    }
  }

  static getSectionCriteria() {
    return {
      id: '-1',
      condition: SearchCriteriaUtils.AND_CONDITION,
      rules: [{
        id: '0',
        field: 'type',
        operator: 'equals',
        value: 'emf:Document'
      },
        this.getCriteria()
      ]
    }
  }

  static getAdvancedSearchCriteria() {
    return {
      condition: SearchCriteriaUtils.AND_CONDITION,
      rules: [{
        condition: SearchCriteriaUtils.AND_CONDITION,
        rules: [{
          id: '1', field: '2', operator: '3', value: '4'
        }]
      }]
    }
  }

}
