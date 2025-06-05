package sae.semestre.six.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sae.semestre.six.inventory.Inventory;
import sae.semestre.six.inventory.InventoryController;
import sae.semestre.six.inventory.InventoryService;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryControllerTest {

    private MockMvc mockMvc;

    private InventoryService inventoryService;

    @BeforeEach
    public void setup() {
        inventoryService = mock(InventoryService.class);
        InventoryController inventoryController = new InventoryController(inventoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
    }

    @Test
    void testProcessSupplierInvoiceSuccess() throws Exception {
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

        doNothing().when(inventoryService).processSupplierInvoice(any());

        mockMvc.perform(post("/inventory/supplier-invoice")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Supplier invoice processed successfully"));
    }

    @Test
    void testProcessSupplierInvoiceError() throws Exception {
        mockMvc.perform(post("/inventory/supplier-invoice")
                        .contentType(APPLICATION_JSON)
                        .content("")) // contenu vide = erreur JSON
                .andExpect(status().isBadRequest()); // 400
    }

    @Test
    void testGetLowStockItemsSuccess() throws Exception {
        Inventory item = new Inventory();
        item.setName("Item 1");
        item.setQuantity(1);
        item.setReorderLevel(5);
        item.setLastRestocked(LocalDateTime.now());

        when(inventoryService.getLowStockItems()).thenReturn(List.of(item));

        mockMvc.perform(get("/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Item 1"));
    }

    @Test
    void testReorderItemsCorrectResponse() throws Exception {
        when(inventoryService.reorderItems()).thenReturn(1);

        mockMvc.perform(post("/inventory/reorder"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reorder requests sent for 1 items"));
    }

    @Test
    void testReorderItemsCallsService() throws Exception {
        when(inventoryService.reorderItems()).thenReturn(2);

        mockMvc.perform(post("/inventory/reorder"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reorder requests sent for 2 items"));

        verify(inventoryService, times(1)).reorderItems();
    }
}
