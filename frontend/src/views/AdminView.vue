<template>
  <div class="page">
    <div class="card header">
      <div>
        <h1 class="title">管理员后台</h1>
        <p class="subtitle">用户 / 规则 / 上报审核 / 商城订单 全流程管理</p>
      </div>
      <div style="display: flex; gap: 8px">
        <button class="btn secondary" @click="loadAll">刷新数据</button>
        <button class="btn danger" @click="logout">退出登录</button>
      </div>
    </div>

    <div v-if="message" class="card panel" style="margin-bottom: 16px; color: #a12828">{{ message }}</div>

    <div class="card panel overview-panel">
      <h3>运营概览</h3>
      <div class="overview-grid">
        <div class="overview-card">
          <div class="name">居民用户</div>
          <div class="value">{{ dashboard.residentCount }}</div>
        </div>
        <div class="overview-card">
          <div class="name">启用规则</div>
          <div class="value">{{ dashboard.activeRuleCount }}</div>
        </div>
        <div class="overview-card warn">
          <div class="name">待审核上报</div>
          <div class="value">{{ dashboard.pendingReportCount }}</div>
        </div>
        <div class="overview-card warn">
          <div class="name">待处理订单</div>
          <div class="value">{{ dashboard.pendingOrderCount }}</div>
        </div>
        <div class="overview-card">
          <div class="name">居民总积分</div>
          <div class="value">{{ dashboard.totalResidentPoints }}</div>
        </div>
        <div class="overview-card">
          <div class="name">累计减碳(kg)</div>
          <div class="value">{{ Number(dashboard.totalCarbonReduction || 0).toFixed(2) }}</div>
        </div>
      </div>
      <div class="overview-foot">
        <span>今日提交上报：{{ dashboard.todaySubmittedReports }}</span>
        <span>今日通过上报：{{ dashboard.todayApprovedReports }}</span>
        <span>今日兑换订单：{{ dashboard.todayRedeemOrders }}</span>
        <span>今日完成订单：{{ dashboard.todayCompletedOrders }}</span>
      </div>
    </div>

    <div class="grid">
      <section class="card panel" style="grid-column: span 6">
        <h3>用户管理</h3>
        <div class="form-row">
          <div class="field">
            <label class="field-label">登录用户名</label>
            <input v-model.trim="userForm.username" class="input" placeholder="例如：alice01（英文/数字）" />
          </div>
          <div class="field">
            <label class="field-label">用户昵称</label>
            <input v-model.trim="userForm.nickname" class="input" placeholder="例如：张三" />
          </div>
        </div>
        <div class="form-row">
          <div class="field">
            <label class="field-label">登录密码</label>
            <input v-model.trim="userForm.password" class="input" placeholder="例如：123456" />
          </div>
          <div class="field">
            <label class="field-label">角色</label>
            <select v-model="userForm.role" class="select">
              <option value="RESIDENT">居民（RESIDENT）</option>
              <option value="ADMIN">管理员（ADMIN）</option>
            </select>
          </div>
        </div>
        <div style="display: flex; gap: 8px; margin-bottom: 10px">
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
                  <button class="btn secondary" @click="editUser(u)">编辑</button>
                  <button class="btn danger" @click="deleteUser(u.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card panel" style="grid-column: span 6">
        <h3>行为规则管理</h3>
        <div class="form-row">
          <div class="field">
            <label class="field-label">规则名称</label>
            <input v-model.trim="ruleForm.name" class="input" placeholder="例如：步行或骑行出行" />
          </div>
          <div class="field">
            <label class="field-label">单次奖励积分（分）</label>
            <input v-model.number="ruleForm.pointsPerAction" type="number" min="0" class="input" placeholder="例如：10" />
          </div>
        </div>
        <div class="form-row">
          <div class="field">
            <label class="field-label">单次减碳量（kg）</label>
            <input v-model.number="ruleForm.carbonReductionPerAction" type="number" min="0" step="0.1" class="input" placeholder="例如：0.5" />
          </div>
          <div class="field">
            <label class="field-label">每日上报上限（次/人）</label>
            <input v-model.number="ruleForm.dailyLimit" type="number" min="1" class="input" placeholder="例如：3" />
          </div>
        </div>
        <div class="form-row">
          <div class="field">
            <label class="field-label">规则状态</label>
            <select v-model="ruleForm.active" class="select">
              <option :value="true">启用（居民可上报）</option>
              <option :value="false">停用（居民不可上报）</option>
            </select>
          </div>
          <div class="field" />
        </div>
        <div class="field">
          <label class="field-label">规则说明</label>
          <textarea v-model.trim="ruleForm.description" class="textarea" placeholder="例如：通勤使用步行/骑行方式，并提供简要凭证"></textarea>
        </div>
        <div style="display: flex; gap: 8px; margin: 10px 0">
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
                <td>{{ r.active ? '启用' : '停用' }}</td>
                <td>
                  <button class="btn secondary" @click="editRule(r)">编辑</button>
                  <button class="btn danger" @click="deleteRule(r.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card panel" style="grid-column: span 12">
        <h3>上报审核</h3>
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
                    class="proof-thumb"
                    alt="凭证图片"
                    @click="openImage(resolveImageUrl(row.proofImageUrl))"
                  />
                  <span v-else>-</span>
                </td>
                <td>{{ fmt(row.submittedAt) }}</td>
                <td>
                  <button class="btn" @click="audit(row.id, true)">通过</button>
                  <button class="btn danger" @click="audit(row.id, false)">驳回</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card panel shop-panel" style="grid-column: span 12">
        <div class="shop-header">
          <div>
            <h3>商城商品管理</h3>
          </div>
          <div class="shop-stats">
            <div class="shop-stat">
              <span>商品总数</span>
              <strong>{{ items.length }}</strong>
            </div>
            <div class="shop-stat">
              <span>上架商品</span>
              <strong>{{ enabledItemCount }}</strong>
            </div>
            <div class="shop-stat">
              <span>总库存</span>
              <strong>{{ totalItemStock }}</strong>
            </div>
          </div>
        </div>

        <div class="shop-layout">
          <div class="shop-editor">
            <h4 class="shop-subtitle">{{ itemForm.id ? '编辑商品' : '新增商品' }}</h4>
            <div class="form-row">
              <div class="field">
                <label class="field-label">商品名称</label>
                <input v-model.trim="itemForm.name" class="input" placeholder="例如：环保帆布袋" />
              </div>
              <div class="field">
                <label class="field-label">兑换积分单价（分）</label>
                <input v-model.number="itemForm.pointsCost" type="number" min="1" class="input" placeholder="例如：100" />
              </div>
            </div>
            <div class="form-row">
              <div class="field">
                <label class="field-label">库存数量（件）</label>
                <input v-model.number="itemForm.stock" type="number" min="0" class="input" placeholder="例如：50" />
              </div>
              <div class="field">
                <label class="field-label">上架状态</label>
                <select v-model="itemForm.enabled" class="select">
                  <option :value="true">上架（居民可见）</option>
                  <option :value="false">下架（居民不可见）</option>
                </select>
              </div>
            </div>
            <div class="field">
              <label class="field-label">商品说明</label>
              <textarea v-model.trim="itemForm.description" class="textarea" placeholder="例如：可重复使用购物袋，尺寸 35x40cm"></textarea>
            </div>
            <div class="shop-editor-actions">
              <button class="btn" @click="saveItem">{{ itemForm.id ? '更新商品' : '新增商品' }}</button>
              <button class="btn secondary" @click="resetItemForm">清空</button>
            </div>
          </div>

          <div class="shop-list">
            <div class="shop-list-head">
              <h4 class="shop-subtitle">商品列表</h4>
              <span class="shop-list-meta">共 {{ items.length }} 个商品</span>
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
                    <td>{{ i.enabled ? '上架' : '下架' }}</td>
                    <td>
                      <button class="btn secondary" @click="editItem(i)">编辑</button>
                      <button class="btn danger" @click="deleteItem(i.id)">删除</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </section>

      <section class="card panel" style="grid-column: span 12">
        <h3>兑换订单管理</h3>
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
                  <button class="btn" @click="updateOrder(o.id, 'COMPLETED')">完成</button>
                  <button class="btn danger" @click="updateOrder(o.id, 'CANCELLED')">取消并退款</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { useAdminPage } from '../composables/useAdminPage'

const {
  message,
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
</script>

<style scoped src="../styles/admin-view.css"></style>
