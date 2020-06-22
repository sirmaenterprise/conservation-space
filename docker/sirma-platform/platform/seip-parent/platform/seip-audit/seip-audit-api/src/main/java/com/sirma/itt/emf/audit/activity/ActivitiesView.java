package com.sirma.itt.emf.audit.activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Entity that represent an activity view entity that contains unique request id and max received date. The data is used
 * to query the recent activities in proper order and grouped by request id as they arrive in the database. <br>
 * The entity cannot be used for inserting data. And it's defined so it can be used in HQL.
 *
 * @author BBonev
 */
@PersistenceUnitBinding({PersistenceUnits.CORE, "seip-auditlog"})
@Entity
@Table(name = "activities")
public class ActivitiesView {

	@Id
	@Column(name = "requestid", insertable = false)
	private String requestId;
	@Column(name = "datereceived", insertable = false)
	private Long dateReceived;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Long getDateReceived() {
		return dateReceived;
	}

	public void setDateReceived(Long dateReceived) {
		this.dateReceived = dateReceived;
	}

}
