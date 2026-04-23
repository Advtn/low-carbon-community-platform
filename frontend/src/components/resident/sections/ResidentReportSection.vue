<template>
  <section class="workspace-pane">
    <div class="pane-head">
      <div>
        <span class="workspace-kicker">Submit</span>
        <h2>低碳行为上报</h2>
        <p>从规则选择、里程辅助、凭证说明到图片上传，所有提交动作都在同一个工作台里完成。</p>
      </div>
      <div class="workspace-actions">
        <button class="btn ghost" type="button" @click="emit('reset-form')">重置表单</button>
      </div>
    </div>

    <div class="workspace-form">
      <div class="workspace-form-grid">
        <label class="field">
          <span class="field-label">行为规则</span>
          <select v-model="reportForm.ruleId" class="select">
            <option disabled value="">请选择行为规则</option>
            <option v-for="r in rules" :key="r.id" :value="r.id">
              {{ r.name }} · +{{ r.pointsPerAction }} 分 / {{ r.carbonReductionPerAction }} kg
            </option>
          </select>
        </label>

        <label class="field">
          <span class="field-label">上报次数</span>
          <div class="resident-qty-wrap">
            <button class="workspace-pill" type="button" @click="emit('change-quantity', -1)">-1</button>
            <input v-model.number="reportForm.quantity" type="number" min="1" class="input resident-qty-input" />
            <button class="workspace-pill" type="button" @click="emit('change-quantity', 1)">+1</button>
          </div>
        </label>
      </div>

      <div v-if="selectedRule" class="workspace-panel">
        <span class="workspace-kicker">Rule Summary</span>
        <h3>{{ selectedRule.name }}</h3>
        <p>{{ selectedRule.description || '当前规则暂无补充说明。' }}</p>
        <div class="workspace-inline-stats">
          <div class="stat">
            <span>今日已用</span>
            <strong>{{ todayUsedCount }} 次</strong>
          </div>
          <div class="stat">
            <span>剩余额度</span>
            <strong>{{ remainingQuota }} 次</strong>
          </div>
          <div class="stat">
            <span>预计奖励</span>
            <strong>+{{ estimatedPoints }} 分</strong>
          </div>
          <div class="stat">
            <span>预计减碳</span>
            <strong>{{ estimatedCarbon }} kg</strong>
          </div>
        </div>
      </div>

      <div class="field">
        <span class="field-label">快捷次数</span>
        <div class="workspace-pills">
          <button
            v-for="q in quickQuantities"
            :key="q"
            class="workspace-pill"
            :class="{ active: Number(reportForm.quantity) === q }"
            type="button"
            @click="emit('set-quantity', q)"
          >
            {{ q }} 次
          </button>
        </div>
      </div>

      <div class="field">
        <span class="field-label">凭证模板</span>
        <div class="workspace-pills">
          <button
            v-for="t in proofTemplates"
            :key="t"
            class="workspace-pill"
            type="button"
            @click="emit('use-template', t)"
          >
            {{ t }}
          </button>
        </div>
      </div>

      <template v-if="isCommuteRule">
        <div class="workspace-form-grid">
          <label class="field">
            <span class="field-label">出行方式</span>
            <select v-model="assistForm.mode" class="select">
              <option v-for="m in transportModes" :key="m" :value="m">{{ m }}</option>
            </select>
          </label>

          <label class="field">
            <span class="field-label">里程（km）</span>
            <input v-model="assistForm.distance" class="input" placeholder="可手动填写或自动计算" />
          </label>
        </div>

        <label class="field">
          <span class="field-label">地点补充</span>
          <input v-model.trim="assistForm.location" class="input" placeholder="例如：社区东门到地铁站" />
        </label>

        <div v-if="isRoutePlannerVisible" class="workspace-panel">
          <div class="pane-head">
            <div>
              <span class="workspace-kicker">Route Helper</span>
              <h2>路线辅助选择</h2>
              <p>地图上先选起点再选终点，系统会自动回填路线里程。</p>
            </div>
            <div class="workspace-actions">
              <button class="btn secondary" type="button" @click="emit('locate-current-position')" :disabled="locatingCurrent">
                {{ locatingCurrent ? '定位中...' : '使用当前位置' }}
              </button>
              <button class="btn ghost" type="button" @click="emit('reset-route-planner')">重选路线</button>
            </div>
          </div>

          <div :ref="setMapContainerRef" class="workspace-map"></div>
          <div class="workspace-inline-stats">
            <div class="stat">
              <span>起点</span>
              <strong>{{ routeStartText }}</strong>
            </div>
            <div class="stat">
              <span>终点</span>
              <strong>{{ routeEndText }}</strong>
            </div>
            <div class="stat">
              <span>路线里程</span>
              <strong>{{ routeDistanceKm || '-' }} km</strong>
            </div>
          </div>
          <div v-if="routeLoading" class="inline-message success">路线计算中，请稍候...</div>
          <div v-if="routeError" class="inline-message error">{{ routeError }}</div>
        </div>
      </template>

      <label class="field">
        <span class="field-label">凭证说明</span>
        <textarea
          v-model.trim="reportForm.proofText"
          class="textarea"
          placeholder="例如：今天步行通勤 2 公里，并上传现场图片作为凭证。"
        />
      </label>

      <div class="workspace-grid-2">
        <label class="field">
          <span class="field-label">上传凭证图片</span>
          <input
            :ref="setImageInputRef"
            type="file"
            class="input proof-image-input"
            accept="image/*"
            @change="emit('proof-image-change', $event)"
          />
          <span class="field-help">支持 jpg / png / webp，单张建议控制在 5MB 以内。</span>
        </label>

        <div class="field">
          <span class="field-label">图片预览</span>
          <div class="workspace-upload-preview">
            <img
              v-if="proofImagePreviewUrl"
              :src="proofImagePreviewUrl"
              alt="凭证预览"
              @click="emit('open-image', proofImagePreviewUrl)"
            />
            <div v-else class="workspace-empty">上传后会在这里显示预览</div>
          </div>
          <div class="workspace-actions">
            <button class="btn secondary" type="button" @click="emit('clear-proof-image')" :disabled="uploadingImage">移除图片</button>
          </div>
        </div>
      </div>

      <div v-if="uploadingImage" class="inline-message success">图片上传中，请稍候后再提交。</div>

      <div v-if="selectedRule" class="workspace-panel">
        <span class="workspace-kicker">Quota</span>
        <h3>今日额度进度</h3>
        <p>系统会把待审核与已通过的次数一起计入今日限额。</p>
        <div class="progress">
          <span :style="{ width: quotaPercent + '%' }"></span>
        </div>
        <div class="workspace-legend">
          <span>当前进度 {{ quotaPercent }}%</span>
          <span>预计本次 +{{ estimatedPoints }} 分 / {{ estimatedCarbon }} kg</span>
        </div>
      </div>

      <div
        v-if="reportMessage"
        class="inline-message"
        :class="reportMessageType === 'success' ? 'success' : 'error'"
      >
        {{ reportMessage }}
      </div>

      <div class="workspace-actions">
        <button class="btn" type="button" @click="emit('submit')" :disabled="uploadingImage">提交上报</button>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  rules: {
    type: Array,
    default: () => []
  },
  reportForm: {
    type: Object,
    required: true
  },
  assistForm: {
    type: Object,
    required: true
  },
  selectedRule: {
    type: Object,
    default: null
  },
  isCommuteRule: {
    type: Boolean,
    required: true
  },
  isRoutePlannerVisible: {
    type: Boolean,
    required: true
  },
  todayUsedCount: {
    type: Number,
    required: true
  },
  remainingQuota: {
    type: Number,
    required: true
  },
  estimatedPoints: {
    type: Number,
    required: true
  },
  estimatedCarbon: {
    type: [Number, String],
    required: true
  },
  quickQuantities: {
    type: Array,
    default: () => []
  },
  transportModes: {
    type: Array,
    default: () => []
  },
  proofTemplates: {
    type: Array,
    default: () => []
  },
  setMapContainerRef: {
    type: Function,
    default: null
  },
  setImageInputRef: {
    type: Function,
    default: null
  },
  uploadingImage: {
    type: Boolean,
    required: true
  },
  proofImagePreviewUrl: {
    type: String,
    default: ''
  },
  routeLoading: {
    type: Boolean,
    required: true
  },
  routeError: {
    type: String,
    default: ''
  },
  locatingCurrent: {
    type: Boolean,
    required: true
  },
  routeDistanceKm: {
    type: [Number, String],
    default: ''
  },
  routeStartText: {
    type: String,
    default: ''
  },
  routeEndText: {
    type: String,
    default: ''
  },
  quotaPercent: {
    type: Number,
    required: true
  },
  reportMessage: {
    type: String,
    default: ''
  },
  reportMessageType: {
    type: String,
    default: 'error'
  }
})

const emit = defineEmits([
  'reset-form',
  'change-quantity',
  'set-quantity',
  'use-template',
  'locate-current-position',
  'reset-route-planner',
  'proof-image-change',
  'clear-proof-image',
  'open-image',
  'submit'
])
</script>
