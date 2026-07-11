package ru.practice.subsidies.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

// TODO: переписать тесты в соответствии со спецификацией

@WebMvcTest(SubsidiesController.class)
@ExtendWith(MockitoExtension.class)
public class SubsidiesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void validate_get_api_v1_select_200() throws Exception {
        mockMvc.perform(get("/api/v1/select"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("select"));
    }

    @Test
    public void validate_post_api_v1_insert_200() throws Exception {
        mockMvc.perform(post("/api/v1/insert"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("insert"));
    }

    @Test
    public void validate_post_api_v1_batch_200() throws Exception {
        mockMvc.perform(post("/api/v1/batch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("batch"));
    }

    @Test
    public void validate_get_api_v1_search_200() throws Exception {
        mockMvc.perform(get("/api/v1/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("search"));
    }

    @Test
    public void validate_delete_api_v1_delete_200() throws Exception {
        mockMvc.perform(delete("/api/v1/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("delete"));
    }
}
