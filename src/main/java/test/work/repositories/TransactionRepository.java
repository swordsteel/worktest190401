package test.work.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import test.work.entities.Transaction;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

	@Query("select u from Transaction u where u.transactionID = ?1")
	Optional<Transaction> findByNK(int transactionID);

}
