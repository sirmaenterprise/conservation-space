import {Inject, Injectable} from 'app/app';
import {DefinitionService} from 'services/rest/definition-service';
import {decorate} from 'common/object-utils';
import {InstanceObject} from 'models/instance-object';
import {TEMPLATE_DEFINITION_TYPE, FOR_OBJECT_TYPE} from './template-constants';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

/**
 * The goal of the "modeling idoc context" is to satisfy the requirement for configuring the widgets using the type
 * for which the template will be applied instead of configuring the widgets using the template type (which is currently
 * being edited).
 *
 * The following algorithm is used - IdocContext.getCurrentObject() is overridden with a code that intercepts only
 * the first call of getCurrentObject() in order to calculate the modeling flag as early as possible.
 *
 * A wrapper of the original IdocContext that substitutes the current object (if the current object is a template)
 * with a fake object that mimics the object that will be created using the current template.
 * If the type of the current object is not a template, the decoration is not needed and the decorated methods are
 * restored to call directly the original methods without applying any additional logic.
 * getSharedObject() and getSharedObjects() are decorated to ensure that the fake object is returned if the current
 * object is requested via these methods and not using getCurrentObject().
 */
@Injectable()
@Inject(DefinitionService, PromiseAdapter)
export class ModelingIdocContextBuilder {

  constructor(definitionService, promiseAdapter) {
    this.definitionService = definitionService;
    this.promiseAdapter = promiseAdapter;
  }

  wrapIdocContext(idocContext) {
    const definitionService = this.definitionService;
    const promiseAdapter = this.promiseAdapter;

    var originalGetCurrentObject = idocContext.getCurrentObject;

    // Ensure the modeling flag is calculated as early as possible (the first time the current object gets loaded)
    idocContext.getCurrentObject = function () {
      return originalGetCurrentObject.apply(idocContext).then(currentObject => {
        if (currentObject.instanceType === TEMPLATE_DEFINITION_TYPE) {
          idocContext.setModeling(true);
        }

        idocContext.getCurrentObject = originalGetCurrentObject;

        return currentObject;
      });
    };

    var wrappedContext = decorate(idocContext, {
      'getCurrentObject': function () {
        if (this.substitution) {
          return promiseAdapter.resolve(this.substitution);
        }

        return idocContext.getCurrentObject().then(currentObject => {
          if (currentObject.instanceType === TEMPLATE_DEFINITION_TYPE) {
            var typeUnderModeling = currentObject.getPropertyValue(FOR_OBJECT_TYPE);

            return definitionService.getDefinitions(typeUnderModeling).then(result => {
              var models = result.data[typeUnderModeling];

              this.substitution = new InstanceObject(currentObject.getId(), models);

              return this.substitution;
            });
          } else {
            // undecorate the methods because the decorator is not longer of use in this case
            wrappedContext.undecorate();

            return currentObject;
          }
        });
      },
      'getSharedObjects': function (ids, widgetId, reset, config) {
        // handles a specific case where getSharedObjects() is used instead of getCurrentObject()
        if (ids.length === 1 && ids[0] === idocContext.getCurrentObjectId()) {
          return this.getCurrentObject().then(currentObject => {
            return {data: [currentObject]};
          });
        }

        return idocContext.getSharedObjects(ids, widgetId, reset, config);
      },
      'getSharedObject': function (id, widgetId, reset) {
        // handles a specific case where getSharedObject() is used instead of getCurrentObject()
        if (id === idocContext.getCurrentObjectId()) {
          return this.getCurrentObject();
        }

        return idocContext.getSharedObject(id, widgetId, reset);
      }
    });

    return wrappedContext;
  }

}