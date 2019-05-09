import {View, Component, Inject} from 'app/app';
import {EventEmitter} from 'common/event-emitter';
import {ModelsService} from 'services/rest/models-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {NamespaceService} from 'services/rest/namespace-service';

import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelSection} from 'administration/model-management/sections/model-section';

import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import _ from 'lodash';

import 'administration/model-management/components/attributes/model-attribute-view';
import 'administration/model-management/components/attributes/type/model-property-type-attribute';

import 'administration/model-management/components/controls/model-controls';
import 'administration/model-management/components/controls/save/model-save';
import 'administration/model-management/components/controls/cancel/model-cancel';

import 'components/collapsible/collapsible-panel';
import 'components/button/button';
import 'filters/to-trusted-html';

import './model-create-field.css!css';
import template from './model-create-field.html!text';

const DELIMITERS = ['#', '/'];

/**
 * Represents a simple edit form for created models of type {@link ModelField}.
 * The form is extending off of general type {@link ModelSection} and supports
 * model changes, validation and saving. Communication with the field form is
 * executed through component events. The component properties represent:
 *
 * - model - the created field to be edited / modified
 * - config - simple component scoped configuration object
 * - context - the context in which these actions are undertaken
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-create-field',
  properties: {
    'model': 'model',
    'config': 'config',
    'context': 'context',
    //TODO: how to provide as proper events through dialog service
    'onModelLoad': 'on-model-load',
    'onModelSave': 'on-model-save',
    'onModelCancel': 'on-model-cancel',
    'onModelActionCreateRequest': 'on-model-action-create-request',
    'onModelActionRevertRequest': 'on-model-action-revert-request',
    'onModelActionExecuteRequest': 'on-model-action-execute-request'
  }
})
@View({
  template
})
@Inject(ModelsService, PromiseAdapter, NamespaceService)
export class ModelCreateField extends ModelSection {

  constructor(modelsService, promiseAdapter, namespaceService) {
    super();
    this.modelsService = modelsService;
    this.promiseAdapter = promiseAdapter;
    this.namespaceService = namespaceService;
  }

  ngOnInit() {
    let type = this.context.getType();
    let promises = [this.modelsService.getOntologies(), this.notifyForModelLoaded(type, false)];

    this.promiseAdapter.all(promises).then(([ontologyModels]) => {
      let properties = this.getProperties(this.context);
      let ontologies = this.getOntologies(ontologyModels);
      properties = this.filterProperties(this.context, properties);

      let options = this.getOptions(properties, ontologies);
      this.createAttributesConfig = {emitter: new EventEmitter()};
      this.propertySelect = {data: options, convertToString: true};
    });
  }

  onPropertySelected() {
    this.setShortUriResolved(false);
    this.setPropertyRestrictions(false);

    let uriAttribute = this.getModelUriAttribute();
    let nameAttribute = this.getModelNameAttribute();
    let uri = this.getModelAttributeValue(uriAttribute);

    if (uri && uri.length) {
      this.clearChanges();

      this.getShortUri(uri).then(r => {
        this.model.setId(r.data[uri]);

        uriAttribute.getValue().setValue(uri);
        nameAttribute.getValue().setValue(r.data[uri]);

        this.model.setProperty(this.getProperty(this.context, uri));
        this.notifyForModelAttributeChange(uriAttribute, this.model);
        this.notifyForModelAttributeChange(nameAttribute, this.model);
        this.notifyForModelStateCalculation(this.model, this.context);

        this.setShortUriResolved(true);
        this.setPropertyRestrictions(this.isPropertyTypeEmpty());
      });
    }
  }

  filterProperties(target, properties) {
    return properties.filter(p => !this.findFieldWithProperty(target, p.getId()));
  }

  findFieldWithProperty(target, id) {
    let fields = target.getFields();
    let uri = (m) => m.getAttribute(ModelAttribute.URI_ATTRIBUTE);
    return _.find(fields, f => id === this.getModelAttributeValue(uri(f)));
  }

  clearChanges() {
    this.revertActions(this.model);
    this.clearActions(this.model);
  }

  //@Override
  getSectionModels() {
    return this.model;
  }

  //@Override
  isModelValid(model) {
    // check if all attributes for the model are valid
    return model.getAttributes().every(a => a.isValid());
  }

  //@Override
  onSaveResolve() {
    // do not reset dirty state of changed attributes on save yet
  }

  //@Override
  notifyForModelStateCalculation(model, context) {
    super.notifyForModelStateCalculation(model, context);
    super.notifyForModelStateCalculation(model.getProperty(), context);
  }

  getOntologies(ontologies) {
    return _.transform(ontologies, (result, value) => result[this.getNormalizedUri(value.id)] = value.title);
  }

  getProperties(target) {
    return target.getType().getProperties();
  }

  getProperty(target, id) {
    return target.getType().getProperty(id);
  }

  getOptions(properties, ontologies) {
    return properties.sort(this.labelSorter.bind(this)).map(property => {
      let source = this.getOntologyUri(property.getId());
      let ontology = ontologies[source] || source;

      property = this.getPropertyItem(property);
      property.text += ` (${ontology})`;
      return property;
    });
  }

  getPropertyItem(property) {
    return {
      id: property.getId(),
      text: property.getDescription().getValue()
    };
  }

  getOntologyUri(uri) {
    let idx = -1;
    DELIMITERS.some(char => {
      idx = uri.lastIndexOf(char);
      return idx !== -1;
    });
    return uri.substring(0, idx);
  }

  getNormalizedUri(uri) {
    return DELIMITERS.some(char => uri[uri.length - 1] === char) ? uri.substring(0, uri.length - 1) : uri;
  }

  getShortUri(uri) {
    return this.namespaceService.convertToShortURI([uri]);
  }

  getModelTitle() {
    return ModelManagementUtility.getModelName(this.model);
  }

  getPropertyTitle() {
    return ModelManagementUtility.getModelName(this.model.getProperty());
  }

  getModelUriAttribute() {
    return this.model.getAttribute(ModelAttribute.URI_ATTRIBUTE);
  }

  getModelNameAttribute() {
    return this.model.getAttribute(ModelAttribute.NAME_ATTRIBUTE);
  }

  getModelAttributeValue(attribute) {
    return attribute.getValue().getValue();
  }

  getPropertyType() {
    return this.model.getProperty().getAttribute(ModelAttribute.PROPERTY_TYPE);
  }

  setShortUriResolved(resolved) {
    this.resolvedShortUri = resolved;
  }

  setPropertyRestrictions(showing) {
    this.showPropertyType = showing;
  }

  hasSelectedProperty() {
    return !!this.model.getProperty();
  }

  isLoadingUri() {
    return this.hasSelectedProperty() && !this.isShortUriResolved();
  }

  isPropertyTypeVisible() {
    return this.showPropertyType;
  }

  isShortUriResolved() {
    return this.resolvedShortUri;
  }

  isPropertyTypeEmpty() {
    return ModelManagementUtility.isAttributeEmpty(this.getPropertyType());
  }

  labelSorter(left, right) {
    let lhsLabel = left.getDescription();
    let rhsLabel = right.getDescription();
    return lhsLabel.getValue().localeCompare(rhsLabel.getValue());
  }
}