<template>
  <div>
    <el-dialog v-model="dialogVisible" @open="onOpen" @close="onClose" title="鉴定审批">
      <el-form ref="formRef" :model="formData" :rules="rules" size="large" label-width="150px"
               label-position="left">
        <el-row :gutter="15">
          <el-col :span="24">
            <el-form-item label="审批结果" prop="approvalResult">
              <el-radio-group v-model="formData.approvalResult" size="default">
                <el-radio v-for="(item, index) in approvalResultOptions" :key="index" :label="item.value"
                          :disabled="item.disabled">{{ item.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="approvalComment">
              <el-input v-model="formData.approvalComment" type="textarea" placeholder="请输入备注"
                        :autosize="{ minRows: 4, maxRows: 4 }" style="width: 100%"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="handelConfirm">确定</el-button>
      </div>
    </el-dialog>

  </div>
</template>
<script setup>
import {
  ElMessage
}
from 'element-plus'


import { defineOptions } from 'vue'


// 显式定义组件名称
defineOptions({
  name: 'IdentificationArchive'
})

const formRef = ref()
const data = reactive({
  formData: {
    approvalResult: 1,
    approvalComment: undefined,
  },
  rules: {
    approvalResult: [{
      required: true,
      message: '审批结果不能为空',
      trigger: 'change'
    }],
    approvalComment: [{
      required: true,
      message: '审批意见不能为空',
      trigger: 'blue'
    }],
  }
})
const {
  formData,
  rules
} = toRefs(data)
const approvalResultOptions = ref([{
  "label": "同意",
  "value": '通过'
}, {
  "label": "拒绝",
  "value": '拒绝'
}])
// 弹窗设置
const dialogVisible = defineModel()
// 弹窗确认回调
const emit = defineEmits(['confirm'])
/**
 * @name: 弹窗打开后执行
 * @description: 弹窗打开后执行方法
 * @return {*}
 */
function onOpen() {}
/**
 * @name: 弹窗关闭时执行
 * @description: 弹窗关闭方法，重置表单
 * @return {*}
 */
function onClose() {
  formRef.value.resetFields()
}
/**
 * @name: 弹窗取消
 * @description: 弹窗取消方法
 * @return {*}
 */
function close() {
  dialogVisible.value = false
}
/**
 * @name: 弹窗表单提交
 * @description: 弹窗表单提交方法
 * @return {*}
 */
function handelConfirm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    // TODO 提交表单
    close()
    // 回调父级组件
    emit('confirm', data.formData)
  })
}

</script>
<style>
</style>
