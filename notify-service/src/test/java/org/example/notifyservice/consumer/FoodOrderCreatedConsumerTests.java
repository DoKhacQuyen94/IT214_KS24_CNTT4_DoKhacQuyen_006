package org.example.notifyservice.consumer;

import org.example.notifyservice.service.EmailService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FoodOrderCreatedConsumerTests {

    @Test
    void delegatesMessageToEmailService() {
        EmailService emailService = mock(EmailService.class);
        FoodOrderCreatedConsumer consumer = new FoodOrderCreatedConsumer(emailService);

        consumer.consume("customer@example.com");

        verify(emailService).sendFoodOrderCreatedEmail("customer@example.com");
    }
}
