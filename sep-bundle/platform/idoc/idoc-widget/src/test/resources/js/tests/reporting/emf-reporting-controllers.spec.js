(function() {
	'use strict';
	
	/*
	 * -- Legend --
	 * angular.mock.module == module
	 * angular.mock.inject == inject
	 */
	
	/**
	 * Suite for all tests related with the EMF Reporting Controllers.
	 */
	describe('EMF Reporting Controllers tests', function() {
		
		beforeEach(function () {
			this.getReportConfig = function() {
				var reportConfig = {
					report : {
						groupBy : 'createdOn',
						groupByType : '',
						timeInterval : '+12MONTH'
					},
					groupBy : [{value:'createdOn',	type:'tdates', label:'Created On'},
					           {value:'modifiedBy', type:'string', label:'Modified By'}]
				};
				return reportConfig;
			};
			
			this.getServiceData = function() {
				var data = {
					facetDates:[],
					facetFields:[{alablaa:[{count:123,name:'something'},{count:456,name:'darkside'}]}]
				};
				return data;
			};
			
			this.getServiceDataWithTime = function(date) {
				var data = {
					facetDates:[],
					facetFields:[{alablaa:[{count:123,name:date.toISOString()},{count:456,name:date.toISOString()}]}]
				};
				return data;
			};
			
		});
		
		/**
		 * Mocks the modules before every test.
		 */
		beforeEach(module('emfReportingServices'));
		beforeEach(module('emfReportingControllers'));
		
		describe('Reporting Controller tests', function() {
			var scope = {};
			var factory = {};
			
			/** Mocks the services. */
			beforeEach(inject(function ($controller, $rootScope, _ReportingFactory_) {
				scope = $rootScope.$new();
				factory = _ReportingFactory_;
				$controller('ReportingController', {
					$scope : scope, 
					ReportingFactory : factory
				});
			}));
			
			it('Getting the report types test', function() {
				var types = scope.getReportTypes();
				expect(types).toBeDefined();
			});
			
			it('Getting the time intervals test', function() {
				var intervals = scope.getTimeIntervals();
				expect(intervals).toBeDefined();
			});
			
			it('Updating group by type test', function() {
				scope.reportConfig = {
					report : {
						groupBy : 'createdOn',
						groupByType : ''
					},
					groupBy : [{value:'createdOn',type:'tdates'}]
				};
				scope.updateGroupByType();
				expect(scope.reportConfig.report.groupByType).toEqual('tdates');
			});
			
			it('Get tagged properties test', function() {
				scope.reportConfig = {};
				scope.reportConfig.availableProperties = [
				    {title:'title1', properties:[{name:'ptitle1',title:'text1'}]},
				    {title:'title2', properties:[{name:'ptitle2',title:'text2'},{name:'ptitle22',title:'text22'}]}];
				var expected = [
				    {text:'title1', children:[{id:'text1',text:'ptitle1'}]},
				    {text:'title2', children:[{id:'text2',text:'ptitle2'},{id:'text22',text:'ptitle22'}]}];
				expect(scope.getGroupedAvailableProperties()).toEqual(expected);
			});
		});
		
		describe('Reporting Aggregated Table Controller tests', function() {
			var scope = {};
			var factory = {};
			
			/** Mocks the services. */
			beforeEach(inject(function ($controller, $rootScope, _ReportingFactory_) {
				scope = $rootScope.$new();
				factory = _ReportingFactory_;
				$controller('ReportingAggregatedTableController', {
					$scope : scope, 
					ReportingFactory : factory
				});
			}));
			
			describe('Get aggregated table data tests', function() {
				
				it("Testing data transformation without date field for group by", function() {
					scope.reportConfig = this.getReportConfig();
					var data = this.getServiceData();
					var aggData = scope.getAggrTableData(data);
					expect(aggData.title).toEqual('Created On');
					expect(aggData.interval).not.toBeDefined();
					expect(aggData.data).toEqual([{count:123, name:'something'},{count:456, name:'darkside'}]);
				});
				
				it("Testing data transformation with date field for group by", function() {
					scope.reportConfig = this.getReportConfig();
					scope.reportConfig.report.groupByType = 'tdates';
					var date = new Date();
					var data = this.getServiceDataWithTime(date);
					
					var aggData = scope.getAggrTableData(data);
					expect(aggData.title).toEqual('Created On');
					expect(aggData.interval).toEqual('Year');
					
					expect(aggData.data).toEqual([{count:123, name:date.getFullYear()},{count:456, name:date.getFullYear()}]);
				});
			});
			
			it('Check Service Data Status test. The ReportingFactory should be called.', function() {
				spyOn(factory, 'checkServiceDataStatus');
				scope.checkServiceDataStatus();
				expect(factory.checkServiceDataStatus).toHaveBeenCalled();
			});
			
		});
		
		xdescribe('Reporting Additional Filters Controller tests', function() {
			var scope = {};
			var factory = {};
			
			/** Mocks the services. */
			beforeEach(inject(function ($controller, $rootScope, _ReportingFactory_) {
				scope = $rootScope.$new();
				factory = _ReportingFactory_;
				$controller('ReportingAdditionalFiltersController', {
					$scope : scope, 
					ReportingFactory : factory
				});
			}));
			
			xit('Update filter properties test', function() {
				
			});
		});
		
		xdescribe('Reporting Chart Controller tests', function() {
			var scope = {};
			var factory = {};
			
			/** Mocks the services. */
			beforeEach(inject(function ($controller, $rootScope, _ReportingFactory_) {
				scope = $rootScope.$new();
				factory = _ReportingFactory_;
				$controller('ReportingChartController', {
					$scope : scope, 
					ReportingFactory : factory
				});
			}));
			
			
			
			
			
			
			
			it('Check Service Data Status test. The ReportingFactory should be called.', function() {
				spyOn(factory, 'checkServiceDataStatus');
				scope.checkServiceDataStatus();
				expect(factory.checkServiceDataStatus).toHaveBeenCalled();
			});
		});
		
		xdescribe('Reporting Raw Table Controller tests', function() {
			
		});
		
	});

}());