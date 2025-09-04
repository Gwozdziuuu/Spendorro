/**
 * ProcessMessage - matches com.mrngwozdz.api.model.response.ProcessMessage
 */
class ProcessMessage {
    constructor(data = {}) {
        this.value = data.value;             // String - The processed message value
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ProcessMessage;
} else if (typeof window !== 'undefined') {
    window.ProcessMessage = ProcessMessage;
}