(function() {
	var components = EMF.extJsComponents || { };
	
	/**
	 * 
	 */
	components.getFieldEditor = function(context, raw, extraConfig, changeHandler) {
		var selected = null;
		var defaultConfig = {
			listeners: { change: changeHandler || $.noop }	
		};
		var config = $.extend(defaultConfig, extraConfig);
		switch (raw.editType) {
			case 'date':
			case 'datetime':
			case 'dateTime':
				selected = function() {
					return Ext.create('Ext.form.DateField', config);
				}
				break;
			case 'int': 
			case 'float': 
			case 'long': 
			case 'double':
			case 'number':
				// TODO: add some generic number editor
				selected = function() {
					return Ext.create('Ext.form.TextField', config);
				}
				break;
			case 'boolean':
				selected = function() {
					return Ext.create('Ext.form.Checkbox', config);
				}
				break;
			case 'text':
			case 'string':
				selected = function() {
					return Ext.create('Ext.form.TextField', config);
				}
				break;
			case 'codelist':
				selected = function() {
				
					if(!raw.codelistNumber) {
						raw.codelistNumber = 0;
					}
				
					var propStore = Ext.create('Ext.data.Store', {
						codelistNumber: raw.codelistNumber,
					    fields	: ['value', 'label'],
					    autoLoad: false,
				        proxy 	: {
				        	type 	: 'ajax',
				            url		: context.contextPath + context.service.codelist + '/' + raw.codelistNumber
				        }
					});
					
					propStore.on('load', function(store) {
					    store.insert(0, { value: null, label: 'Choose one...'});
					});
					
					propStore.load();
					
					var mergedConfig = $.extend(
						true,
						{
							store		: propStore,
							queryMode	: 'local',
							displayField: 'label',
							valueField	: 'value',
							matchFieldWidth: false
						},
						config
					);
						    
					var propCombo = Ext.create('Ext.form.ComboBox', mergedConfig);
					return propCombo;
				}
				break;
			default:
				console.warn('No suitable field found for type: ' + raw.editType + ', using default - TextField');
				selected = function() {
					return Ext.create('Ext.form.TextField', config);
				}
				break;
		}
		
		return selected() || null;
	}
	
	components.getCellEditor = function(context, raw, extraConfig) {
		return Ext.create('Ext.grid.CellEditor', {
			field: EMF.extJsComponents.getFieldEditor(context, raw, extraConfig)
        });
	}
	
	EMF.extJsComponents = components; 
}());