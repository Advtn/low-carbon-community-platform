import { ref, watch } from 'vue'

export function useAdminDialogs({
  userForm,
  ruleForm,
  itemForm,
  editUser,
  resetUserForm,
  saveUser,
  editRule,
  resetRuleForm,
  saveRule,
  editItem,
  resetItemForm,
  saveItem,
  message,
  messageType,
  clearMessage
}) {
  const userDialogOpen = ref(false)
  const ruleDialogOpen = ref(false)
  const itemDialogOpen = ref(false)
  const successDialogVisible = ref(false)
  const successDialogMessage = ref('')

  watch([message, messageType], ([nextMessage, nextType]) => {
    if (nextType !== 'success' || !nextMessage) {
      return
    }
    successDialogMessage.value = nextMessage
    successDialogVisible.value = true
    clearMessage()
  })

  function openCreateUserDialog() {
    resetUserForm()
    userDialogOpen.value = true
  }

  function openEditUserDialog(user) {
    editUser(user)
    userDialogOpen.value = true
  }

  function closeUserDialog() {
    userDialogOpen.value = false
    resetUserForm()
  }

  async function submitUserDialog() {
    const ok = await saveUser()
    if (ok) {
      userDialogOpen.value = false
    }
  }

  function openCreateRuleDialog() {
    resetRuleForm()
    ruleDialogOpen.value = true
  }

  function openEditRuleDialog(rule) {
    editRule(rule)
    ruleDialogOpen.value = true
  }

  function closeRuleDialog() {
    ruleDialogOpen.value = false
    resetRuleForm()
  }

  async function submitRuleDialog() {
    const ok = await saveRule()
    if (ok) {
      ruleDialogOpen.value = false
    }
  }

  function openCreateItemDialog() {
    resetItemForm()
    itemDialogOpen.value = true
  }

  function openEditItemDialog(item) {
    editItem(item)
    itemDialogOpen.value = true
  }

  function closeItemDialog() {
    itemDialogOpen.value = false
    resetItemForm()
  }

  async function submitItemDialog() {
    const ok = await saveItem()
    if (ok) {
      itemDialogOpen.value = false
    }
  }

  function closeSuccessDialog() {
    successDialogVisible.value = false
  }

  return {
    userDialogOpen,
    ruleDialogOpen,
    itemDialogOpen,
    successDialogVisible,
    successDialogMessage,
    openCreateUserDialog,
    openEditUserDialog,
    closeUserDialog,
    submitUserDialog,
    openCreateRuleDialog,
    openEditRuleDialog,
    closeRuleDialog,
    submitRuleDialog,
    openCreateItemDialog,
    openEditItemDialog,
    closeItemDialog,
    submitItemDialog,
    closeSuccessDialog
  }
}
