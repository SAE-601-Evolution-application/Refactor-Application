package sae.semestre.six.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    @Query(value = "SELECT SUM(b.total_amount) FROM bills b", nativeQuery = true)
    Double findTotalRevenue();

    List<Bill> findByStatus(Status status);
} 