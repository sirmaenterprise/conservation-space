window.PluginRegistry = (function() {
	var registry = {};
	registry.get = function() {
		return [{
			"order": 10,
			"name": "createProject",
			"component": "create-project",
			"module": "sandbox/components/dropdown-menu/create-project"
		},
		{
			"order": 15,
			"name": "createCase",
			"component": "create-case",
			"module": "sandbox/components/dropdown-menu/create-case"
		},
		{
			"order": 20,
			"name": "createDocument",
			"component": "create-document",
			"module": "sandbox/components/dropdown-menu/create-document"
		}
		];
	};
	return registry;
})();

