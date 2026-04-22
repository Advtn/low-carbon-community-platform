const adminSections = [
  {
    id: 'overview',
    short: '总',
    label: '总览',
    hint: '今日运营概况',
    description: '先看今天的社区脉搏、库存情况和待办任务。',
    visibleInSidebar: true
  },
  {
    id: 'users',
    short: '户',
    label: '用户管理',
    hint: '居民与管理员账号',
    description: '创建与维护居民、管理员账号及其角色信息。',
    visibleInSidebar: true
  },
  {
    id: 'rules',
    short: '规',
    label: '规则管理',
    hint: '积分与减碳口径',
    description: '配置行为规则、积分值、减碳量与日上限。',
    visibleInSidebar: true
  },
  {
    id: 'audits',
    short: '审',
    label: '审核工作台',
    hint: '待审核上报',
    description: '集中处理居民行为上报，完成通过或驳回。',
    visibleInSidebar: true
  },
  {
    id: 'items',
    short: '商',
    label: '商品管理',
    hint: '积分商城库存',
    description: '维护商品信息、库存数量与上下架状态。',
    visibleInSidebar: true
  },
  {
    id: 'orders',
    short: '单',
    label: '订单管理',
    hint: '履约与退款',
    description: '处理兑换订单状态，完成履约或取消退款。',
    visibleInSidebar: true
  },
  {
    id: 'profile',
    short: '我',
    label: '个人中心',
    hint: '查看与编辑资料',
    description: '集中查看并维护你的基础资料、岗位信息与个人介绍。',
    visibleInSidebar: false
  }
]

export default adminSections
