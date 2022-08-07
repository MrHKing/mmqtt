<template>
  <a-drawer
    :confirmLoading="confirmLoading"
    :title="view.plugsName"
    :width="720"
    :visible="visible"
    :body-style="{ paddingBottom: '80px' }"
    @close="onClose"
  >
    <a-form layout="vertical" hide-required-mark>
      <div :key="index" v-for="(item, index) in view.propertyItems">
        <a-form-item :label="item.label">
          <a-input
            v-if="item.component === 'INPUT'"
            v-model="item.modal"
          />
          <a-select
            v-if="item.component === 'SELECT'"
            v-model="item.modal"
          >
            <a-select-option v-for="(resource, index) in item.value" :key="index" :value="resource.value">
              {{ resource.key }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </div>
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
import { putAction } from '@/api/manage'
export default {
  data() {
    return {
      title: '编辑插件',
      visible: false,
      curRecord: null,
      confirmLoading: false,
      view: {
        plugsName: '',
        plugsCode: '',
        isEnable: false,
        propertyItems: [
          {
            label: '',
            code: '',
            component: '',
            value: [
              {
                key: '',
                value: ''
              }
            ],
            modal: ''
          }
        ]
      },
      selectResources: [],
      type: '',
      url: {
        update: '/v1/plugs'
      }
    }
  },
  created() {
  },
  methods: {
    update(record) {
      console.log(record)
      this.visible = true
      this.view = record
      console.log(this.view)
    },
    onClose() {
      this.visible = false
    },
    handleOk() {
      this.confirmLoading = true
      const obj = putAction(this.url.update, this.view)
      obj
        .then(res => {
          if (res.code === 200) {
            this.$message.success(res.message)
            this.$emit('ok')
          } else {
            this.$message.warning(res.message)
          }
        })
        .finally(() => {
          this.confirmLoading = false
          this.onClose()
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
