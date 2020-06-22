import {Injectable} from 'app/app';

/**
 * Service used to track states and changes across the different administration tools.
 *
 * @author Mihail Radkov
 */
@Injectable()
export class AdminToolRegistry {

  constructor() {
    this.clearStates();
  }

  /**
   * Clear all registered states.
   */
  clearStates() {
    this.stateRegistry = {};
  }

  /**
   * Register the specified tool with the given state. Passing <code>true</code> for the state means the tool has
   * unsaved changes while <code>false</code> means the opposite.
   *
   * @param {string} tool the administration tool for which the state will be registered
   * @param {boolean} state the state to be registered
   */
  setState(tool, state) {
    this.stateRegistry[tool] = state;
  }

  /**
   * Extract the current state for a given tool. Result of this call might be either <code>true</code> for the state meaning the tool has
   * unsaved changes or <code>false</code> meaning the tool has no unsaved changes or modifications.
   *
   * @param {string} tool the administration tool for which the state will be registered
   * @returns {boolean} true | false
   */
  getState(tool) {
    return this.stateRegistry[tool] || false;
  }

  /**
   * Determines if at least one administration tool is in state where it has unsaved changes.
   *
   * @returns {boolean} <code>true</code> if a tool has unsaved changes or
   *                    <code>false</code> if none of the tools have unsaved changes
   */
  hasUnsavedState() {
    return Object.keys(this.stateRegistry).some(key => this.stateRegistry[key]);
  }
}