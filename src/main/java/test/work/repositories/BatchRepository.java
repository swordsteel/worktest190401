package test.work.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import test.work.entities.Batch;

import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Integer> {

	@Query("select u from Batch u where u.filename = ?1")
	Optional<Batch> findByNK(String filename);

}
