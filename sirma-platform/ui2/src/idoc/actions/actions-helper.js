const ROOT_PLACEHOLDER = '/';

/**
 * Helper class containing methods for filtering and extracting actions and building necessary for that purpose configuration objects.
 */
export class ActionsHelper {

  static getActionsLoaderConfig(currentObject, placeholder) {
    return {
      'context-id': null,
      placeholder: placeholder,
      path: currentObject.getContextPathIds()
    };
  }

  static collectImplementedHandlers(placeholder) {
    return PluginRegistry.get('actions').reduce((total, current) => {
      if (!current.notApplicable || current.notApplicable.indexOf(placeholder) === -1) {
        total[current.name] = current;
      }
      return total;
    }, {});
  }

  /**
   * Getter for the actions filter config.
   *
   * @param leaveDisabled if true removes the disabled actions
   * @param root if true removes all action which aren't in the header section
   * @param pathParam removes the actions that don't contain the param in their actionPath property
   * @param placeholder the placeholder of the action handlers
   * @returns {{disabled: *, root: *, pathParam: *, handlers: *}}
   */
  static getFilterCriteria(leaveDisabled, root, pathParam, placeholder) {
    return {
      disabled: leaveDisabled,
      root: root,
      pathParam: pathParam,
      handlers: ActionsHelper.collectImplementedHandlers(placeholder)
    };
  }

  /**
   * Gets actions for specific path. Extracts the actions that are in groups
   * and adds them to the array with the other actions for the given path.
   * Used when extracting header actions (for example).
   *
   * @param actions from which to extract
   * @param criteria the criteria containing the action path
   * @param pathActions the array with the extracted actions(initially empty)
   */
  static getActionsForGivenPath(actions, criteria, pathActions) {
    actions.map((action) => {
      if (action.data) {
        return ActionsHelper.getActionsForGivenPath(action.data, criteria, pathActions);
      } else {
        if (ActionsHelper.leaveAction(action, criteria)) {
          pathActions.push(action);
        }
      }
    });
    return pathActions;
  }

  /**
   * Checks if the given action should remain and passes all the filters.
   *
   * @param action the action to be filtered
   * @param criteria the filter criteria
   *          - disabled: boolean | if false removes the disabled actions
   *          - pathParam: string | removes the actions that don't contain the param in their actionPath property
   *          - root: boolean | if true returns only the header section properties
   *          - handlers: array | if there isn't implemented action handler removes the action
   */
  static leaveAction(action, criteria) {
    let handlerName = action.serverOperation + 'Action';
    if (criteria.handlers && !criteria.handlers[handlerName]) {
      return false;
    }

    if (!criteria.disabled && action.disabled) {
      return false;
    }

    if (criteria.root && (!action.actionPath || action.actionPath !== ROOT_PLACEHOLDER)) {
      return false;
    }

    if (criteria.pathParam && (!action.actionPath || action.actionPath.indexOf(criteria.pathParam) !== -1)) {
      return false;
    }

    return true;
  }

  /**
   * Filters the actions and the groups by the given filter criteria.
   * If a group remains with no actions it is also removed.
   *
   * @param actions the actions and the groups to be filtered
   * @param criteria the filter criteria
   */
  static filterGroupsAndActions(actions, criteria) {
    return actions.filter((action) => {
      if (action.data) {
        action.data = ActionsHelper.filterGroupsAndActions(action.data, criteria);
        return action.data.length;
      } else {
        return ActionsHelper.leaveAction(action, criteria);
      }
    });
  }

  static filterActions(actions, filterCriteria) {
    let filteredActions = [];
    // if specific criteria is provided for the actions path
    // the actions with the given path should be extracted from their groups
    if (filterCriteria.root || filterCriteria.pathParam) {
      ActionsHelper.getActionsForGivenPath(actions, filterCriteria, filteredActions);
    } else {
      filteredActions = ActionsHelper.filterGroupsAndActions(actions, filterCriteria);
    }
    return filteredActions;
  }

  static extractActions(actions, filterCriteria) {
    actions = ActionsHelper.filterActions(actions, filterCriteria);
    return ActionsHelper.mapActions(actions, filterCriteria);
  }

  static mapActions(actions, filterCriteria) {
    return actions.map((action) => {
      let handlerName;
      if (action.data) {
        action.data.items = ActionsHelper.mapActions(action.data, filterCriteria);
      } else {
        handlerName = action.serverOperation + 'Action';
        if (!filterCriteria.handlers[handlerName]) {
          handlerName = 'dummyAction';
        }
      }

      return {
        // The user operation is the actual operation that user requests. For example if user requests operation
        // 'approve', then the server operation would be 'transition'. This way a group of identical operations
        // might be handled by a single action handler.
        action: action.userOperation,
        // Defined action handler: resolved on the server.
        // If the server operation is 'transition', then action handler would be TransitionAction.
        // Action handlers are registered as plugins for the 'actions' extension point and are resolved by name.
        name: handlerName,
        //Data is object containing items array with the subactions.
        data: action.data,
        label: action.label,
        tooltip: action.tooltip,
        disabled: action.disabled,
        confirmationMessage: action.confirmationMessage,
        extensionPoint: 'actions',
        configuration: action.configuration,
        //Used by the actions menu for selector
        id: action.userOperation,
        cssClass: `seip-action-${action.userOperation}`,
        forceRefresh: action.data ? false : filterCriteria.handlers[handlerName].forceRefresh
      };
    });
  }

}

