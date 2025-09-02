package com.mrngwozdz.behavioral;

import com.mrngwozdz.AbstractIntegrationTest;
import com.mrngwozdz.api.model.response.GetEventsResponse;
import com.mrngwozdz.controller.EventControllerUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
class EventBehavioralTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateCorrectNumberOfEventsFromMultipleGetRequests() {
        var initialEvent = EventControllerUtils.getEvents()
                .statusCode(200)
                .extract().as(GetEventsResponse.class);
        assertThat(initialEvent.events().size()).isEqualTo(1);

        EventControllerUtils.getEvents(5).statusCode(200);
        EventControllerUtils.getEvents(10).statusCode(200);

        var getEventsResponse = EventControllerUtils.getEvents().statusCode(200).extract().as(GetEventsResponse.class);
        assertThat(getEventsResponse.events().size()).isEqualTo(4);

        assertThat(getEventsResponse.events().get(2).methodName()).isEqualTo("getEvents");
        assertThat(getEventsResponse.events().get(2).status()).isEqualTo("SUCCESS");
        assertThat(getEventsResponse.events().get(2).events().size()).isEqualTo(2);

        verifyDurationTimesAreGreaterThanZero(getEventsResponse);
        verifyEventGroupsSortedByFirstEventTime(getEventsResponse);
        verifyEventsInGroupsSortedChronologically(getEventsResponse);
    }

    private void verifyDurationTimesAreGreaterThanZero(GetEventsResponse response) {
        response.events().stream()
                .filter(eventGroup -> eventGroup.events().size() >= 2)
                .forEach(eventGroup -> {
                    assertThat(eventGroup.duration()).isGreaterThan(0L);
                    eventGroup.events().forEach(event -> {
                        assertThat(event.createdAt()).isNotNull();
                    });
                });
    }

    private void verifyEventGroupsSortedByFirstEventTime(GetEventsResponse response) {
        for (int i = 0; i < response.events().size() - 1; i++) {
            var currentGroup = response.events().get(i);
            var nextGroup = response.events().get(i + 1);
            
            var currentFirstEventTime = currentGroup.events().get(0).createdAt();
            var nextFirstEventTime = nextGroup.events().get(0).createdAt();
            
            assertThat(currentFirstEventTime).isAfterOrEqualTo(nextFirstEventTime);
        }
    }

    private void verifyEventsInGroupsSortedChronologically(GetEventsResponse response) {
        response.events().forEach(eventGroup -> {
            var events = eventGroup.events();
            for (int i = 0; i < events.size() - 1; i++) {
                var currentEvent = events.get(i);
                var nextEvent = events.get(i + 1);
                assertThat(currentEvent.createdAt()).isBeforeOrEqualTo(nextEvent.createdAt());
            }
        });
    }

}