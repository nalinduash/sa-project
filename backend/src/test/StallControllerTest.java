package com.bookfair.controller;

import com.bookfair.dto.StallDTO;
import com.bookfair.service.StallService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StallController.class)
class StallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StallService stallService;

    @Test
    void testGetAllStalls_Returns200OK() throws Exception {
        // Limit: Only verify the endpoint works and returns 200 status
        when(stallService.getAllStalls()).thenReturn(List.of(new StallDTO()));
        
        mockMvc.perform(get("/api/stalls"))
                .andExpect(status().isOk());
    }
}