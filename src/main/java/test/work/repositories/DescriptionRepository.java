package test.work.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import test.work.entities.Description;

import java.util.Optional;

@Repository
public interface DescriptionRepository extends JpaRepository<Description, Integer> {

	@Query("select u from Description u where u.description = ?1")
	Optional<Description> findByNK(String description);

}
