<template>
  <!-- hidden PageHeaderWrapper title demo -->

  <a-card :loading="confirmLoading" :bordered="false">
    <a-row :gutter="48">
      <a-col :md="12" :sm="24">
        <a-form :form="form">
          <a-form-item label="规则名称">
            <a-input
              v-decorator="['name', { rules: [{ required: true, message: '请输入规则名称' }] }]"
              placeholder="请输入规则名称"
            />
          </a-form-item>

          <a-form-item label="SQL">
            <codemirror
              v-decorator="['sql', { rules: [{ required: true, message: '请输入SQL' }] }]"
              :options="options"
            ></codemirror>
          </a-form-item>
          <!-- <a-form-item label="是否启用">
            <a-switch v-decorator="['enable', { valuePropName: 'checked' }]" />
          </a-form-item> -->
          <a-form-item label="备注">
            <a-textarea
              v-decorator="[
                'description',
                {
                  rules: [{ required: true, message: '请输入备注' }]
                }
              ]"
              :rows="4"
              placeholder="请输入备注"
            />
          </a-form-item>
        </a-form>
      </a-col>
      <a-col :md="12" :sm="24">
        <a-card title="说明">
          <p>
            规则引擎是标准MQTT之上的核心基于SQL的数据处理和分发组件。它使筛选和处理MQTT消息和设备生命周期事件变得容易，并将数据分发移动到HTTP服务器、数据库、消息队列，甚至其他MQTT代理
          </p>
          <p>1.选择发布到topic/#的消息，然后选择所有字段:</p>
          <p style="background: rgba(55, 54, 61, 0.2); padding: 10px 5px">SELECT * FROM "topic/#"</p>
          <p>2.查询专门字段SQL:</p>
          <p style="background: rgba(55, 54, 61, 0.2); padding: 10px 5px">
            SELECT this.payload.value, this.payload.deviceName FROM "topic/#"
          </p>
          <p>3.条件查询SQL:</p>
          <p style="background: rgba(55, 54, 61, 0.2); padding: 10px 5px">
            SELECT this.payload.value, this.payload.deviceName FROM "topic/#" WHERE this.payload.deviceName like 'abc%'
          </p>
        </a-card>
      </a-col>
    </a-row>
    <a-divider />
    <a-row :gutter="48">
      <a-col :md="24" :sm="24">
        <a-list :grid="{ gutter: 24, lg: 3, md: 2, sm: 1, xs: 1 }" :data-source="ruleEngine.resourcesMateDatas">
          <a-list-item slot="renderItem" slot-scope="item">
            <template v-if="!item || item.resourceID === undefined">
              <a-button @click="handleResourceSave({})" class="new-btn" type="dashed">
                <a-icon type="plus" />
                目标资源
              </a-button>
            </template>
            <template v-else>
              <a-card :title="item.title">
                <div slot="title">{{ item.resourceID }}</div>
                <a slot="actions" @click="handleResourceSave(item)">编辑</a>
                <a-popconfirm slot="actions" title="Sure to delete?" @confirm="() => handleDelete(item)">
                  <a href="javascript:;">删除</a>
                </a-popconfirm>
                {{ getDescription(item) }}
              </a-card>
            </template>
          </a-list-item>
        </a-list>
      </a-col>
    </a-row>
    <a-divider />
    <a-button htmlType="submit" @click="onSave" type="primary">保存</a-button>
    <a-button style="margin-left: 8px" @click="onClose">取消</a-button>
    <a-modal
      title="资源编辑"
      :visible="visible"
      :confirm-loading="confirmResourceLoading"
      @ok="handleResourceOk"
      @cancel="handleResourceCancel"
    >
      <a-form :form="resourcesform" layout="vertical" hide-required-mark>
        <a-row :gutter="16">
          <a-form-item label="资源类型">
            <a-select
              @change="resourceTypeChange"
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
        </a-row>
        <a-row :gutter="16">
          <a-form-item label="资源id">
            <a-select
              v-decorator="[
                'resourceID',
                {
                  rules: [{ required: true, message: '请选择资源id' }]
                }
              ]"
              placeholder="请选择资源id"
            >
              <a-select-option v-for="(resource, index) in selectResources" :key="index">
                {{ resource.resourceID }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-row>
        <a-row :gutter="16">
          <div v-if="curType != 'KAFKA' && curType != 'MQTT_BROKER'">
            <a-form-item label="SQL">
              <codemirror
                v-decorator="['resource.sql', { rules: [{ required: true, message: '请输入SQL' }] }]"
                :options="options"
              ></codemirror>
            </a-form-item>
          </div>
          <div v-if="curType === 'KAFKA'">
            <a-form-item label="Topic">
              <a-input
                v-decorator="['resource.topic', { rules: [{ required: true, message: '请输入Topic' }] }]"
                placeholder="请输入Topic"
              />
            </a-form-item>
          </div>
          <div v-if="curType === 'MQTT_BROKER'">
            <a-form-item label="Payload">
              <a-input
                v-decorator="['resource.payload', { rules: [{ required: false, message: '请输入Payload' }] }]"
                placeholder="请输入Payload"
              />
            </a-form-item>
          </div>
        </a-row>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script>
import { codemirror } from 'vue-codemirror-lite'
import { getAction, postAction } from '@/api/manage'
import 'codemirror/lib/codemirror.css'
require('codemirror/mode/sql/sql.js')
require('codemirror/mode/vue/vue')
require('codemirror/addon/hint/show-hint.js')
require('codemirror/addon/hint/show-hint.css')
require('codemirror/theme/base16-light.css')
require('codemirror/addon/selection/active-line')

export default {
  name: 'RuleEngineForm',
  components: { codemirror },
  props: {
    id: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      visible: false,
      resourceType: '',
      confirmLoading: false,
      confirmResourceLoading: false,
      form: this.$form.createForm(this),
      resourcesform: this.$form.createForm(this),
      curResources: [],
      resourceIndex: 0,
      resourceEditIndex: null,
      resourcesAddFlag: true,
      selectResources: [],
      ruleEngine: {
        ruleId: '',
        name: '',
        sql: 'SELECT \n   this.payload.msg as msg \nFROM \n   "t/#" \nWHERE \n   this.payload.msg = ' + "'hello'",
        description: '',
        enable: true,
        resourcesMateDatas: []
      },
      curType: '',
      options: {
        mode: { name: 'text/x-sql', json: true },
        height: 500,
        lineNumbers: true,
        tabSize: 2,
        theme: 'base16-light',
        line: true,
        autoCloseTags: true,
        lineWrapping: true,
        styleActiveLine: true,
        extraKeys: { 'Ctrl-Space': 'autocomplete' }, // 自定义快捷键
        hintOptions: {
          tables: {}
        }
      },
      url: {
        listResources: '/v1/resources/resources',
        listResourcesByRuleId: '/v1/ruleEngine',
        save: '/v1/ruleEngine'
      }
    }
  },
  mounted() {
    this.form.resetFields()
    console.log(this.id)
    this.listResources()
    this.listResourcesByRuleId()
  },
  methods: {
    listResources() {
      getAction(this.url.listResources, {}).then(res => {
        this.curResources = []
        this.curResources.push({})
        res.data.forEach(resource => {
          this.curResources.push(resource)
        })
        console.log(this.curResources)
      })
    },
    listResourcesByRuleId() {
      getAction(this.url.listResourcesByRuleId, { ruleId: this.id }).then(res => {
        if (res.data) {
          this.ruleEngine = res.data
        }

        console.log(this.ruleEngine)
        this.$nextTick(() => {
          this.ruleEngine.resourcesMateDatas.forEach(x => {
            x.resourceIndex = this.resourceIndex
            this.resourceIndex++
          })
          this.ruleEngine.resourcesMateDatas.unshift({})
          this.form.setFieldsValue({
            name: this.ruleEngine.name,
            ruleId: this.ruleEngine.ruleId,
            sql: this.ruleEngine.sql,
            description: this.ruleEngine.description,
            enable: true,
            resourcesMateDatas: this.ruleEngine.resourcesMateDatas
          })
        })
      })
    },

    getDescription(item) {
      return (
        '资源ID:' + item.resourceID + ' 资源类型:' + item.type + this.getResourceContentByType(item.resource, item.type)
      )
    },
    getResourceContentByType(resource, type) {
      switch (type) {
        case 'MYSQL':
          return ' ip:' + resource.ip + ' port:' + resource.port + ' 数据库名称:' + resource.databaseName
        case 'POSTGRESQL':
          return ' ip:' + resource.ip + ' port:' + resource.port + ' 数据库名称:' + resource.databaseName
        case 'SQLSERVER':
          return ' ip:' + resource.ip + ' port:' + resource.port + ' 数据库名称:' + resource.databaseName
        case 'TDENGINE':
          return ' ip:' + resource.ip + ' port:' + resource.port + ' 数据库名称:' + resource.databaseName
        case 'KAFKA':
          return ' Kafka服务:' + resource.server + ' topic:' + resource.topic
        default:
          return ''
      }
    },
    handleDelete(item) {
      const index = this.ruleEngine.resourcesMateDatas.indexOf(item)
      if (index > -1) {
        this.ruleEngine.resourcesMateDatas.splice(index, 1)
      }
      // that.ruleEngine.resources.
    },
    handleResourceSave(record) {
      this.resourcesAddFlag = !record.resourceID
      console.log(this.resourcesAddFlag)
      if (!this.resourcesAddFlag) {
        this.resourceEditIndex = record.resourceIndex
      }

      console.log(record)
      this.curType = record.type
      this.visible = true
      this.resourcesform.resetFields()
      const type = record.type
      switch (type) {
        case 'MYSQL':
        case 'POSTGRESQL':
        case 'SQLSERVER':
        case 'TDENGINE':
          this.$nextTick(() => {
            this.resourcesform.setFieldsValue({
              resourceID: record.resourceID,
              type: type,
              description: record.description,
              resource: {
                ip: record.resource.ip,
                port: record.resource.port,
                databaseName: record.resource.databaseName,
                password: record.resource.password,
                username: record.resource.username,
                sql: record.resource.sql
              }
            })
          })
          break
        case 'KAFKA':
          this.$nextTick(() => {
            this.resourcesform.setFieldsValue({
              resourceID: record.resourceID,
              type: type,
              description: record.description,
              resource: {
                server: record.resource.server,
                password: record.resource.password,
                username: record.resource.username,
                topic: record.resource.topic
              }
            })
          })
          break
      }
    },
    handleResourceOk() {
      const that = this
      this.resourcesform.validateFields((err, values) => {
        if (!err) {
          that.confirmResourceLoading = true
          console.log(values)
          const formData = Object.assign({}, values)
          console.log(formData)
          if (formData) {
            const resources = that.curResources.filter(x => formData.type === x.type)
            if (resources) {
              const resource = resources[0]
              resource.resource.sql = formData.resource.sql
              resource.resource.topic = formData.resource.topic
              resource.resourceIndex = that.resourceIndex
              that.resourceIndex++
              console.log(that.resourcesAddFlag)
              if (that.resourcesAddFlag) {
                that.ruleEngine.resourcesMateDatas.push(resource)
              } else {
                for (const editResources in that.ruleEngine.resourcesMateDatas) {
                  if (that.ruleEngine.resourcesMateDatas[editResources].resourceIndex === that.resourceEditIndex) {
                    that.ruleEngine.resourcesMateDatas[editResources] = resource
                    that.ruleEngine.resourcesMateDatas[editResources].resourceIndex = that.resourceIndex
                    that.resourceIndex++
                  }
                }
                console.log(that.ruleEngine)
              }
            }
          }
          that.confirmResourceLoading = false
          that.handleResourceCancel()
        }
      })
    },
    onSave() {
      const that = this
      // 触发表单验证
      this.form.validateFields((err, values) => {
        if (!err) {
          that.confirmLoading = true
          console.log(values)
          const formData = Object.assign({}, values)
          console.log(formData)
          this.ruleEngine.resourcesMateDatas.splice(0, 1)
          formData.resourcesMateDatas = this.ruleEngine.resourcesMateDatas
          formData.ruleId = this.id
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
    onClose() {
      this.$router.push({ name: 'ruleEngine' })
    },
    handleResourceCancel() {
      this.visible = false
    },
    resourceTypeChange(value) {
      this.selectResources = this.curResources.filter(x => value === x.type)
      this.curType = value
      this.resourcesform.setFieldsValue({
        resourceID: ''
      })
    }
  }
}
</script>
<style lang="less" scoped>
@import '~@/components/index.less';

.card-list {
  /deep/ .ant-card-body:hover {
    .ant-card-meta-title > a {
      color: @primary-color;
    }
  }

  /deep/ .ant-card-meta-title {
    margin-bottom: 12px;

    & > a {
      display: inline-block;
      max-width: 100%;
      color: rgba(0, 0, 0, 0.85);
    }
  }

  /deep/ .meta-content {
    position: relative;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    height: 64px;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;

    margin-bottom: 1em;
  }
}

.card-avatar {
  width: 48px;
  height: 48px;
  border-radius: 48px;
}

.ant-card-actions {
  background: #f7f9fa;

  li {
    float: left;
    text-align: center;
    margin: 12px 0;
    color: rgba(0, 0, 0, 0.45);
    width: 50%;

    &:not(:last-child) {
      border-right: 1px solid #e8e8e8;
    }

    a {
      color: rgba(0, 0, 0, 0.45);
      line-height: 22px;
      display: inline-block;
      width: 100%;
      &:hover {
        color: @primary-color;
      }
    }
  }
}

.new-btn {
  background-color: #fff;
  border-radius: 2px;
  width: 100%;
  height: 188px;
}
</style>
