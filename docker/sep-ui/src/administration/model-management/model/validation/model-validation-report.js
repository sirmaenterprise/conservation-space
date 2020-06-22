/**
 * Data class, representing the validation report, which is usually displayed in {@link ModelListValidationView}. It
 * contains a mapping of modelId and an array of error messages. Each message has a 'severity' property and a 'message'
 * property. The severity can have one of these values: "ERROR" or "WARNING".
 *
 * @author Radoslav Dimitrov
 */
export class ModelValidationReport {

  constructor(validationResponse = {nodes: []}) {
    this.mapping = this.validationResponseToMap(validationResponse);
  }

  getErrorsForModel(modelId) {
    return this.mapping[modelId];
  }

  hasErrors(modelId) {
    let errors = this.getErrorsForModel(modelId);
    return errors && errors.length > 0;
  }

  hasCriticalErrors(modelId) {
    let errors = this.getErrorsForModel(modelId);
    return errors && errors.filter(error => ModelValidationReport.isErrorMessage(error)).length > 0;
  }

  static isErrorMessage(message) {
    return message && message.severity && message.severity === ModelValidationReport.ERROR_MESSAGE_SEVERITY;
  }

  static isWarningMessage(message) {
    return message && message.severity && message.severity === ModelValidationReport.WARNING_MESSAGE_SEVERITY;
  }

  validationResponseToMap(response) {
    return response.nodes && response.nodes
      .filter(node => node.messages && node.messages.length > 0)
      .reduce((map, node) => {
        map[node.id] = node.messages;
        return map;
      }, {});
  }
}

ModelValidationReport.ERROR_MESSAGE_SEVERITY = 'ERROR';
ModelValidationReport.WARNING_MESSAGE_SEVERITY = 'WARNING';