<template>
  <main class="workspace-shell">
    <div class="workspace-frame">
      <aside class="workspace-sidebar">
        <div class="workspace-brand">
          <div class="workspace-brand-mark">AD</div>
          <div>
            <strong>社区运营后台</strong>
            <span>规则、审核、商城与订单协同</span>
          </div>
        </div>

        <nav class="workspace-nav" aria-label="管理员模块导航">
          <button
            v-for="section in filteredSidebarSections"
            :key="section.id"
            class="workspace-nav-item"
            :class="{ active: activeSection === section.id }"
            @click="openSection(section.id)"
          >
            <span class="workspace-nav-mark">{{ section.short }}</span>
            <span class="workspace-nav-copy">
              <strong>{{ section.label }}</strong>
              <span>{{ section.hint }}</span>
            </span>
          </button>
          <div v-if="!filteredSidebarSections.length" class="workspace-nav-empty">
            没有匹配到模块，试试其他关键词
          </div>
        </nav>

        <div class="workspace-footer">
          <strong>今日待处理</strong>
          <p>待审核上报 {{ dashboard.pendingReportCount }} 条，待处理订单 {{ dashboard.pendingOrderCount }} 笔。</p>
        </div>
      </aside>

      <section class="workspace-main">
        <header class="workspace-topbar">
          <div>
            <div class="workspace-breadcrumb">管理员端 / {{ activeSectionMeta ? activeSectionMeta.label : '未打开模块' }}</div>
            <h1 class="workspace-title">{{ activeSectionMeta ? activeSectionMeta.label : '请选择一个工作模块' }}</h1>
            <p class="workspace-subtitle">
              {{ activeSectionMeta ? activeSectionMeta.description : '左侧点击一个模块即可在顶部生成最近打开页签，关闭后会自动回到上一个仍然打开的页签。' }}
            </p>
          </div>

          <div class="workspace-toolbar">
            <div class="workspace-search">
              <input
                ref="sectionSearchInputRef"
                v-model.trim="sectionSearchKeyword"
                class="workspace-search-input"
                type="search"
                placeholder="搜索模块，回车打开首个匹配项"
                aria-label="搜索管理端模块"
                @keydown.enter.prevent="openFirstMatchedSection"
                @keydown.esc.prevent="clearSectionSearch"
              />
              <kbd>Ctrl K</kbd>
            </div>
            <div class="workspace-actions">
              <button class="btn secondary" @click="loadAll">刷新数据</button>
            </div>
            <div
              class="workspace-avatar-wrap"
              @mouseenter="avatarMenuOpen = true"
              @mouseleave="avatarMenuOpen = false"
            >
              <button class="workspace-avatar" @click="avatarMenuOpen = !avatarMenuOpen">
                <span class="workspace-avatar-image workspace-avatar-photo">
                  <img :src="profileAvatarUrl" alt="头像" />
                </span>
              </button>

              <div v-if="avatarMenuOpen" class="workspace-avatar-menu">
                <div class="workspace-avatar-header">
                  <div class="workspace-avatar-image workspace-avatar-photo">
                    <img :src="profileAvatarUrl" alt="头像" />
                  </div>
                  <div class="workspace-avatar-meta">
                    <strong>{{ profileCenterForm.name }}</strong>
                    <span>{{ profileCenterForm.role }}</span>
                  </div>
                </div>

                <div class="workspace-avatar-actions">
                  <button class="workspace-avatar-action" @click="openProfileCenter">
                    <span class="workspace-avatar-icon">人</span>
                    <span>个人中心</span>
                  </button>
                  <button class="workspace-avatar-action danger" @click="logout">
                    <span class="workspace-avatar-icon">退</span>
                    <span>退出登录</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </header>

        <div class="workspace-tabs">
          <button
            v-for="section in openTabSections"
            :key="section.id"
            class="workspace-tab"
            :class="{ active: activeSection === section.id }"
            @click="activeSection = section.id"
          >
            <span class="workspace-tab-label">{{ section.label }}</span>
            <span class="workspace-tab-close" @click.stop="closeSection(section.id)">×</span>
          </button>
        </div>

        <div class="workspace-content">
          <div
            v-if="message && messageType !== 'success'"
            class="action-toast is-error"
            :style="{ '--toast-duration': '5s' }"
            role="status"
            aria-live="assertive"
          >
            <span class="action-toast-icon" aria-hidden="true">!</span>
            <div class="action-toast-body">
              <p class="action-toast-title">操作失败</p>
              <p class="action-toast-text">{{ message }}</p>
              <span class="action-toast-progress" aria-hidden="true"></span>
            </div>
            <button class="action-toast-close" type="button" aria-label="关闭提示" @click="clearMessage">×</button>
          </div>

          <div v-if="successDialogVisible" class="success-dialog-mask" role="dialog" aria-modal="true" aria-labelledby="success-dialog-title">
            <section class="success-dialog-card">
              <header class="success-dialog-head">
                <h3 id="success-dialog-title">操作成功</h3>
              </header>
              <div class="success-dialog-body">
                <p>{{ successDialogMessage }}</p>
              </div>
              <footer class="success-dialog-actions">
                <button class="btn" type="button" @click="closeSuccessDialog">确定</button>
              </footer>
            </section>
          </div>

          <div v-if="!activeSection" class="workspace-empty-stage">
            <span class="workspace-kicker">Workspace</span>
            <h2>从左侧打开一个管理模块</h2>
            <p>例如先打开“审核工作台”处理待审核上报，或者打开“商品管理”维护积分商城库存。</p>
          </div>

          <section v-if="activeSection === 'overview'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Overview</span>
                <h2>运营总览</h2>
                <p>从居民活跃、规则启用、待处理任务到当日订单履约，把关键运营指标集中在同一工作区。</p>
              </div>
              <span class="badge">更新时间 {{ fmt(dashboard.updatedAt) }}</span>
            </div>

            <div class="metric-strip">
              <div class="metric-unit">
                <span>居民用户</span>
                <strong>{{ dashboard.residentCount }}</strong>
                <small>当前系统中的居民账号总数</small>
              </div>
              <div class="metric-unit">
                <span>启用规则</span>
                <strong>{{ dashboard.activeRuleCount }}</strong>
                <small>居民端当前可上报的低碳行为</small>
              </div>
              <div class="metric-unit">
                <span>待审核上报</span>
                <strong>{{ dashboard.pendingReportCount }}</strong>
                <small>需要人工确认的行为记录</small>
              </div>
              <div class="metric-unit">
                <span>待处理订单</span>
                <strong>{{ dashboard.pendingOrderCount }}</strong>
                <small>待履约或退款的兑换订单</small>
              </div>
            </div>

            <div class="workspace-grid-2">
              <div class="workspace-surface">
                <span class="workspace-kicker">Today</span>
                <h3>今日运营脉搏</h3>
                <div class="workspace-list">
                  <article class="workspace-list-item">
                    <div>
                      <strong>今日提交上报</strong>
                      <p>衡量社区参与热度的最直接指标。</p>
                    </div>
                    <span class="badge">{{ dashboard.todaySubmittedReports }}</span>
                  </article>
                  <article class="workspace-list-item">
                    <div>
                      <strong>今日通过上报</strong>
                      <p>反映审核效率与激励发放节奏。</p>
                    </div>
                    <span class="badge">{{ dashboard.todayApprovedReports }}</span>
                  </article>
                  <article class="workspace-list-item">
                    <div>
                      <strong>今日兑换订单</strong>
                      <p>衡量积分体系是否形成持续激励。</p>
                    </div>
                    <span class="badge warn">{{ dashboard.todayRedeemOrders }}</span>
                  </article>
                  <article class="workspace-list-item">
                    <div>
                      <strong>今日完成订单</strong>
                      <p>体现后端履约处理能力。</p>
                    </div>
                    <span class="badge">{{ dashboard.todayCompletedOrders }}</span>
                  </article>
                </div>
              </div>

              <div class="workspace-surface">
                <span class="workspace-kicker">Resources</span>
                <h3>资源概况</h3>
                <p>从库存与积分角度快速判断运营是否需要补货或调整激励策略。</p>
                <div class="workspace-inline-stats">
                  <div class="stat">
                    <span>居民总积分</span>
                    <strong>{{ dashboard.totalResidentPoints }}</strong>
                  </div>
                  <div class="stat">
                    <span>累计减碳量</span>
                    <strong>{{ Number(dashboard.totalCarbonReduction || 0).toFixed(2) }}</strong>
                  </div>
                  <div class="stat">
                    <span>上架商品</span>
                    <strong>{{ dashboard.enabledItemCount }}</strong>
                  </div>
                  <div class="stat">
                    <span>总库存</span>
                    <strong>{{ dashboard.enabledItemStock }}</strong>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <section v-if="activeSection === 'users'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Users</span>
                <h2>用户管理</h2>
                <p>创建居民与管理员账号，并对昵称、角色与密码进行维护。</p>
              </div>
              <div class="workspace-actions">
                <span class="badge">{{ users.length }} 个账号</span>
                <button class="btn" @click="openCreateUserDialog">新增用户</button>
              </div>
            </div>

            <div class="table-wrap">
              <table class="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>用户名</th>
                    <th>昵称</th>
                    <th>角色</th>
                    <th>积分</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!pagedUsers.length">
                    <td colspan="6" class="workspace-table-empty">暂无用户数据</td>
                  </tr>
                  <tr v-for="u in pagedUsers" :key="u.id">
                    <td>{{ u.id }}</td>
                    <td>{{ u.username }}</td>
                    <td>{{ u.nickname }}</td>
                    <td>{{ u.role }}</td>
                    <td>{{ u.totalPoints }}</td>
                    <td>
                      <div class="workspace-actions admin-mini-actions">
                        <button class="btn secondary" @click="openEditUserDialog(u)">编辑</button>
                        <button class="btn danger" @click="deleteUser(u.id)">删除</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <AppPagination
              v-if="users.length > usersPageSize"
              :current-page="usersPage"
              :total-items="users.length"
              :page-size="usersPageSize"
              @change="usersPage = $event"
            />

            <div v-if="userDialogOpen" class="workspace-modal-mask" @click.self="closeUserDialog">
              <section class="workspace-modal-card">
                <header class="workspace-modal-head">
                  <div>
                    <span class="workspace-kicker">User</span>
                    <h3>{{ userForm.id ? '编辑用户' : '新增用户' }}</h3>
                  </div>
                  <button class="workspace-modal-close" type="button" aria-label="关闭弹窗" @click="closeUserDialog">×</button>
                </header>

                <div class="workspace-modal-body">
                  <div class="workspace-form-grid">
                    <label class="field">
                      <span class="field-label">登录用户名</span>
                      <input
                        v-model.trim="userForm.username"
                        class="input"
                        :class="{ 'has-error': userFormErrors.username }"
                        placeholder="例如：resident_01"
                        @input="clearUserFieldError('username')"
                      />
                      <span v-if="userFormErrors.username" class="field-error-text">{{ userFormErrors.username }}</span>
                    </label>
                    <label class="field">
                      <span class="field-label">用户昵称</span>
                      <input
                        v-model.trim="userForm.nickname"
                        class="input"
                        :class="{ 'has-error': userFormErrors.nickname }"
                        placeholder="例如：张三"
                        @input="clearUserFieldError('nickname')"
                      />
                      <span v-if="userFormErrors.nickname" class="field-error-text">{{ userFormErrors.nickname }}</span>
                    </label>
                  </div>

                  <div class="workspace-form-grid">
                    <label class="field">
                      <span class="field-label">登录密码</span>
                      <input
                        v-model.trim="userForm.password"
                        class="input"
                        :class="{ 'has-error': userFormErrors.password }"
                        :placeholder="userForm.id ? '留空表示不修改密码' : '至少 6 位'"
                        @input="clearUserFieldError('password')"
                      />
                      <span v-if="userFormErrors.password" class="field-error-text">{{ userFormErrors.password }}</span>
                    </label>
                    <label class="field">
                      <span class="field-label">角色</span>
                      <select
                        v-model="userForm.role"
                        class="select"
                        :class="{ 'has-error': userFormErrors.role }"
                        @change="clearUserFieldError('role')"
                      >
                        <option value="RESIDENT">居民（RESIDENT）</option>
                        <option value="ADMIN">管理员（ADMIN）</option>
                      </select>
                      <span v-if="userFormErrors.role" class="field-error-text">{{ userFormErrors.role }}</span>
                    </label>
                  </div>
                </div>

                <footer class="workspace-actions workspace-modal-actions">
                  <button class="btn" type="button" @click="submitUserDialog">
                    {{ userForm.id ? '保存更新' : '确认新增' }}
                  </button>
                  <button class="btn secondary" type="button" @click="closeUserDialog">取消</button>
                </footer>
              </section>
            </div>
          </section>

          <section v-if="activeSection === 'rules'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Rules</span>
                <h2>行为规则配置</h2>
                <p>维护积分、减碳量、日上限与启停状态，形成居民端的激励逻辑。</p>
              </div>
              <div class="workspace-actions">
                <span class="badge">{{ rules.length }} 条规则</span>
                <button class="btn" @click="openCreateRuleDialog">新增规则</button>
              </div>
            </div>

            <div class="table-wrap">
              <table class="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>名称</th>
                    <th>积分</th>
                    <th>减碳</th>
                    <th>日限</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!pagedRules.length">
                    <td colspan="7" class="workspace-table-empty">暂无规则数据</td>
                  </tr>
                  <tr v-for="r in pagedRules" :key="r.id">
                    <td>{{ r.id }}</td>
                    <td>{{ r.name }}</td>
                    <td>{{ r.pointsPerAction }}</td>
                    <td>{{ r.carbonReductionPerAction }}</td>
                    <td>{{ r.dailyLimit }}</td>
                    <td><span :class="r.active ? 'badge' : 'badge err'">{{ r.active ? '启用' : '停用' }}</span></td>
                    <td>
                      <div class="workspace-actions admin-mini-actions">
                        <button class="btn secondary" @click="openEditRuleDialog(r)">编辑</button>
                        <button class="btn danger" @click="deleteRule(r.id)">删除</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <AppPagination
              v-if="rules.length > rulesPageSize"
              :current-page="rulesPage"
              :total-items="rules.length"
              :page-size="rulesPageSize"
              @change="rulesPage = $event"
            />

            <div v-if="ruleDialogOpen" class="workspace-modal-mask" @click.self="closeRuleDialog">
              <section class="workspace-modal-card">
                <header class="workspace-modal-head">
                  <div>
                    <span class="workspace-kicker">Rule</span>
                    <h3>{{ ruleForm.id ? '编辑规则' : '新增规则' }}</h3>
                  </div>
                  <button class="workspace-modal-close" type="button" aria-label="关闭弹窗" @click="closeRuleDialog">×</button>
                </header>

                <div class="workspace-modal-body">
                  <div class="workspace-form-grid">
                    <label class="field">
                      <span class="field-label">规则名称</span>
                      <input
                        v-model.trim="ruleForm.name"
                        class="input"
                        :class="{ 'has-error': ruleFormErrors.name }"
                        placeholder="例如：步行或骑行出行"
                        @input="clearRuleFieldError('name')"
                      />
                      <span v-if="ruleFormErrors.name" class="field-error-text">{{ ruleFormErrors.name }}</span>
                    </label>
                    <label class="field">
                      <span class="field-label">单次奖励积分</span>
                      <input
                        v-model.number="ruleForm.pointsPerAction"
                        type="number"
                        min="0"
                        class="input"
                        :class="{ 'has-error': ruleFormErrors.pointsPerAction }"
                        @input="clearRuleFieldError('pointsPerAction')"
                      />
                      <span v-if="ruleFormErrors.pointsPerAction" class="field-error-text">{{ ruleFormErrors.pointsPerAction }}</span>
                    </label>
                  </div>

                  <div class="workspace-form-grid">
                    <label class="field">
                      <span class="field-label">单次减碳量（kg）</span>
                      <input
                        v-model.number="ruleForm.carbonReductionPerAction"
                        type="number"
                        min="0"
                        step="0.1"
                        class="input"
                        :class="{ 'has-error': ruleFormErrors.carbonReductionPerAction }"
                        @input="clearRuleFieldError('carbonReductionPerAction')"
                      />
                      <span v-if="ruleFormErrors.carbonReductionPerAction" class="field-error-text">{{ ruleFormErrors.carbonReductionPerAction }}</span>
                    </label>
                    <label class="field">
                      <span class="field-label">每日上报上限</span>
                      <input
                        v-model.number="ruleForm.dailyLimit"
                        type="number"
                        min="1"
                        class="input"
                        :class="{ 'has-error': ruleFormErrors.dailyLimit }"
                        @input="clearRuleFieldError('dailyLimit')"
                      />
                      <span v-if="ruleFormErrors.dailyLimit" class="field-error-text">{{ ruleFormErrors.dailyLimit }}</span>
                    </label>
                  </div>

                  <div class="workspace-form-grid">
                    <label class="field">
                      <span class="field-label">规则状态</span>
                      <select v-model="ruleForm.active" class="select">
                        <option :value="true">启用</option>
                        <option :value="false">停用</option>
                      </select>
                    </label>
                    <div class="field">
                      <span class="field-label">口径提醒</span>
                      <span class="field-help">建议写清居民需要提供的凭证与实际行为口径。</span>
                    </div>
                  </div>

                  <label class="field">
                    <span class="field-label">规则说明</span>
                    <textarea
                      v-model.trim="ruleForm.description"
                      class="textarea"
                      placeholder="例如：通勤使用步行或骑行方式，并上传简要说明与凭证图片。"
                    ></textarea>
                  </label>
                </div>

                <footer class="workspace-actions workspace-modal-actions">
                  <button class="btn" type="button" @click="submitRuleDialog">
                    {{ ruleForm.id ? '保存更新' : '确认新增' }}
                  </button>
                  <button class="btn secondary" type="button" @click="closeRuleDialog">取消</button>
                </footer>
              </section>
            </div>
          </section>

          <section v-if="activeSection === 'audits'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Audit</span>
                <h2>待审核上报</h2>
                <p>集中查看居民提交的行为说明、凭证图片与时间，快速完成通过或驳回。</p>
              </div>
              <span class="badge warn">{{ pendingReports.length }} 条待处理</span>
            </div>

            <div class="table-wrap">
              <table class="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>居民</th>
                    <th>规则</th>
                    <th>次数</th>
                    <th>凭证说明</th>
                    <th>凭证图片</th>
                    <th>提交时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!pagedPendingReports.length">
                    <td colspan="8" class="workspace-table-empty">当前没有待审核上报</td>
                  </tr>
                  <tr v-for="row in pagedPendingReports" :key="row.id">
                    <td>{{ row.id }}</td>
                    <td>{{ row.nickname }}</td>
                    <td>{{ row.ruleName }}</td>
                    <td>{{ row.quantity }}</td>
                    <td>{{ row.proofText || '-' }}</td>
                    <td>
                      <img
                        v-if="row.proofImageUrl"
                        :src="resolveImageUrl(row.proofImageUrl)"
                        class="media-thumb"
                        alt="凭证图片"
                        @click="openImage(resolveImageUrl(row.proofImageUrl))"
                      />
                      <span v-else>-</span>
                    </td>
                    <td>{{ fmt(row.submittedAt) }}</td>
                    <td>
                      <div class="workspace-actions admin-mini-actions">
                        <button class="btn" @click="audit(row.id, true)">通过</button>
                        <button class="btn danger" @click="audit(row.id, false)">驳回</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <AppPagination
              v-if="pendingReports.length > pendingReportsPageSize"
              :current-page="pendingReportsPage"
              :total-items="pendingReports.length"
              :page-size="pendingReportsPageSize"
              @change="pendingReportsPage = $event"
            />
          </section>

          <section v-if="activeSection === 'items'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Mall</span>
                <h2>商城商品管理</h2>
                <p>在统一工作区里维护商品、库存和上下架状态，让兑换端与库存治理同步更新。</p>
              </div>
              <div class="workspace-actions">
                <div class="workspace-inline-stats admin-items-summary">
                  <div class="stat">
                    <span>商品总数</span>
                    <strong>{{ items.length }}</strong>
                  </div>
                  <div class="stat">
                    <span>上架商品</span>
                    <strong>{{ enabledItemCount }}</strong>
                  </div>
                  <div class="stat">
                    <span>总库存</span>
                    <strong>{{ totalItemStock }}</strong>
                  </div>
                </div>
                <button class="btn" @click="openCreateItemDialog">新增商品</button>
              </div>
            </div>

            <div class="table-wrap">
              <table class="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>名称</th>
                    <th>单价</th>
                    <th>库存</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!pagedItems.length">
                    <td colspan="6" class="workspace-table-empty">暂无商品数据</td>
                  </tr>
                  <tr v-for="i in pagedItems" :key="i.id">
                    <td>{{ i.id }}</td>
                    <td>{{ i.name }}</td>
                    <td>{{ i.pointsCost }}</td>
                    <td>{{ i.stock }}</td>
                    <td><span :class="i.enabled ? 'badge' : 'badge err'">{{ i.enabled ? '上架' : '下架' }}</span></td>
                    <td>
                      <div class="workspace-actions admin-mini-actions">
                        <button class="btn secondary" @click="openEditItemDialog(i)">编辑</button>
                        <button class="btn danger" @click="deleteItem(i.id)">删除</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <AppPagination
              v-if="items.length > itemsPageSize"
              :current-page="itemsPage"
              :total-items="items.length"
              :page-size="itemsPageSize"
              @change="itemsPage = $event"
            />

            <div v-if="itemDialogOpen" class="workspace-modal-mask" @click.self="closeItemDialog">
              <section class="workspace-modal-card">
                <header class="workspace-modal-head">
                  <div>
                    <span class="workspace-kicker">Item</span>
                    <h3>{{ itemForm.id ? '编辑商品' : '新增商品' }}</h3>
                  </div>
                  <button class="workspace-modal-close" type="button" aria-label="关闭弹窗" @click="closeItemDialog">×</button>
                </header>

                <div class="workspace-modal-body">
                  <div class="workspace-form-grid">
                    <label class="field">
                      <span class="field-label">商品名称</span>
                      <input
                        v-model.trim="itemForm.name"
                        class="input"
                        :class="{ 'has-error': itemFormErrors.name }"
                        placeholder="例如：环保帆布袋"
                        @input="clearItemFieldError('name')"
                      />
                      <span v-if="itemFormErrors.name" class="field-error-text">{{ itemFormErrors.name }}</span>
                    </label>
                    <label class="field">
                      <span class="field-label">兑换积分单价</span>
                      <input
                        v-model.number="itemForm.pointsCost"
                        type="number"
                        min="1"
                        class="input"
                        :class="{ 'has-error': itemFormErrors.pointsCost }"
                        @input="clearItemFieldError('pointsCost')"
                      />
                      <span v-if="itemFormErrors.pointsCost" class="field-error-text">{{ itemFormErrors.pointsCost }}</span>
                    </label>
                  </div>

                  <div class="workspace-form-grid">
                    <label class="field">
                      <span class="field-label">库存数量</span>
                      <input
                        v-model.number="itemForm.stock"
                        type="number"
                        min="0"
                        class="input"
                        :class="{ 'has-error': itemFormErrors.stock }"
                        @input="clearItemFieldError('stock')"
                      />
                      <span v-if="itemFormErrors.stock" class="field-error-text">{{ itemFormErrors.stock }}</span>
                    </label>
                    <label class="field">
                      <span class="field-label">上架状态</span>
                      <select v-model="itemForm.enabled" class="select">
                        <option :value="true">上架</option>
                        <option :value="false">下架</option>
                      </select>
                    </label>
                  </div>

                  <label class="field">
                    <span class="field-label">商品说明</span>
                    <textarea v-model.trim="itemForm.description" class="textarea" placeholder="例如：适合居民日常重复使用的环保生活用品。"></textarea>
                  </label>
                </div>

                <footer class="workspace-actions workspace-modal-actions">
                  <button class="btn" type="button" @click="submitItemDialog">
                    {{ itemForm.id ? '保存更新' : '确认新增' }}
                  </button>
                  <button class="btn secondary" type="button" @click="closeItemDialog">取消</button>
                </footer>
              </section>
            </div>
          </section>

          <section v-if="activeSection === 'orders'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Orders</span>
                <h2>兑换订单管理</h2>
                <p>统一处理履约与退款，避免订单状态长时间停留在待处理。</p>
              </div>
              <span class="badge">{{ orders.length }} 笔订单</span>
            </div>

            <div class="table-wrap">
              <table class="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>居民</th>
                    <th>商品</th>
                    <th>数量</th>
                    <th>积分</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!pagedOrders.length">
                    <td colspan="8" class="workspace-table-empty">暂无订单数据</td>
                  </tr>
                  <tr v-for="o in pagedOrders" :key="o.id">
                    <td>{{ o.id }}</td>
                    <td>{{ o.nickname }}</td>
                    <td>{{ o.itemName }}</td>
                    <td>{{ o.quantity }}</td>
                    <td>{{ o.totalPoints }}</td>
                    <td><span :class="badgeClass(o.status)">{{ formatStatusLabel(o.status) }}</span></td>
                    <td>{{ fmt(o.createdAt) }}</td>
                    <td>
                      <div v-if="o.status === 'PENDING'" class="workspace-actions admin-mini-actions">
                        <button class="btn" @click="updateOrder(o.id, 'COMPLETED')">完成</button>
                        <button class="btn danger" @click="updateOrder(o.id, 'CANCELLED')">取消并退款</button>
                      </div>
                      <span v-else class="admin-order-locked">已结束</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <AppPagination
              v-if="orders.length > ordersPageSize"
              :current-page="ordersPage"
              :total-items="orders.length"
              :page-size="ordersPageSize"
              @change="ordersPage = $event"
            />
          </section>

          <section v-if="activeSection === 'profile'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Profile</span>
                <h2>个人中心</h2>
                <p>集中查看并维护你的基础资料、岗位信息与个人介绍。</p>
              </div>
              <button class="btn" @click="saveProfile">保存资料</button>
            </div>

            <div v-if="profileSaveMessage" class="inline-message" :class="profileSaveType === 'success' ? 'success' : 'error'">
              {{ profileSaveMessage }}
            </div>

            <div class="profile-center">
              <section class="profile-card">
                <div class="profile-cover"></div>
                <div class="profile-body">
                  <div class="profile-avatar-wrap">
                    <div class="profile-avatar profile-avatar-photo">
                      <img :src="profileAvatarUrl" alt="个人头像" />
                    </div>
                    <div class="profile-avatar-upload">
                      <input
                        ref="profileAvatarInputRef"
                        class="profile-avatar-input"
                        type="file"
                        accept="image/*"
                        @change="onProfileAvatarChange"
                      />
                      <button
                        class="btn ghost profile-avatar-btn"
                        type="button"
                        :disabled="profileAvatarUploading"
                        @click="triggerProfileAvatarUpload"
                      >
                        {{ profileAvatarUploading ? '上传中...' : '上传头像' }}
                      </button>
                    </div>
                  </div>
                  <h3 class="profile-name">{{ profileCenterForm.name }}</h3>
                  <p class="profile-desc">{{ profileCenterForm.bio }}</p>

                  <div class="profile-list">
                    <div class="profile-list-item">
                      <span>邮</span>
                      <strong>{{ profileCenterForm.email }}</strong>
                    </div>
                    <div class="profile-list-item">
                      <span>岗</span>
                      <strong>{{ profileCenterForm.role }}</strong>
                    </div>
                    <div class="profile-list-item">
                      <span>城</span>
                      <strong>{{ profileCenterForm.city }}</strong>
                    </div>
                    <div class="profile-list-item">
                      <span>组</span>
                      <strong>{{ profileCenterForm.organization }}</strong>
                    </div>
                  </div>

                  <div class="profile-tags">
                    <span v-for="tag in profileCenterForm.tags" :key="tag" class="profile-tag">{{ tag }}</span>
                  </div>
                </div>
              </section>

              <section class="profile-panel">
                <div class="profile-panel-head">
                  <h3>基本设置</h3>
                </div>
                <div class="profile-panel-body">
                  <div class="workspace-form">
                    <div class="workspace-form-grid">
                      <label class="field">
                        <span class="field-label">姓名</span>
                        <input v-model="profileCenterForm.name" class="input" />
                      </label>
                      <label class="field">
                        <span class="field-label">性别</span>
                        <select v-model="profileCenterForm.gender" class="select">
                          <option value="女">女</option>
                          <option value="男">男</option>
                          <option value="保密">保密</option>
                        </select>
                      </label>
                    </div>

                    <div class="workspace-form-grid">
                      <label class="field">
                        <span class="field-label">昵称</span>
                        <input v-model="profileCenterForm.nickname" class="input" />
                      </label>
                      <label class="field">
                        <span class="field-label">邮箱</span>
                        <input v-model="profileCenterForm.email" class="input" />
                      </label>
                    </div>

                    <div class="workspace-form-grid">
                      <label class="field">
                        <span class="field-label">手机</span>
                        <input v-model="profileCenterForm.phone" class="input" />
                      </label>
                      <label class="field">
                        <span class="field-label">地址</span>
                        <input v-model="profileCenterForm.address" class="input" />
                      </label>
                    </div>

                    <label class="field">
                      <span class="field-label">个人介绍</span>
                      <textarea v-model="profileCenterForm.bio" class="textarea"></textarea>
                    </label>
                  </div>
                </div>
              </section>
            </div>
          </section>
        </div>
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useAdminPage } from '../composables/useAdminPage'
import { updateAdminProfile, uploadImage } from '../services/adminService'
import AppPagination from '../components/AppPagination.vue'
import adminSections from '../constants/adminSections'
import { isValidSectionId, persistWorkspaceState, restoreWorkspaceState } from '../utils/workspaceState'

const defaultProfileAvatarUrl = `data:image/svg+xml;utf8,${encodeURIComponent(
  `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 128 128"><defs><linearGradient id="g" x1="0" x2="1" y1="0" y2="1"><stop offset="0" stop-color="#9cc9b2"/><stop offset="1" stop-color="#e0c39f"/></linearGradient></defs><rect width="128" height="128" rx="28" fill="url(#g)"/><circle cx="64" cy="50" r="22" fill="#f7f5ef"/><path d="M24 118c4-22 19-34 40-34s36 12 40 34" fill="#f7f5ef"/></svg>`
)}`
const profileAvatarStorageKey = 'lowcarbon:admin-avatar:v1'

function readCachedAvatarUrl() {
  try {
    return localStorage.getItem(profileAvatarStorageKey) || ''
  } catch {
    return ''
  }
}

function writeCachedAvatarUrl(url) {
  try {
    if (url) {
      localStorage.setItem(profileAvatarStorageKey, url)
      return
    }
    localStorage.removeItem(profileAvatarStorageKey)
  } catch {
    // Ignore storage failures.
  }
}

const workspaceStateStorageKey = 'lowcarbon:admin-workspace:v1'
const restoredWorkspaceState = restoreWorkspaceState({
  storageKey: workspaceStateStorageKey,
  sections: adminSections
})
const sectionSearchKeyword = ref('')
const sectionSearchInputRef = ref(null)
const sidebarSections = computed(() => adminSections.filter((section) => section.visibleInSidebar))
const filteredSidebarSections = computed(() => {
  const keyword = sectionSearchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return sidebarSections.value
  }

  return sidebarSections.value.filter((section) => {
    const searchableText = `${section.label} ${section.hint} ${section.description}`.toLowerCase()
    return searchableText.includes(keyword)
  })
})
const openTabs = ref(restoredWorkspaceState.openTabs)
const activeSection = ref(restoredWorkspaceState.activeSection)
const avatarMenuOpen = ref(false)
const userDialogOpen = ref(false)
const ruleDialogOpen = ref(false)
const itemDialogOpen = ref(false)
const successDialogVisible = ref(false)
const successDialogMessage = ref('')
const profileSaveMessage = ref('')
const profileSaveType = ref('success')
const profileAvatarInputRef = ref(null)
const profileAvatarUploading = ref(false)
const userFormErrors = reactive({
  username: '',
  nickname: '',
  password: '',
  role: ''
})
const ruleFormErrors = reactive({
  name: '',
  pointsPerAction: '',
  carbonReductionPerAction: '',
  dailyLimit: ''
})
const itemFormErrors = reactive({
  name: '',
  pointsCost: '',
  stock: ''
})

const openTabSections = computed(() =>
  openTabs.value
    .map((id) => adminSections.find((section) => section.id === id))
    .filter(Boolean)
)

const activeSectionMeta = computed(
  () => adminSections.find((section) => section.id === activeSection.value) || null
)

function openSection(sectionId) {
  if (!isValidSectionId(adminSections, sectionId)) {
    return
  }
  if (!openTabs.value.includes(sectionId)) {
    openTabs.value.push(sectionId)
  }
  activeSection.value = sectionId
}

function closeSection(sectionId) {
  const index = openTabs.value.indexOf(sectionId)
  if (index === -1) return

  const nextTabs = openTabs.value.filter((id) => id !== sectionId)
  openTabs.value = nextTabs

  if (activeSection.value === sectionId) {
    if (nextTabs.length === 0) {
      activeSection.value = null
      return
    }

    const fallbackIndex = index > 0 ? index - 1 : 0
    activeSection.value = nextTabs[fallbackIndex] || nextTabs[0]
  }
}

function openProfileCenter() {
  openSection('profile')
  avatarMenuOpen.value = false
}

function openCreateUserDialog() {
  resetUserForm()
  clearUserFormErrors()
  userDialogOpen.value = true
}

function openEditUserDialog(user) {
  editUser(user)
  clearUserFormErrors()
  userDialogOpen.value = true
}

function closeUserDialog() {
  userDialogOpen.value = false
  clearUserFormErrors()
  resetUserForm()
}

async function submitUserDialog() {
  clearUserFormErrors()
  if (!validateUserForm()) {
    return
  }
  const ok = await saveUser()
  if (ok) {
    userDialogOpen.value = false
  }
}

function openCreateRuleDialog() {
  resetRuleForm()
  clearRuleFormErrors()
  ruleDialogOpen.value = true
}

function openEditRuleDialog(rule) {
  editRule(rule)
  clearRuleFormErrors()
  ruleDialogOpen.value = true
}

function closeRuleDialog() {
  ruleDialogOpen.value = false
  clearRuleFormErrors()
  resetRuleForm()
}

async function submitRuleDialog() {
  clearRuleFormErrors()
  if (!validateRuleForm()) {
    return
  }
  const ok = await saveRule()
  if (ok) {
    ruleDialogOpen.value = false
  }
}

function openCreateItemDialog() {
  resetItemForm()
  clearItemFormErrors()
  itemDialogOpen.value = true
}

function openEditItemDialog(item) {
  editItem(item)
  clearItemFormErrors()
  itemDialogOpen.value = true
}

function closeItemDialog() {
  itemDialogOpen.value = false
  clearItemFormErrors()
  resetItemForm()
}

function closeSuccessDialog() {
  successDialogVisible.value = false
}

async function submitItemDialog() {
  clearItemFormErrors()
  if (!validateItemForm()) {
    return
  }
  const ok = await saveItem()
  if (ok) {
    itemDialogOpen.value = false
  }
}

function clearErrors(target) {
  Object.keys(target).forEach((key) => {
    target[key] = ''
  })
}

function clearUserFormErrors() {
  clearErrors(userFormErrors)
}

function clearRuleFormErrors() {
  clearErrors(ruleFormErrors)
}

function clearItemFormErrors() {
  clearErrors(itemFormErrors)
}

function clearUserFieldError(field) {
  userFormErrors[field] = ''
}

function clearRuleFieldError(field) {
  ruleFormErrors[field] = ''
}

function clearItemFieldError(field) {
  itemFormErrors[field] = ''
}

function validateUserForm() {
  if (!String(userForm.username || '').trim()) {
    userFormErrors.username = '请输入登录用户名'
  }
  if (!String(userForm.nickname || '').trim()) {
    userFormErrors.nickname = '请输入用户昵称'
  }
  if (!String(userForm.role || '').trim()) {
    userFormErrors.role = '请选择角色'
  }

  const password = String(userForm.password || '').trim()
  const isEditing = Boolean(userForm.id)
  if (!isEditing && !password) {
    userFormErrors.password = '请输入登录密码'
  } else if (password && password.length < 6) {
    userFormErrors.password = '登录密码至少 6 位'
  }

  return !Object.values(userFormErrors).some(Boolean)
}

function validateRuleForm() {
  if (!String(ruleForm.name || '').trim()) {
    ruleFormErrors.name = '请输入规则名称'
  }

  const pointsPerAction = Number(ruleForm.pointsPerAction)
  if (!Number.isFinite(pointsPerAction) || pointsPerAction < 0) {
    ruleFormErrors.pointsPerAction = '积分需为大于等于 0 的数字'
  }

  const carbonReductionPerAction = Number(ruleForm.carbonReductionPerAction)
  if (!Number.isFinite(carbonReductionPerAction) || carbonReductionPerAction < 0) {
    ruleFormErrors.carbonReductionPerAction = '减碳量需为大于等于 0 的数字'
  }

  const dailyLimit = Number(ruleForm.dailyLimit)
  if (!Number.isInteger(dailyLimit) || dailyLimit <= 0) {
    ruleFormErrors.dailyLimit = '每日上报上限需为正整数'
  }

  return !Object.values(ruleFormErrors).some(Boolean)
}

function validateItemForm() {
  if (!String(itemForm.name || '').trim()) {
    itemFormErrors.name = '请输入商品名称'
  }

  const pointsCost = Number(itemForm.pointsCost)
  if (!Number.isFinite(pointsCost) || pointsCost <= 0) {
    itemFormErrors.pointsCost = '积分单价需为大于 0 的数字'
  }

  const stock = Number(itemForm.stock)
  if (!Number.isInteger(stock) || stock < 0) {
    itemFormErrors.stock = '库存需为大于等于 0 的整数'
  }

  return !Object.values(itemFormErrors).some(Boolean)
}

function clearSectionSearch() {
  sectionSearchKeyword.value = ''
}

function openFirstMatchedSection() {
  const firstMatch = filteredSidebarSections.value[0]
  if (firstMatch) {
    openSection(firstMatch.id)
  }
}

function focusSectionSearch() {
  if (!sectionSearchInputRef.value) {
    return
  }
  sectionSearchInputRef.value.focus()
  sectionSearchInputRef.value.select()
}

function handleWorkspaceShortcut(event) {
  if ((event.ctrlKey || event.metaKey) && String(event.key).toLowerCase() === 'k') {
    event.preventDefault()
    focusSectionSearch()
  }
}

function triggerProfileAvatarUpload() {
  if (profileAvatarUploading.value) return
  profileAvatarInputRef.value?.click()
}

async function onProfileAvatarChange(event) {
  const file = event?.target?.files?.[0]
  if (event?.target) {
    event.target.value = ''
  }
  if (!file) return

  if (!String(file.type || '').startsWith('image/')) {
    profileSaveType.value = 'error'
    profileSaveMessage.value = '请选择图片文件'
    return
  }
  if (Number(file.size || 0) > 5 * 1024 * 1024) {
    profileSaveType.value = 'error'
    profileSaveMessage.value = '头像文件不能超过 5MB'
    return
  }

  profileAvatarUploading.value = true
  profileSaveMessage.value = ''
  try {
    const { data } = await uploadImage(file)
    const avatarUrl = data?.url || data?.path || ''
    if (!avatarUrl) {
      throw new Error('头像地址为空')
    }
    profileCenterForm.avatarUrl = avatarUrl
    if (profile.value) {
      profile.value.avatarUrl = avatarUrl
      profile.value.avatar = avatarUrl
    }
    writeCachedAvatarUrl(avatarUrl)
    profileSaveType.value = 'success'
    profileSaveMessage.value = '头像上传成功，请点击“保存资料”持久化'
  } catch (error) {
    profileSaveType.value = 'error'
    profileSaveMessage.value = error.message || '头像上传失败，请稍后重试'
  } finally {
    profileAvatarUploading.value = false
  }
}

async function saveProfile() {
  profileSaveMessage.value = ''
  try {
    const { data } = await updateAdminProfile({
      fullName: profileCenterForm.name,
      nickname: profileCenterForm.nickname,
      gender: profileCenterForm.gender,
      email: profileCenterForm.email,
      phone: profileCenterForm.phone,
      address: profileCenterForm.address,
      bio: profileCenterForm.bio,
      city: profileCenterForm.city,
      organization: profileCenterForm.organization,
      tags: profileCenterForm.tags.join(','),
      avatarUrl: profileCenterForm.avatarUrl,
      avatar: profileCenterForm.avatarUrl
    })

    syncProfileCenterForm(data)
    profile.value = data
    writeCachedAvatarUrl(data.avatarUrl || data.avatar || profileCenterForm.avatarUrl || '')
    updateSessionUser({ nickname: data.nickname, avatarUrl: data.avatarUrl || data.avatar || profileCenterForm.avatarUrl })
    profileSaveType.value = 'success'
    profileSaveMessage.value = '个人资料已保存'
  } catch (error) {
    profileSaveType.value = 'error'
    profileSaveMessage.value = error.message || '保存失败，请稍后重试'
  }
}

const {
  message,
  messageType,
  clearMessage,
  profile,
  dashboard,
  users,
  rules,
  pendingReports,
  items,
  orders,
  userForm,
  ruleForm,
  itemForm,
  enabledItemCount,
  totalItemStock,
  loadAll,
  saveUser,
  editUser,
  resetUserForm,
  deleteUser,
  saveRule,
  editRule,
  resetRuleForm,
  deleteRule,
  audit,
  saveItem,
  editItem,
  resetItemForm,
  deleteItem,
  updateOrder,
  badgeClass,
  formatStatusLabel,
  fmt,
  resolveImageUrl,
  openImage,
  logout
} = useAdminPage()

const profileCenterForm = reactive({
  name: '社区管理员',
  gender: '保密',
  nickname: '运营管理员',
  email: 'admin@lowcarbon.local',
  phone: '13800009999',
  address: '广东省深圳市南山区低碳社区运营中心',
  bio: '负责社区低碳积分平台的规则配置、审核治理、商城履约与整体运营推进。',
  role: '社区运营管理员',
  city: '广东省深圳市',
  organization: '低碳社区运营中心',
  avatarUrl: readCachedAvatarUrl(),
  tags: ['规则治理', '审核管理', '运营协同', '社区激励']
})

function parseTags(value) {
  if (!value) {
    return []
  }
  return String(value)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function syncProfileCenterForm(source) {
  profileCenterForm.name = source.fullName || source.nickname || profileCenterForm.name
  profileCenterForm.gender = source.gender || '保密'
  profileCenterForm.nickname = source.nickname || profileCenterForm.nickname
  profileCenterForm.email = source.email || 'admin@lowcarbon.local'
  profileCenterForm.phone = source.phone || '13800009999'
  profileCenterForm.address = source.address || '广东省深圳市南山区低碳社区运营中心'
  profileCenterForm.bio = source.bio || '负责社区低碳积分平台的规则配置、审核治理、商城履约与整体运营推进。'
  profileCenterForm.role = source.role === 'ADMIN' ? '社区运营管理员' : '社区低碳居民'
  profileCenterForm.city = source.city || '广东省深圳市'
  profileCenterForm.organization = source.organization || '低碳社区运营中心'
  profileCenterForm.avatarUrl = source.avatarUrl || source.avatar || readCachedAvatarUrl() || profileCenterForm.avatarUrl || ''
  writeCachedAvatarUrl(profileCenterForm.avatarUrl)
  profileCenterForm.tags = parseTags(source.tags)
  if (!profileCenterForm.tags.length) {
    profileCenterForm.tags = ['规则治理', '审核管理', '运营协同', '社区激励']
  }
}

function updateSessionUser(patch) {
  const raw = sessionStorage.getItem('user')
  if (!raw) {
    return
  }
  try {
    const parsed = JSON.parse(raw)
    sessionStorage.setItem('user', JSON.stringify({ ...parsed, ...patch }))
  } catch {
    // Ignore malformed session cache.
  }
}

watch(
  profile,
  (value) => {
    if (!value) {
      return
    }
    syncProfileCenterForm(value)
  },
  { immediate: true, deep: true }
)

watch([message, messageType], ([nextMessage, nextType]) => {
  if (nextType !== 'success' || !nextMessage) {
    return
  }
  successDialogMessage.value = nextMessage
  successDialogVisible.value = true
  clearMessage()
})

const profileAvatarUrl = computed(() => {
  const candidate = profileCenterForm.avatarUrl || profile.value?.avatarUrl || profile.value?.avatar || ''
  if (!candidate) return defaultProfileAvatarUrl
  const resolved = resolveImageUrl(candidate)
  return resolved || defaultProfileAvatarUrl
})

const usersPageSize = 5
const rulesPageSize = 5
const pendingReportsPageSize = 5
const itemsPageSize = 5
const ordersPageSize = 5

const usersPage = ref(1)
const rulesPage = ref(1)
const pendingReportsPage = ref(1)
const itemsPage = ref(1)
const ordersPage = ref(1)

function clampPage(pageRef, totalItems, pageSize) {
  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize))
  if (pageRef.value > totalPages) {
    pageRef.value = totalPages
  }
}

const pagedUsers = computed(() => {
  const start = (usersPage.value - 1) * usersPageSize
  return users.value.slice(start, start + usersPageSize)
})

const pagedRules = computed(() => {
  const start = (rulesPage.value - 1) * rulesPageSize
  return rules.value.slice(start, start + rulesPageSize)
})

const pagedPendingReports = computed(() => {
  const start = (pendingReportsPage.value - 1) * pendingReportsPageSize
  return pendingReports.value.slice(start, start + pendingReportsPageSize)
})

const pagedItems = computed(() => {
  const start = (itemsPage.value - 1) * itemsPageSize
  return items.value.slice(start, start + itemsPageSize)
})

const pagedOrders = computed(() => {
  const start = (ordersPage.value - 1) * ordersPageSize
  return orders.value.slice(start, start + ordersPageSize)
})

watch(() => users.value.length, (value) => clampPage(usersPage, value, usersPageSize), { immediate: true })
watch(() => rules.value.length, (value) => clampPage(rulesPage, value, rulesPageSize), { immediate: true })
watch(() => pendingReports.value.length, (value) => clampPage(pendingReportsPage, value, pendingReportsPageSize), { immediate: true })
watch(() => items.value.length, (value) => clampPage(itemsPage, value, itemsPageSize), { immediate: true })
watch(() => orders.value.length, (value) => clampPage(ordersPage, value, ordersPageSize), { immediate: true })
watch(
  [openTabs, activeSection],
  () => {
    persistWorkspaceState({
      storageKey: workspaceStateStorageKey,
      openTabs: openTabs.value,
      activeSection: activeSection.value
    })
  },
  { deep: true }
)

onMounted(() => {
  window.addEventListener('keydown', handleWorkspaceShortcut)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleWorkspaceShortcut)
})
</script>

<style scoped src="../styles/admin-view.css"></style>
