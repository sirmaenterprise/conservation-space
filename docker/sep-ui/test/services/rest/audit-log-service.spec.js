import {AuditLogService, AUDIT_LOG_URL} from 'services/rest/audit-log-service';

var auditLogService;

describe('AuditLogService ', () => {

  it('should use the correct service URL', () => {
    auditLogService = new AuditLogService();
    expect(auditLogService.getServiceUrl()).to.be.equal(AUDIT_LOG_URL);
  });

});