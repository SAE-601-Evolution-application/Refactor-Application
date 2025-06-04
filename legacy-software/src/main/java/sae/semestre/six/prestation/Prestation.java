package sae.semestre.six.prestation;

import jakarta.persistence.*;

/**
 * Représente une prestation médicale avec un nom et un prix.
 */
@Entity
@Table(name = "prestations")
public class Prestation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom de la prestation médicale (ex: CONSULTATION, XRAY, CHIRURGIE).
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Prix de la prestation en euros.
     */
    @Column(nullable = false)
    private Double price;

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name.toUpperCase();
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
