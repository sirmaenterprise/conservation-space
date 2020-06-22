import {ModelListView, RADIOBTN, CHECKBOX} from 'administration/model-management/components/list/model-list-view';
import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelList} from 'administration/model-management/model/model-list';

describe('ModelListView', () => {

  let modelListView;

  beforeEach(() => {
    modelListView = new ModelListView();
    modelListView.onAction = sinon.spy();
    modelListView.ngOnInit();
  });

  it('should provide a default configuration', () => {
    expect(modelListView.config).to.deep.eq({
      singleSelection: true,
      selectableItems: false
    });
  });

  it('should notify that action is performed on the list', () => {
    let item = new ModelBase('1');

    modelListView.config.selected = new ModelList()
      .insert(item);

    modelListView.models = new ModelList()
      .insert(item)
      .insert(new ModelBase('2'))
      .insert(new ModelBase('3'));

    modelListView.selectItem(item);
    expect(modelListView.onAction.calledOnce).to.be.true;

    modelListView.onAction.reset();

    modelListView.deselectItem(item);
    expect(modelListView.onAction.calledOnce).to.be.true;

    modelListView.onAction.reset();

    modelListView.selectAll();
    expect(modelListView.onAction.calledOnce).to.be.true;

    modelListView.onAction.reset();

    modelListView.deselectAll();
    expect(modelListView.onAction.calledOnce).to.be.true;
  });

  it('should provide a proper configuration when configured for selection', () => {
    modelListView.config.selectableItems = true;
    modelListView.ngOnInit();

    expect(modelListView.config).to.deep.eq({
      singleSelection: true,
      selectableItems: true,
      selected: new ModelList()
    });
  });

  it('should properly select / deselect item in single selection', () => {
    modelListView.config.selectableItems = true;
    modelListView.config.singleSelection = true;
    modelListView.ngOnInit();

    let item = new ModelBase('1');
    modelListView.toggleItem(item);
    expect(modelListView.config.selected.getModel('1')).to.eq(item);
    expect(modelListView.config.selected.getModels().length).to.eq(1);

    item = new ModelBase('2');
    modelListView.toggleItem(item);
    expect(modelListView.config.selected.getModel('2')).to.eq(item);
    expect(modelListView.config.selected.getModels().length).to.eq(1);
  });

  it('should properly select / deselect item in multi selection', () => {
    modelListView.config.selectableItems = true;
    modelListView.config.singleSelection = false;
    modelListView.ngOnInit();

    let item = new ModelBase('1');
    modelListView.toggleItem(item);
    expect(modelListView.config.selected.getModel('1')).to.eq(item);
    expect(modelListView.config.selected.getModels().length).to.eq(1);

    modelListView.toggleItem(item);
    expect(modelListView.config.selected.getModels().length).to.eq(0);

    let item2 = new ModelBase('2');
    modelListView.toggleItem(item);
    modelListView.toggleItem(item2);

    expect(modelListView.config.selected.getModel('1')).to.eq(item);
    expect(modelListView.config.selected.getModel('2')).to.eq(item2);
    expect(modelListView.config.selected.getModels().length).to.eq(2);
  });

  it('should properly select all models', () => {
    modelListView.config.selected = new ModelList();
    modelListView.models = new ModelList()
      .insert(new ModelBase('1'))
      .insert(new ModelBase('2'))
      .insert(new ModelBase('3'));

    modelListView.selectAll();
    expect(modelListView.config.selected.getModels().length).to.eq(3);
  });

  it('should properly deselect all models', () => {
    modelListView.config.selected = new ModelList()
      .insert(new ModelBase('1'))
      .insert(new ModelBase('2'))
      .insert(new ModelBase('3'));

    modelListView.deselectAll();
    expect(modelListView.config.selected.getModels().length).to.eq(0);
  });

  it('should properly select a single item', () => {
    modelListView.config.selected = new ModelList();

    let item = new ModelBase('1');
    modelListView.selectItem(item);
    expect(modelListView.config.selected.getModel('1')).to.eq(item);
    expect(modelListView.config.selected.getModels().length).to.eq(1);
  });

  it('should properly deselect a single item', () => {
    let item = new ModelBase('1');
    modelListView.config.selected = new ModelList().insert(item);

    modelListView.deselectItem(item);
    expect(modelListView.config.selected.getModel('1')).to.not.exist;
    expect(modelListView.config.selected.getModels().length).to.eq(0);
  });

  it('should check if an item is selected', () => {
    let item = new ModelBase('1');
    modelListView.config.selected = new ModelList().insert(item);

    modelListView.isSelected(item);
    expect(modelListView.isSelected(item)).to.be.true;
  });

  it('should resolve the proper selection control type', () => {
    modelListView.config.singleSelection = true;
    expect(modelListView.getSelectionControlType()).to.eq(RADIOBTN);

    modelListView.config.singleSelection = false;
    expect(modelListView.getSelectionControlType()).to.eq(CHECKBOX);
  });

  it('should resolve if select or deselect controls are enabled', () => {
    modelListView.config.selectableItems = true;

    modelListView.config.singleSelection = true;
    expect(modelListView.isSelectDeselectEnabled()).to.be.false;

    modelListView.config.singleSelection = false;
    expect(modelListView.isSelectDeselectEnabled()).to.be.true;
  });

  it('should properly resolve if there are any models present to be displayed', () => {
    modelListView.models = new ModelList()
      .insert(new ModelBase('1'))
      .insert(new ModelBase('2'))
      .insert(new ModelBase('3'));
    expect(modelListView.hasModelsToDisplay()).to.be.true;

    modelListView.models = null;
    expect(modelListView.hasModelsToDisplay()).to.be.false;

    modelListView.models = new ModelList();
    expect(modelListView.hasModelsToDisplay()).to.be.false;
  });
});