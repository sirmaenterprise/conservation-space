import {ContextualObjectsFactory} from 'services/context/contextual-objects-factory';
import {TranslateService} from 'services/i18n/translate-service';

import {stub} from 'test/test-utils';

export function stubContextualFactory() {
  let translateStub = stub(TranslateService);
  return new ContextualObjectsFactory(translateStub);
}