package sae.semestre.six;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sae.semestre.six.inventory.Inventory;
import sae.semestre.six.inventory.InventoryDao;
import sae.semestre.six.supplier.SupplierInvoice;
import sae.semestre.six.supplier.SupplierInvoiceDetail;
import sae.semestre.six.service.EmailService;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
//@Import(InventoryControllerTestConfig.class)
class TestIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    private Inventory inventory;

    @BeforeEach
    void setup() {
        inventory = new Inventory();
        inventory.setItemCode("ITEM001");
        inventory.setName("Test Item");
        inventory.setQuantity(5);
        inventory.setUnitPrice(10.0);
        inventory.setReorderLevel(5);
    }

    @Test
    void testProcessSupplierInvoice_success() throws Exception {
        /*SupplierInvoiceDetail detail = new SupplierInvoiceDetail();
        detail.setInventory(inventory);
        detail.setQuantity(10);
        detail.setUnitPrice(12.5);

        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setDetails(Set.of(detail));

        mockMvc.perform(post("/inventory/supplier-invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoice)))
                .andExpect(status().isOk())
                .andExpect(content().string("Supplier invoice processed successfully"));

        verify(inventoryDao, times(1)).update(any(Inventory.class));*/
    }

    @Test
    void testGetLowStockItems_success() throws Exception {
        Inventory lowStockItem = new Inventory();
        lowStockItem.setItemCode("LOW001");
        lowStockItem.setName("Low Stock Item");
        lowStockItem.setQuantity(2);
        lowStockItem.setReorderLevel(5);

        when(inventoryDao.findAll()).thenReturn(List.of(lowStockItem));

        mockMvc.perform(get("/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].itemCode").value("LOW001"));
    }

    /*@Test
    void testReorderItems_success() throws Exception {
        when(inventoryDao.findNeedingRestock()).thenReturn(List.of(inventory));

        mockMvc.perform(post("/inventory/reorder"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reorder requests sent for 1 items"));

        verify(emailService, times(1)).sendEmail(
                eq("supplier@example.com"),
                anyString(),
                contains(inventory.getName())
        );
    }*/
}