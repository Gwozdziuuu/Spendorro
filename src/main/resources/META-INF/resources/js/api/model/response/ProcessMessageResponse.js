/**
 * ProcessMessageResponse - matches com.mrngwozdz.api.model.response.ProcessMessageResponse
 */
class ProcessMessageResponse {
    constructor(data = {}) {
        this.processMessage = data.processMessage ? (() => {
            // Check if ProcessMessage is available (browser or Node.js)
            if (typeof ProcessMessage !== 'undefined') {
                return new ProcessMessage(data.processMessage);
            } else if (typeof require !== 'undefined') {
                const ProcessMessage = require('./ProcessMessage.js');
                return new ProcessMessage(data.processMessage);
            }
            return data.processMessage; // Fallback to raw object
        })() : null;
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ProcessMessageResponse;
} else if (typeof window !== 'undefined') {
    window.ProcessMessageResponse = ProcessMessageResponse;
}