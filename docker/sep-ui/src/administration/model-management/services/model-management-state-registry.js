import {Injectable} from 'app/app';

/**
 * Service used to track states of sections in the model management page.
 *
 * @author Mihail Radkov
 */
@Injectable()
export class ModelManagementStateRegistry {

  constructor() {
    this.clearSectionStates();
  }

  /**
   * Clear all registered section states.
   */
  clearSectionStates() {
    this.sectionStates = {};
  }

  /**
   * Register the specified section with the given state. Passing <code>true</code> for isDirty state means it has
   * unsaved changes while <code>false</code> means the opposite.
   *
   * @param {string} section the section for which the state will be registered
   * @param {boolean} isDirty the state to be registered
   */
  setSectionState(section, isDirty) {
    this.sectionStates[section] = isDirty;
  }

  /**
   * Determines if at least one section is in state where it has unsaved changes.
   *
   * @returns {boolean} <code>true</code> if a section has unsaved changes or
   *                    <code>false</code> if none of the sections have unsaved changes
   */
  hasDirtyState() {
    return Object.values(this.sectionStates).some(sectionState => !!sectionState);
  }
}