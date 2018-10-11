/**
 * Contains constants about the available elements in the advanced search form.
 */
export class AdvancedSearchComponents {

  /**
   * Returns all available elements.
   */
  static getAllComponents() {
    return [
      AdvancedSearchComponents.TYPE,
      AdvancedSearchComponents.CONDITION,
      AdvancedSearchComponents.PROPERTY,
      AdvancedSearchComponents.OPERATOR,
      AdvancedSearchComponents.ADD_RULE,
      AdvancedSearchComponents.ADD_GROUP,
      AdvancedSearchComponents.REMOVE_RULE,
      AdvancedSearchComponents.REMOVE_GROUP
    ];
  }

}

AdvancedSearchComponents.TYPE = 'type';
AdvancedSearchComponents.CONDITION = 'condition';
AdvancedSearchComponents.PROPERTY = 'property';
AdvancedSearchComponents.OPERATOR = 'operator';
AdvancedSearchComponents.ADD_RULE = 'addRule';
AdvancedSearchComponents.ADD_GROUP = 'addGroup';
AdvancedSearchComponents.REMOVE_RULE = 'removeRule';
AdvancedSearchComponents.REMOVE_GROUP = 'removeGroup';