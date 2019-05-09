import {ModelList} from 'administration/model-management/model/model-list';

/**
 * Data transfer object carrying the models determined for deploy and the models version for which they were calculated.
 *
 * It also can act as holder for models selected for deployment.
 *
 * @author Mihail Radkov
 */
export class ModelDeployRequest {

  setModels(models) {
    this.models = models;
    return this;
  }

  getModels() {
    return this.models;
  }

  setVersion(version) {
    this.version = version;
    return this;
  }

  getVersion() {
    return this.version;
  }

  setSelectedModels(selected) {
    this.selected = this.filterSelectedModels(selected);
    return this;
  }

  getSelectedModels() {
    return this.selected;
  }

  setValidationReport(validationReport) {
    this.validationReport = validationReport;
    return this;
  }

  getValidationReport() {
    return this.validationReport;
  }

  filterSelectedModels(selected) {
    let report = this.getValidationReport();

    if (!report) {
      return selected;
    }
    let list = new ModelList();
    selected.getModels()
      .filter(model => !report.hasCriticalErrors(model.getId()))
      .forEach(model => list.insert(model));
    return list;
  }
}