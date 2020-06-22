import {RelatedObjectsMap} from 'idoc/widget/datatable-widget/datatable-widget';
import {InstanceObject} from 'models/instance-object';

describe('RelatedObjectsMap', () => {

  it('on construct should create an empty map', () => {
    let relatedObjectsMap = new RelatedObjectsMap();
    expect(relatedObjectsMap.map).to.eql({});
  });

  describe('addRelatedIds', () => {

    it('should add mapping for passed ids list by instance and relation type', () => {
      let relatedObjectsMap = new RelatedObjectsMap();

      relatedObjectsMap.addRelatedIds('emf:1', 'emf:createdBy', ['user1']);
      expect(relatedObjectsMap.map).to.eql({
        'emf:1': {
          'emf:createdBy': ['user1']
        }
      });

      relatedObjectsMap.addRelatedIds('emf:1', 'emf:createdBy', ['user2', 'user3']);
      expect(relatedObjectsMap.map).to.eql({
        'emf:1': {
          'emf:createdBy': ['user1', 'user2', 'user3']
        }
      });

      relatedObjectsMap.addRelatedIds('emf:1', 'emf:references', ['emf:11', 'emf:12']);
      expect(relatedObjectsMap.map).to.eql({
        'emf:1': {
          'emf:createdBy': ['user1', 'user2', 'user3'],
          'emf:references': ['emf:11', 'emf:12']
        }
      });

      relatedObjectsMap.addRelatedIds('emf:2', 'emf:references', ['emf:13', 'emf:14']);
      expect(relatedObjectsMap.map).to.eql({
        'emf:1': {
          'emf:createdBy': ['user1', 'user2', 'user3'],
          'emf:references': ['emf:11', 'emf:12']
        },
        'emf:2': {
          'emf:references': ['emf:13', 'emf:14']
        }
      });

    });

  });

  describe('populateObjects', () => {

    it('should replace all instance ids in the mapping with respective InstanceObjects', () => {
      let relatedObjectsMap = new RelatedObjectsMap();
      relatedObjectsMap.addRelatedIds('emf:1', 'emf:createdBy', ['user1']);
      relatedObjectsMap.addRelatedIds('emf:1', 'emf:parentOf', ['emf:10', 'emf:11']);
      relatedObjectsMap.addRelatedIds('emf:1', 'emf:dependsOn', []);
      relatedObjectsMap.addRelatedIds('emf:2', 'emf:createdBy', ['user2']);
      relatedObjectsMap.addRelatedIds('emf:2', 'emf:parentOf', ['emf:12']);
      relatedObjectsMap.addRelatedIds('emf:2', 'emf:dependsOn', ['emf:10', 'emf:11']);

      let objectsList = [
        {data: [new InstanceObject('user1')]},
        {data: [new InstanceObject('emf:10'), new InstanceObject('emf:11')]},
        {data: []},
        {data: [new InstanceObject('user2')]},
        {data: [new InstanceObject('emf:12')]},
        {data: [new InstanceObject('emf:10'), new InstanceObject('emf:11')]}
      ];

      relatedObjectsMap.populateObjects(objectsList);

      expect(relatedObjectsMap.getRelatedObjects('emf:1', 'emf:createdBy')[0].getId()).to.equal('user1');
      expect(relatedObjectsMap.getRelatedObjects('emf:1', 'emf:parentOf')[0].getId()).to.equal('emf:10');
      expect(relatedObjectsMap.getRelatedObjects('emf:1', 'emf:parentOf')[1].getId()).to.equal('emf:11');
      expect(relatedObjectsMap.getRelatedObjects('emf:1', 'emf:dependsOn').length).to.equal(0);
      expect(relatedObjectsMap.getRelatedObjects('emf:2', 'emf:createdBy')[0].getId()).to.equal('user2');
      expect(relatedObjectsMap.getRelatedObjects('emf:2', 'emf:parentOf')[0].getId()).to.equal('emf:12');
      expect(relatedObjectsMap.getRelatedObjects('emf:2', 'emf:dependsOn')[0].getId()).to.equal('emf:10');
      expect(relatedObjectsMap.getRelatedObjects('emf:2', 'emf:dependsOn')[1].getId()).to.equal('emf:11');
    });

  });

  describe('getFirstRelatedObject', () => {
    it('should return first related object by instanceId and relationType if exists or undefined', () => {
      let relatedObjectsMap = new RelatedObjectsMap();
      relatedObjectsMap.addRelatedIds('emf:1', 'emf:parentOf', ['emf:10', 'emf:11']);
      relatedObjectsMap.addRelatedIds('emf:1', 'emf:createdBy', []);
      let objectsList = [
        {data: [new InstanceObject('emf:10'), new InstanceObject('emf:11')]},
        {data: []}
      ];
      relatedObjectsMap.populateObjects(objectsList);

      let object = relatedObjectsMap.getFirstRelatedObject('emf:1', 'emf:parentOf');
      expect(object.getId()).to.equal('emf:10');

      object = relatedObjectsMap.getFirstRelatedObject('emf:1', 'emf:createdBy');
      expect(object).to.be.undefined;
    });
  });

});