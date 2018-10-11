describe('DropdownMenuManager', function() {
    var dropdownMenuManager;
    var mockGroup1 = 'mockmenugroup';

    beforeEach(function() {
        dropdownMenuManager = new DropdownMenuManager();
    });

    it('should add new menu group', function() {
        dropdownMenuManager.addMenuGroup(mockGroup1);
        var menusList = dropdownMenuManager.getMenus();
        expect(menusList.hasOwnProperty(mockGroup1)).toBe(true);
    });

    it('should add new item', function() {
        var mockLabel = 'mockLabel';
        var mockItem = {
            label: mockLabel
        }
        dropdownMenuManager.addMenuGroup(mockGroup1, mockGroup1);
        dropdownMenuManager.addItem(mockGroup1, mockItem);
        var menusList = dropdownMenuManager.getMenus();
        var menuItems = menusList[mockGroup1].getItems();
        var item = menuItems[mockLabel];
        expect(item).toBe(mockItem);
    });

    it('should read the description passed from the configuration', function() {
        var mockEditor = {
            config: {},
            addMenuGroup: function() {}
        };

        mockEditor.config.dropdownmenumanager = {
            'mock1': {
                items: [{
                    label: 'mock1Item1Label',
                    command: 'mock',
                    order: 1
                }, {
                    label: 'mock1Item2Label',
                    command: 'mock',
                    order: 2
                }]
            },
            'mock2': {
                items: [{
                    label: 'mock2Item1Label',
                    command: 'mock',
                    order: 1
                }],
                label: {
                    text: "More",
                    width: 30
                }
            }
        };

        dropdownMenuManager.readConfiguration(mockEditor);

        var menusList = dropdownMenuManager.getMenus();
        var mock1Items = menusList['mock1'].getItems();
        var mock2Items = menusList['mock2'].getItems();


        expect(mock1Items.hasOwnProperty('mock1Item1Label')).toBe(true);
        //expect(mock1Items.hasOwnProperty('mock1Item2Label')).toBe(true);
        expect(mock2Items.hasOwnProperty('mock2Item1Label')).toBe(true);
    });
});

describe('DropdownMenu', function() {
    var dropdownMenu;
    var mockGroup = 'mockGroup';
    var mockLabel = 'mockLabel';
    var mockItem = {
        label: mockLabel
    }

    beforeEach(function() {
        dropdownMenu = new DropdownMenu(mockGroup);
    });

    it('should add item', function() {
        dropdownMenu.addItem(mockItem);
        var itemFromDropdown = dropdownMenu.getItems()[mockLabel];
        expect(itemFromDropdown).toBe(mockItem);
    });

    it('should add group property to the mock object', function() {
        dropdownMenu.addItem(mockItem);
        var itemFromDropdown = dropdownMenu.getItems()[mockLabel];
        expect(itemFromDropdown.hasOwnProperty('group')).toBe(true);
    });

    it('should check if the group property is the same as the menu group', function() {
        dropdownMenu.addItem(mockItem);
        var itemFromDropdown = dropdownMenu.getItems()[mockLabel];
        expect(itemFromDropdown['group']).toBe(dropdownMenu.getMenuGroup());
    });
});