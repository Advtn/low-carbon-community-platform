<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Rules</span>
        <h2>行为规则配置</h2>
        <p>维护积分、减碳量、日上限与启停状态，形成居民端的激励逻辑。</p>
      </div>
      <div class="workspace-actions">
        <span class="badge">{{ rules.length }} 条规则</span>
        <button class="btn" @click="emit('create-rule')">新增规则</button>
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
          <tr v-for="rule in pagedRules" :key="rule.id">
            <td>{{ rule.id }}</td>
            <td>{{ rule.name }}</td>
            <td>{{ rule.pointsPerAction }}</td>
            <td>{{ rule.carbonReductionPerAction }}</td>
            <td>{{ rule.dailyLimit }}</td>
            <td><span :class="rule.active ? 'badge' : 'badge err'">{{ rule.active ? '启用' : '停用' }}</span></td>
            <td>
              <div class="workspace-actions admin-mini-actions">
                <button class="btn secondary" @click="emit('edit-rule', rule)">编辑</button>
                <button class="btn danger" @click="emit('delete-rule', rule.id)">删除</button>
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
      @change="emit('update:rulesPage', $event)"
    />

    <div v-if="ruleDialogOpen" class="workspace-modal-mask" @click.self="emit('close-rule-dialog')">
      <section class="workspace-modal-card">
        <header class="workspace-modal-head">
          <div>
            <span class="workspace-kicker">Rule</span>
            <h3>{{ ruleForm.id ? '编辑规则' : '新增规则' }}</h3>
          </div>
          <button class="workspace-modal-close" type="button" aria-label="关闭弹窗" @click="emit('close-rule-dialog')">×</button>
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
                @input="emit('clear-rule-field-error', 'name')"
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
                @input="emit('clear-rule-field-error', 'pointsPerAction')"
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
                @input="emit('clear-rule-field-error', 'carbonReductionPerAction')"
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
                @input="emit('clear-rule-field-error', 'dailyLimit')"
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
          <button class="btn" type="button" @click="emit('submit-rule-dialog')">
            {{ ruleForm.id ? '保存更新' : '确认新增' }}
          </button>
          <button class="btn secondary" type="button" @click="emit('close-rule-dialog')">取消</button>
        </footer>
      </section>
    </div>
  </section>
</template>

<script setup>
import AppPagination from '../../AppPagination.vue'

defineProps({
  rules: {
    type: Array,
    default: () => []
  },
  pagedRules: {
    type: Array,
    default: () => []
  },
  rulesPageSize: {
    type: Number,
    required: true
  },
  rulesPage: {
    type: Number,
    required: true
  },
  ruleDialogOpen: {
    type: Boolean,
    default: false
  },
  ruleForm: {
    type: Object,
    required: true
  },
  ruleFormErrors: {
    type: Object,
    required: true
  }
})

const emit = defineEmits([
  'update:rulesPage',
  'create-rule',
  'edit-rule',
  'delete-rule',
  'close-rule-dialog',
  'submit-rule-dialog',
  'clear-rule-field-error'
])
</script>
