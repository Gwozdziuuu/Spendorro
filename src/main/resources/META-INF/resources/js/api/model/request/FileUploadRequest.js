/**
 * FileUploadRequest - matches com.mrngwozdz.api.model.request.FileUploadRequest
 */
class FileUploadRequest {
    constructor(file, text = null, fileName = null) {
        this.file = file;                    // InputStream - The file input stream
        this.text = text;                    // String - Optional text to send with the file
        this.fileName = fileName;            // String - Name of the file
    }

    toFormData() {
        const formData = new FormData();
        formData.append('file', this.file);
        if (this.text) {
            formData.append('text', this.text);
        }
        if (this.fileName) {
            formData.append('fileName', this.fileName);
        }
        return formData;
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = FileUploadRequest;
} else if (typeof window !== 'undefined') {
    window.FileUploadRequest = FileUploadRequest;
}