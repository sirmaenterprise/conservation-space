import {ContextualObjectsFactory, CURRENT_OBJECT} from 'services/context/contextual-objects-factory';
import {TranslateService} from 'services/i18n/translate-service';
import {ANY_OBJECT} from 'search/utils/search-criteria-utils';
import {HEADER_DEFAULT, HEADER_BREADCRUMB, HEADER_COMPACT} from 'instance-header/header-constants';

import {stub} from 'test/test-utils';

describe('ContextualObjectsFactory', () => {

  let factory;
  beforeEach(() => {
    let translateStub = stub(TranslateService);
    translateStub.translateInstant.returns('translation');
    factory = new ContextualObjectsFactory(translateStub);
  });

  it('should produce current object instance', () => {
    let currentObject = factory.getCurrentObject();
    expect(currentObject.id).to.equal(CURRENT_OBJECT);

    expect(factory.translateService.translateInstant.calledOnce).to.be.true;
    expect(currentObject.properties.title).to.equal('translation');

    let header = '<span data-property="title">translation</span>';
    assertHeaders(currentObject, header);
  });

  it('should cache the current object', () => {
    let currentObject = factory.getCurrentObject();
    let currentObject2 = factory.getCurrentObject();
    expect(currentObject).to.equal(currentObject2);
    expect(factory.translateService.translateInstant.calledOnce).to.be.true;
  });

  it('should produce any object instance', () => {
    let anyObject = factory.getAnyObject();
    expect(anyObject.id).to.equal(ANY_OBJECT);

    expect(factory.translateService.translateInstant.calledOnce).to.be.true;
    expect(anyObject.properties.title).to.equal('translation');

    let header = '<span data-property="title">translation</span>';
    assertHeaders(anyObject, header);
  });

  it('should cache the any object', () => {
    let anyObject = factory.getCurrentObject();
    let anyObject2 = factory.getCurrentObject();
    expect(anyObject).to.equal(anyObject2);
    expect(factory.translateService.translateInstant.calledOnce).to.be.true;
  });

  function assertHeaders(object, header) {
    expect(object.headers[HEADER_DEFAULT]).to.equal(header);
    expect(object.headers[HEADER_COMPACT]).to.equal(header);
    expect(object.headers[HEADER_BREADCRUMB]).to.equal(header);
  }

});