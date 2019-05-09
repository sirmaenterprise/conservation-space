import {View, Component, Inject} from 'app/app';
import {EventEmitter} from 'common/event-emitter';
import {NamespaceService} from 'services/rest/namespace-service';

import {ModelSection} from 'administration/model-management/sections/model-section';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import {ModelManagementModelsService} from 'administration/model-management/services/utility/model-management-models-service';
import {ModelPropertyMetaData as DATA_TYPES} from 'administration/model-management/meta/model-property-meta';

import 'administration/model-management/components/attributes/model-attribute-view';

import 'administration/model-management/components/controls/model-controls';
import 'administration/model-management/components/controls/save/model-save';
import 'administration/model-management/components/controls/cancel/model-cancel';

import 'components/collapsible/collapsible-panel';
import 'components/button/button';
import 'filters/to-trusted-html';

import './model-create-property.css!css';
import template from './model-create-property.html!text';

const DELIMITERS = ['#', '/'];
const LONG_NAME = 'LONG_NAME';
const SHORT_NAME = 'SHORT_NAME';

/**
 * Represents a simple edit form for created models of type {@link ModelProperty}.
 * The form is extending off of general type {@link ModelSection} and supports
 * model changes, validation and saving. Communication with the property form is
 * executed through component events. The component properties represent:
 *
 * - model - the created property to be edited / modified
 * - fields - related to the property fields to be edited
 * - config - simple component scoped configuration object
 * - context - the context in which these actions are undertaken
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-create-property',
  properties: {
    'model': 'model',
    'fields': 'fields',
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
@Inject(NamespaceService, ModelManagementModelsService)
export class ModelCreateProperty extends ModelSection {

  constructor(namespaceService, modelManagementModelsService) {
    super();
    this.namespaceService = namespaceService;
    this.modelManagementModelsService = modelManagementModelsService;
  }

  ngOnInit() {
    let uri = this.getClassFromContext().getId();
    this.getShortUri(this.getUriFromString(uri)).then(res => {
      this.initAttributesConfig();
      this.initModelsIdentifiers();
      this.initNamespace(res.data);

      this.linkPropertyToDomain();
      this.linkFieldsToProperty();

      this.notifyForModelStateCalculation(this.getField(), this.context);
      this.notifyForModelStateCalculation(this.getProperty(), this.context);
    });
  }

  initAttributesConfig() {
    this.createAttributesConfig = {emitter: new EventEmitter()};
  }

  initNamespace(data) {
    this.namespace = {LONG_NAME: Object.keys(data)[0], SHORT_NAME: Object.values(data)[0]};
  }

  initModelsIdentifiers() {
    // initially assign dummy identifiers to fields and property
    this.getProperty().setId(this.getModelType(this.getProperty()));
    this.getFields().forEach((f, i) => f.setId(this.getModelType(f) + i));
  }

  linkFieldsToProperty() {
    // link field models with the same instance of semantic property
    this.getFields().forEach(f => f.setProperty(this.getProperty()));
  }

  linkPropertyToDomain() {
    // link & compute the domain attribute of the semantic property
    let domain = this.computeAndSetPropertyDomainAttribute();
    super.notifyForModelAttributeChange(domain, this.getProperty());
  }

  //@Override
  isSaveAllowed() {
    return !this.isPropertyDuplicate() && super.isSaveAllowed();
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
  onSavePerform(actions) {
    let propertyId = this.getPropertyUriAttribute(this.getProperty()).getValue().getValue();
    let fieldId = this.getField() && this.getFieldNameAttribute(this.getField()).getValue().getValue();

    this.getProperty().setId(propertyId);
    this.getFields().forEach(f => f.setId(fieldId));

    return super.onSavePerform(actions);
  }

  //@Override
  notifyForModelAttributeChange(attribute, context) {
    if (context === this.getField()) {
      // sync rest of model fields bound form
      this.getFieldsRest().forEach(field => {
        let affected = field.getAttribute(attribute.getId());
        affected.getValue().setValue(attribute.getValue().getValue());
        super.notifyForModelAttributeChange(affected, field);
      });
    } else if (attribute.getId() === ModelAttribute.PROPERTY_TITLE) {
      // set the uri based on the entered semantic title
      // notify for change in the uri attribute for property
      let uri = this.computeAndSetPropertyUriAttribute();
      uri && super.notifyForModelAttributeChange(uri, context);

      // set the audit based on the entered semantic title
      // notify for change in the audit attribute for property
      let audit = this.computeAndSetPropertyAuditAttribute();
      audit && super.notifyForModelAttributeChange(audit, context);

      // compute uris of all fields model bound to the form
      // notify for change in the uri attribute for field
      let uris = this.computeAndSetFieldsUriAttribute();
      uris.forEach((u, i) => super.notifyForModelAttributeChange(u, this.getFields()[i]));

      // compute identifiers of all fields model bound to the form
      // notify for change in the identifiers attribute for field
      let names = this.computeAndSetFieldsNameAttribute();
      names.forEach((u, i) => super.notifyForModelAttributeChange(u, this.getFields()[i]));

      // compute the property and make sure that it's not already present
      // based on the uri of the property computed based on the title
      this.isDuplicateProperty = this.computeAndGetPropertyUniqueness();
    }
    return super.notifyForModelAttributeChange(attribute, context);
  }

  //@Override
  getSectionModels() {
    return [this.getProperty(), ...this.getFields()];
  }

  getShortUri(uri) {
    return this.namespaceService.convertToShortURI([uri]);
  }

  getClassFromContext() {
    return this.isClass(this.context) ? this.context : this.context.getType();
  }

  getDestinationModels() {
    return (this.isClass(this.context) ?
      this.context.getSuperTypes() : [this.context]).map(model => this.getModelNameOrEmpty(model));
  }

  getField() {
    return Array.isArray(this.fields) ? this.fields[0] : this.fields;
  }

  getFields() {
    return Array.isArray(this.fields) ? this.fields : [this.fields];
  }

  getFieldsRest() {
    let fields = this.getFields();
    return fields && fields.length > 1 ? fields.slice(1) : [];
  }

  getFieldTitle() {
    return this.getModelNameOrEmpty(this.getField());
  }

  getFieldAttributes() {
    return this.getField().getAttributes();
  }

  getFieldUriAttribute(field) {
    return field.getAttribute(ModelAttribute.URI_ATTRIBUTE);
  }

  getFieldNameAttribute(field) {
    return field.getAttribute(ModelAttribute.NAME_ATTRIBUTE);
  }

  getProperty() {
    return this.model;
  }

  getPropertyTitle() {
    return this.getModelNameOrEmpty(this.getProperty());
  }

  getPropertyIdentifier() {
    let title = this.getPropertyTitle();
    return title ? this.computeUniquePropertyName(title) : '';
  }

  getPropertyAttributes() {
    return this.getProperty().getAttributes();
  }

  getPropertyTypeAttribute(property) {
    return property.getAttribute(ModelAttribute.PROPERTY_TYPE);
  }

  getPropertyUriAttribute(property) {
    return property.getAttribute(ModelAttribute.PROPERTY_URI);
  }

  getPropertyAuditAttribute(property) {
    return property.getAttribute(ModelAttribute.AUDIT_ATTRIBUTE);
  }

  getPropertyDomainAttribute(property) {
    return property.getAttribute(ModelAttribute.DOMAIN_ATTRIBUTE);
  }

  getLongNamespace() {
    return this.namespace[LONG_NAME];
  }

  getShortNamespace() {
    return this.namespace[SHORT_NAME];
  }

  getUriFromString(uri) {
    let idx = -1;
    // find the namespace name
    DELIMITERS.some(char => {
      idx = uri.lastIndexOf(char);
      return idx !== -1;
    });
    // with the namespace delimiter
    return uri.substring(0, idx + 1);
  }

  getModelNameOrEmpty(model) {
    return ModelManagementUtility.getModelName(model) || '';
  }

  getModelType(model) {
    return ModelManagementUtility.getModelType(model);
  }

  computeUniquePropertyName(name) {
    name = name.replace(/[^\w\s]/gi, '');
    return name.toLowerCase().split(' ')
      .map((s, i) => i === 0 ? s : this.capitalize(s)).join('');
  }

  computeAndSetPropertyUriAttribute() {
    let value = this.getLongNamespace() + this.getPropertyIdentifier();
    let uri = this.getPropertyUriAttribute(this.getProperty());
    uri.getValue().setValue(value);
    return uri;
  }

  computeAndSetPropertyDomainAttribute() {
    let targetDomain = this.getClassFromContext().getId();
    let domain = this.getPropertyDomainAttribute(this.getProperty());
    domain.getValue().setValue(targetDomain);
    return domain;
  }

  computeAndSetPropertyAuditAttribute() {
    let type = this.getPropertyTypeAttribute(this.getProperty());
    if (type.getValue().getValue() === DATA_TYPES.DATA_PROPERTY) {
      return null;
    }
    let id = this.capitalize(this.getPropertyIdentifier());
    let audit = this.getPropertyAuditAttribute(this.getProperty());
    let actions = ModelCreateProperty.AUDIT_ACTIONS.map(a => a + id);
    audit.getValue().setValue(actions.join(ModelCreateProperty.AUDIT_DELIMITER));
    return audit;
  }

  computeAndSetFieldsNameAttribute() {
    let value = this.getShortNamespace() + this.getPropertyIdentifier();
    return this.getFields().map(f => {
      let name = this.getFieldNameAttribute(f);
      name.getValue().setValue(value);
      return name;
    });
  }

  computeAndSetFieldsUriAttribute() {
    let value = this.getLongNamespace() + this.getPropertyIdentifier();
    return this.getFields().map(f => {
      let uri = this.getFieldUriAttribute(f);
      uri.getValue().setValue(value);
      return uri;
    });
  }

  computeAndGetPropertyUniqueness() {
    let uri = this.getPropertyUriAttribute(this.getProperty()).getValue();
    return this.modelManagementModelsService.hasModel(uri.getValue(), ModelProperty);
  }

  capitalize(string) {
    return string && string.length >= 1 ? string[0].toUpperCase() + string.substring(1) : string;
  }

  isPropertySectionVisible() {
    return !!this.getProperty() && this.createAttributesConfig;
  }

  isFieldSectionVisible() {
    return !!this.getDestinationModels().length && this.createAttributesConfig;
  }

  isPropertyDuplicate() {
    return this.isDuplicateProperty;
  }

  isEditable(attribute) {
    return ModelCreateProperty.DISABLED_ATTRIBUTES.indexOf(attribute.getId()) === -1;
  }

  isClass(model) {
    return ModelManagementUtility.isModelClass(model);
  }
}

ModelCreateProperty.AUDIT_DELIMITER = '|';
ModelCreateProperty.AUDIT_ACTIONS = ['+add', '-remove', 'change'];

ModelCreateProperty.DISABLED_ATTRIBUTES = [
  ModelAttribute.PROPERTY_URI,
  ModelAttribute.URI_ATTRIBUTE,
  ModelAttribute.NAME_ATTRIBUTE,
  ModelAttribute.DOMAIN_ATTRIBUTE,
  ModelAttribute.INVERSE_ATTRIBUTE,
  ModelAttribute.SUBPROPERTY_ATTRIBUTE
];