/**
 * API Index - Barrel export for all API classes
 * This file provides a single import point for all API-related classes
 */

// Import all model classes
// Note: This works in Node.js environment. For browser, use script tags in HTML.

// Model DTOs
const EventItemDTO = require('./model/EventItemDTO.js');
const EventGroupDTO = require('./model/EventGroupDTO.js');

// Request models
const MessageRequest = require('./model/request/MessageRequest.js');
const FileUploadRequest = require('./model/request/FileUploadRequest.js');

// Response models
const ProcessMessage = require('./model/response/ProcessMessage.js');
const ProcessMessageResponse = require('./model/response/ProcessMessageResponse.js');
const GetEventsResponse = require('./model/response/GetEventsResponse.js');

// API Client
const { RestApiClient, API_ENDPOINTS, HTTP_METHODS, CONTENT_TYPES } = require('../api-client.js');

// Export all
module.exports = {
    // DTOs
    EventItemDTO,
    EventGroupDTO,
    
    // Requests
    MessageRequest,
    FileUploadRequest,
    
    // Responses
    ProcessMessage,
    ProcessMessageResponse,
    GetEventsResponse,
    
    // API Client and constants
    RestApiClient,
    API_ENDPOINTS,
    HTTP_METHODS,
    CONTENT_TYPES
};