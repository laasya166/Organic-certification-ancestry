package com.soa.ancestry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soa.ancestry.dto.CreateBlendRequest;
import com.soa.ancestry.dto.CreateLotRequest;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"soa.s03.t08.events", "soa.s03.t08.events.DLT"})
@DirtiesContext
class LotAncestryServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Seeded lots exist and can be retrieved")
    void testSeededLots() throws Exception {
        mockMvc.perform(get("/lots/A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("A"))
                .andExpect(jsonPath("$.product").value("oats"))
                .andExpect(jsonPath("$.quantityKg").value(500.0))
                .andExpect(jsonPath("$.ownerUsername").value("proc1"));

        mockMvc.perform(get("/lots/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Create lot endpoint works")
    void testCreateLot() throws Exception {
        CreateLotRequest request = new CreateLotRequest("E", "sugar", 50.0, "proc1");
        mockMvc.perform(post("/lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("E"))
                .andExpect(jsonPath("$.product").value("sugar"))
                .andExpect(jsonPath("$.quantityKg").value(50.0));
    }

    @Test
    @DisplayName("Create blend validates empty inputs with 400 and unknown lots with 404")
    void testCreateBlendValidation() throws Exception {
        // Empty inputs -> 400
        CreateBlendRequest emptyInputs = new CreateBlendRequest(
                List.of(),
                LocalDate.parse("2026-10-27"),
                new CreateBlendRequest.OutputLotDto("X0", "test", 100.0),
                null
        );
        mockMvc.perform(post("/blends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyInputs)))
                .andExpect(status().isBadRequest());

        // Unknown input lot -> 404
        CreateBlendRequest unknownLot = new CreateBlendRequest(
                List.of("A", "NON_EXISTENT_LOT"),
                LocalDate.parse("2026-10-27"),
                new CreateBlendRequest.OutputLotDto("X0", "test", 100.0),
                null
        );
        mockMvc.perform(post("/blends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownLot)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Create blend saves ancestry and returns BlendResponse")
    void testCreateBlendSuccess() throws Exception {
        CreateBlendRequest request = new CreateBlendRequest(
                List.of("A", "B"),
                LocalDate.parse("2026-10-27"),
                new CreateBlendRequest.OutputLotDto("X1", "Organic Muesli", 700.0),
                null
        );

        String responseContent = mockMvc.perform(post("/blends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.outputLotId").value("X1"))
                .andExpect(jsonPath("$.processedAt").value("2026-10-27"))
                .andExpect(jsonPath("$.inputLotIds[0]").value("A"))
                .andExpect(jsonPath("$.inputLotIds[1]").value("B"))
                .andReturn().getResponse().getContentAsString();

        Long blendId = objectMapper.readTree(responseContent).get("id").asLong();

        // Check GET /blends/{id}
        mockMvc.perform(get("/blends/" + blendId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(blendId))
                .andExpect(jsonPath("$.outputLotId").value("X1"))
                .andExpect(jsonPath("$.inputLotIds[0]").value("A"))
                .andExpect(jsonPath("$.inputLotIds[1]").value("B"));
    }
}
