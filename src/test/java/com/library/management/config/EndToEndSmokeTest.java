package com.library.management.config;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EndToEndSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateListAndFetchBook() throws Exception {
        String body = """
                {
                  "title": "Effective Java",
                  "author": "Joshua Bloch",
                  "genre": "Software",
                  "isbn": "978-0134685991",
                  "publisher": "Addison-Wesley",
                  "publishedYear": 2018,
                  "totalCopies": 5,
                  "availableCopies": 5,
                  "price": 49.99,
                  "description": "Best practices for the Java platform"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        Integer id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(get("/api/v1/books/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Effective Java"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));

        mockMvc.perform(get("/api/v1/books")
                        .param("title", "Effective")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].isbn").value("978-0134685991"));
    }

    @Test
    void shouldExposeHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
