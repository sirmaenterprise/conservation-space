/**
 * Represents a rule outcome.
 *
 * Each validation rule {@link ModelRule} can have outcome. Outcome describes how attribute values and behaviour
 * should be changed if the rule is fulfilled. If outcome is not supplied attribute remain unchanged after rule
 * evaluation.
 *
 * @author Stela Djulgerova
 */
export class ModelRuleOutcome {

  isUpdateable() {
    return this.updateable;
  }

  setUpdateable(updateable) {
    this.updateable = updateable;
    return this;
  }

  isMandatory() {
    return this.mandatory;
  }

  setMandatory(mandatory) {
    this.mandatory = mandatory;
    return this;
  }

  isVisible() {
    return this.visible;
  }

  setVisible(visible) {
    this.visible = visible;
    return this;
  }
}