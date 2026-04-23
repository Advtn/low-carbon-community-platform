import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { use, init } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { LabelLayout, UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'

use([PieChart, TooltipComponent, LegendComponent, LabelLayout, UniversalTransition, CanvasRenderer])

export function useBehaviorChart(reports) {
  const chartRef = ref(null)
  let behaviorChart = null

  const behaviorRatioData = computed(() => {
    const sourceReports = Array.isArray(reports.value) ? reports.value : []
    const approvedRows = sourceReports.filter((row) => row.status === 'APPROVED')
    const sourceRows = approvedRows.length ? approvedRows : sourceReports
    const counter = new Map()

    sourceRows.forEach((row) => {
      const ruleName = row.ruleName || '其他行为'
      const quantity = Number(row.quantity || 0)
      counter.set(ruleName, (counter.get(ruleName) || 0) + quantity)
    })

    return Array.from(counter.entries())
      .map(([name, value]) => ({ name, value }))
      .filter((item) => item.value > 0)
      .sort((a, b) => b.value - a.value)
  })

  const hasBehaviorRatioData = computed(() => behaviorRatioData.value.length > 0)
  const behaviorTotalCount = computed(() =>
    behaviorRatioData.value.reduce((sum, item) => sum + Number(item.value || 0), 0)
  )

  function setChartRef(el) {
    chartRef.value = el
  }

  function buildBehaviorChartOption() {
    return {
      color: ['#1d7a52', '#5db07f', '#9dd0a7', '#c7713f', '#e4b793', '#7aa7b0'],
      tooltip: {
        trigger: 'item',
        formatter: '{b}<br/>次数：{c}（{d}%）'
      },
      legend: {
        bottom: 0,
        icon: 'circle',
        textStyle: {
          color: '#5d6f66'
        }
      },
      series: [
        {
          name: '行为占比',
          type: 'pie',
          radius: ['40%', '68%'],
          center: ['50%', '42%'],
          itemStyle: {
            borderColor: '#fffdf7',
            borderWidth: 3
          },
          label: {
            show: true,
            formatter: '{d}%',
            color: '#18322a',
            fontWeight: 600
          },
          data: behaviorRatioData.value
        }
      ]
    }
  }

  function renderBehaviorChart() {
    if (!chartRef.value) return

    if (behaviorChart && behaviorChart.getDom() !== chartRef.value) {
      behaviorChart.dispose()
      behaviorChart = null
    }

    if (!behaviorChart) {
      behaviorChart = init(chartRef.value)
    }

    if (!hasBehaviorRatioData.value) {
      behaviorChart.clear()
      behaviorChart.setOption({
        title: {
          text: '暂无行为占比数据',
          left: 'center',
          top: 'middle',
          textStyle: {
            color: '#8a9a92',
            fontSize: 14,
            fontWeight: 500
          }
        }
      })
      return
    }

    behaviorChart.setOption(buildBehaviorChartOption(), true)
  }

  function resizeBehaviorChart() {
    if (behaviorChart) {
      behaviorChart.resize()
    }
  }

  watch(
    behaviorRatioData,
    async () => {
      await nextTick()
      renderBehaviorChart()
      resizeBehaviorChart()
    },
    { deep: true }
  )

  onMounted(async () => {
    await nextTick()
    renderBehaviorChart()
    window.addEventListener('resize', resizeBehaviorChart)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeBehaviorChart)
    if (behaviorChart) {
      behaviorChart.dispose()
      behaviorChart = null
    }
  })

  return {
    behaviorTotalCount,
    hasBehaviorRatioData,
    setChartRef
  }
}
