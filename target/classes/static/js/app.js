(function () {
    const authSection = document.getElementById('auth-section');
    const appSection = document.getElementById('app-section');
    const loginBtn = document.getElementById('loginBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const loginStatus = document.getElementById('loginStatus');
    const whoami = document.getElementById('whoami');

    const booksTools = document.getElementById('books-tools');
    const booksTableBody = document.querySelector('#booksTable tbody');
    const booksStatus = document.getElementById('booksStatus');

    const issuanceTools = document.getElementById('issuance-tools');
    const selectBook = document.getElementById('selectBook');
    const selectUser = document.getElementById('selectUser');
    const loanDays = document.getElementById('loanDays');
    const issueBookBtn = document.getElementById('issueBookBtn');
    const issuancesTableBody = document.querySelector('#issuancesTable tbody');
    const issuancesStatus = document.getElementById('issuancesStatus');

    const addBookBtn = document.getElementById('addBookBtn');
    const newTitle = document.getElementById('newTitle');
    const newAuthor = document.getElementById('newAuthor');
    const newIsbn = document.getElementById('newIsbn');
    const newCategory = document.getElementById('newCategory');
    const newCopies = document.getElementById('newCopies');

    const API = {
        base: window.location.origin,
        token: null,
        role: null,
        async login(username, password) {
        const res = await fetch(`${this.base}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            if (!res.ok) throw new Error('Invalid credentials');
            const data = await res.json();
                this.token = data.token;
                localStorage.setItem('token', this.token);
                // decode JWT payload and set role/username in a robust way
            try {
                const payload = JSON.parse(atob(this.token.split('.')[1]));
                    // roles may be in `roles` (registration/login) or `auth` (older tokens)
                    if (payload.roles && payload.roles.length > 0) {
                        // roles may be strings or objects
                        const r = payload.roles[0];
                        this.role = (typeof r === 'string') ? r : (r.name || r || null);
                    } else if (payload.auth && payload.auth.length > 0) {
                        const a = payload.auth[0];
                        // auth entries may be {authority: 'ROLE_LIBRARIAN'} or strings
                        if (typeof a === 'string') this.role = a.replace('ROLE_', '');
                        else if (a.authority) this.role = a.authority.replace('ROLE_', '');
                        else this.role = null;
                    } else {
                        this.role = null;
                    }
                    this.usernameFromToken = payload.sub || payload.subject || null;
                } catch (_) {
                    this.role = null;
                    this.usernameFromToken = null;
                }
            return data;
        },
        async getBooks() {
                const headers = {};
                if (this.token) headers['Authorization'] = `Bearer ${this.token}`;
                const res = await fetch(`${this.base}/books`, { headers });
            if (!res.ok) throw new Error('Failed to load books');
            return res.json();
        },
        async getUsers() {
            const headers = {};
            if (this.token) headers['Authorization'] = `Bearer ${this.token}`;
            const res = await fetch(`${this.base}/users`, { headers });
            if (!res.ok) throw new Error('Failed to load users');
            return res.json();
        },
        async getIssuances() {
            const headers = {};
            if (this.token) headers['Authorization'] = `Bearer ${this.token}`;
            const res = await fetch(`${this.base}/issuances/active`, { headers });
            if (!res.ok) throw new Error('Failed to load issuances');
            return res.json();
        },
        async issueBook(userId, bookId, loanDays) {
            const headers = { 'Content-Type': 'application/json' };
            if (this.token) headers['Authorization'] = `Bearer ${this.token}`;
            const res = await fetch(`${this.base}/issuances`, {
                method: 'POST',
                headers,
                body: JSON.stringify({ userId, bookId, loanDays })
            });
            if (!res.ok) throw new Error('Failed to issue book');
            return res.json();
        },
        async returnIssuance(issuanceId) {
            const headers = {};
            if (this.token) headers['Authorization'] = `Bearer ${this.token}`;
            const res = await fetch(`${this.base}/issuances/${issuanceId}/return`, {
                method: 'POST',
                headers
            });
            if (!res.ok) throw new Error('Failed to return issuance');
            return res.json();
        },
        async deleteBook(id) {
            const headers = {};
            if (this.token) headers['Authorization'] = `Bearer ${this.token}`;
            const res = await fetch(`${this.base}/books/${id}`, {
                method: 'DELETE',
                headers
            });
            if (!res.ok && res.status !== 204) throw new Error('Failed to delete book');
            return true;
        },
        async addBook(book) {
            const headers = { 'Content-Type': 'application/json' };
            if (this.token) headers['Authorization'] = `Bearer ${this.token}`;
            const res = await fetch(`${this.base}/books`, {
                method: 'POST',
                headers,
                body: JSON.stringify(book)
            });
            if (!res.ok) throw new Error('Failed to add book');
            return res.json();
        }
    };

    function setStatus(el, message, type) {
        if (!el) return;
        el.textContent = message || '';
        el.classList.remove('success', 'error');
        if (type === 'success') el.classList.add('success');
        if (type === 'error') el.classList.add('error');
    }

    function setLoggedInUI(username) {
        authSection.classList.add('hidden');
        appSection.classList.remove('hidden');
        whoami.textContent = `Signed in as ${username || API.usernameFromToken || 'user'}`;
        // show book management tools only to librarians
        const isLibrarian = API.role === 'LIBRARIAN';
        booksTools.classList.toggle('hidden', !isLibrarian);
        issuanceTools.classList.toggle('hidden', !isLibrarian);
    }

    function setLoggedOutUI() {
        appSection.classList.add('hidden');
        authSection.classList.remove('hidden');
        loginStatus.textContent = '';
        booksTableBody.innerHTML = '';
        booksStatus.textContent = '';
        localStorage.removeItem('token');
        API.token = null;
    }

    async function refreshBooks() {
        booksStatus.textContent = 'Loading books...';
        try {
            const books = await API.getBooks();
            booksTableBody.innerHTML = '';
            selectBook.innerHTML = '';
            books.forEach(b => {
                const opt = document.createElement('option');
                opt.value = b.id;
                opt.textContent = `${b.title} (${b.availableCopies}/${b.totalCopies})`;
                selectBook.appendChild(opt);
                const tr = document.createElement('tr');
                const actions = document.createElement('td');
                // if librarian, add a delete button
                if (API.role === 'LIBRARIAN') {
                    const del = document.createElement('button');
                    del.textContent = 'Delete';
                    del.className = 'danger';
                    del.addEventListener('click', async () => {
                        if (!confirm(`Delete book '${b.title}'?`)) return;
                        setStatus(booksStatus, 'Deleting...', null);
                        del.disabled = true;
                        try {
                            await API.deleteBook(b.id);
                            await refreshBooks();
                            setStatus(booksStatus, 'Book deleted.', 'success');
                        } catch (e) {
                            setStatus(booksStatus, e.message || 'Failed to delete book', 'error');
                        }
                        del.disabled = false;
                    });
                    actions.appendChild(del);
                } else {
                    actions.textContent = '-';
                }

                tr.innerHTML = `
                    <td>${b.id}</td>
                    <td>${b.title}</td>
                    <td>${b.author || ''}</td>
                    <td>${b.isbn || ''}</td>
                    <td>${b.availableCopies}/${b.totalCopies}</td>
                `;
                tr.appendChild(actions);
                booksTableBody.appendChild(tr);
            });
            booksStatus.textContent = `Loaded ${books.length} book(s).`;
            try { await refreshIssuances(); } catch (_) {}
        } catch (e) {
            booksStatus.textContent = e.message || 'Error loading books';
        }
    }

    async function refreshUsers() {
        try {
            const users = await API.getUsers();
            selectUser.innerHTML = '';
            users.forEach(u => {
                const opt = document.createElement('option');
                opt.value = u.id;
                opt.textContent = `${u.username} (${Array.from(u.roles || []).join(',')})`;
                selectUser.appendChild(opt);
            });
        } catch (e) {
            // swallow if not authorized
        }
    }

    async function refreshIssuances() {
        issuancesStatus.textContent = 'Loading issuances...';
        try {
            const issuances = await API.getIssuances();
            issuancesTableBody.innerHTML = '';
            issuances.forEach(i => {
                const tr = document.createElement('tr');
                const actions = document.createElement('td');
                if (API.role === 'LIBRARIAN') {
                    const ret = document.createElement('button');
                    ret.textContent = 'Return';
                    ret.addEventListener('click', async () => {
                        issuancesStatus.textContent = 'Returning...';
                        try {
                            await API.returnIssuance(i.id);
                            await refreshIssuances();
                            await refreshBooks();
                            issuancesStatus.textContent = 'Book returned.';
                        } catch (e) {
                            issuancesStatus.textContent = e.message || 'Failed to return book';
                        }
                    });
                    actions.appendChild(ret);
                }

                tr.innerHTML = `
                    <td>${i.id}</td>
                    <td>${i.book.title}</td>
                    <td>${i.user.username}</td>
                    <td>${i.issuedDate}</td>
                    <td>${i.dueDate}</td>
                    <td>${i.returnedDate || ''}</td>
                    <td>${i.fineAmount || 0}</td>
                `;
                tr.appendChild(actions);
                issuancesTableBody.appendChild(tr);
            });
            issuancesStatus.textContent = `Loaded ${issuances.length} issuance(s).`;
        } catch (e) {
            issuancesStatus.textContent = e.message || 'Error loading issuances';
        }
    }

    loginBtn.addEventListener('click', async () => {
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        setStatus(loginStatus, 'Signing in...', null);
        try {
            await API.login(username, password);
            setLoggedInUI(username);
            await refreshBooks();
            setStatus(loginStatus, '', null);
        } catch (e) {
            setStatus(loginStatus, e.message || 'Login failed', 'error');
        }
    });

    logoutBtn.addEventListener('click', () => {
        setLoggedOutUI();
    });

    addBookBtn.addEventListener('click', async () => {
        const book = {
            title: newTitle.value.trim(),
            author: newAuthor.value.trim(),
            isbn: newIsbn.value.trim(),
            category: newCategory.value.trim(),
            totalCopies: parseInt(newCopies.value || '1', 10),
            availableCopies: parseInt(newCopies.value || '1', 10)
        };
        setStatus(booksStatus, 'Adding book...', null);
        addBookBtn.disabled = true;
        try {
            await API.addBook(book);
            newTitle.value = '';
            newAuthor.value = '';
            newIsbn.value = '';
            newCategory.value = '';
            newCopies.value = '1';
            await refreshBooks();
            setStatus(booksStatus, 'Book added.', 'success');
        } catch (e) {
            setStatus(booksStatus, e.message || 'Failed to add book (are you librarian?)', 'error');
        }
        addBookBtn.disabled = false;
    });

    issueBookBtn.addEventListener('click', async () => {
        const userId = parseInt(selectUser.value, 10);
        const bookId = parseInt(selectBook.value, 10);
        const days = parseInt(loanDays.value || '14', 10);
        setStatus(issuancesStatus, 'Issuing...', null);
        issueBookBtn.disabled = true;
        try {
            await API.issueBook(userId, bookId, days);
            await refreshIssuances();
            await refreshBooks();
            setStatus(issuancesStatus, 'Book issued.', 'success');
        } catch (e) {
            setStatus(issuancesStatus, e.message || 'Failed to issue book', 'error');
        }
        issueBookBtn.disabled = false;
    });

    // Auto-login if token exists
    (async function initFromStorage() {
        const token = localStorage.getItem('token');
        if (token) {
            API.token = token;
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                if (payload.roles && payload.roles.length > 0) {
                    const r = payload.roles[0];
                    API.role = (typeof r === 'string') ? r : (r.name || r || null);
                }
                API.usernameFromToken = payload.sub || payload.subject || null;
            } catch (_) {
                API.role = null;
                API.usernameFromToken = null;
            }
            setLoggedInUI(API.usernameFromToken || 'user');
            try { await refreshBooks(); await refreshUsers(); await refreshIssuances(); } catch (_) {}
        }
    })();
})();


