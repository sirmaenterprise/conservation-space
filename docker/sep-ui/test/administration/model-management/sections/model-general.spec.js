import {EventEmitter} from 'common/event-emitter';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelGeneral} from 'administration/model-management/sections/general/model-general';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {stub} from 'test/test-utils';

describe('ModelGeneralSection', () => {

  let clazz;
  let definition;
  let modelGeneralSection;

  beforeEach(() => {
    clazz = new ModelClass('clazz');
    definition = new ModelDefinition('definition').setType(clazz);
    modelGeneralSection = new ModelGeneral();

    modelGeneralSection.emitter = stub(EventEmitter);
    modelGeneralSection.model = definition;

    modelGeneralSection.notifyForModelsSave = sinon.spy();
    modelGeneralSection.notifyForModelRevert = sinon.spy();
    modelGeneralSection.notifyForSectionStateChange = sinon.spy();
    modelGeneralSection.notifyForModelAttributeChange = sinon.spy();
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
});