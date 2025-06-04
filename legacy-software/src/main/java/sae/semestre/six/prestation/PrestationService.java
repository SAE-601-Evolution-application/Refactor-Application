package sae.semestre.six.prestation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;

/**
 * Gère la logique métier des prestations médicales.
 */
@Service
public class PrestationService {

    @Autowired
    private PrestationRepository prestationRepository;

    /**
     * Crée une nouvelle prestation médicale.
     */
    public Prestation createPrestation(Prestation prestation) {
        return prestationRepository.save(prestation);
    }

    /**
     * Met à jour une prestation existante.
     */
    public Optional<Prestation> updatePrestation(Long id, Double price) {
        return prestationRepository.findById(id)
                .map(p -> {
                    p.setName(p.getName());
                    p.setPrice(price);
                    return prestationRepository.save(p);
                });
    }

    /**
     * Supprime une prestation par ID.
     */
    public void deletePrestation(Long id) {
        prestationRepository.deleteById(id);
    }

    /**
     * Récupère toutes les prestations.
     */
    public List<Prestation> getAllPrestations() {
        return prestationRepository.findAll();
    }

    public Optional<Prestation> getPrestationByName(String name) {
        return prestationRepository.findByName(name);
    }
}
