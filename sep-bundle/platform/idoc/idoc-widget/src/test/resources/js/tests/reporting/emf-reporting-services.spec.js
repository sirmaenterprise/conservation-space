(function() {
	'use strict';
	
	/*
	 * -- Legend --
	 * angular.mock.module == module
	 * angular.mock.inject == inject
	 */
	
	/**
	 * Suite for all tests related with the EMF Reporting Services.
	 */
	describe('EMF Reporting Services tests', function() {
	
		/**
		 * Mocks the module before every test.
		 */
		beforeEach(module('emfReportingServices'));
		
		/**
		 * Tests the partition filter.
		 */
		describe('Partition filter test', function() {
			it('Array [1,2,3,4,5,6,7] should be partitioned to [[1,2,3],[4,5,6],[7]] with size 3', inject(function(partitionFilter) {
				expect(partitionFilter([1,2,3,4,5,6,7], 3)).toEqual([[1,2,3], [4,5,6], [7]]);
			}));
			it('Empty [] should be partitioned to [] without considering the size', inject(function(partitionFilter) {
				expect(partitionFilter([], -3)).toEqual([]);
				expect(partitionFilter([], 0)).toEqual([]);
				expect(partitionFilter([], 3)).toEqual([]);
				expect(partitionFilter([], 15)).toEqual([]);
			}));
		});
		
		
		/**
		 * Suite for Common Semantic Properties Service tests.
		 */
		describe('Common Semantic Properties Service tests', function() {
			var httpBackend = {};
			var mockedSemanticService = {};
			
			/** Mocks the service and http. */
			beforeEach(inject(function (SemanticPropertiesService, $httpBackend) {
				mockedSemanticService = SemanticPropertiesService;
				httpBackend = $httpBackend;
			}));
			
			afterEach(function() {
				httpBackend.verifyNoOutstandingExpectation();
				httpBackend.verifyNoOutstandingRequest();
			});
			
			it('Semantic Properties Service should be defined', function() {
				expect(mockedSemanticService).toBeDefined();
			});
			
			it('Request parameters for types=[type1,type2] should be "forType=type1,type2"', function() {
				var types = ['type1', 'type2'];
				httpBackend.expectGET(EMF.reporting.propertiesServicePath+"?commonOnly=true&forType=type1,type2&multiValued=false").respond(200, {});
				var promise = mockedSemanticService.loadCommonSemanticProperties(types);
				promise.then(function (result) { 
					expect(result.status).toEqual(200);
				});
				httpBackend.flush();
			});
			
		});
		
		
		/**
		 * Suite for Faceted Search Service tests.
		 */
		describe('Faceted Search Service tests', function() {
			var mockedService = {};
			
			/** Mocks the service */
			beforeEach(inject(function (FacetedSearchService) {
				mockedService = FacetedSearchService;
			}));
			
			it('Faceted Search Service should be defined', function() {
				expect(mockedService).toBeDefined();
			});
			
			describe('Test faced search configuration building', function() {
				it('Without date type for group by', function() {
					var reportConfig = {report: { groupBy:'groupingBy'}};
					var config = mockedService.buildFacetedSearchConfig(reportConfig);
					expect(config.params.q).toEqual("*:*");
					expect(config.params.q).toEqual("*:*");
					expect(config.params.multiValued).toEqual(false);
					expect(config.params.includePrefix).toEqual(false);
					expect(config.params.field).toEqual("_sort_groupingBy");
				});
				xit('With date type for group by', function() {
					
				});
			});
			
			describe('Test faced start date builder', function() {
				var date = new Date();
				var dateString = $.datepicker.formatDate(SF.config.dateFormatPattern, date);
				it('Without provided date the default facet date is "NOW/DAY-31DAYS"', function() {
					expect(mockedService.buildFacetStartDate()).toEqual("NOW/DAY-31DAYS");
					expect(mockedService.buildFacetStartDate("")).toEqual("NOW/DAY-31DAYS");
				});
				it('With current date&time the faced date should be "NOW/DAY+0DAYS"', function() {
					expect(mockedService.buildFacetStartDate(dateString)).toEqual("NOW/DAY+0DAYS");
				});
				it('With date atleast 24 hours in the future the faced date should be "NOW/DAY+1DAYS"', function() {
					date.setHours(date.getHours() + 24 );
					dateString = $.datepicker.formatDate(SF.config.dateFormatPattern, date);
					expect(mockedService.buildFacetStartDate(dateString)).toEqual("NOW/DAY+1DAYS");
				});
				it('With date atleast 24h in the past the faced date should be "NOW/DAY-1DAYS"', function() {
					date.setHours(date.getHours() - 48 );
					dateString = $.datepicker.formatDate(SF.config.dateFormatPattern, date);
					expect(mockedService.buildFacetStartDate(dateString)).toEqual("NOW/DAY-1DAYS");
				});
			});
			
			describe('Test faced end date builder', function() {
				var date = new Date();
				var dateString = $.datepicker.formatDate(SF.config.dateFormatPattern, date);
				it('Without provided date the default faced date is "NOW/HOUR+1DAYS"', function() {
					expect(mockedService.buildFacetEndDate()).toEqual("NOW/HOUR+1DAYS");
					expect(mockedService.buildFacetEndDate("")).toEqual("NOW/HOUR+1DAYS");
				});
				it('With current date&time the faced date should be "NOW/HOUR+1DAYS"', function() {
					expect(mockedService.buildFacetEndDate(dateString)).toEqual("NOW/HOUR+1DAYS");
				});
				it('With date atleast 24 hours in the future the faced date should be "NOW/HOUR+2DAYS"', function() {
					date.setHours(date.getHours() + 24 );
					dateString = $.datepicker.formatDate(SF.config.dateFormatPattern, date);
					expect(mockedService.buildFacetEndDate(dateString)).toEqual("NOW/HOUR+2DAYS");
				});
				it('With date atleast 24h in the past the faced date should be "NOW/HOUR-1DAYS"', function() {
					date.setHours(date.getHours() - 48 );
					dateString = $.datepicker.formatDate(SF.config.dateFormatPattern, date);
					expect(mockedService.buildFacetEndDate(dateString)).toEqual("NOW/HOUR-1DAYS");
				});
			});
			
			describe('Test getting a date object from string', function() {
				//expect(mockedService.getDateFromSimpleString()).toBe(null);
				it('The result for providing an empty string as date should be null', function() {
					expect(mockedService.getDateFromSimpleString("")).toBe(null);
				});
				it('Providing "October/01/2014" as date should create a date object', function() {
					var dateAsString = "October/01/2014";
					var date = mockedService.getDateFromSimpleString(dateAsString);
					expect(date).not.toBe(null); 
					expect(date.getMonth() + 1).toBe(10);
				});
			});
			
			describe('Test difference in days between two dates', function() {
				var date1 = new Date();
				var date2 = new Date();
				it('The difference between two current dates should be 0', function() {
					expect(mockedService.getDifferenceInDays(date1,date2)).toEqual(0);
				});
				it('The difference between a date in the past and a current date should be -1', function() {
					date1.setHours(date1.getHours() - 24 );
					expect(mockedService.getDifferenceInDays(date1, date2)).toEqual(-1);
				});
				it('The difference between a current date and a date in the past should be 1', function() {
					expect(mockedService.getDifferenceInDays(date2, date1)).toEqual(1);
				});
			});
			
			describe('Test URI filters builder', function() {
				it('Providing empty array should return "()"', function() {
					expect(mockedService.buildUriFilters()).toEqual("()");
					expect(mockedService.buildUriFilters([])).toEqual("()");
				});
				it('Providing [{dbId:"123"}, {dbId:"456"}, {dbId:"789"}] should build "(uri:\"123" OR uri:\"456" OR uri:\"789")"', function() {
					var uris = [{dbId:'123'}, {dbId:'456'}, {dbId:'789'}];
					expect(mockedService.buildUriFilters(uris)).toEqual('(uri:\"123" OR uri:\"456" OR uri:\"789")');
				});
			});
			
			//TODO: 
			xdescribe('Check request parameters', function() {
			});
			
		});
		
		
		/**
		 * Suite for Object Search Service tests.
		 */
		describe('Object Search Service tests', function() {
			var httpBackend = {};
			var mockedService = {};
			
			/** Mocks the service and http. */
			beforeEach(inject(function (ObjectSearchService, $httpBackend) {
				mockedService = ObjectSearchService;
				httpBackend = $httpBackend;
			}));
			
			afterEach(function() {
				httpBackend.verifyNoOutstandingExpectation();
				httpBackend.verifyNoOutstandingRequest();
			});
			
			it('Object Search Service should be defined', function() {
				expect(mockedService).toBeDefined();
			});
			
			//TODO: Better test
			it('Testing POST method', function() {
				var path = EMF.reporting.objectServicePath + "/objectsByIdentity";
				var data = {};
				httpBackend.expectPOST(path, data).respond(200, {});
				
				var promise = mockedService.searchByIdentity(data);
				promise.then(function (result) { 
					expect(result.status).toEqual(200);
				});
				httpBackend.flush();
			});
		});
		
		describe('Reporting Factory tests', function() {
			var factory = {};
			
			/** Mocks the service and http. */
			beforeEach(inject(function (ReportingFactory) {
				factory = ReportingFactory;
			}));
			
			describe('Check service data status tests', function() {
				var response = {};
				it('Providing empty response should return false', function() {
					expect(factory.checkServiceDataStatus()).toEqual(false);
					expect(factory.checkServiceDataStatus(response)).toEqual(false);
				});
				it('Providing status code different than 200 should return false', function() {
					response.status=404;
					expect(factory.checkServiceDataStatus(response)).toEqual(false);
				});
				it('Providing empty data should return false', function() {
					response.status = 200;
					expect(factory.checkServiceDataStatus(response)).toEqual(false);
				});
				it('Providing no facets should return false', function() {
					response.data = {};
					expect(factory.checkServiceDataStatus(response)).toEqual(false);
				});
				it('Providing facetFields should return true', function() {
					response.data.facetFields = {};
					expect(factory.checkServiceDataStatus(response)).toEqual(true);
				});
				it('Providing facetDates & facetFields should return true', function() {
					response.data.facetDates = {};
					expect(factory.checkServiceDataStatus(response)).toEqual(true);
				});
			});
			
			describe('Getting correct data tests', function() {
				var data = {facetDates:[], facetFields:[]};
				it('Providing empty facetDates&facedFields should return undefined result', function() {	
					expect(factory.getCorrectData(data)).toBeUndefined();
				});
				it('Providing empty facetsDates should return facetFields', function() {	
					data = {facetDates:[], facetFields:[{bla:'123'}]};
					expect(factory.getCorrectData(data)).toEqual([{bla:'123'}]);
				});
				it('Providing no empty facets should return facetDates', function() {	
					data = {facetDates:[{evenMoreBla:'zumzum'}], facetFields:[{bla:'123'}]};
					expect(factory.getCorrectData(data)).toEqual([{evenMoreBla:'zumzum'}]);
				});
			});
			
			describe('Generate data store fields tests', function() {
				it('Providing empty array should result in returning empty array', function() {	
					expect(factory.generateDataStoreFields()).toEqual([]);
					expect(factory.generateDataStoreFields('')).toEqual([]);
				});
				it('Providing ["ludogorec"] should result in [{name: "ludogorec", mapping: "ludogorec"}]', function() {	
					expect(factory.generateDataStoreFields(['ludogorec'])).toEqual([{name: 'ludogorec', mapping: 'ludogorec'}]);
				});
				it("Providing ['1','2'] should result in [{name: '1', mapping: '1'},{name: '2', mapping: '2'}]", function() {	
					expect(factory.generateDataStoreFields(['ludogorec'])).toEqual([{name: 'ludogorec', mapping: 'ludogorec'}]);
					expect(factory.generateDataStoreFields(['1','2'])).toEqual([{name: '1', mapping: '1'},{name: '2', mapping: '2'}]);
				});
			});
			
			describe('Filter objects array tests', function() {
				//Only the object properties that will be tested are included
				var objectsArray = [				                    
				         {createdBy: "admin", status: "APPROVED"},
				         {createdBy: "admin", status: "APPROVED"},
				         {createdBy: "automatron", status: "SUBMITTED"},
				         {createdBy: "admin", status: "SUBMITTED"},
				         {createdBy: "testUser", status: "STARTED"},
				         {createdBy: "user3", status: "SUBMITTED"},
				         {createdBy: "ivan", status: "APPROVED"}
				         ];
				
				it("Filtering the objects array by [{property: 'createdBy', value: 'admin'}]", function() {
					expect(factory.filterObjectsArray(objectsArray, [{property: 'createdBy', value: 'admin'}]))
					.toEqual([{createdBy: 'admin', status: 'APPROVED'}, {createdBy: 'admin', status: 'APPROVED'}, {createdBy: 'admin', status: 'SUBMITTED'}]);
				});
				it("Filtering the objects array by [{property: 'status', value: 'STARTED'}]", function() {
					expect(factory.filterObjectsArray(objectsArray, [{property: 'status', value: 'STARTED'}]))
					.toEqual([{createdBy: "testUser", status: "STARTED"}]);
				});
				it("Filtering the objects array by [{property: 'status', value: 'APPROVED'}, {property: 'createdBy', value: 'min'}]", function() {
					expect(factory.filterObjectsArray(objectsArray, [{property: 'status', value: 'APPROVED'}, {property: 'createdBy', value: 'min'}]))
					.toEqual([{createdBy: "admin", status: "APPROVED"}, {createdBy: "admin", status: "APPROVED"}]);
				});
				it("Filtering the objects array by [{property: 'status', value: 'app'}, {property: 'createdBy', value: 'IVAN'}]", function() {
					expect(factory.filterObjectsArray(objectsArray, [{property: 'status', value: 'app'}, {property: 'createdBy', value: 'IVAN'}]))
					.toEqual([{createdBy: "ivan", status: "APPROVED"}]);
				});
			});
			
			//TODO: More testing!
			describe("Get property tests for the array [{type:'1',value:'one'},{type:'2',value:'two'},{type:'3',value:'three'}]", function() {
				var object;
				it('Providing empty array should result in returning {}', function() {	
					expect(factory.getProperty()).toEqual({});
				});
				it("The result for calling the function like getProperty('3',object,'type','value') should be 'three' ", function() {	
					object = [{type:'1',value:'one'},{type:'2',value:'two'},{type:'3',value:'three'}];
					expect(factory.getProperty('3',object,'type','value')).toEqual('three');
				});
			});
			
			describe("Extract properties tests for the array [{value:'avada',title:'AvadaKadavra'},{value:'imper',title:'Imperius'}]", function() {
				var array = [];
				it('Providing empty parameters should result in empty array', function() {	
					expect(factory.extractProperties()).toEqual([]);
					expect(factory.extractProperties([])).toEqual([]);
					expect(factory.extractProperties([],'')).toEqual([]);
					expect(factory.extractProperties([],'bla')).toEqual([]);
				});
				array = [{value:'avada',title:'AvadaKadavra'},{value:'imper',title:'Imperius'}];
				it("Given the method is called with (array,'value') the result should be ['avada','imper']", function() {	
					expect(factory.extractProperties(array,'value')).toEqual(['avada','imper']);
				});
				it("Given the method is called with (array,'title') the result should be ['AvadaKadavra','Imperius']", function() {	
					expect(factory.extractProperties(array,'title')).toEqual(['AvadaKadavra','Imperius']);
				});
				//expect(factory.extractProperties(array)).toEqual([]);
				//expect(factory.extractProperties(array,'')).toEqual([]);
				//expect(factory.extractProperties(array,'bla')).toEqual([]);
			});
			
			describe('Format tick date tests for the date "20.12.2014 02:47 UTC"', function() {
				// 20.12.2014 02:47 UTC - 51 week
				var isoString = new Date(1419043623114).toISOString();
				it('Given "12MONTH" for time interval the result should be "2014"', function() {	
					expect(factory.formatTickDate(isoString, '12MONTH')).toEqual(2014);
				});
				it('Given "1MONTH" for time interval the result should be "12"', function() {	
					expect(factory.formatTickDate(isoString, '1MONTH')).toEqual(12);
				});
				it('Given "7DAY" for time interval the result should be "[2014, 51]"', function() {	
					expect(factory.formatTickDate(isoString, '7DAY')).toEqual([2014, 51]);
				});
				it('Given "1DAY" for time interval the result should be "20/12/2014"', function() {	
					expect(factory.formatTickDate(isoString, '1DAY')).toEqual('20/12/2014');
				});
				it('Given "1HOUR" for time interval the result should be "20/12/2014 2:47"', function() {	
					expect(factory.formatTickDate(isoString, '1HOUR')).toEqual('20/12/2014 2:47');
				});
			});
			
			describe('Ontology parser tests', function() {
				it('Providing empty string should result in empty string', function() {	
					expect(factory.parseOntologyURI("")).toEqual("");
				});
				it('Providing "ala-bala-123" as URI should result in "ala-bala-123"', function() {	
					expect(factory.parseOntologyURI("ala-bala-123")).toEqual("ala-bala-123");
				});
				it('Providing "ala/bala-123" as URI should result in "bala-123"', function() {	
					expect(factory.parseOntologyURI("ala/bala-123")).toEqual("bala-123");
				});
				it('Providing "ala/bala#123" as URI should result in "123"', function() {	
					expect(factory.parseOntologyURI("ala/bala#123")).toEqual("123");
				});
			});
			
		});
		
	});

}());	
