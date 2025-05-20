package sae.semestre.six.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sae.semestre.six.inventory.Inventory;
import sae.semestre.six.inventory.InventoryDao;
import sae.semestre.six.inventory.InventoryController;
import sae.semestre.six.service.EmailService;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryIntegrationTest {

    private MockMvc mockMvc;

    private InventoryDao inventoryDao;
    private EmailService emailService;
    private InventoryController inventoryController;

    @BeforeEach
    public void setup() throws Exception {
        inventoryDao = mock(InventoryDao.class);
        emailService = mock(EmailService.class);
        inventoryController = new InventoryController();

        // Injection par réflexion
        var daoField = InventoryController.class.getDeclaredField("inventoryDao");
        daoField.setAccessible(true);
        daoField.set(inventoryController, inventoryDao);

        var mailField = InventoryController.class.getDeclaredField("emailService");
        mailField.setAccessible(true);
        mailField.set(inventoryController, emailService);

        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
    }

    // ---------------------------------------------------
    // /inventory/supplier-invoice
    // ---------------------------------------------------

    @Test
    void testProcessSupplierInvoice_success() throws Exception {
        String json = """
        {
          "details": [
            {
              "quantity": 5,
              "unitPrice": 10.0,
              "inventory": {
                "itemCode": "ITEM001",
                "quantity": 10,
                "unitPrice": 9.0
              }
            }
          ]
        }
        """;

        mockMvc.perform(post("/inventory/supplier-invoice")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Supplier invoice processed successfully"));
    }

    @Test
    void testProcessSupplierInvoice_error() throws Exception {
        mockMvc.perform(post("/inventory/supplier-invoice")
                        .contentType(APPLICATION_JSON)
                        .content("")) // contenu vide = erreur JSON
                .andExpect(status().isBadRequest()); // 400
    }

    // ---------------------------------------------------
    // /inventory/low-stock
    // ---------------------------------------------------

    @Test
    void testGetLowStockItems_success() throws Exception {
        Inventory item = new Inventory();
        item.setName("Item 1");
        item.setQuantity(1);
        item.setReorderLevel(5);
        item.setLastRestocked(LocalDateTime.now());

        when(inventoryDao.findAll()).thenReturn(List.of(item));

        mockMvc.perform(get("/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------------------------------------------------
    // /inventory/reorder
    // ---------------------------------------------------

    @Test
    void testReorderItems_basicResponse() throws Exception {
        Inventory item = new Inventory();
        item.setName("Item A");
        item.setItemCode("A001");
        item.setReorderLevel(10);

        when(inventoryDao.findNeedingRestock()).thenReturn(List.of(item));

        mockMvc.perform(post("/inventory/reorder"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Reorder requests sent")));
    }

    @Test
    void testReorderItems_sendEmailAndDaoInteraction() throws Exception {
        // Préparer les données
        var item = new sae.semestre.six.inventory.Inventory();
        item.setItemCode("CODE123");
        item.setName("Test Item");
        item.setReorderLevel(5);
        item.setQuantity(3);

        when(inventoryDao.findNeedingRestock()).thenReturn(List.of(item));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        mockMvc.perform(post("/inventory/reorder"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Reorder requests sent for 1 items")));

        verify(inventoryDao, times(1)).findNeedingRestock();
        verify(emailService, times(1)).sendEmail(
                eq("supplier@example.com"),
                eq("Reorder Request"),
                argThat(body -> body.contains("Test Item"))
        );
    }
}
