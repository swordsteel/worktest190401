package test.work.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
public class Batch {

	@GeneratedValue
	@Id
	private Long id;
	@Column(nullable = false)
	@NaturalId
	private String filename;
	private Integer totalNumberOfRows;
	private Integer numberOfNewTrans;
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;
	@Column(nullable = false)
	@CreationTimestamp
	private Date creationDate;

	public Batch(String fileName, Date startDate) {
		this.filename = fileName;
		this.startDate = startDate;
	}

}
