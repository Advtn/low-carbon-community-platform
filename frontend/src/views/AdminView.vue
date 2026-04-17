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
            v-for="section in sidebarSections"
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
              <span>搜索工作台</span>
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
                <span class="workspace-avatar-image">{{ avatarInitials }}</span>
              </button>

              <div v-if="avatarMenuOpen" class="workspace-avatar-menu">
                <div class="workspace-avatar-header">
                  <div class="workspace-avatar-image">{{ avatarInitials }}</div>
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
          <div v-if="message" class="inline-message error">{{ message }}</div>

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
              <span class="badge">{{ users.length }} 个账号</span>
            </div>

            <div class="workspace-form">
              <div class="workspace-form-grid">
                <label class="field">
                  <span class="field-label">登录用户名</span>
                  <input v-model.trim="userForm.username" class="input" placeholder="例如：resident_01" />
                </label>
                <label class="field">
                  <span class="field-label">用户昵称</span>
                  <input v-model.trim="userForm.nickname" class="input" placeholder="例如：张三" />
                </label>
              </div>

              <div class="workspace-form-grid">
                <label class="field">
                  <span class="field-label">登录密码</span>
                  <input
                    v-model.trim="userForm.password"
                    class="input"
                    :placeholder="userForm.id ? '留空表示不修改密码' : '至少 6 位'"
                  />
                </label>
                <label class="field">
                  <span class="field-label">角色</span>
                  <select v-model="userForm.role" class="select">
                    <option value="RESIDENT">居民（RESIDENT）</option>
                    <option value="ADMIN">管理员（ADMIN）</option>
                  </select>
                </label>
              </div>

              <div class="workspace-actions">
                <button class="btn" @click="saveUser">{{ userForm.id ? '更新用户' : '新增用户' }}</button>
                <button class="btn secondary" @click="resetUserForm">清空</button>
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
                    <tr v-for="u in users" :key="u.id">
                      <td>{{ u.id }}</td>
                      <td>{{ u.username }}</td>
                      <td>{{ u.nickname }}</td>
                      <td>{{ u.role }}</td>
                      <td>{{ u.totalPoints }}</td>
                      <td>
                        <div class="workspace-actions admin-mini-actions">
                          <button class="btn secondary" @click="editUser(u)">编辑</button>
                          <button class="btn danger" @click="deleteUser(u.id)">删除</button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </section>

          <section v-if="activeSection === 'rules'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Rules</span>
                <h2>行为规则配置</h2>
                <p>维护积分、减碳量、日上限与启停状态，形成居民端的激励逻辑。</p>
              </div>
              <span class="badge">{{ rules.length }} 条规则</span>
            </div>

            <div class="workspace-form">
              <div class="workspace-form-grid">
                <label class="field">
                  <span class="field-label">规则名称</span>
                  <input v-model.trim="ruleForm.name" class="input" placeholder="例如：步行或骑行出行" />
                </label>
                <label class="field">
                  <span class="field-label">单次奖励积分</span>
                  <input v-model.number="ruleForm.pointsPerAction" type="number" min="0" class="input" />
                </label>
              </div>

              <div class="workspace-form-grid">
                <label class="field">
                  <span class="field-label">单次减碳量（kg）</span>
                  <input v-model.number="ruleForm.carbonReductionPerAction" type="number" min="0" step="0.1" class="input" />
                </label>
                <label class="field">
                  <span class="field-label">每日上报上限</span>
                  <input v-model.number="ruleForm.dailyLimit" type="number" min="1" class="input" />
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

              <div class="workspace-actions">
                <button class="btn" @click="saveRule">{{ ruleForm.id ? '更新规则' : '新增规则' }}</button>
                <button class="btn secondary" @click="resetRuleForm">清空</button>
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
                    <tr v-for="r in rules" :key="r.id">
                      <td>{{ r.id }}</td>
                      <td>{{ r.name }}</td>
                      <td>{{ r.pointsPerAction }}</td>
                      <td>{{ r.carbonReductionPerAction }}</td>
                      <td>{{ r.dailyLimit }}</td>
                      <td><span :class="r.active ? 'badge' : 'badge err'">{{ r.active ? '启用' : '停用' }}</span></td>
                      <td>
                        <div class="workspace-actions admin-mini-actions">
                          <button class="btn secondary" @click="editRule(r)">编辑</button>
                          <button class="btn danger" @click="deleteRule(r.id)">删除</button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
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
                  <tr v-for="row in pendingReports" :key="row.id">
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
          </section>

          <section v-if="activeSection === 'items'" class="workspace-pane">
            <div class="pane-head">
              <div>
                <span class="workspace-kicker">Mall</span>
                <h2>商城商品管理</h2>
                <p>在统一工作区里维护商品、库存和上下架状态，让兑换端与库存治理同步更新。</p>
              </div>
              <div class="workspace-inline-stats">
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
            </div>

            <div class="workspace-grid-2">
              <div class="workspace-form">
                <div class="workspace-form-grid">
                  <label class="field">
                    <span class="field-label">商品名称</span>
                    <input v-model.trim="itemForm.name" class="input" placeholder="例如：环保帆布袋" />
                  </label>
                  <label class="field">
                    <span class="field-label">兑换积分单价</span>
                    <input v-model.number="itemForm.pointsCost" type="number" min="1" class="input" />
                  </label>
                </div>

                <div class="workspace-form-grid">
                  <label class="field">
                    <span class="field-label">库存数量</span>
                    <input v-model.number="itemForm.stock" type="number" min="0" class="input" />
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

                <div class="workspace-actions">
                  <button class="btn" @click="saveItem">{{ itemForm.id ? '更新商品' : '新增商品' }}</button>
                  <button class="btn secondary" @click="resetItemForm">清空</button>
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
                    <tr v-for="i in items" :key="i.id">
                      <td>{{ i.id }}</td>
                      <td>{{ i.name }}</td>
                      <td>{{ i.pointsCost }}</td>
                      <td>{{ i.stock }}</td>
                      <td><span :class="i.enabled ? 'badge' : 'badge err'">{{ i.enabled ? '上架' : '下架' }}</span></td>
                      <td>
                        <div class="workspace-actions admin-mini-actions">
                          <button class="btn secondary" @click="editItem(i)">编辑</button>
                          <button class="btn danger" @click="deleteItem(i.id)">删除</button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
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
                  <tr v-for="o in orders" :key="o.id">
                    <td>{{ o.id }}</td>
                    <td>{{ o.nickname }}</td>
                    <td>{{ o.itemName }}</td>
                    <td>{{ o.quantity }}</td>
                    <td>{{ o.totalPoints }}</td>
                    <td><span :class="badgeClass(o.status)">{{ o.status }}</span></td>
                    <td>{{ fmt(o.createdAt) }}</td>
                    <td>
                      <div class="workspace-actions admin-mini-actions">
                        <button class="btn" @click="updateOrder(o.id, 'COMPLETED')">完成</button>
                        <button class="btn danger" @click="updateOrder(o.id, 'CANCELLED')">取消并退款</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
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
                  <div class="profile-avatar">{{ avatarInitials }}</div>
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
import { computed, reactive, ref, watch } from 'vue'
import { useAdminPage } from '../composables/useAdminPage'
import { updateAdminProfile } from '../services/adminService'

const adminSections = [
  { id: 'overview', short: '总', label: '总览', hint: '今日运营概况', description: '先看今天的社区脉搏、库存情况和待办任务。', visibleInSidebar: true },
  { id: 'users', short: '户', label: '用户管理', hint: '居民与管理员账号', description: '创建与维护居民、管理员账号及其角色信息。', visibleInSidebar: true },
  { id: 'rules', short: '规', label: '规则管理', hint: '积分与减碳口径', description: '配置行为规则、积分值、减碳量与日上限。', visibleInSidebar: true },
  { id: 'audits', short: '审', label: '审核工作台', hint: '待审核上报', description: '集中处理居民行为上报，完成通过或驳回。', visibleInSidebar: true },
  { id: 'items', short: '商', label: '商品管理', hint: '积分商城库存', description: '维护商品信息、库存数量与上下架状态。', visibleInSidebar: true },
  { id: 'orders', short: '单', label: '订单管理', hint: '履约与退款', description: '处理兑换订单状态，完成履约或取消退款。', visibleInSidebar: true },
  { id: 'profile', short: '我', label: '个人中心', hint: '查看与编辑资料', description: '集中查看并维护你的基础资料、岗位信息与个人介绍。', visibleInSidebar: false }
]

const sidebarSections = computed(() => adminSections.filter((section) => section.visibleInSidebar))
const openTabs = ref(['overview'])
const activeSection = ref('overview')
const avatarMenuOpen = ref(false)
const profileSaveMessage = ref('')
const profileSaveType = ref('success')

const openTabSections = computed(() =>
  openTabs.value
    .map((id) => adminSections.find((section) => section.id === id))
    .filter(Boolean)
)

const activeSectionMeta = computed(
  () => adminSections.find((section) => section.id === activeSection.value) || null
)

function openSection(sectionId) {
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
      tags: profileCenterForm.tags.join(',')
    })

    syncProfileCenterForm(data)
    profile.value = data
    updateSessionUser({ nickname: data.nickname })
    profileSaveType.value = 'success'
    profileSaveMessage.value = '个人资料已保存'
  } catch (error) {
    profileSaveType.value = 'error'
    profileSaveMessage.value = error.message || '保存失败，请稍后重试'
  }
}

const {
  message,
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

const avatarInitials = computed(() => {
  const text = String(profileCenterForm.name || 'AD').trim()
  return text.slice(0, 2).toUpperCase()
})
</script>

<style scoped src="../styles/admin-view.css"></style>
