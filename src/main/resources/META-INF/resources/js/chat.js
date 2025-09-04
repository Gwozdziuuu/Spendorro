// Chat functionality
class ChatApp {
    constructor() {
        this.messagesContainer = document.getElementById('messages');
        this.messageInput = document.getElementById('messageInput');
        this.sendButton = document.getElementById('sendButton');
        this.statusContainer = document.getElementById('status');
        this.fileInput = document.getElementById('fileInput');
        this.attachButton = document.getElementById('attachButton');
        this.filePreview = document.getElementById('filePreview');
        this.previewImage = document.getElementById('previewImage');
        this.fileName = document.getElementById('fileName');
        this.fileSize = document.getElementById('fileSize');
        this.removeFileButton = document.getElementById('removeFile');
        
        this.selectedFile = null;
        this.apiClient = new RestApiClient();
        this.initializeEventListeners();
        this.messageInput.focus();
    }

    initializeEventListeners() {
        this.sendButton.addEventListener('click', () => {
            this.sendMessage();
        });

        // Enter key to send message
        this.messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        // Auto-resize input
        this.messageInput.addEventListener('input', () => {
            this.messageInput.style.height = 'auto';
            this.messageInput.style.height = this.messageInput.scrollHeight + 'px';
        });

        // File attachment
        this.attachButton.addEventListener('click', () => {
            this.fileInput.click();
        });

        this.fileInput.addEventListener('change', (e) => {
            this.handleFileSelect(e.target.files[0]);
        });

        this.removeFileButton.addEventListener('click', () => {
            this.clearSelectedFile();
        });
    }

    async sendMessage() {
        const text = this.messageInput.value.trim();
        const hasFile = this.selectedFile !== null;
        
        // Need at least text or file
        if (!text && !hasFile) return;

        // Disable input while sending
        this.setLoading(true);

        // Show user message in UI
        if (hasFile) {
            const imageUrl = URL.createObjectURL(this.selectedFile);
            this.addImageMessage(imageUrl, text, 'user');
        } else {
            this.addMessage(text, 'user');
        }

        // Clear input
        this.messageInput.value = '';
        this.messageInput.style.height = 'auto';

        try {
            let result;

            if (hasFile) {
                // Send with file upload using API client
                result = await this.apiClient.uploadFile(this.selectedFile, text);
            } else {
                // Send text only using API client
                result = await this.apiClient.sendMessage(text);
            }
            console.log('API Response:', result); // Debug log
            
            // Add bot response to UI
            if (result.processMessage && result.processMessage.value) {
                this.addMessage(result.processMessage.value, 'bot');
            } else if (result.payload) {
                this.addMessage(result.payload, 'bot');
            } else if (result.message) {
                this.addMessage(result.message, 'bot');
            } else {
                this.addMessage('Otrzymałem twoją wiadomość!', 'bot');
            }

            // Clear file selection after successful send
            if (hasFile) {
                this.clearSelectedFile();
            }

        } catch (error) {
            console.error('Error sending message:', error);
            this.addErrorMessage('Błąd podczas wysyłania wiadomości: ' + error.message);
        } finally {
            this.setLoading(false);
            this.messageInput.focus();
        }
    }

    addMessage(text, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}-message`;

        const bubbleDiv = document.createElement('div');
        bubbleDiv.className = 'message-bubble';
        bubbleDiv.textContent = text;

        const timeDiv = document.createElement('div');
        timeDiv.className = 'message-time';
        timeDiv.textContent = this.getCurrentTime();

        messageDiv.appendChild(bubbleDiv);
        messageDiv.appendChild(timeDiv);

        this.messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();
    }

    addErrorMessage(text) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        errorDiv.textContent = text;

        this.messagesContainer.appendChild(errorDiv);
        this.scrollToBottom();
    }

    getCurrentTime() {
        const now = new Date();
        return now.toLocaleTimeString('pl-PL', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
    }

    scrollToBottom() {
        this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
    }

    setLoading(loading) {
        this.sendButton.disabled = loading;
        this.messageInput.disabled = loading;
        this.attachButton.disabled = loading;

        if (loading) {
            this.statusContainer.style.display = 'block';
            this.sendButton.textContent = '...';
        } else {
            this.statusContainer.style.display = 'none';
            this.sendButton.textContent = 'Wyślij';
        }
    }

    handleFileSelect(file) {
        if (!file) return;

        // Validate file type
        if (!file.type.startsWith('image/')) {
            this.addErrorMessage('Proszę wybrać plik obrazu (JPG, PNG, GIF, etc.)');
            return;
        }

        // Validate file size (max 10MB)
        const maxSize = 10 * 1024 * 1024;
        if (file.size > maxSize) {
            this.addErrorMessage('Plik jest za duży. Maksymalny rozmiar to 10MB.');
            return;
        }

        this.selectedFile = file;
        this.showFilePreview(file);
    }

    showFilePreview(file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            this.previewImage.src = e.target.result;
            this.fileName.textContent = file.name;
            this.fileSize.textContent = this.formatFileSize(file.size);
            this.filePreview.style.display = 'block';
        };
        reader.readAsDataURL(file);
    }

    clearSelectedFile() {
        this.selectedFile = null;
        this.fileInput.value = '';
        this.filePreview.style.display = 'none';
        this.previewImage.src = '';
        this.fileName.textContent = '';
        this.fileSize.textContent = '';
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    addImageMessage(imageUrl, text, sender = 'user') {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}-message`;

        const bubbleDiv = document.createElement('div');
        bubbleDiv.className = 'message-bubble';

        // Add image
        const img = document.createElement('img');
        img.src = imageUrl;
        img.className = 'message-image';
        img.alt = 'Uploaded image';
        bubbleDiv.appendChild(img);

        // Add text if provided
        if (text && text.trim()) {
            const textDiv = document.createElement('div');
            textDiv.textContent = text;
            textDiv.style.marginTop = '0.5rem';
            bubbleDiv.appendChild(textDiv);
        }

        const timeDiv = document.createElement('div');
        timeDiv.className = 'message-time';
        timeDiv.textContent = this.getCurrentTime();

        messageDiv.appendChild(bubbleDiv);
        messageDiv.appendChild(timeDiv);

        this.messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();
    }
}

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new ChatApp();
});