package test.work.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import test.work.entities.Batch;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Integer> {
}
