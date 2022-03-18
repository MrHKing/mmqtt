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
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="资源类型">
            <a-select
              @change="typeChange"
              v-decorator="[
                'type',
                {
                  rules: [{ required: true, message: '请选择资源类型' }]
                }
              ]"
              placeholder="请选择资源类型"
            >
              <a-select-option value="MYSQL"> Mysql </a-select-option>
              <a-select-option value="POSTGRESQL"> Postgresql </a-select-option>
              <a-select-option value="SQLSERVER"> SqlServer </a-select-option>
              <a-select-option value="TDENGINE"> Tdengine </a-select-option>
              <a-select-option value="KAFKA"> Kafka </a-select-option>
              <a-select-option value="MQTT_BROKER"> MQTT Broker </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="测试连接">
            <a-button :style="{ marginRight: '8px' }" type="primary" @click="handleConnect"> Test Connect </a-button>
          </a-form-item>
        </a-col>
      </a-row>
      <div v-show="type === 'MYSQL' || type === 'POSTGRESQL' || type === 'SQLSERVER' || type === 'TDENGINE'">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="IP">
              <a-input
                v-decorator="[
                  'resource.ip',
                  {
                    rules: [
                      {
                        required:
                          type === 'MYSQL' || type === 'POSTGRESQL' || type === 'SQLSERVER' || type === 'TDENGINE'
                            ? true
                            : false,
                        message: '请输入IP'
                      }
                    ]
                  }
                ]"
                placeholder="请输入IP"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="PORT">
              <a-input
                v-decorator="[
                  'resource.port',
                  {
                    rules: [
                      {
                        required:
                          type === 'MYSQL' || type === 'POSTGRESQL' || type === 'SQLSERVER' || type === 'TDENGINE'
                            ? true
                            : false,
                        message: '请输入端口'
                      }
                    ]
                  }
                ]"
                placeholder="请输入端口"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="账户">
              <a-input
                v-decorator="[
                  'resource.username',
                  {
                    rules: [
                      {
                        required:
                          type === 'MYSQL' || type === 'POSTGRESQL' || type === 'SQLSERVER' || type === 'TDENGINE'
                            ? true
                            : false,
                        message: '请输入账户'
                      }
                    ]
                  }
                ]"
                placeholder="请输入账户"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="密码">
              <a-input-password
                v-decorator="[
                  'resource.password',
                  {
                    rules: [
                      {
                        required:
                          type === 'MYSQL' || type === 'POSTGRESQL' || type === 'SQLSERVER' || type === 'TDENGINE'
                            ? true
                            : false,
                        message: '请输入密码'
                      }
                    ]
                  }
                ]"
                placeholder="请输入密码"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="数据库">
              <a-input
                v-decorator="[
                  'resource.databaseName',
                  {
                    rules: [
                      {
                        required:
                          type === 'MYSQL' || type === 'POSTGRESQL' || type === 'SQLSERVER' || type === 'TDENGINE'
                            ? true
                            : false,
                        message: '请输入要连接的数据库'
                      }
                    ]
                  }
                ]"
                placeholder="请输入要连接的数据库"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12"> </a-col>
        </a-row>
      </div>
      <div v-show="type === 'KAFKA'">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Kafka服务">
              <a-input
                v-decorator="[
                  'resource.server',
                  {
                    rules: [{ required: type === 'KAFKA' ? true : false, message: '请输入Kafka服务' }]
                  }
                ]"
                placeholder="请输入Kafka服务"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12"> </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Kafka账户">
              <a-input
                v-decorator="[
                  'resource.username',
                  {
                    rules: [{ required: false, message: '请输入Kafka账户' }]
                  }
                ]"
                placeholder="请输入Kafka账户"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Kafka密码">
              <a-input-password
                v-decorator="[
                  'resource.password',
                  {
                    rules: [{ required: false, message: '请输入Kafka密码' }]
                  }
                ]"
                placeholder="请输入Kafka密码"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </div>
      <div v-show="type === 'MQTT_BROKER'">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="MQTT服务">
              <a-input
                v-decorator="[
                  'resource.server',
                  {
                    rules: [{ required: type === 'MQTT_BROKER' ? true : false, message: '请输入MQTT服务地址' }]
                  }
                ]"
                placeholder="[ssl://ip:port] OR [tcp://ip:port]"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="SSL启用">
              <a-select
                v-decorator="[
                  'resource.sslEnable',
                  {
                    rules: [{ required: false, message: '请输入SSL启用' }]
                  }
                ]"
              >
                <a-select-option value="true">
                  true
                </a-select-option>
                <a-select-option value="false">
                  false
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="MQTT账户">
              <a-input
                v-decorator="[
                  'resource.username',
                  {
                    rules: [{ required: false, message: '请输入Kafka账户' }]
                  }
                ]"
                placeholder="请输入MQTT账户"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="MQTT密码">
              <a-input-password
                v-decorator="[
                  'resource.password',
                  {
                    rules: [{ required: false, message: '请输入MQTT密码' }]
                  }
                ]"
                placeholder="请输入MQTT密码"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </div>
      <a-row :gutter="16">
        <a-col :span="24">
          <a-form-item label="备注">
            <a-textarea
              v-decorator="[
                'description',
                {
                  rules: [{ required: false, message: '请输入备注' }]
                }
              ]"
              :rows="4"
              placeholder="请输入备注"
            />
          </a-form-item>
        </a-col>
      </a-row>
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
import { postAction } from '@/api/manage'
export default {
  data() {
    return {
      title: '操作',
      visible: false,
      curResourceID: null,
      confirmLoading: false,
      type: '',
      form: this.$form.createForm(this),
      url: {
        save: '/v1/resources',
        testConnect: '/v1/resources/testConnect'
      }
    }
  },
  created() {},
  methods: {
    save(record) {
      this.form.resetFields()
      this.visible = true
      if (record) {
        console.log(record)
        this.curResourceID = record.resourceID
        this.setFieldsValueByType(record.type, record)
      }
    },
    setFieldsValueByType(type, record) {
      this.type = type
      switch (type) {
        case 'MYSQL':
        case 'POSTGRESQL':
        case 'SQLSERVER':
        case 'TDENGINE':
          this.$nextTick(() => {
            this.form.setFieldsValue({
              resourceID: record.resourceID,
              type: type,
              description: record.description,
              resource: {
                ip: record.resource.ip,
                port: record.resource.port,
                databaseName: record.resource.databaseName,
                password: record.resource.password,
                username: record.resource.username
              }
            })
          })
          break
        case 'KAFKA':
          this.$nextTick(() => {
            this.form.setFieldsValue({
              resourceID: record.resourceID,
              type: type,
              description: record.description,
              resource: {
                server: record.resource.server,
                password: record.resource.password,
                username: record.resource.username
              }
            })
          })
          break
        case 'MQTT_BROKER':
          this.$nextTick(() => {
            this.form.setFieldsValue({
              resourceID: record.resourceID,
              type: type,
              description: record.description,
              resource: {
                server: record.resource.server,
                password: record.resource.password,
                username: record.resource.username,
                sslEnable: record.resource.sslEnable ? record.resource.sslEnable : 'false'
              }
            })
          })
          break
      }
    },
    beforeUpload(flie) {
      console.log(flie)
      this.form.setFieldsValue('resource.caCertStr', flie)
      return false
    },
    typeChange(value) {
      this.type = value
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
          const formData = Object.assign({}, values)
          console.log(formData)
          formData.resourceID = this.curResourceID
          const obj = postAction(this.url.save, formData)
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
    handleConnect() {
      const that = this
      // 触发表单验证
      this.form.validateFields((err, values) => {
        if (!err) {
          const formData = Object.assign({}, values)
          formData.resourceID = this.curResourceID
          console.log(formData)
          postAction(this.url.testConnect, formData).then(res => {
            if (res.code === 200) {
              that.$message.success('连接成功！')
            } else {
              that.$message.warning('连接失败！')
            }
          })
        }
        // const formData = Object.assign({}, values)
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
