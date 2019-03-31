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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
public class Transaction {

	@GeneratedValue
	@Id
	private Long id;
	@Column(nullable = false)
	@NaturalId
	private Integer transaction;
	@Temporal(TemporalType.DATE)
	Date date;
	private Long amount;
	@Column(nullable = false)
	@CreationTimestamp
	private Date Creation;
	@JoinColumn
	@ManyToOne(fetch = FetchType.LAZY)
	private Description description;
	@JoinColumn
	@ManyToOne(fetch = FetchType.LAZY)
	private Batch batch;

	public Transaction(int transaction, Date date, long amount, Description description, Batch batch) {
		this.transaction = transaction;
		this.date = date;
		this.amount = amount;
		this.description = description;
		this.batch = batch;
	}

}
