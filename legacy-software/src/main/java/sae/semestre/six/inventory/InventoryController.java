package sae.semestre.six.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sae.semestre.six.supplier.SupplierInvoice;
import sae.semestre.six.supplier.SupplierInvoiceDetail;
import sae.semestre.six.service.EmailService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.IOException;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    
    @Autowired
    private InventoryDao inventoryDao;
    
    private final EmailService emailService = EmailService.getInstance();
    
    
    @PostMapping("/supplier-invoice")
    public ResponseEntity<String> processSupplierInvoice(@RequestBody SupplierInvoice invoice) {
        for (SupplierInvoiceDetail detail : invoice.getDetails()) {
            Inventory inventory = detail.getInventory();

            inventory.setQuantity(inventory.getQuantity() + detail.getQuantity());
            inventory.setUnitPrice(detail.getUnitPrice());
            inventory.setLastRestocked(LocalDateTime.now());

            inventoryDao.update(inventory);
        }

        return ResponseEntity.ok("Supplier invoice processed successfully");
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockItems() {
        List<Inventory> lowStock = inventoryDao.findAll().stream()
                .filter(Inventory::needsRestock)
                .collect(Collectors.toList());

        return ResponseEntity.ok(lowStock);
    }

    @PostMapping("/reorder")
    public ResponseEntity<String> reorderItems() throws IOException {
        List<Inventory> lowStockItems = inventoryDao.findNeedingRestock();

        for (Inventory item : lowStockItems) {
            int reorderQuantity = item.getReorderLevel() * 2;

            try (FileWriter fw = new FileWriter("C:\\hospital\\orders.txt", true)) {
                fw.write("REORDER: " + item.getItemCode() + ", Quantity: " + reorderQuantity + "\n");
            }

            emailService.sendEmail(
                "supplier@example.com",
                "Reorder Request",
                "Please restock " + item.getName() + " (Quantity: " + reorderQuantity + ")"
            );
        }
        
        return ResponseEntity.ok("Reorder requests sent for " + lowStockItems.size() + " items");
    }
} 