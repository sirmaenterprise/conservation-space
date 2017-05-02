import {SelectIdocTemplate} from 'idoc/template/select-idoc-template';
import {AfterEditActionExecutedEvent} from 'idoc/actions/events/after-edit-action-executed-event';
import {PromiseStub} from 'test/promise-stub';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('SelectIdocTemplate', () => {

  var mockConfiguration = (persisted, content) => {
    var object = {
      isPersisted: function () {
        return persisted;
      },
      models: {definitionId: 'defid'},
      content: content
    };
    var context = {
      getCurrentObject: () => {
        return {
          then: (cb) => cb(object)
        }
      }
    };
    return {
      currentObject: object,
      idocContext: context
    }
  };

  var action;
  var eventbus;
  var templateService;

  beforeEach(() => {
    eventbus = {
      subscribe: sinon.spy()
    };
    templateService = {
      loadTemplates: sinon.spy(() => {
        return PromiseStub.resolve({
          data: [{
            id: '123',
            properties: {
              title: 'Template 123'
            }
          }]
        });
      }),
      loadContent: sinon.spy(() => {
        return PromiseStub.resolve({data: 'primary-content'});
      })
    };
    action = new SelectIdocTemplate(templateService, eventbus, mock$scope());
  });

  it('should subscribe for AfterEditActionExecutedEvent', () => {
    expect(eventbus.subscribe.getCall(0).args[0]).to.equal(AfterEditActionExecutedEvent);
  });

  describe('adaptTemplates', () => {

    it('should throw if the given templates array is null', () => {
      expect(action.adaptTemplates.bind(action, null)).to.throw;
    });

    it('should throw if the given templates array is undefined', () => {
      expect(action.adaptTemplates.bind(action, undefined)).to.throw;
    });

    it('should transform object to { id: <template-id>, text: <template-title> }', () => {
      var expected = [{
        id: '1',
        templateInstanceId: 'templateId',
        text: 'T1'
      }];
      var toConvert = [{
        id: '1',
        templateInstanceId: 'templateId',
        properties: {title: 'T1'}
      }];

      expect(action.adaptTemplates(toConvert)).to.deep.eq(expected);
    });

    it('should move the primary template to be the first in the array', () => {
      var expected = [{
        id: '1',
        templateInstanceId: 'templateId',
        text: 'T1'
      }, {
        id: '2',
        templateInstanceId: 'templateId',
        text: 'T2'
      }, {
        id: '3',
        templateInstanceId: 'templateId',
        text: 'T3'
      }];
      var toConvert = [{
        id: '2',
        templateInstanceId: 'templateId',
        properties: {title: 'T2'}
      }, {
        id: '3',
        templateInstanceId: 'templateId',
        properties: {title: 'T3'}
      }, {
        id: '1',
        templateInstanceId: 'templateId',
        properties: {title: 'T1', primary: true}
      }];

      expect(action.adaptTemplates(toConvert)).to.deep.eq(expected);
    });
  });

  describe('handleEntityLoad', () => {
    it('should throw if data is null', () => {
      expect(action.handleEntityLoad.bind(action, null)).to.throw;
    });

    it('should throw if data is undefined', () => {
      expect(action.handleEntityLoad.bind(action, undefined)).to.throw;
    });

    it('should throw if data does not contain the context as its first element', () => {
      expect(action.handleEntityLoad.bind(action, [{}])).to.throw;
    });

    it('should not load templates if no object is provided in the configuration', () => {
      action.handleEntityLoad();
      expect(templateService.loadTemplates.called).to.be.false;
    });

    it('should not load templates if object is persisted and has a content', () => {
      action.config = mockConfiguration(true, 'content');

      action.handleEntityLoad();
      expect(templateService.loadTemplates.called).to.be.false;
    });

    it('should load templates if object is not persisted', () => {
      action.config = mockConfiguration(false, 'content');

      action.handleEntityLoad();
      expect(templateService.loadTemplates.calledOnce).to.be.true;
      expect(templateService.loadTemplates.getCall(0).args[0]).to.eq('defid');
    });

    it('should construct select component configuration for displaying available templates', () => {
      action.config = mockConfiguration(false, 'content');

      action.handleEntityLoad();
      expect(action.selectTemplateConfig).to.exist;
    });

    it('should load primary template if the object is persisted but has no content', () => {
      action.config = mockConfiguration(true, undefined);
      action.callback = () => {
      };

      action.handleEntityLoad();

      expect(templateService.loadContent.called).to.be.true;
      expect(templateService.loadContent.getCall(0).args[0]).to.equal('123');
    });

    it('should provide the template content to the callback function', () => {
      action.config = mockConfiguration(true, undefined);
      action.callback = sinon.spy();

      action.handleEntityLoad();

      expect(action.callback.called).to.be.true;
      expect(action.callback.getCall(0).args[0]).to.equal('primary-content');
    });
  });

  describe('isSelectDisplayed()', () => {
    it('should not show the select if iDoc is in preview mode', () => {
      action.config = mockConfiguration(true, '');
      action.config.idocContext.isEditMode = () => {
        return false;
      };
      action.selectTemplateConfig = {};
      expect(action.isSelectDisplayed()).to.be.false;
    });

    it('should not show the select if the iDoc is in edit mode but the object has been persisted', () => {
      action.config = mockConfiguration(true, '');
      action.config.idocContext.isEditMode = () => {
        return true;
      };
      action.selectTemplateConfig = {};
      expect(action.isSelectDisplayed()).to.be.false;
    });

    it('should show the select if the iDoc is in edit mode and the object is not persisted', () => {
      action.config = mockConfiguration(false, '');
      action.config.idocContext.isEditMode = () => {
        return true;
      };
      action.selectTemplateConfig = {};
      expect(action.isSelectDisplayed()).to.be.true;
    });

    it('should not show the select if the select configuration is missing', () => {
      action.config = mockConfiguration(false, '');
      action.config.idocContext.isEditMode = () => {
        return true;
      };
      expect(action.isSelectDisplayed()).to.be.false;
    });
  });

  it('ngOnDestroy should unsubscribe event subscriptions', () => {
    action.events = [{unsubscribe: sinon.spy()}, {unsubscribe: sinon.spy()}];
    action.ngOnDestroy();
    expect(action.events[0].unsubscribe.callCount).to.equals(1);
    expect(action.events[1].unsubscribe.callCount).to.equals(1);
  });
});