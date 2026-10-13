/**
 * dashboard.js — Front-End Application Controller
 * TrackMyWallet | Connected to Spring Boot REST Endpoints
 */

// Global Application State
let currentUser = null;
let categoriesList = [];
let trendChartInstance = null;
let categoryChartInstance = null;
let reportChartInstance = null;
let pendingDeleteAction = null;

// ============================================================
//  INITIALIZATION & AUTH GUARD
// ============================================================
document.addEventListener('DOMContentLoaded', async () => {
  // 1. Authenticate Current Session
  const isAuthenticated = await checkAuthSession();
  if (!isAuthenticated) return;

  // 2. Set default dates in form inputs
  const todayStr = new Date().toISOString().split('T')[0];
  const expenseDateInput = document.getElementById('expenseFormDate');
  if (expenseDateInput) expenseDateInput.value = todayStr;

  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;
  const budgetYearInput = document.getElementById('budgetFormYear');
  const budgetMonthSelect = document.getElementById('budgetFormMonth');
  if (budgetYearInput) budgetYearInput.value = currentYear;
  if (budgetMonthSelect) budgetMonthSelect.value = currentMonth;

  // 3. Set default report dates (1st of this month to today)
  setReportPeriod('thisMonth');

  // 4. Load initial application data
  await loadCategories();
  await loadDashboardSummary();
  await loadBudgets();
  await loadExpenses();
});

/**
 * Verify session with GET /api/auth/me. Redirect to login if unauthenticated.
 */
async function checkAuthSession() {
  try {
    const res = await fetch('/api/auth/me', { credentials: 'include' });
    if (!res.ok) {
      window.location.replace('/login.html');
      return false;
    }
    const json = await res.json();
    if (!json.success || !json.data) {
      window.location.replace('/login.html');
      return false;
    }

    currentUser = json.data;
    renderUserProfile(currentUser);
    return true;
  } catch (err) {
    window.location.replace('/login.html');
    return false;
  }
}

function renderUserProfile(user) {
  const fullNameEl = document.getElementById('userFullName');
  const usernameEl = document.getElementById('userUsername');
  const avatarEl = document.getElementById('userAvatar');

  if (fullNameEl) fullNameEl.textContent = user.fullName || user.username;
  if (usernameEl) usernameEl.textContent = '@' + user.username;
  if (avatarEl) {
    const initial = (user.fullName || user.username || 'U').trim().charAt(0).toUpperCase();
    avatarEl.textContent = initial;
  }
}

// ============================================================
//  NAVIGATION & SECTION SWITCHING
// ============================================================
function switchSection(sectionId) {
  const sections = ['overview', 'expenses', 'budgets', 'categories', 'reports'];

  sections.forEach(sec => {
    const secEl = document.getElementById('section' + capitalize(sec));
    const navBtn = document.getElementById('nav' + capitalize(sec));
    if (secEl) secEl.style.display = (sec === sectionId) ? 'block' : 'none';
    if (navBtn) {
      if (sec === sectionId) navBtn.classList.add('active');
      else navBtn.classList.remove('active');
    }
  });

  const pageTitle = document.getElementById('pageTitle');
  const pageSubtitle = document.getElementById('pageSubtitle');

  switch (sectionId) {
    case 'overview':
      if (pageTitle) pageTitle.textContent = 'Dashboard Overview';
      if (pageSubtitle) pageSubtitle.textContent = 'Track your financial pulse and monthly cash flow';
      loadDashboardSummary();
      break;
    case 'expenses':
      if (pageTitle) pageTitle.textContent = 'Expenses Management';
      if (pageSubtitle) pageSubtitle.textContent = 'View, filter, edit and record your day-to-day spending';
      loadExpenses();
      break;
    case 'budgets':
      if (pageTitle) pageTitle.textContent = 'Monthly Budget Planner';
      if (pageSubtitle) pageSubtitle.textContent = 'Plan monthly limits and monitor spending thresholds';
      loadBudgets();
      break;
    case 'categories':
      if (pageTitle) pageTitle.textContent = 'Category Manager';
      if (pageSubtitle) pageSubtitle.textContent = 'Customize your expense categories, icons, and colors';
      loadCategories();
      break;
    case 'reports':
      if (pageTitle) pageTitle.textContent = 'Reports & Financial Analytics';
      if (pageSubtitle) pageSubtitle.textContent = 'Analyze date-range summaries and export detailed CSV reports';
      loadReport();
      break;
  }

  // Close mobile sidebar if open
  const sidebar = document.getElementById('appSidebar');
  const overlay = document.getElementById('sidebarOverlay');
  if (sidebar) sidebar.classList.remove('open');
  if (overlay) overlay.classList.remove('open');
}

function toggleSidebar() {
  const sidebar = document.getElementById('appSidebar');
  const overlay = document.getElementById('sidebarOverlay');
  if (sidebar) sidebar.classList.toggle('open');
  if (overlay) overlay.classList.toggle('open');
}

// ============================================================
//  CATEGORY SERVICE & UI
// ============================================================
async function loadCategories() {
  try {
    const res = await fetch('/api/categories', { credentials: 'include' });
    const json = await res.json();
    if (!json.success) return;

    categoriesList = json.data || [];
    populateCategoryDropdowns(categoriesList);
    renderCategoryCards(categoriesList);
  } catch (err) {
    console.error('Failed to load categories', err);
  }
}

function populateCategoryDropdowns(categories) {
  const formCatSelect = document.getElementById('expenseFormCategory');
  const filterCatSelect = document.getElementById('expenseCategoryFilter');
  const reportCatSelect = document.getElementById('reportCategoryFilter');

  if (formCatSelect) {
    const currentVal = formCatSelect.value;
    formCatSelect.innerHTML = '<option value="">Select a category</option>';
    categories.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c.categoryId;
      opt.textContent = `${c.icon || '🏷️'} ${c.name}`;
      formCatSelect.appendChild(opt);
    });
    if (currentVal) formCatSelect.value = currentVal;
  }

  if (filterCatSelect) {
    const currentVal = filterCatSelect.value;
    filterCatSelect.innerHTML = '<option value="">All Categories</option>';
    categories.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c.categoryId;
      opt.textContent = `${c.icon || '🏷️'} ${c.name}`;
      filterCatSelect.appendChild(opt);
    });
    if (currentVal) filterCatSelect.value = currentVal;
  }

  if (reportCatSelect) {
    const currentVal = reportCatSelect.value;
    reportCatSelect.innerHTML = '<option value="">All Categories</option>';
    categories.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c.categoryId;
      opt.textContent = `${c.icon || '🏷️'} ${c.name}`;
      reportCatSelect.appendChild(opt);
    });
    if (currentVal) reportCatSelect.value = currentVal;
  }
}

function renderCategoryCards(categories) {
  const grid = document.getElementById('categoryCardsGrid');
  if (!grid) return;

  if (categories.length === 0) {
    grid.innerHTML = '<div class="card-loading">No categories found. Click "+ Add New Category" above to create one.</div>';
    return;
  }

  grid.innerHTML = '';
  categories.forEach(c => {
    const card = document.createElement('div');
    card.className = 'category-card glass-panel';
    card.innerHTML = `
      <div class="cat-card-top">
        <div class="cat-icon-frame" style="background: ${hexToRgba(c.color || '#7c3aed', 0.15)}; border: 1px solid ${hexToRgba(c.color || '#7c3aed', 0.35)};">
          ${c.icon || '🏷️'}
        </div>
        <span class="cat-color-dot" style="background: ${c.color || '#7c3aed'};"></span>
      </div>
      <div>
        <div class="cat-name">${escapeHtml(c.name)}</div>
        <div class="cat-expense-count">${c.expenseCount || 0} recorded expense${c.expenseCount === 1 ? '' : 's'}</div>
      </div>
      <div class="cat-card-footer">
        <button class="btn-icon-action" onclick="openEditCategoryModal(${c.categoryId}, '${escapeHtml(c.name)}', '${c.icon || '🏷️'}', '${c.color || '#7c3aed'}')">✏️ Edit</button>
        <button class="btn-icon-action btn-icon-delete" onclick="promptDeleteCategory(${c.categoryId}, '${escapeHtml(c.name)}')">🗑️ Delete</button>
      </div>
    `;
    grid.appendChild(card);
  });
}

function openCategoryModal() {
  document.getElementById('categoryFormId').value = '';
  document.getElementById('categoryFormName').value = '';
  document.getElementById('categoryFormIcon').value = '🏷️';
  document.getElementById('categoryFormColor').value = '#7c3aed';
  document.getElementById('categoryModalTitle').textContent = 'Add New Category';
  openModal('categoryModal');
}

function openEditCategoryModal(id, name, icon, color) {
  document.getElementById('categoryFormId').value = id;
  document.getElementById('categoryFormName').value = name;
  document.getElementById('categoryFormIcon').value = icon || '🏷️';
  document.getElementById('categoryFormColor').value = color || '#7c3aed';
  document.getElementById('categoryModalTitle').textContent = 'Edit Category';
  openModal('categoryModal');
}

async function saveCategory(event) {
  event.preventDefault();
  const id = document.getElementById('categoryFormId').value;
  const name = document.getElementById('categoryFormName').value.trim();
  const icon = document.getElementById('categoryFormIcon').value.trim();
  const color = document.getElementById('categoryFormColor').value;

  if (!name) {
    showToast('Category name is required', 'error');
    return;
  }

  const payload = { name, icon, color };
  const url = id ? `/api/categories/${id}` : '/api/categories';
  const method = id ? 'PUT' : 'POST';

  try {
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(payload)
    });
    const json = await res.json();

    if (json.success) {
      showToast(json.message || 'Category saved successfully', 'success');
      closeModal('categoryModal');
      await loadCategories();
      await loadDashboardSummary();
    } else {
      showToast(json.message || 'Failed to save category', 'error');
    }
  } catch (err) {
    showToast('Network error while saving category', 'error');
  }
}

function promptDeleteCategory(categoryId, categoryName) {
  pendingDeleteAction = async () => {
    try {
      const res = await fetch(`/api/categories/${categoryId}`, {
        method: 'DELETE',
        credentials: 'include'
      });
      const json = await res.json();
      if (json.success) {
        showToast(json.message || 'Category deleted', 'success');
        await loadCategories();
        await loadDashboardSummary();
      } else {
        showToast(json.message || 'Cannot delete category', 'error');
      }
    } catch (err) {
      showToast('Network error deleting category', 'error');
    }
  };

  document.getElementById('deleteModalMessage').textContent =
    `Are you sure you want to delete the category "${categoryName}"? Categories linked to existing expenses cannot be deleted.`;
  openModal('deleteModal');
}

// ============================================================
//  EXPENSE SERVICE & UI
// ============================================================
async function loadExpenses() {
  const catFilter = document.getElementById('expenseCategoryFilter')?.value || '';
  const startDate = document.getElementById('expenseStartDate')?.value || '';
  const endDate = document.getElementById('expenseEndDate')?.value || '';
  const keyword = document.getElementById('expenseSearchInput')?.value.trim() || '';

  const params = new URLSearchParams();
  if (catFilter) params.append('categoryId', catFilter);
  if (startDate) params.append('startDate', startDate);
  if (endDate) params.append('endDate', endDate);
  if (keyword) params.append('keyword', keyword);

  const tbody = document.getElementById('expensesTableTbody');
  if (tbody) tbody.innerHTML = '<tr><td colspan="5" class="table-loading">Loading expenses...</td></tr>';

  try {
    const res = await fetch(`/api/expenses?${params.toString()}`, { credentials: 'include' });
    const json = await res.json();

    if (!json.success) {
      if (tbody) tbody.innerHTML = `<tr><td colspan="5" class="table-loading">Error loading expenses</td></tr>`;
      return;
    }

    const expenses = json.data || [];
    renderExpensesTable(expenses);

    // Update header stats
    const titleEl = document.getElementById('expensesCountTitle');
    const subtitleEl = document.getElementById('expensesTotalSubtitle');
    const totalAmount = expenses.reduce((sum, e) => sum + Number(e.amount || 0), 0);

    if (titleEl) titleEl.textContent = `All Expenses (${expenses.length})`;
    if (subtitleEl) subtitleEl.textContent = `Total in view: ₹${formatCurrency(totalAmount)}`;
  } catch (err) {
    if (tbody) tbody.innerHTML = '<tr><td colspan="5" class="table-loading">Network error loading expenses</td></tr>';
  }
}

function renderExpensesTable(expenses) {
  const tbody = document.getElementById('expensesTableTbody');
  if (!tbody) return;

  if (expenses.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" class="table-loading">No expenses found matching the criteria. Click "+ Add Expense" to record one!</td></tr>';
    return;
  }

  tbody.innerHTML = '';
  expenses.forEach(e => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${formatDate(e.expenseDate)}</td>
      <td>
        <span class="category-pill" style="background: ${hexToRgba(e.categoryColor || '#7c3aed', 0.15)}; border-color: ${hexToRgba(e.categoryColor || '#7c3aed', 0.35)};">
          <span>${e.categoryIcon || '🏷️'}</span>
          <span>${escapeHtml(e.categoryName || 'Uncategorized')}</span>
        </span>
      </td>
      <td>${escapeHtml(e.description || '—')}</td>
      <td class="amount-cell">₹${formatCurrency(e.amount)}</td>
      <td>
        <div class="actions-cell">
          <button class="btn-icon-action" onclick="openEditExpenseModal(${e.expenseId})">✏️ Edit</button>
          <button class="btn-icon-action btn-icon-delete" onclick="promptDeleteExpense(${e.expenseId}, '${formatCurrency(e.amount)}')">🗑️ Delete</button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function handleExpenseFilter() {
  loadExpenses();
}

function resetExpenseFilters() {
  const cat = document.getElementById('expenseCategoryFilter');
  const start = document.getElementById('expenseStartDate');
  const end = document.getElementById('expenseEndDate');
  const search = document.getElementById('expenseSearchInput');

  if (cat) cat.value = '';
  if (start) start.value = '';
  if (end) end.value = '';
  if (search) search.value = '';

  loadExpenses();
}

function openExpenseModal() {
  document.getElementById('expenseFormId').value = '';
  document.getElementById('expenseFormAmount').value = '';
  document.getElementById('expenseFormCategory').value = '';
  document.getElementById('expenseFormDate').value = new Date().toISOString().split('T')[0];
  document.getElementById('expenseFormDescription').value = '';
  document.getElementById('expenseModalTitle').textContent = 'Add New Expense';
  openModal('expenseModal');
}

async function openEditExpenseModal(id) {
  try {
    const res = await fetch(`/api/expenses/${id}`, { credentials: 'include' });
    const json = await res.json();
    if (!json.success || !json.data) {
      showToast('Could not find expense details', 'error');
      return;
    }
    const e = json.data;
    document.getElementById('expenseFormId').value = e.expenseId;
    document.getElementById('expenseFormAmount').value = e.amount;
    document.getElementById('expenseFormCategory').value = e.categoryId;
    document.getElementById('expenseFormDate').value = e.expenseDate;
    document.getElementById('expenseFormDescription').value = e.description || '';
    document.getElementById('expenseModalTitle').textContent = 'Edit Expense';
    openModal('expenseModal');
  } catch (err) {
    showToast('Failed to load expense for editing', 'error');
  }
}

async function saveExpense(event) {
  event.preventDefault();
  const id = document.getElementById('expenseFormId').value;
  const amount = document.getElementById('expenseFormAmount').value;
  const categoryId = document.getElementById('expenseFormCategory').value;
  const expenseDate = document.getElementById('expenseFormDate').value;
  const description = document.getElementById('expenseFormDescription').value;

  if (!amount || Number(amount) <= 0) {
    showToast('Amount must be greater than zero', 'error');
    return;
  }
  if (!categoryId) {
    showToast('Please select a category', 'error');
    return;
  }
  if (!expenseDate) {
    showToast('Expense date is required', 'error');
    return;
  }

  const payload = {
    amount: Number(amount),
    categoryId: Number(categoryId),
    expenseDate,
    description
  };

  const url = id ? `/api/expenses/${id}` : '/api/expenses';
  const method = id ? 'PUT' : 'POST';

  try {
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(payload)
    });
    const json = await res.json();

    if (json.success) {
      showToast(json.message || 'Expense saved successfully', 'success');
      closeModal('expenseModal');
      await loadExpenses();
      await loadDashboardSummary();
      await loadBudgets();
    } else {
      showToast(json.message || 'Failed to save expense', 'error');
    }
  } catch (err) {
    showToast('Network error saving expense', 'error');
  }
}

function promptDeleteExpense(expenseId, amountFormatted) {
  pendingDeleteAction = async () => {
    try {
      const res = await fetch(`/api/expenses/${expenseId}`, {
        method: 'DELETE',
        credentials: 'include'
      });
      const json = await res.json();
      if (json.success) {
        showToast(json.message || 'Expense deleted', 'success');
        await loadExpenses();
        await loadDashboardSummary();
        await loadBudgets();
      } else {
        showToast(json.message || 'Failed to delete expense', 'error');
      }
    } catch (err) {
      showToast('Network error deleting expense', 'error');
    }
  };

  document.getElementById('deleteModalMessage').textContent =
    `Are you sure you want to delete this expense of ₹${amountFormatted}?`;
  openModal('deleteModal');
}

// ============================================================
//  DASHBOARD OVERVIEW & ANALYTICS
// ============================================================
async function loadDashboardSummary() {
  try {
    const res = await fetch('/api/dashboard/summary', { credentials: 'include' });
    const json = await res.json();
    if (!json.success || !json.data) return;

    const data = json.data;

    // Period badge
    const badgeEl = document.getElementById('currentPeriodBadge');
    if (badgeEl) badgeEl.textContent = `📅 ${data.periodLabel || 'Current Month'}`;

    // 1. Month Spent Card
    const monthSpentEl = document.getElementById('cardMonthSpent');
    if (monthSpentEl) monthSpentEl.textContent = `₹${formatCurrency(data.totalExpensesThisMonth)}`;

    // 2. Budget Card in Overview
    const budgetValueEl = document.getElementById('cardBudgetValue');
    const budgetProgressFill = document.getElementById('cardBudgetProgressFill');
    const budgetPercentEl = document.getElementById('cardBudgetPercent');
    const budgetRemainingEl = document.getElementById('cardBudgetRemaining');
    const budgetIconBadge = document.getElementById('budgetStatusIcon');

    if (data.budget) {
      const b = data.budget;
      if (budgetValueEl) budgetValueEl.textContent = `₹${formatCurrency(b.amount)}`;
      const pct = Math.min(100, Math.max(0, b.percentageUsed || 0));
      if (budgetProgressFill) {
        budgetProgressFill.style.width = `${pct}%`;
        budgetProgressFill.className = 'budget-progress-fill' + (b.isExceeded ? ' exceeded' : (b.isNearLimit ? ' warning' : ''));
      }
      if (budgetPercentEl) budgetPercentEl.textContent = `${b.percentageUsed}% used`;
      if (budgetRemainingEl) {
        budgetRemainingEl.textContent = b.isExceeded
          ? `Over by ₹${formatCurrency(Math.abs(b.remainingAmount))}`
          : `Left: ₹${formatCurrency(b.remainingAmount)}`;
      }
      if (budgetIconBadge) {
        budgetIconBadge.textContent = b.isExceeded ? '⚠️' : (b.isNearLimit ? '🔔' : '🎯');
      }
    } else {
      if (budgetValueEl) budgetValueEl.textContent = 'No budget set';
      if (budgetProgressFill) budgetProgressFill.style.width = '0%';
      if (budgetPercentEl) budgetPercentEl.textContent = '0% used';
      if (budgetRemainingEl) budgetRemainingEl.textContent = 'Set a monthly limit';
    }

    // 3. Highest Expense Card
    const highExpEl = document.getElementById('cardHighestExpense');
    const highExpFooter = document.getElementById('cardHighestExpenseFooter');
    if (data.highestExpenseThisMonth) {
      const h = data.highestExpenseThisMonth;
      if (highExpEl) highExpEl.textContent = `₹${formatCurrency(h.amount)}`;
      if (highExpFooter) highExpFooter.textContent = `${h.categoryIcon || '🏷️'} ${h.categoryName} (${h.description || formatDate(h.expenseDate)})`;
    } else {
      if (highExpEl) highExpEl.textContent = '₹0.00';
      if (highExpFooter) highExpFooter.textContent = 'No expenses this month';
    }

    // 4. Total Transactions Card
    const totalTxnEl = document.getElementById('cardTotalTransactions');
    const lifetimeEl = document.getElementById('cardLifetimeTotal');
    if (totalTxnEl) totalTxnEl.textContent = data.expensesCountThisMonth || '0';
    if (lifetimeEl) lifetimeEl.textContent = `Lifetime: ₹${formatCurrency(data.totalExpensesLifetime)} (${data.totalExpensesCount || 0} total)`;

    // 5. Recent Transactions Table
    renderRecentExpenses(data.recentExpenses || []);

    // 6. Charts
    renderTrendChart(data.monthlyTrends || []);
    renderCategoryChart(data.categoryBreakdown || []);
  } catch (err) {
    console.error('Failed to load dashboard summary', err);
  }
}

function renderRecentExpenses(expenses) {
  const tbody = document.getElementById('recentExpensesTbody');
  if (!tbody) return;

  if (expenses.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" class="table-loading">No recent transactions recorded.</td></tr>';
    return;
  }

  tbody.innerHTML = '';
  expenses.slice(0, 5).forEach(e => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${formatDate(e.expenseDate)}</td>
      <td>
        <span class="category-pill" style="background: ${hexToRgba(e.categoryColor || '#7c3aed', 0.15)}; border-color: ${hexToRgba(e.categoryColor || '#7c3aed', 0.35)};">
          <span>${e.categoryIcon || '🏷️'}</span>
          <span>${escapeHtml(e.categoryName)}</span>
        </span>
      </td>
      <td>${escapeHtml(e.description || '—')}</td>
      <td class="amount-cell">₹${formatCurrency(e.amount)}</td>
      <td>
        <div class="actions-cell">
          <button class="btn-icon-action" onclick="openEditExpenseModal(${e.expenseId})">✏️</button>
          <button class="btn-icon-action btn-icon-delete" onclick="promptDeleteExpense(${e.expenseId}, '${formatCurrency(e.amount)}')">🗑️</button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function renderTrendChart(trends) {
  const canvas = document.getElementById('trendChartCanvas');
  const emptyState = document.getElementById('trendChartEmpty');
  if (!canvas) return;

  if (!trends || trends.length === 0) {
    if (emptyState) emptyState.style.display = 'block';
    canvas.style.display = 'none';
    return;
  }

  if (emptyState) emptyState.style.display = 'none';
  canvas.style.display = 'block';

  const labels = trends.map(t => t.monthLabel);
  const values = trends.map(t => Number(t.totalAmount || 0));

  if (trendChartInstance) trendChartInstance.destroy();

  if (typeof Chart === 'undefined') {
    canvas.parentElement.innerHTML = '<div class="chart-empty-state">Chart library is offline.</div>';
    return;
  }

  const ctx = canvas.getContext('2d');
  trendChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: 'Monthly Expense (₹)',
        data: values,
        backgroundColor: 'rgba(124, 58, 237, 0.65)',
        borderColor: '#a855f7',
        borderWidth: 2,
        borderRadius: 8
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#111129',
          titleColor: '#f3f4fd',
          bodyColor: '#a855f7',
          padding: 10,
          callbacks: {
            label: (ctx) => ` ₹${formatCurrency(ctx.raw)}`
          }
        }
      },
      scales: {
        x: {
          grid: { color: 'rgba(255, 255, 255, 0.05)' },
          ticks: { color: '#9aa0bc' }
        },
        y: {
          grid: { color: 'rgba(255, 255, 255, 0.05)' },
          ticks: {
            color: '#9aa0bc',
            callback: (v) => '₹' + v
          }
        }
      }
    }
  });
}

function renderCategoryChart(categories) {
  const canvas = document.getElementById('categoryChartCanvas');
  const emptyState = document.getElementById('categoryChartEmpty');
  if (!canvas) return;

  if (!categories || categories.length === 0) {
    if (emptyState) emptyState.style.display = 'block';
    canvas.style.display = 'none';
    return;
  }

  if (emptyState) emptyState.style.display = 'none';
  canvas.style.display = 'block';

  const labels = categories.map(c => `${c.icon || '🏷️'} ${c.categoryName}`);
  const values = categories.map(c => Number(c.totalAmount || 0));
  const colors = categories.map(c => c.color || '#7c3aed');

  if (categoryChartInstance) categoryChartInstance.destroy();

  if (typeof Chart === 'undefined') return;

  const ctx = canvas.getContext('2d');
  categoryChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [{
        data: values,
        backgroundColor: colors,
        borderWidth: 2,
        borderColor: '#080816'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: '#f3f4fd', boxWidth: 12, padding: 12 }
        },
        tooltip: {
          backgroundColor: '#111129',
          callbacks: {
            label: (ctx) => ` ₹${formatCurrency(ctx.raw)} (${categories[ctx.dataIndex].percentage}%)`
          }
        }
      },
      cutout: '68%'
    }
  });
}

// ============================================================
//  BUDGET MANAGEMENT
// ============================================================
async function loadBudgets() {
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;

  try {
    const res = await fetch('/api/budgets', { credentials: 'include' });
    const json = await res.json();
    if (!json.success) return;

    const budgets = json.data || [];
    renderBudgetsTable(budgets);

    // Active Month Card
    const current = budgets.find(b => b.year === currentYear && b.month === currentMonth);
    const limitEl = document.getElementById('budgetFocusLimit');
    const spentEl = document.getElementById('budgetFocusSpent');
    const remainingEl = document.getElementById('budgetFocusRemaining');
    const fillEl = document.getElementById('budgetFocusProgressFill');
    const percentEl = document.getElementById('budgetFocusPercentText');
    const alertEl = document.getElementById('budgetFocusStatusAlert');
    const monthNameEl = document.getElementById('budgetFocusMonthName');

    const monthNames = ["January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December"];
    if (monthNameEl) monthNameEl.textContent = `${monthNames[currentMonth - 1]} ${currentYear}`;

    if (current) {
      if (limitEl) limitEl.textContent = `₹${formatCurrency(current.amount)}`;
      if (spentEl) spentEl.textContent = `₹${formatCurrency(current.spentAmount)}`;
      if (remainingEl) remainingEl.textContent = `₹${formatCurrency(current.remainingAmount)}`;

      const pct = Math.min(100, Math.max(0, current.percentageUsed || 0));
      if (fillEl) {
        fillEl.style.width = `${pct}%`;
        fillEl.className = 'budget-progress-fill' + (current.isExceeded ? ' exceeded' : (current.isNearLimit ? ' warning' : ''));
      }
      if (percentEl) percentEl.textContent = `${current.percentageUsed}% Used`;
      if (alertEl) {
        alertEl.textContent = current.statusMessage || '';
        alertEl.style.color = current.isExceeded ? 'var(--color-rose)' : (current.isNearLimit ? 'var(--color-amber)' : 'var(--color-emerald)');
      }
    } else {
      if (limitEl) limitEl.textContent = '₹0.00';
      if (spentEl) spentEl.textContent = '₹0.00';
      if (remainingEl) remainingEl.textContent = '₹0.00';
      if (fillEl) fillEl.style.width = '0%';
      if (percentEl) percentEl.textContent = '0% Used';
      if (alertEl) {
        alertEl.textContent = 'No budget configured for this month.';
        alertEl.style.color = 'var(--text-muted)';
      }
    }
  } catch (err) {
    console.error('Failed to load budgets', err);
  }
}

function renderBudgetsTable(budgets) {
  const tbody = document.getElementById('budgetsTableTbody');
  if (!tbody) return;

  if (budgets.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="table-loading">No monthly budgets configured yet. Click "Set / Adjust Budget" to start!</td></tr>';
    return;
  }

  const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

  tbody.innerHTML = '';
  budgets.forEach(b => {
    let statusClass = 'on-track';
    let statusText = `${b.percentageUsed}% Used`;
    if (b.isExceeded) {
      statusClass = 'exceeded';
      statusText = 'Exceeded';
    } else if (b.isNearLimit) {
      statusClass = 'warning';
      statusText = 'Near Limit';
    }

    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${monthNames[b.month - 1]} ${b.year}</strong></td>
      <td class="amount-cell">₹${formatCurrency(b.amount)}</td>
      <td class="amount-cell">₹${formatCurrency(b.spentAmount)}</td>
      <td class="amount-cell">₹${formatCurrency(b.remainingAmount)}</td>
      <td>
        <span class="badge-status ${statusClass}">${statusText}</span>
      </td>
      <td>
        <div class="actions-cell">
          <button class="btn-icon-action" onclick="openEditBudgetModal(${b.budgetId}, ${b.year}, ${b.month}, ${b.amount})">✏️</button>
          <button class="btn-icon-action btn-icon-delete" onclick="promptDeleteBudget(${b.budgetId}, '${monthNames[b.month - 1]} ${b.year}')">🗑️</button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function openBudgetModal() {
  document.getElementById('budgetFormId').value = '';
  document.getElementById('budgetFormYear').value = new Date().getFullYear();
  document.getElementById('budgetFormMonth').value = new Date().getMonth() + 1;
  document.getElementById('budgetFormAmount').value = '';
  document.getElementById('budgetModalTitle').textContent = 'Set Monthly Budget';
  openModal('budgetModal');
}

function openEditBudgetModal(id, year, month, amount) {
  document.getElementById('budgetFormId').value = id;
  document.getElementById('budgetFormYear').value = year;
  document.getElementById('budgetFormMonth').value = month;
  document.getElementById('budgetFormAmount').value = amount;
  document.getElementById('budgetModalTitle').textContent = 'Update Monthly Budget';
  openModal('budgetModal');
}

async function saveBudget(event) {
  event.preventDefault();
  const id = document.getElementById('budgetFormId').value;
  const year = document.getElementById('budgetFormYear').value;
  const month = document.getElementById('budgetFormMonth').value;
  const amount = document.getElementById('budgetFormAmount').value;

  if (!amount || Number(amount) <= 0) {
    showToast('Budget amount must be greater than zero', 'error');
    return;
  }

  const payload = {
    year: Number(year),
    month: Number(month),
    amount: Number(amount)
  };

  const url = id ? `/api/budgets/${id}` : '/api/budgets';
  const method = id ? 'PUT' : 'POST';

  try {
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(payload)
    });
    const json = await res.json();

    if (json.success) {
      showToast(json.message || 'Budget saved successfully', 'success');
      closeModal('budgetModal');
      await loadBudgets();
      await loadDashboardSummary();
    } else {
      showToast(json.message || 'Failed to save budget', 'error');
    }
  } catch (err) {
    showToast('Network error saving budget', 'error');
  }
}

async function quickSetBudget(amount) {
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;

  try {
    const res = await fetch('/api/budgets', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        year: currentYear,
        month: currentMonth,
        amount: Number(amount)
      })
    });
    const json = await res.json();
    if (json.success) {
      showToast(`Monthly budget set to ₹${formatCurrency(amount)}`, 'success');
      await loadBudgets();
      await loadDashboardSummary();
    } else {
      showToast(json.message || 'Failed to set budget', 'error');
    }
  } catch (err) {
    showToast('Network error setting budget', 'error');
  }
}

function promptDeleteBudget(budgetId, periodName) {
  pendingDeleteAction = async () => {
    try {
      const res = await fetch(`/api/budgets/${budgetId}`, {
        method: 'DELETE',
        credentials: 'include'
      });
      const json = await res.json();
      if (json.success) {
        showToast(json.message || 'Budget removed', 'success');
        await loadBudgets();
        await loadDashboardSummary();
      } else {
        showToast(json.message || 'Failed to remove budget', 'error');
      }
    } catch (err) {
      showToast('Network error removing budget', 'error');
    }
  };

  document.getElementById('deleteModalMessage').textContent =
    `Are you sure you want to remove the budget for ${periodName}?`;
  openModal('deleteModal');
}

// ============================================================
//  REPORTS & CSV EXPORT
// ============================================================
function setReportPeriod(preset) {
  const startEl = document.getElementById('reportStartDate');
  const endEl = document.getElementById('reportEndDate');
  const today = new Date();

  let start, end;
  if (preset === 'thisMonth') {
    start = new Date(today.getFullYear(), today.getMonth(), 1);
    end = today;
  } else if (preset === 'lastMonth') {
    start = new Date(today.getFullYear(), today.getMonth() - 1, 1);
    end = new Date(today.getFullYear(), today.getMonth(), 0);
  } else if (preset === 'last90') {
    start = new Date();
    start.setDate(today.getDate() - 90);
    end = today;
  }

  if (startEl && start) startEl.value = start.toISOString().split('T')[0];
  if (endEl && end) endEl.value = end.toISOString().split('T')[0];

  loadReport();
}

async function loadReport() {
  const startDate = document.getElementById('reportStartDate')?.value || '';
  const endDate = document.getElementById('reportEndDate')?.value || '';
  const categoryId = document.getElementById('reportCategoryFilter')?.value || '';

  const params = new URLSearchParams();
  if (startDate) params.append('startDate', startDate);
  if (endDate) params.append('endDate', endDate);
  if (categoryId) params.append('categoryId', categoryId);

  try {
    const res = await fetch(`/api/reports/summary?${params.toString()}`, { credentials: 'include' });
    const json = await res.json();
    if (!json.success || !json.data) return;

    const data = json.data;

    // KPI Cards
    const totalEl = document.getElementById('reportTotalSpent');
    const periodLabel = document.getElementById('reportPeriodLabel');
    const txnCountEl = document.getElementById('reportTxnCount');
    const dailyAvgEl = document.getElementById('reportDailyAvg');
    const highestEl = document.getElementById('reportHighestExp');
    const highestFooter = document.getElementById('reportHighestExpFooter');

    if (totalEl) totalEl.textContent = `₹${formatCurrency(data.totalAmount)}`;
    if (periodLabel) periodLabel.textContent = `${formatDate(data.startDate)} to ${formatDate(data.endDate)}`;
    if (txnCountEl) txnCountEl.textContent = data.transactionCount || 0;
    if (dailyAvgEl) dailyAvgEl.textContent = `₹${formatCurrency(data.dailyAverage)}`;

    if (data.highestExpense) {
      if (highestEl) highestEl.textContent = `₹${formatCurrency(data.highestExpense.amount)}`;
      if (highestFooter) highestFooter.textContent = `${data.highestExpense.categoryIcon || '🏷️'} ${data.highestExpense.categoryName}`;
    } else {
      if (highestEl) highestEl.textContent = '₹0.00';
      if (highestFooter) highestFooter.textContent = 'None';
    }

    // Category breakdown list
    renderReportBreakdown(data.categoryBreakdown || []);
    renderReportChart(data.categoryBreakdown || []);

    // Itemized table
    renderReportTable(data.expenses || []);
  } catch (err) {
    console.error('Failed to load report summary', err);
  }
}

function renderReportBreakdown(categories) {
  const container = document.getElementById('reportCategoryBreakdownList');
  if (!container) return;

  if (categories.length === 0) {
    container.innerHTML = '<p class="table-loading">No expense data recorded in this timeframe.</p>';
    return;
  }

  container.innerHTML = '';
  categories.forEach(c => {
    const row = document.createElement('div');
    row.className = 'breakdown-row';
    row.innerHTML = `
      <div class="breakdown-info">
        <span class="breakdown-name">${c.icon || '🏷️'} ${escapeHtml(c.categoryName)}</span>
        <span class="breakdown-val">₹${formatCurrency(c.totalAmount)} (${c.percentage}%)</span>
      </div>
      <div class="breakdown-bar-track">
        <div class="breakdown-bar-fill" style="width: ${c.percentage}%; background: ${c.color || '#7c3aed'};"></div>
      </div>
    `;
    container.appendChild(row);
  });
}

function renderReportChart(categories) {
  const canvas = document.getElementById('reportChartCanvas');
  const empty = document.getElementById('reportChartEmpty');
  if (!canvas) return;

  if (!categories || categories.length === 0) {
    if (empty) empty.style.display = 'block';
    canvas.style.display = 'none';
    return;
  }

  if (empty) empty.style.display = 'none';
  canvas.style.display = 'block';

  const labels = categories.map(c => `${c.icon || '🏷️'} ${c.categoryName}`);
  const values = categories.map(c => Number(c.totalAmount || 0));
  const colors = categories.map(c => c.color || '#7c3aed');

  if (reportChartInstance) reportChartInstance.destroy();
  if (typeof Chart === 'undefined') return;

  const ctx = canvas.getContext('2d');
  reportChartInstance = new Chart(ctx, {
    type: 'pie',
    data: {
      labels,
      datasets: [{
        data: values,
        backgroundColor: colors,
        borderWidth: 2,
        borderColor: '#080816'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right',
          labels: { color: '#f3f4fd', boxWidth: 12 }
        }
      }
    }
  });
}

function renderReportTable(expenses) {
  const tbody = document.getElementById('reportTableTbody');
  if (!tbody) return;

  if (expenses.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" class="table-loading">No transactions found for this period.</td></tr>';
    return;
  }

  tbody.innerHTML = '';
  expenses.forEach(e => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${formatDate(e.expenseDate)}</td>
      <td>
        <span class="category-pill" style="background: ${hexToRgba(e.categoryColor || '#7c3aed', 0.15)}; border-color: ${hexToRgba(e.categoryColor || '#7c3aed', 0.35)};">
          <span>${e.categoryIcon || '🏷️'}</span>
          <span>${escapeHtml(e.categoryName)}</span>
        </span>
      </td>
      <td>${escapeHtml(e.description || '—')}</td>
      <td class="amount-cell">₹${formatCurrency(e.amount)}</td>
    `;
    tbody.appendChild(tr);
  });
}

function exportReportCsv() {
  const startDate = document.getElementById('reportStartDate')?.value || '';
  const endDate = document.getElementById('reportEndDate')?.value || '';
  const categoryId = document.getElementById('reportCategoryFilter')?.value || '';

  const params = new URLSearchParams();
  if (startDate) params.append('startDate', startDate);
  if (endDate) params.append('endDate', endDate);
  if (categoryId) params.append('categoryId', categoryId);

  window.location.href = `/api/reports/export-csv?${params.toString()}`;
  showToast('Preparing CSV report download...', 'info');
}

// ============================================================
//  LOGOUT ACTION
// ============================================================
async function handleLogout() {
  try {
    const res = await fetch('/api/auth/logout', {
      method: 'POST',
      credentials: 'include'
    });
    window.location.replace('/login.html');
  } catch (err) {
    window.location.replace('/login.html');
  }
}

// ============================================================
//  MODAL & CONFIRMATION HELPERS
// ============================================================
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.style.display = 'flex';
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.style.display = 'none';
}

function handleModalBackdropClick(event, modalId) {
  if (event.target.id === modalId) {
    closeModal(modalId);
  }
}

const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
if (confirmDeleteBtn) {
  confirmDeleteBtn.addEventListener('click', async () => {
    if (pendingDeleteAction) {
      const action = pendingDeleteAction;
      pendingDeleteAction = null;
      closeModal('deleteModal');
      await action();
    }
  });
}

// ============================================================
//  TOAST SYSTEM
// ============================================================
function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const icon = type === 'success' ? '✅' : (type === 'error' ? '❌' : 'ℹ️');

  toast.innerHTML = `
    <span>${icon} ${escapeHtml(message)}</span>
    <button class="toast-close" onclick="this.parentElement.remove()">✕</button>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    if (toast.parentElement) toast.remove();
  }, 4500);
}

// ============================================================
//  UTILITY HELPERS
// ============================================================
function formatCurrency(val) {
  if (val === null || val === undefined || isNaN(val)) return '0.00';
  return Number(val).toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  const parts = dateStr.split('-');
  if (parts.length === 3) {
    const d = new Date(parts[0], parts[1] - 1, parts[2]);
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
  return dateStr;
}

function hexToRgba(hex, alpha) {
  let c = hex.replace('#', '');
  if (c.length === 3) c = c.split('').map(x => x + x).join('');
  if (c.length !== 6) return `rgba(124, 58, 237, ${alpha})`;
  const r = parseInt(c.substring(0, 2), 16);
  const g = parseInt(c.substring(2, 4), 16);
  const b = parseInt(c.substring(4, 6), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

function escapeHtml(text) {
  if (!text) return '';
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function capitalize(str) {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1);
}
