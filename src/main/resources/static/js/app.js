// ===========================================================
// Leave Ledger — frontend application logic
// Talks to the Spring Boot API served from the same origin.
// ===========================================================

const state = {
  token: localStorage.getItem('ll_token') || null,
  user: JSON.parse(localStorage.getItem('ll_user') || 'null'),
};

// ---------- API helper ----------
async function api(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (state.token) headers['Authorization'] = `Bearer ${state.token}`;

  const res = await fetch(path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  let data = null;
  const text = await res.text();
  if (text) {
    try { data = JSON.parse(text); } catch (e) { data = null; }
  }

  if (!res.ok) {
    const message = (data && data.message) ? data.message
      : (res.status === 403 || res.status === 401) ? 'You are not authorized to do that.'
      : `Request failed (${res.status})`;
    throw new Error(message);
  }
  return data;
}

// ---------- session ----------
function setSession(authResponse) {
  state.token = authResponse.token;
  state.user = {
    id: authResponse.userId,
    fullName: authResponse.fullName,
    email: authResponse.email,
    role: authResponse.role,
  };
  localStorage.setItem('ll_token', state.token);
  localStorage.setItem('ll_user', JSON.stringify(state.user));
}

function clearSession() {
  state.token = null;
  state.user = null;
  localStorage.removeItem('ll_token');
  localStorage.removeItem('ll_user');
}

// ---------- view switching ----------
const views = {
  auth: document.getElementById('view-auth'),
  employee: document.getElementById('view-employee'),
  manager: document.getElementById('view-manager'),
};

function showView(name) {
  Object.entries(views).forEach(([key, el]) => el.classList.toggle('hidden', key !== name));
}

function renderIdentity() {
  const identityEl = document.getElementById('identity');
  if (!state.user) {
    identityEl.classList.add('hidden');
    return;
  }
  identityEl.classList.remove('hidden');
  document.getElementById('identity-name').textContent = state.user.fullName;
  document.getElementById('identity-role').textContent = state.user.role;
}

function route() {
  renderIdentity();
  if (!state.token || !state.user) {
    showView('auth');
    return;
  }
  if (state.user.role === 'MANAGER') {
    showView('manager');
    loadManagerLeaves();
  } else {
    showView('employee');
    loadEmployeeDashboard();
  }
}

// ===========================================================
// AUTH
// ===========================================================
const loginFormWrap = document.getElementById('login-form-wrap');
const registerFormWrap = document.getElementById('register-form-wrap');
const authError = document.getElementById('auth-error');

document.getElementById('show-register').addEventListener('click', () => {
  loginFormWrap.classList.add('hidden');
  registerFormWrap.classList.remove('hidden');
  authError.classList.add('hidden');
});
document.getElementById('show-login').addEventListener('click', () => {
  registerFormWrap.classList.add('hidden');
  loginFormWrap.classList.remove('hidden');
  authError.classList.add('hidden');
});

function showAuthError(message) {
  authError.textContent = message;
  authError.classList.remove('hidden');
}

document.getElementById('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  authError.classList.add('hidden');
  const email = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;
  try {
    const data = await api('/api/auth/login', { method: 'POST', body: { email, password } });
    setSession(data);
    route();
  } catch (err) {
    showAuthError(err.message || 'Could not sign in. Check your email and password.');
  }
});

document.getElementById('register-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  authError.classList.add('hidden');
  const body = {
    fullName: document.getElementById('reg-fullname').value.trim(),
    email: document.getElementById('reg-email').value.trim(),
    password: document.getElementById('reg-password').value,
    department: document.getElementById('reg-department').value.trim(),
    role: document.getElementById('reg-role').value,
  };
  try {
    const data = await api('/api/auth/register', { method: 'POST', body });
    setSession(data);
    route();
  } catch (err) {
    showAuthError(err.message || 'Could not create the account.');
  }
});

document.getElementById('logout-btn').addEventListener('click', () => {
  clearSession();
  route();
});

// ===========================================================
// EMPLOYEE VIEW
// ===========================================================
const applyForm = document.getElementById('apply-form');
const toggleApplyBtn = document.getElementById('toggle-apply');

toggleApplyBtn.addEventListener('click', () => {
  applyForm.classList.toggle('hidden');
  toggleApplyBtn.textContent = applyForm.classList.contains('hidden') ? '+ New request' : '\u2212 Close';
});

applyForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const feedback = document.getElementById('apply-feedback');
  feedback.textContent = 'Submitting...';
  feedback.className = 'feedback';

  const body = {
    leaveType: document.getElementById('apply-type').value,
    startDate: document.getElementById('apply-start').value,
    endDate: document.getElementById('apply-end').value,
    reason: document.getElementById('apply-reason').value.trim(),
  };

  try {
    await api('/api/employee/leaves', { method: 'POST', body });
    feedback.textContent = 'Request submitted.';
    feedback.className = 'feedback success';
    applyForm.reset();
    await loadEmployeeDashboard();
    setTimeout(() => {
      applyForm.classList.add('hidden');
      toggleApplyBtn.textContent = '+ New request';
      feedback.textContent = '';
    }, 1200);
  } catch (err) {
    feedback.textContent = err.message || 'Could not submit this request.';
    feedback.className = 'feedback error';
  }
});

async function loadEmployeeDashboard() {
  document.getElementById('employee-greeting').textContent = `Good day, ${firstName(state.user.fullName)}.`;

  try {
    const balance = await api('/api/employee/leaves/balance');
    document.getElementById('stat-remaining').textContent = balance.remainingLeaveBalance;
    document.getElementById('stat-used').textContent = balance.usedLeaveBalance;
    document.getElementById('stat-pending').textContent = balance.pendingRequestsCount;
    document.getElementById('stat-total').textContent = balance.totalLeaveBalance;
  } catch (err) {
    console.error(err);
  }

  try {
    const history = await api('/api/employee/leaves');
    renderEmployeeLedger(history);
  } catch (err) {
    console.error(err);
  }
}

function renderEmployeeLedger(requests) {
  const container = document.getElementById('employee-ledger');
  if (!requests.length) {
    container.innerHTML = '<p class="empty-note">Nothing recorded yet. Your first request will show up here.</p>';
    return;
  }

  container.innerHTML = requests.map((r) => `
    <div class="ledger-row status-${r.status.toLowerCase()}">
      <div class="ledger-main">
        <span class="ledger-dates">${formatRange(r.startDate, r.endDate)} &nbsp;&middot;&nbsp; ${r.numberOfDays} day${r.numberOfDays > 1 ? 's' : ''}</span>
        <span class="ledger-meta">${titleCase(r.leaveType)}${r.reason ? ' — ' + escapeHtml(r.reason) : ''}</span>
        ${r.managerComment ? `<span class="ledger-reason">Manager note: ${escapeHtml(r.managerComment)}</span>` : ''}
      </div>
      <div class="ledger-side">
        <span class="status-tag">${r.status}</span>
        ${r.status === 'PENDING' ? `<button class="ghost-btn" data-cancel="${r.id}">Cancel</button>` : ''}
      </div>
    </div>
  `).join('');

  container.querySelectorAll('[data-cancel]').forEach((btn) => {
    btn.addEventListener('click', async () => {
      btn.disabled = true;
      try {
        await api(`/api/employee/leaves/${btn.dataset.cancel}/cancel`, { method: 'PATCH' });
        await loadEmployeeDashboard();
      } catch (err) {
        alert(err.message || 'Could not cancel this request.');
        btn.disabled = false;
      }
    });
  });
}

// ===========================================================
// MANAGER VIEW
// ===========================================================
let currentFilter = '';

document.querySelectorAll('.filter-btn').forEach((btn) => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.filter-btn').forEach((b) => b.classList.remove('active'));
    btn.classList.add('active');
    currentFilter = btn.dataset.status;
    loadManagerLeaves();
  });
});

async function loadManagerLeaves() {
  document.getElementById('manager-greeting').textContent = `Requests to review, ${firstName(state.user.fullName)}.`;
  try {
    const query = currentFilter ? `?status=${currentFilter}` : '';
    const requests = await api(`/api/manager/leaves${query}`);
    renderManagerLedger(requests);
  } catch (err) {
    console.error(err);
  }
}

function renderManagerLedger(requests) {
  const container = document.getElementById('manager-ledger');
  if (!requests.length) {
    container.innerHTML = '<p class="empty-note">No requests to show for this filter.</p>';
    return;
  }

  container.innerHTML = requests.map((r) => `
    <div class="ledger-row status-${r.status.toLowerCase()}">
      <div class="ledger-main">
        <span class="ledger-dates">${escapeHtml(r.employeeName)} &nbsp;&middot;&nbsp; ${escapeHtml(r.department || '')}</span>
        <span class="ledger-meta">${formatRange(r.startDate, r.endDate)} &nbsp;&middot;&nbsp; ${titleCase(r.leaveType)} &nbsp;&middot;&nbsp; ${r.numberOfDays} day${r.numberOfDays > 1 ? 's' : ''}</span>
        ${r.reason ? `<span class="ledger-reason">"${escapeHtml(r.reason)}"</span>` : ''}
      </div>
      <div class="ledger-side">
        <span class="status-tag">${r.status}</span>
        ${r.status === 'PENDING' ? `
          <button class="ghost-btn approve" data-approve="${r.id}">Approve</button>
          <button class="ghost-btn reject" data-reject="${r.id}">Reject</button>
        ` : ''}
      </div>
    </div>
  `).join('');

  container.querySelectorAll('[data-approve]').forEach((btn) => {
    btn.addEventListener('click', () => reviewRequest(btn.dataset.approve, true));
  });
  container.querySelectorAll('[data-reject]').forEach((btn) => {
    btn.addEventListener('click', () => reviewRequest(btn.dataset.reject, false));
  });
}

async function reviewRequest(id, approve) {
  const comment = prompt(approve ? 'Add a note for this approval (optional):' : 'Why is this being rejected? (optional)') || '';
  try {
    await api(`/api/manager/leaves/${id}/review`, { method: 'PATCH', body: { approve, comment } });
    await loadManagerLeaves();
  } catch (err) {
    alert(err.message || 'Could not save this decision.');
  }
}

// ---------- small formatting helpers ----------
function firstName(fullName) { return (fullName || '').split(' ')[0]; }
function titleCase(s) { return s ? s.charAt(0) + s.slice(1).toLowerCase() : s; }
function formatRange(start, end) {
  const opts = { month: 'short', day: 'numeric' };
  const s = new Date(start + 'T00:00:00').toLocaleDateString(undefined, opts);
  const e = new Date(end + 'T00:00:00').toLocaleDateString(undefined, opts);
  return start === end ? s : `${s} \u2013 ${e}`;
}
function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

// ---------- boot ----------
route();