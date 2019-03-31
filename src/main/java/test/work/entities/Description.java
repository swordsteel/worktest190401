package test.work.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
public class Description {

	@GeneratedValue
	@Id
	private Long id;
	@Column(nullable = false)
	@NaturalId
	private String description;
	@Column(nullable = false)
	@CreationTimestamp
	private Date creation;
	@JoinColumn
	@ManyToOne(fetch = FetchType.LAZY)
	private Batch batch;

	public Description(String description, Batch batch) {
		this.description = description;
		this.batch = batch;
	}

}
