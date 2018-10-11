import {EventEmitter} from 'common/event-emitter';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelGeneral} from 'administration/model-management/sections/general/model-general';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

import {stub} from 'test/test-utils';

describe('ModelGeneralSection', () => {

  let clazz;
  let scopeMock;
  let definition;
  let modelGeneralSection;

  beforeEach(() => {
    clazz = new ModelClass('clazz');
    definition = new ModelDefinition('definition').setType(clazz);

    scopeMock = mockScope();
    modelGeneralSection = new ModelGeneral(scopeMock);
    modelGeneralSection.emitter = stub(EventEmitter);
    modelGeneralSection.model = definition;
  });

  it('should initialize class and definition models', () => {
    modelGeneralSection.ngOnInit();
    expect(modelGeneralSection.class).to.eq(clazz);
    expect(modelGeneralSection.definition).to.eq(definition);
  });

  it('should subscribe to model change or reload', () => {
    modelGeneralSection.ngOnInit();
    expect(modelGeneralSection.emitter.subscribe.calledOnce).to.be.true;
  });

  it('should check if a model is a definition model', () => {
    expect(modelGeneralSection.isModelDefinition(clazz)).to.be.false;
    expect(modelGeneralSection.isModelDefinition(definition)).to.be.true;
  });

  function mockScope() {
    return {
      $watch: sinon.spy(() => {
      })
    };
  }
});