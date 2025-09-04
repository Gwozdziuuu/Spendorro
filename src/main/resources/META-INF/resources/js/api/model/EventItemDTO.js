/**
 * EventItemDTO - matches com.mrngwozdz.api.model.EventItemDTO
 */
class EventItemDTO {
    constructor(data = {}) {
        this.eventType = data.eventType;     // String - Type of the event (e.g., "API_REQUEST")
        this.description = data.description; // String - Description of the event
        this.eventData = data.eventData;     // String - JSON data containing event details
        this.createdAt = data.createdAt;     // Instant - Timestamp when the event was created
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = EventItemDTO;
} else if (typeof window !== 'undefined') {
    window.EventItemDTO = EventItemDTO;
}