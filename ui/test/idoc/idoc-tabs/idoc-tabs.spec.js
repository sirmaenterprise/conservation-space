import {IdocTabs} from 'idoc/idoc-tabs/idoc-tabs';
import {DragAndDrop} from 'components/draganddrop/drag-and-drop';

describe('Tests for IdocTabs component', () => {

  let eventbus = {
    publish: sinon.spy(),
    subscribe: sinon.spy()
  };

  let originalFunction = DragAndDrop.makeDraggable;

  before(() => {
    DragAndDrop.makeDraggable = sinon.spy();
  });

  after(() => {
    DragAndDrop.makeDraggable = originalFunction;
  });

  beforeEach(() => {
    eventbus.publish.reset();
  });

  it('should return true when in edit mode', () => {
    IdocTabs.prototype.mode = 'edit';
    IdocTabs.prototype.config = {
      activeTabId: 'tab_1',
      tabs: []
    };
    let idocTabs = new IdocTabs(mockScope(), mockElement(), {}, eventbus);
    expect(idocTabs.isEditMode()).to.be.true;
  });

  it('should not allow drag of single tab', () => {
    IdocTabs.prototype.config = {
      activeTabId: 'tab_1',
      tabs: [{id: 'tab_1', title: 'new_tab', showNavigation: true, showComments: false}]
    };
    IdocTabs.prototype.mode = 'edit';
    let idocTabs = new IdocTabs(mockScope(), mockElement(), {}, eventbus);
    expect(idocTabs.isDraggAllowed()).to.be.false;
  });

  it('should not allow drag in preview mode', () => {
    IdocTabs.prototype.config = {
      activeTabId: 'tab_1',
      tabs: [{id: 'tab_1', title: 'new_one', showNavigation: true, showComments: false},
        {id: 'tab_2', title: 'new_two', showNavigation: true, showComments: false}]
    };
    IdocTabs.prototype.mode = 'preview';
    let idocTabs = new IdocTabs(mockScope(), mockElement(), {}, eventbus);
    expect(idocTabs.isDraggAllowed()).to.be.false;
  });

  it('should allow drag in edit mode if more than one tab exist', () => {
    IdocTabs.prototype.config = {
      activeTabId: 'tab_1',
      tabs: [{id: 'tab_1', title: 'new_tab', showNavigation: true, showComments: false},
        {id: 'tab_2', title: 'new_two', showNavigation: true, showComments: false}]
    };
    IdocTabs.prototype.mode = 'edit';
    let idocTabs = new IdocTabs(mockScope(), mockElement(), {}, eventbus);
    expect(idocTabs.isDraggAllowed()).to.be.true;
  });
});

function mockElement() {
  return {
    sortable: function () {

    }
  }
}

function mockScope() {
  let mockScope = {
    $watch: ()=> {
    },
    $$phase: undefined
  };
  return mockScope;
}