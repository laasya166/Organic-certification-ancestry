package com.soa.certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soa.certificate.domain.CertificateStatus;
import com.soa.certificate.dto.CreateCertificateRequest;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CertificateServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Fixed demo date 2026-10-27: Lot A and B valid, C has NO_CERTIFICATE, D is EXPIRED")
    void testValidityOnDemoDate() throws Exception {
        mockMvc.perform(get("/certificates/validity")
                        .param("lotIds", "A,B,C,D")
                        .param("on", "2026-10-27"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lotId").value("A"))
                .andExpect(jsonPath("$[0].valid").value(true))
                .andExpect(jsonPath("$[0].reason").isEmpty())
                .andExpect(jsonPath("$[1].lotId").value("B"))
                .andExpect(jsonPath("$[1].valid").value(true))
                .andExpect(jsonPath("$[1].reason").isEmpty())
                .andExpect(jsonPath("$[2].lotId").value("C"))
                .andExpect(jsonPath("$[2].valid").value(false))
                .andExpect(jsonPath("$[2].reason").value("NO_CERTIFICATE"))
                .andExpect(jsonPath("$[3].lotId").value("D"))
                .andExpect(jsonPath("$[3].valid").value(false))
                .andExpect(jsonPath("$[3].reason").value("EXPIRED"));
    }

    @Test
    @DisplayName("Boundary tests: validFrom and validTo are inclusive")
    void testBoundaryDates() throws Exception {
        // Lot A is valid 2026-01-01..2026-12-31
        mockMvc.perform(get("/certificates/validity")
                        .param("lotIds", "A")
                        .param("on", "2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valid").value(true));

        mockMvc.perform(get("/certificates/validity")
                        .param("lotIds", "A")
                        .param("on", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valid").value(true));

        mockMvc.perform(get("/certificates/validity")
                        .param("lotIds", "A")
                        .param("on", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valid").value(false))
                .andExpect(jsonPath("$[0].reason").value("NOT_YET_VALID"));

        mockMvc.perform(get("/certificates/validity")
                        .param("lotIds", "A")
                        .param("on", "2027-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valid").value(false))
                .andExpect(jsonPath("$[0].reason").value("EXPIRED"));
    }

    @Test
    @DisplayName("Create certificate and revoke certificate flow")
    void testCreateAndRevokeFlow() throws Exception {
        CreateCertificateRequest createRequest = new CreateCertificateRequest(
                "TEST-LOT-1",
                "Certifier X",
                LocalDate.parse("2026-05-01"),
                LocalDate.parse("2026-11-01"),
                CertificateStatus.ACTIVE
        );

        String responseContent = mockMvc.perform(post("/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.lotId").value("TEST-LOT-1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(responseContent).get("id").asLong();

        // Check validity before revoke on 2026-10-27
        mockMvc.perform(get("/certificates/validity")
                        .param("lotIds", "TEST-LOT-1")
                        .param("on", "2026-10-27"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valid").value(true));

        // Revoke certificate
        mockMvc.perform(post("/certificates/" + id + "/revoke"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));

        // Check validity after revoke on 2026-10-27
        mockMvc.perform(get("/certificates/validity")
                        .param("lotIds", "TEST-LOT-1")
                        .param("on", "2026-10-27"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valid").value(false))
                .andExpect(jsonPath("$[0].reason").value("REVOKED"));
    }
}
