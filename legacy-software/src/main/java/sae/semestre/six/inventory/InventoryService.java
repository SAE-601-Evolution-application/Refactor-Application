package sae.semestre.six.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sae.semestre.six.service.EmailService;
import sae.semestre.six.supplier.SupplierInvoice;
import sae.semestre.six.supplier.SupplierInvoiceDetail;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryDao inventoryDao;

    @Autowired
    private EmailService emailService;

    public InventoryService(InventoryDao inventoryDao) {
        this.inventoryDao = inventoryDao;
    }

    public void processSupplierInvoice(SupplierInvoice invoice) {
        for (SupplierInvoiceDetail detail : invoice.getDetails()) {
            Inventory inventory = detail.getInventory();
            inventory.setQuantity(inventory.getQuantity() + detail.getQuantity());
            inventory.setUnitPrice(detail.getUnitPrice());
            inventory.setLastRestocked(LocalDateTime.now());
            inventoryDao.update(inventory);
        }
    }

    public List<Inventory> getLowStockItems() {
        return inventoryDao.findAll().stream()
                .filter(Inventory::needsRestock)
                .collect(Collectors.toList());
    }

    public int reorderItems() throws IOException {
        List<Inventory> lowStockItems = inventoryDao.findNeedingRestock();

        try (FileWriter fw = new FileWriter("C:\\hospital\\orders.txt", true)) {
            for (Inventory item : lowStockItems) {
                int reorderQuantity = item.getReorderLevel() * 2;
                fw.write("REORDER: " + item.getItemCode() + ", Quantity: " + reorderQuantity + "\n");

                emailService.sendEmail(
                        "supplier@example.com",
                        "Reorder Request",
                        "Please restock " + item.getName() + " (Quantity: " + reorderQuantity + ")"
                );
            }
        }

        return lowStockItems.size();
    }
}
