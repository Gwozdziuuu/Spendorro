// --- Utilities ---
class EventTypeStyler {
    static classFor(type) {
        const map = {
            API_REQUEST: 'type-request',
            API_RESPONSE: 'type-response',
            API_ERROR: 'type-error'
        };
        return map[type] || 'type-request';
    }
}

class Dom {
    static byId(id) {
        return document.getElementById(id);
    }

    static el(tag, opts = {}) {
        const e = document.createElement(tag);
        if (opts.className) e.className = opts.className;
        if (opts.html != null) e.innerHTML = opts.html;
        if (opts.text != null) e.textContent = opts.text;
        if (opts.attrs) {
            Object.entries(opts.attrs).forEach(([k, v]) => e.setAttribute(k, v));
        }
        return e;
    }
}

// --- Views ---
class SpinnerView {
    constructor(id) {
        this.el = Dom.byId(id);
    }

    show() {
        this.el.style.display = 'flex';
    }

    hide() {
        this.el.style.display = 'none';
    }
}

class ErrorView {
    constructor(wrapperId, msgId) {
        this.wrapper = Dom.byId(wrapperId);
        this.msg = Dom.byId(msgId);
    }

    show(message) {
        this.msg.textContent = message;
        this.wrapper.style.display = 'block';
    }

    hide() {
        this.wrapper.style.display = 'none';
        this.msg.textContent = '';
    }
}

class ModalView {
    constructor(modalId, contentId, closeBtnId) {
        this.modal = Dom.byId(modalId);
        this.content = Dom.byId(contentId);
        this.closeBtn = Dom.byId(closeBtnId);
        this._bind();
    }

    _bind() {
        this.closeBtn.addEventListener('click', () => this.close());
        document.addEventListener('click', (e) => {
            if (e.target === this.modal) {
                this.close();
            }
        });
    }

    open(html) {
        this.content.innerHTML = html;
        this.modal.classList.add('open');
        this.modal.setAttribute('aria-hidden', 'false');
    }

    close() {
        this.modal.classList.remove('open');
        this.modal.setAttribute('aria-hidden', 'true');
        this.content.innerHTML = '';
    }
}

class EventsTableView {
    constructor(tbodyId) {
        this.tbody = Dom.byId(tbodyId);
    }

    render(groups, handlers) {
        this.tbody.innerHTML = '';
        groups.forEach((g, i) => {
            const groupRow = this._groupRow(g, i, handlers.onToggle);
            const detailsRow = this._detailsRow(g, i, handlers.onEventClick);
            this.tbody.append(groupRow, detailsRow);
        });
    }

    _groupRow(group, index, onToggle) {
        const rowClass = group._isNew ? 'group-row new-group' : 'group-row';
        const row = Dom.el('tr', {
            className: rowClass,
            attrs: { 'data-index': String(index) }
        });

        const statusClass = group.status === 'SUCCESS' ? 'status-success' : 'status-failure';
        const eventCount = Array.isArray(group.events) ? group.events.length : 0;

        row.innerHTML = `
            <td><span class="expand-icon" id="expand-icon-${index}">▶</span></td>
            <td><strong>${group.methodName ?? 'Unknown'}</strong></td>
            <td><span class="status-badge ${statusClass}">${group.status ?? 'UNKNOWN'}</span></td>
            <td>${Number(group.duration ?? 0)}ms</td>
            <td>${eventCount} events</td>`;

        row.addEventListener('click', () => onToggle(index));
        return row;
    }

    _detailsRow(group, gIndex, onEventClick) {
        const row = Dom.el('tr', {
            className: 'event-details',
            attrs: { id: `details-${gIndex}` }
        });

        const items = (group.events ?? []).map((ev, eIndex) => {
            const typeClass = EventTypeStyler.classFor(ev.eventType);
            const when = ev.createdAt ? new Date(ev.createdAt).toLocaleString() : 'N/A';
            const item = Dom.el('div', { className: 'event-item' });

            item.innerHTML = `
                <div style="display:flex; align-items:center; gap:.5rem; margin-bottom:.5rem;">
                    <span class="event-type-label ${typeClass}">${ev.eventType ?? 'N/A'}</span>
                    <small>${when}</small>
                </div>
                <p style="margin:0;"><strong>Description:</strong> ${ev.description ?? 'N/A'}</p>`;

            item.addEventListener('click', () => onEventClick(gIndex, eIndex));
            return item;
        });

        const wrapper = Dom.el('td', { className: 'event-list' });
        const inner = Dom.el('div', { attrs: { style: 'padding:1rem;' } });
        inner.append(Dom.el('h4', { text: 'Events in this group:' }));

        if (items.length) {
            items.forEach(x => inner.appendChild(x));
        } else {
            inner.appendChild(Dom.el('p', { text: 'No events in this group' }));
        }

        wrapper.colSpan = 5;
        wrapper.appendChild(inner);
        row.appendChild(wrapper);
        return row;
    }

    toggle(index) {
        const detailsRow = Dom.byId(`details-${index}`);
        const icon = Dom.byId(`expand-icon-${index}`);
        const isOpen = detailsRow.classList.toggle('expanded');

        if (icon) {
            icon.textContent = isOpen ? '▼' : '▶';
            icon.classList.toggle('expanded', isOpen);
        }
    }
}

// --- Data / API ---
// API calls moved to api-client.js - using RestApiClient
class ApiClient extends RestApiClient {
    // Inherits all API methods from RestApiClient
}

// --- Application (orchestrator) ---
class App {
    constructor({ api, spinner, error, table, modal }) {
        this.api = api;
        this.spinner = spinner;
        this.error = error;
        this.table = table;
        this.modal = modal;
        this.state = { groups: [] };
        this.pollingInterval = null;
    }

    async init() {
        this.error.hide();
        this._showContainer(false);
        this.spinner.show();

        // Load banner
        this._loadBanner();

        try {
            const groups = await this.api.getEvents();
            if (!groups.length) {
                this.error.show('No event groups found');
                return;
            }

            this.state.groups = groups;
            this.table.render(groups, {
                onToggle: (i) => this.table.toggle(i),
                onEventClick: (gi, ei) => this.showEventDetails(gi, ei)
            });
            this._showContainer(true);
            
            // Start polling for new events
            this._startPolling();
        } catch (err) {
            this.error.show(`Error loading events: ${err.message}`);
        } finally {
            this.spinner.hide();
        }
    }


    _startPolling() {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
        }

        this.pollingInterval = setInterval(async () => {
            try {
                const groups = await this.api.getEvents();
                this._updateGroups(groups);
            } catch (error) {
                console.error('Polling failed:', error);
            }
        }, 5000); // Poll every 5 seconds
    }

    _updateGroups(newGroups) {
        // Simple approach: replace all groups and mark new ones
        const existingSerials = new Set(this.state.groups.map(g => g.serial));
        
        newGroups.forEach(group => {
            if (!existingSerials.has(group.serial)) {
                group._isNew = true; // Mark as new for styling
            }
        });

        this.state.groups = newGroups;
        this.table.render(this.state.groups, {
            onToggle: (i) => this.table.toggle(i),
            onEventClick: (gi, ei) => this.showEventDetails(gi, ei)
        });

        // Remove new flags after animation
        setTimeout(() => {
            this.state.groups.forEach(group => delete group._isNew);
            this.table.render(this.state.groups, {
                onToggle: (i) => this.table.toggle(i),
                onEventClick: (gi, ei) => this.showEventDetails(gi, ei)
            });
        }, 1000);
    }


    async _loadBanner() {
        try {
            const bannerText = await this.api.getBanner();
            const bannerContainer = Dom.byId('banner-container');
            if (bannerContainer && bannerText) {
                bannerContainer.textContent = bannerText;
                bannerContainer.style.display = 'block';
            }
        } catch (err) {
            console.warn('Could not load banner:', err.message);
        }
    }


    showEventDetails(groupIndex, eventIndex) {
        const g = this.state.groups[groupIndex];
        const e = g?.events?.[eventIndex];
        if (!g || !e) return;

        const typeClass = EventTypeStyler.classFor(e.eventType);
        const when = e.createdAt ? new Date(e.createdAt).toLocaleString() : 'N/A';
        const eventDataHtml = e.eventData ? 
            `<h4>Event Data:</h4><div class="event-data">${this._prettyJson(e.eventData)}</div>` : '';

        const html = `
            <div>
                <h3 id="event-details-title">Event Details</h3>
                <div style="display:flex; align-items:center; gap:.5rem; margin-bottom:1rem;">
                    <span class="event-type-label ${typeClass}">${e.eventType ?? 'N/A'}</span>
                    <small>${when}</small>
                </div>
                <p><strong>Description:</strong> ${e.description ?? 'N/A'}</p>
                <p><strong>Group:</strong> ${g.methodName ?? 'Unknown'}</p>
                <p><strong>Group Status:</strong> <span class="status-badge ${g.status === 'SUCCESS' ? 'status-success' : 'status-failure'}">${g.status ?? 'UNKNOWN'}</span></p>
                <p><strong>Group Duration:</strong> ${Number(g.duration ?? 0)}ms</p>
                ${eventDataHtml}
            </div>`;

        this.modal.open(html);
    }

    _showContainer(visible) {
        Dom.byId('events-container').style.display = visible ? 'block' : 'none';
    }

    _escapeHtml(str) {
        return String(str).replace(/[&<>"']/g, s => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        }[s]));
    }

    _prettyJson(raw) {
        try {
            const obj = typeof raw === 'string' ? JSON.parse(raw) : raw;
            return this._escapeHtml(JSON.stringify(obj, null, 2));
        } catch {
            return this._escapeHtml(String(raw));
        }
    }
}

// --- Bootstrap ---
document.addEventListener('DOMContentLoaded', () => {
    const app = new App({
        api: new ApiClient(),
        spinner: new SpinnerView('events-loading'),
        error: new ErrorView('events-error', 'error-message'),
        table: new EventsTableView('events-tbody'),
        modal: new ModalView('event-details-modal', 'modal-content', 'modal-close')
    });
    app.init();
});

// Clean up polling when page unloads
window.addEventListener('beforeunload', () => {
    if (window.app && window.app.pollingInterval) {
        clearInterval(window.app.pollingInterval);
    }
});