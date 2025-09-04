/**
 * GetEventsResponse - matches com.mrngwozdz.api.model.response.GetEventsResponse
 */
class GetEventsResponse {
    constructor(data = {}) {
        this.events = (data.events || []).map(e => {
            // Check if EventGroupDTO is available (browser or Node.js)
            if (typeof EventGroupDTO !== 'undefined') {
                return new EventGroupDTO(e);
            } else if (typeof require !== 'undefined') {
                const EventGroupDTO = require('../EventGroupDTO.js');
                return new EventGroupDTO(e);
            }
            return e; // Fallback to raw object
        }); // List<EventGroupDTO>
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = GetEventsResponse;
} else if (typeof window !== 'undefined') {
    window.GetEventsResponse = GetEventsResponse;
}