package sae.semestre.six.price;

import sae.semestre.six.inventory.Inventory;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "price_history")
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;
    
    @Column(name = "old_price")
    private Double oldPrice;
    
    @Column(name = "new_price")
    private Double newPrice;
    
    @Column(name = "change_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime changeDate = LocalDateTime.now();
    
    
    public Double getPriceIncrease() {
        return newPrice - oldPrice;
    }
    
    public Double getPercentageChange() {
        return (newPrice - oldPrice) / oldPrice * 100;
    }
} 