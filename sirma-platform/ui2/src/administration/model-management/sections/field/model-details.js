import {View, Component, Inject} from 'app/app';
import {EventEmitter} from 'common/event-emitter';

import {ModelField} from 'administration/model-management/model/model-field';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import {ConfirmationDialogService} from 'components/dialog/confirmation-dialog-service';

import 'administration/model-management/components/attributes/model-attribute-view';
import 'administration/model-management/sections/field/model-field-controls';
import 'components/collapsible/collapsible-panel';
import 'filters/to-trusted-html';

import './model-details.css!css';
import template from './model-details.html!text';

/**
 * Component for visualizing the general and behavioural attributes of a provided ModelField or ModelRegion.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'model-details',
  properties: {
    'model': 'model',
    'context': 'context'
  },
  events: ['onAttributeChange', 'onModelNavigate', 'onModelRestoreInherited']
})
@View({
  template
})
@Inject(ConfirmationDialogService)
export class ModelDetails {

  constructor(confirmationDialogService) {
    this.confirmationDialogService = confirmationDialogService;
  }

  ngOnInit() {
    // localized emitters for both sets of attributes displayed
    this.fieldAttributesConfig = {emitter: new EventEmitter()};
    this.propertyAttributesConfig = {emitter: new EventEmitter()};
  }

  isField() {
    return this.model instanceof ModelField;
  }

  isProperty() {
    return this.model instanceof ModelProperty;
  }

  isInherited() {
    return ModelManagementUtility.isInherited(this.model, this.context);
  }

  isEditable() {
    if (this.isProperty() && this.isInherited()) {
      return false;
    }
  }

  isRestoreInheritedForFieldEnabled() {
    return !this.isProperty() && !this.isInherited() && !!this.model.getReference()
      && this.model.isOwningModels() && this.context.getParent() !== null;
  }

  isRestoreInheritedForAttributeEnabled(attribute) {
    return !this.isProperty() && this.isRestoreInheritedForFieldEnabled()
      && attribute.getRestrictions().isUpdateable() && this.context.getParent() !== null
      && !ModelManagementUtility.isInherited(attribute, this.model);
  }

  getModelTitle(model) {
    return model && model.getDescription().getValue();
  }

  onFieldAttributeChanged(attribute) {
    return this.onAttributeChange && this.onAttributeChange({attribute});
  }

  onPropertyAttributeChanged(attribute) {
    return this.onAttributeChange && this.onAttributeChange({attribute});
  }

  onModelAttributeRestoreInherited(attribute) {
    this.createRestoreInheritedDialog('administration.models.management.restore.inherited.attribute.confirm').then(() => {
      this.onModelRestoreInherited && this.onModelRestoreInherited({model: this.model, toRestore: attribute});
    });
  }

  onModelFieldRestoreInherited(event) {
    // Prevent expand/collapse
    event.stopPropagation();

    this.createRestoreInheritedDialog('administration.models.management.restore.inherited.model.confirm').then(() => {
      this.onModelRestoreInherited && this.onModelRestoreInherited({model: this.model, toRestore: this.model});
    });
  }

  onModelFieldNavigated(event) {
    // Prevent expand/collapse
    event.stopPropagation();

    this.onModelNavigate && this.onModelNavigate({model: this.model});
  }

  onModelPropertyNavigated(event) {
    // Prevent expand/collapse
    event.stopPropagation();

    this.onModelNavigate && this.onModelNavigate({model: this.model.getProperty()});
  }

  createRestoreInheritedDialog(message) {
    return this.confirmationDialogService.confirm({message});
  }
}