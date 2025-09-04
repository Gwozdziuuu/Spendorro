/**
 * EventGroupDTO - matches com.mrngwozdz.api.model.EventGroupDTO
 */
class EventGroupDTO {
    constructor(data = {}) {
        this.serial = data.serial;           // UUID - Unique identifier for grouping related events
        this.methodName = data.methodName;   // String - Name of the method that was executed
        this.duration = data.duration;       // Long - Total duration of the request in milliseconds
        this.status = data.status;           // String - Status of the request execution ("SUCCESS" or "FAILURE")
        this.events = (data.events || []).map(e => {
            // Check if EventItemDTO is available (browser or Node.js)
            if (typeof EventItemDTO !== 'undefined') {
                return new EventItemDTO(e);
            } else if (typeof require !== 'undefined') {
                const EventItemDTO = require('./EventItemDTO.js');
                return new EventItemDTO(e);
            }
            return e; // Fallback to raw object
        }); // List<EventItemDTO>
        this._isNew = data._isNew || false;  // UI flag (not in Java model)
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = EventGroupDTO;
} else if (typeof window !== 'undefined') {
    window.EventGroupDTO = EventGroupDTO;
}