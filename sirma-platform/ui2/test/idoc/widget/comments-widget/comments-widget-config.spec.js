import {CommentsWidgetConfig, EMPTY_CRITERIA} from 'idoc/widget/comments-widget/comments-widget-config';

describe('CommentsWidgetConfig', () => {
  let obejctSelectorHelper = {
    getSelectedItems: ()=> {
    }
  };

  let translateService = {
    translateInstant: ()=> {
      return 'translated';
    }
  };

  let newScope = {
    $destroy: sinon.spy()
  };

  let $scope = {
    $new: sinon.stub().returns(newScope)
  };

  let $compile = ()=> {
    return sinon.spy();
  };

  let newElement = {
    empty: sinon.spy(),
    append: sinon.spy()
  };

  let $element = {
    find: sinon.stub().returns(newElement)
  };
  CommentsWidgetConfig.prototype.config = {};
  let commentsWidgetConfig;
  beforeEach(()=> {
    commentsWidgetConfig = new CommentsWidgetConfig(obejctSelectorHelper, translateService, $scope, $compile, $element);
  });

  it('should clear the filters', ()=> {
    commentsWidgetConfig.config = {
      filterCriteria: {
        field: 'field'
      }
    };

    commentsWidgetConfig.clearFilters();

    expect($scope.$new.called).to.be.true;
    expect(newElement.empty.called).to.be.true;
    expect(newElement.append.called).to.be.true;
  });

  it('should append the select current object', function () {
    let objectSelectorComponent = {
      append: sinon.spy()
    };
    commentsWidgetConfig.appendSelectCurrentObject(objectSelectorComponent);
    expect(objectSelectorComponent.append.callCount).to.equal(1);
  });

  it('should create a filter configuration', () => {
    expect(commentsWidgetConfig.config.filterConfig.disabled).to.be.false;
    expect(commentsWidgetConfig.config.filterConfig.searchMediator).to.exist;
  });
});