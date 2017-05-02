import {ObjectBrowser} from 'components/object-browser/object-browser';
import _ from 'lodash';

const ENTITY_ID = 'emf:99fd21fc-678e-405a-84f0-eb83d69aa415';

// stub init tree because it depends on DOM
ObjectBrowser.prototype.initTree = function () {
};
ObjectBrowser.prototype.context = {
  id: ENTITY_ID
};

describe('Object Browser', function () {
  describe('adaptModel() should adapt backend model to jstree nodes', function () {
    it('when the model provides children nodes', function () {
      var model = [createEntry('1', 5)];
      var objectBrowser = new ObjectBrowser();

      var result = objectBrowser.adaptModel(model);

      expect(result).to.have.length(1);
      expect(result[0].id).to.equal('dbId-1');
      expect(result[0].data).to.equal('1');
      expect(result[0].text).to.equal('Tree element 1');
      expect(result[0].children).to.have.length(5);
      expect(result[0].children[0].data).to.equal('1/0');
    });

    it('enabling lazy children loading when the model doesn\'t provide children but is not a leaf', function () {
      var model = [createEntry('1', 0, false)];
      var objectBrowser = new ObjectBrowser();

      var result = objectBrowser.adaptModel(model);

      // having children set to 'true' enables lazy loading in jstree
      expect(result[0].children).to.be.true;
    });

    it('adding a style class that hides the checkbox for nodes that don\'t support checking', function () {
      var model = [createEntry('1', 1, false)];
      model[0].children[0].checked = false;

      var objectBrowser = new ObjectBrowser();

      var result = objectBrowser.adaptModel(model);
      expect(result[0].a_attr.class).to.equal('instance-link hide-checkbox');
      expect(result[0].children[0].a_attr).to.eql({'class': 'instance-link'});
    });
  });

  describe('findElement()', function () {
    it('should find element in a hierarchy', function () {
      var model = [createEntry('1', 5)];

      var objectBrowser = new ObjectBrowser();

      const DB_ID = 'dbId-1/3';

      var result = objectBrowser.findElement(model, DB_ID);

      expect(result.dbId).to.equal(DB_ID);
    });
  });

});

function createEntry(id, childCount, leaf) {
  var children;
  if (childCount) {
    children = [];
    for (let i = 0; i < childCount; i++) {
      children.push(createEntry(id + '/' + i, 0));
    }
  }

  return {
    id: id,
    dbId: 'dbId-' + id,
    text: 'Tree element ' + id,
    children: children,
    leaf: leaf
  };
}