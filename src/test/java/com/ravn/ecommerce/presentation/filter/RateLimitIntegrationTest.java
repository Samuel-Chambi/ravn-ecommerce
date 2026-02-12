package com.ravn.ecommerce.presentation.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRateLimiting() throws Exception {
        // Allowed 5 requests
        for (int i = 0; i < 5; i++) {
            int requestNum = i + 1;
            mockMvc.perform(get("/auth/reset-password")
                    .header("X-Forwarded-For", "127.0.0.1"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        if (status == 429) {
                            throw new AssertionError("Should not be rate limited yet at request " + requestNum);
                        }
                    });
        }

        // 6th request should be blocked
        mockMvc.perform(get("/auth/reset-password"))
                .andExpect(status().isTooManyRequests());
    }
}
