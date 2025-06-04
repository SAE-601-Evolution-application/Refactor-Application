package sae.semestre.six.prestation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Interface d'accès aux données pour les prestations médicales.
 */
public interface PrestationRepository extends JpaRepository<Prestation, Long> {

    /**
     * Recherche une prestation par son nom.
     * @param name Nom de la prestation.
     * @return Une prestation optionnelle.
     */
    Optional<Prestation> findByName(String name);
}
