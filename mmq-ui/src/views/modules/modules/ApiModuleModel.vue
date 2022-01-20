<template>
  <a-drawer
    :confirmLoading="confirmLoading"
    title="保存新资源"
    :width="720"
    :visible="visible"
    :body-style="{ paddingBottom: '80px' }"
    @close="onClose"
  >
    <a-form :form="form" layout="vertical" hide-required-mark>
      <a-form-item label="appId">
        <a-input
          v-decorator="['configs.appId', { rules: [{ required: true, message: '请输入appId' }] }]"
          placeholder="请输入appId"
        />
      </a-form-item>
      <a-form-item label="appSecret">
        <a-input
          v-decorator="['configs.appSecret', { rules: [{ required: true, message: '请输入appSecret' }] }]"
          placeholder="请输入appSecret"
        />
      </a-form-item>
    </a-form>
    <div
      :style="{
        position: 'absolute',
        right: 0,
        bottom: 0,
        width: '100%',
        borderTop: '1px solid #e9e9e9',
        padding: '10px 16px',
        background: '#fff',
        textAlign: 'right',
        zIndex: 1
      }"
    >
      <a-button :style="{ marginRight: '8px' }" @click="onClose"> Cancel </a-button>
      <a-button type="primary" @click="handleOk"> Submit </a-button>
    </div>
  </a-drawer>
</template>

<script>
import { getAction, putAction } from '@/api/manage'
export default {
  data() {
    return {
      title: '操作',
      visible: false,
      curRecord: null,
      confirmLoading: false,
      curResources: [],
      selectResources: [],
      type: '',
      form: this.$form.createForm(this),
      url: {
        listResources: '/v1/resources/resources',
        update: '/v1/modules'
      }
    }
  },
  created() {
    this.listResources()
  },
  methods: {
    listResources() {
      getAction(this.url.listResources, {}).then(res => {
        this.curResources = []
        res.data.forEach(resource => {
          this.curResources.push(resource)
        })
        console.log(this.curResources)
      })
    },
    update(record) {
      console.log(record)
      this.form.resetFields()
      this.visible = true
      this.curRecord = record
      if (record) {
        this.key = record.key
        this.$nextTick(() => {
          this.form.setFieldsValue({
            key: record.key,
            enable: record.enable,
            description: record.description,
            icon: record.icon,
            modelEnum: record.modelEnum,
            configs: {
              appSecret: record.configs.appSecret,
              appId: record.configs.appId
            }
          })
        })
      }
    },
    typeChange(value) {
      console.log(value)
      this.selectResources = this.curResources.filter(x => value === x.type)
      this.curType = value
    },
    onClose() {
      this.visible = false
    },
    handleOk() {
      const that = this
      // 触发表单验证
      this.form.validateFields((err, values) => {
        console.log(err)
        if (!err) {
          that.confirmLoading = true
          console.log(values)
          this.curRecord.configs = values.configs
          const formData = Object.assign({}, this.curRecord)
          console.log(formData)
          const obj = putAction(this.url.update, formData)
          obj
            .then(res => {
              if (res.code === 200) {
                that.$message.success(res.message)
                that.$emit('ok')
              } else {
                that.$message.warning(res.message)
              }
            })
            .finally(() => {
              that.confirmLoading = false
              that.onClose()
            })
        }
      })
    },
    filterOption(input, option) {
      if (!option.componentOptions.children[0].text) {
        return false
      }
      return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    },
    handleCancel() {
      this.onClose()
    }
  }
}
</script>

<style scoped>
</style>
