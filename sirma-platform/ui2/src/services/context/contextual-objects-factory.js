import {Injectable, Inject} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {ANY_OBJECT} from 'search/utils/search-criteria-utils';
import {HEADER_DEFAULT, HEADER_BREADCRUMB, HEADER_COMPACT} from 'instance-header/header-constants';

export const CURRENT_OBJECT = 'current_object';

/**
 * Factory for constructing an instance object for the current or any context/object. Those are not real instances,
 * they should be used as wildcards in queries or selection views.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(TranslateService)
export class ContextualObjectsFactory {

  constructor(translateService) {
    this.translateService = translateService;
  }

  getCurrentObject() {
    if (!this.currentObject) {
      this.currentObject = this.constructObject(CURRENT_OBJECT, 'context.current.object');
    }
    return this.currentObject;
  }

  getAnyObject() {
    if (!this.anyObject) {
      this.anyObject = this.constructObject(ANY_OBJECT, 'search.advanced.value.anyObject');
    }
    return this.anyObject;
  }

  constructObject(id, labelKey) {
    let label = this.translateService.translateInstant(labelKey);
    let currentObject = {
      id: id,
      properties: {
        title: label
      },
      headers: {}
    };
    currentObject.headers[HEADER_DEFAULT] = `<span data-property="title">${label}</span>`;
    currentObject.headers[HEADER_BREADCRUMB] = currentObject.headers[HEADER_DEFAULT];
    currentObject.headers[HEADER_COMPACT] = currentObject.headers[HEADER_DEFAULT];
    return currentObject;
  }
}
