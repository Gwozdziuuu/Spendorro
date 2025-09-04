/**
 * MessageRequest - matches com.mrngwozdz.api.model.request.MessageRequest
 */
class MessageRequest {
    constructor(text) {
        this.text = text;                    // String - The message text
    }

    toJSON() {
        return {
            text: this.text
        };
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = MessageRequest;
} else if (typeof window !== 'undefined') {
    window.MessageRequest = MessageRequest;
}