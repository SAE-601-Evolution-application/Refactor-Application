package sae.semestre.six.insurance;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InsuranceRepository extends JpaRepository<Insurance, Long> {
    Insurance findInsuranceByPatientId(int patientId);
}
