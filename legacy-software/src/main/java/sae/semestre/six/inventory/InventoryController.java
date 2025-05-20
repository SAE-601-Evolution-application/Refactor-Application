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

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/supplier-invoice")
    public ResponseEntity<String> processSupplierInvoice(@RequestBody SupplierInvoice invoice) {
        inventoryService.processSupplierInvoice(invoice);
        return ResponseEntity.ok("Supplier invoice processed successfully");
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockItems() {
        List<Inventory> lowStock = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStock);
    }

    @PostMapping("/reorder")
    public ResponseEntity<String> reorderItems() throws IOException {
        int count = inventoryService.reorderItems();
        return ResponseEntity.ok("Reorder requests sent for " + count + " items");
    }
} 