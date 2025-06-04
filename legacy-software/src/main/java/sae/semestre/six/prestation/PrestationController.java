package sae.semestre.six.prestation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour les prestations médicales.
 */
@RestController
@RequestMapping("/api/prestations")
public class PrestationController {

    @Autowired
    private  PrestationService prestationService;


    /**
     * Crée une nouvelle prestation médicale.
     * @param prestation La prestation à créer.
     * @return La prestation créée.
     */
    @PostMapping
    public ResponseEntity<Prestation> createPrestation(@RequestBody Prestation prestation) {
        return ResponseEntity.ok(prestationService.createPrestation(prestation));
    }

    /**
     * Met à jour une prestation existante.
     * @param id ID de la prestation.
     * @param prestation Nouvelles données.
     * @return La prestation mise à jour.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Prestation> updatePrestation(@RequestParam Long id, @RequestParam Double price) {
        return prestationService.updatePrestation(id, price)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Supprime une prestation par ID.
     * @param id ID de la prestation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrestation(@PathVariable Long id) {
        prestationService.deletePrestation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère toutes les prestations médicales.
     */
    @GetMapping
    public ResponseEntity<List<Prestation>> getAllPrestations() {
        return ResponseEntity.ok(prestationService.getAllPrestations());
    }

    /**
     * Récupère une prestation par ID.
     */
    @GetMapping("/{name}")
    public ResponseEntity<Prestation> getPrestationById(@PathVariable String name) {
        return prestationService.getPrestationByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
