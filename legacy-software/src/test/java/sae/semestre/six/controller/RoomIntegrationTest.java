package sae.semestre.six.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sae.semestre.six.room.RoomController;
import sae.semestre.six.room.RoomService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomIntegrationTest {

    private MockMvc mockMvc;

    private RoomService roomService;

    @BeforeEach
    public void setup() {
        roomService = mock(RoomService.class);
        RoomController controller = new RoomController(roomService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void assignRoom_success() throws Exception {
        when(roomService.assignRoom(anyLong(), anyString())).thenReturn("Room assigned successfully");

        mockMvc.perform(post("/rooms/assign")
                        .param("appointmentId", "1")
                        .param("roomNumber", "101"))
                .andExpect(status().isOk())
                .andExpect(content().string("Room assigned successfully"));

        verify(roomService).assignRoom(1L, "101");
    }

    @Test
    void assignRoom_badRequest_whenIllegalArgument() throws Exception {
        when(roomService.assignRoom(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Only surgeons can use surgery rooms"));

        mockMvc.perform(post("/rooms/assign")
                        .param("appointmentId", "1")
                        .param("roomNumber", "Surgery1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Only surgeons can use surgery rooms"));

        verify(roomService).assignRoom(1L, "Surgery1");
    }

    @Test
    void assignRoom_internalServerError_whenUnexpectedException() throws Exception {
        when(roomService.assignRoom(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/rooms/assign")
                        .param("appointmentId", "1")
                        .param("roomNumber", "101"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error: Database error"));

        verify(roomService).assignRoom(1L, "101");
    }

    @Test
    void getRoomAvailability_success() throws Exception {
        Map<String, Object> availability = Map.of(
                "roomNumber", "101",
                "capacity", 5,
                "currentPatients", 3,
                "available", true
        );

        when(roomService.getRoomAvailability(anyString())).thenReturn(availability);

        mockMvc.perform(get("/rooms/availability")
                        .param("roomNumber", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"))
                .andExpect(jsonPath("$.capacity").value(5))
                .andExpect(jsonPath("$.currentPatients").value(3))
                .andExpect(jsonPath("$.available").value(true));

        verify(roomService).getRoomAvailability("101");
    }

    @Test
    void getRoomAvailability_badRequest_whenRoomNotFound() throws Exception {
        when(roomService.getRoomAvailability(anyString()))
                .thenThrow(new IllegalArgumentException("Room not found"));

        mockMvc.perform(get("/rooms/availability")
                        .param("roomNumber", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Room not found"));

        verify(roomService).getRoomAvailability("999");
    }
}
