<template>
  <WorkspaceShell
    brand-mark="AD"
    brand-title="社区运营后台"
    brand-subtitle="规则、审核、商城与订单协同"
    nav-aria-label="管理员模块导航"
    :sections="filteredSidebarSections"
    :search-keyword="sectionSearchKeyword"
    @update:searchKeyword="sectionSearchKeyword = $event"
    :set-search-input-ref="setSectionSearchInput"
    search-aria-label="搜索管理端模块"
    :open-tabs="openTabSections"
    :active-section="activeSection"
    @update:activeSection="activeSection = $event"
    :active-section-meta="activeSectionMeta"
    :avatar-menu-open="avatarMenuOpen"
    @update:avatarMenuOpen="avatarMenuOpen = $event"
    :profile-avatar-url="profileAvatarUrl"
    :profile-name="profileCenterForm.name"
    :profile-role="profileCenterForm.role"
    footer-title="今日待处理"
    :footer-text="`待审核上报 ${dashboard.pendingReportCount} 条，待处理订单 ${dashboard.pendingOrderCount} 笔。`"
    breadcrumb-prefix="管理员端"
    empty-selection-title="请选择一个工作模块"
    empty-selection-description="左侧点击一个模块即可在顶部生成最近打开页签，关闭后会自动回到上一个仍然打开的页签。"
    empty-state-title="从左侧打开一个管理模块"
    empty-state-description="例如先打开“审核工作台”处理待审核上报，或者打开“商品管理”维护积分商城库存。"
    @open-section="openSection"
    @close-section="closeSection"
    @clear-section-search="clearSectionSearch"
    @open-first-matched-section="openFirstMatchedSection"
    @refresh="loadAll"
    @open-profile="openProfileCenter"
    @logout="logout"
  >
    <template #feedback>
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
    </template>

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

    <ProfileCenterPanel
      v-if="activeSection === 'profile'"
      description-text="集中查看并维护你的基础资料、岗位信息与个人介绍。"
      :form="profileCenterForm"
      :avatar-url="profileAvatarUrl"
      :save-message="profileSaveMessage"
      :save-type="profileSaveType"
      :avatar-uploading="profileAvatarUploading"
      :set-avatar-input-ref="setProfileAvatarInput"
      role-meta-mark="岗"
      @save="saveProfile"
      @trigger-avatar-upload="triggerProfileAvatarUpload"
      @avatar-change="onProfileAvatarChange"
    />
  </WorkspaceShell>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useAdminPage } from '../composables/useAdminPage'
import { usePagination } from '../composables/shared/usePagination'
import { useProfileCenter } from '../composables/shared/useProfileCenter'
import { useWorkspaceState } from '../composables/workspace/useWorkspaceState'
import { updateAdminProfile, uploadImage } from '../services/adminService'
import AppPagination from '../components/AppPagination.vue'
import ProfileCenterPanel from '../components/profile/ProfileCenterPanel.vue'
import WorkspaceShell from '../components/workspace/WorkspaceShell.vue'
import adminSections from '../constants/adminSections'

const workspaceStateStorageKey = 'lowcarbon:admin-workspace:v1'
const {
  sectionSearchKeyword,
  setSectionSearchInput,
  filteredSidebarSections,
  openTabs,
  activeSection,
  openTabSections,
  activeSectionMeta,
  openSection,
  closeSection,
  clearSectionSearch,
  openFirstMatchedSection
} = useWorkspaceState({
  storageKey: workspaceStateStorageKey,
  sections: adminSections
})
const avatarMenuOpen = ref(false)
const userDialogOpen = ref(false)
const ruleDialogOpen = ref(false)
const itemDialogOpen = ref(false)
const successDialogVisible = ref(false)
const successDialogMessage = ref('')
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

watch([message, messageType], ([nextMessage, nextType]) => {
  if (nextType !== 'success' || !nextMessage) {
    return
  }
  successDialogMessage.value = nextMessage
  successDialogVisible.value = true
  clearMessage()
})

const {
  profileCenterForm,
  profileSaveMessage,
  profileSaveType,
  profileAvatarUploading,
  profileAvatarUrl,
  setProfileAvatarInput,
  triggerProfileAvatarUpload,
  onProfileAvatarChange,
  saveProfile
} = useProfileCenter({
  storageKey: 'lowcarbon:admin-avatar:v1',
  defaults: {
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
    avatarUrl: '',
    tags: ['规则治理', '审核管理', '运营协同', '社区激励']
  },
  sourceWatcher: () => profile.value,
  resolveImageUrl,
  uploadImage,
  updateProfile: updateAdminProfile,
  applyProfileData: (data) => {
    profile.value = data
  },
  resolveDefaultEmail: () => 'admin@lowcarbon.local'
})

const usersPageSize = 5
const rulesPageSize = 5
const pendingReportsPageSize = 5
const itemsPageSize = 5
const ordersPageSize = 5

const { currentPage: usersPage, pagedItems: pagedUsers } = usePagination(users, usersPageSize)
const { currentPage: rulesPage, pagedItems: pagedRules } = usePagination(rules, rulesPageSize)
const { currentPage: pendingReportsPage, pagedItems: pagedPendingReports } = usePagination(
  pendingReports,
  pendingReportsPageSize
)
const { currentPage: itemsPage, pagedItems: pagedItems } = usePagination(items, itemsPageSize)
const { currentPage: ordersPage, pagedItems: pagedOrders } = usePagination(orders, ordersPageSize)
</script>

<style scoped src="../styles/admin-view.css"></style>
