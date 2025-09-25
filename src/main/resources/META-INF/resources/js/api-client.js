// REST API Client - All API calls extracted from the application
class RestApiClient {

    /**
     * Get banner text from the server
     * @returns {Promise<string>} Banner text
     */
    async getBanner() {
        const response = await fetch('/banner');
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return await response.text();
    }

    // ========== Chat/Message API ==========

    /**
     * Send a text message to the server
     * @param {string} text - The message text to send
     * @returns {Promise<Object>} Response from server
     */
    async sendMessage(text) {
        const messageRequest = new MessageRequest(text);
        
        const response = await fetch('/message', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(messageRequest.toJSON())
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    }

    /**
     * Upload a file with optional text message
     * @param {File} file - The file to upload
     * @param {string} text - Optional text to send with the file
     * @param {string} fileName - Optional file name
     * @returns {Promise<Object>} Response from server
     */
    async uploadFile(file, text = null, fileName = null) {
        const uploadRequest = new FileUploadRequest(file, text, fileName || file.name);
        const formData = uploadRequest.toFormData();

        const response = await fetch('/upload', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    }
}

// ========== API Model Objects ==========
// All model objects have been extracted to separate files in /api/model/
// Load them via script tags in HTML or require() in Node.js

// ========== API Endpoints Configuration ==========

const API_ENDPOINTS = {
    BANNER: '/banner',

    // Chat/Message endpoints
    MESSAGE: '/api/message',
    UPLOAD: '/api/upload'
};

const HTTP_METHODS = {
    GET: 'GET',
    POST: 'POST',
    PUT: 'PUT',
    DELETE: 'DELETE'
};

const CONTENT_TYPES = {
    JSON: 'application/json',
    FORM_DATA: 'multipart/form-data'
};

// ========== Export ==========
// For use in other modules if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        RestApiClient,
        API_ENDPOINTS,
        HTTP_METHODS,
        CONTENT_TYPES
    };
} else if (typeof window !== 'undefined') {
    window.RestApiClient = RestApiClient;
    window.API_ENDPOINTS = API_ENDPOINTS;
    window.HTTP_METHODS = HTTP_METHODS;
    window.CONTENT_TYPES = CONTENT_TYPES;
}