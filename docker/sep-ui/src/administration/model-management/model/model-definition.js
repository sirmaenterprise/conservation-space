import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelList} from 'administration/model-management/model/model-list';
import {ModelActionGroup} from 'administration/model-management/model/model-action-group';
import {ModelAction} from 'administration/model-management/model/model-action';
import {ModelHeader} from 'administration/model-management/model/model-header';

/**
 * Represents a model of a definition extending the {@link ModelBase}
 * with a property which is used to determine if the definition is
 * abstract or not.
 *
 * Each definition holds a collection of models which can be of different
 * type. Either of type {@link ModelField} or a {@link ModelRegion} type.
 *
 * @author Svetlozar Iliev
 */
export class ModelDefinition extends ModelBase {

  constructor(id, parent = null, abstract = false, type = null) {
    super(id, parent);
    this.type = type;
    this.abstract = abstract;

    this.fields = new ModelList();
    this.regions = new ModelList();
    this.headers = new ModelList();

    this.actions = new ModelList();
    this.actionGroups = new ModelList();
  }

  getType() {
    return this.type;
  }

  setType(type) {
    this.type = type;
    return this;
  }

  getRegion(id) {
    return this.regions.getModel(id);
  }

  getRegions() {
    return this.regions.getModels();
  }

  getDirtyRegions() {
    return ModelBase.getDirtyModels(this.getOwnRegions());
  }

  getOwnRegions() {
    return this.getOwnModels(this.getRegions());
  }

  getField(id) {
    return this.fields.getModel(id);
  }

  getFields() {
    return this.fields.getModels();
  }

  getDirtyFields() {
    return ModelBase.getDirtyModels(this.getOwnFields());
  }

  getOwnFields() {
    return this.getOwnModels(this.getFields());
  }

  addField(field) {
    if (field instanceof ModelField) {
      this.fields.insert(field);
    }
    return this;
  }

  removeField(field) {
    let id = field.getId();
    if (this.getField(id)) {
      this.fields.remove(id);
    }
    return this;
  }

  addRegion(region) {
    if (region instanceof ModelRegion) {
      this.regions.insert(region);
    }
    return this;
  }

  addActionGroup(actionGroup) {
    if (actionGroup instanceof ModelActionGroup) {
      this.actionGroups.insert(actionGroup);
    }
    return this;
  }

  getActionGroup(id) {
    return this.actionGroups.getModel(id);
  }

  getActionGroups() {
    return this.actionGroups.getModels();
  }

  getDirtyActionGroups() {
    return ModelBase.getDirtyModels(this.getOwnActionGroups());
  }

  getOwnActionGroups() {
    return this.getOwnModels(this.getActionGroups());
  }

  addAction(action) {
    if (action instanceof ModelAction) {
      this.actions.insert(action);
    }
    return this;
  }

  getAction(id) {
    return this.actions.getModel(id);
  }

  getActions() {
    return this.actions.getModels();
  }

  getDirtyActions() {
    return ModelBase.getDirtyModels(this.getOwnActions());
  }

  getOwnActions() {
    return this.getOwnModels(this.getActions());
  }

  addHeader(header) {
    if (header instanceof ModelHeader) {
      this.headers.insert(header);
    }
    return this;
  }

  getHeaders() {
    return this.headers.getModels();
  }

  getDirtyHeaders() {
    return ModelBase.getDirtyModels(this.getOwnHeaders());
  }

  getOwnHeaders() {
    return this.getOwnModels(this.getHeaders());
  }

  getHeader(id) {
    return this.headers.getModel(id);
  }

  copyFrom(src) {
    super.copyFrom(src);

    this.type = src.type;
    this.abstract = src.abstract;

    this.fields.copyFrom(src.fields);
    this.regions.copyFrom(src.regions);
    this.headers.copyFrom(src.headers);

    this.actions.copyFrom(src.actions);
    this.actionGroups.copyFrom(src.actionGroups);
    return this;
  }

  isAbstract() {
    return this.abstract;
  }

  isOwningModels() {
    return super.isOwningModels()   ||
      !!this.getOwnFields().length  ||
      !!this.getOwnHeaders().length ||
      !!this.getOwnRegions().length ||
      !!this.getOwnActions().length ||
      !!this.getOwnActionGroups().length;
  }

  isValid() {
    return super.isValid()
      && ModelBase.areModelsValid(this.getDirtyFields())
      && ModelBase.areModelsValid(this.getDirtyHeaders())
      && ModelBase.areModelsValid(this.getDirtyRegions())
      && ModelBase.areModelsValid(this.getDirtyActions())
      && ModelBase.areModelsValid(this.getDirtyActionGroups());
  }

  isDirty() {
    return super.isDirty()
      || ModelBase.areModelsDirty(this.getOwnFields())
      || ModelBase.areModelsDirty(this.getOwnHeaders())
      || ModelBase.areModelsDirty(this.getOwnRegions())
      || ModelBase.areModelsDirty(this.getOwnActions())
      || ModelBase.areModelsDirty(this.getOwnActionGroups());
  }
}