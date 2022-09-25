<template>
  <a-drawer
    title="ACL管理"
    :width="1000"
    :visible="visible"
    :body-style="{ paddingBottom: '80px' }"
    @close="onClose"
  >
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="客户端ID">
                <a-input v-model="queryParam.clientId" placeholder="" />
              </a-form-item>
            </a-col>
            <!-- <a-col :md="8" :sm="24">
              <a-form-item label="账户名">
                <a-input v-model="queryParam.ruleId" placeholder="" />
              </a-form-item>
            </a-col> -->
            <a-col :md="8" :sm="24">
              <a-form-item label="TOPIC">
                <a-input v-model="queryParam.topic" placeholder="" />
              </a-form-item>
            </a-col>
            <template v-if="advanced"> </template>
            <a-col :md="(!advanced && 8) || 24" :sm="24">
              <span
                class="table-page-search-submitButtons"
                :style="(advanced && { float: 'right', overflow: 'hidden' }) || {}"
              >
                <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
                <a-button style="margin-left: 8px" @click="() => (this.queryParam = {})">重置</a-button>
                <a @click="toggleAdvanced" style="margin-left: 8px">
                  {{ advanced ? '收起' : '展开' }}
                  <a-icon :type="advanced ? 'up' : 'down'" />
                </a>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="table-operator"><a-button type="primary" @click="handleSave({})">添加</a-button></div>
      <s-table
        ref="table"
        size="default"
        rowKey="key"
        :columns="columns"
        :data="loadData"
        :alert="true">
        <span slot="serial" slot-scope="text, record, index">
          {{ index + 1 }}
        </span>
        <span slot="enable" slot-scope="record">
          <a-tag v-show="record.enable" color="red"> 停止 </a-tag>
          <a-tag v-show="!record.enable" color="green"> 启用 </a-tag>
        </span>
        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="handleSave(record)">编辑</a>
            <a-divider type="vertical" />
            <a-popconfirm v-if="list.length" title="Sure to delete?" @confirm="() => handleDelete(record)">
              <a href="javascript:;">删除</a>
            </a-popconfirm>
          </template>
        </span>
      </s-table>
    </a-card>
     <a-modal :visible="formVisible" title="Basic Modal" @cancel="handleCancel" @ok="handleOk">
      <a-form :form="form" layout="vertical" hide-required-mark>
        <a-form-item label="是否允许">
          <a-select
            v-decorator="[
              'allow',
              {
                rules: [{ required: true, message: '请选择是否允许' }]
              }
            ]"
            placeholder="请选择是否允许"
          >
            <a-select-option value=0> 拒绝 </a-select-option>
            <a-select-option value=1> 允许 </a-select-option>
          </a-select>
        </a-form-item>
        <!-- <a-form-item label="IP地址">
          <a-input
            v-decorator="['ipaddr', { rules: [{ required: true, message: '请输入ip地址' }] }]"
            placeholder="请输入ip地址"
          />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input
            v-decorator="['username', { rules: [{ required: false, message: '请输入用户名' }] }]"
            placeholder="请输入用户名"
          />
        </a-form-item> -->
        <a-form-item label="客户端ID">
          <a-input
            v-decorator="['clientId', { rules: [{ required: true, message: '请输入客户端ID' }] }]"
            placeholder="请输入客户端ID"
          />
        </a-form-item>
        <a-form-item label="topic">
          <a-input
            v-decorator="['topic', { rules: [{ required: false, message: '请输入topic' }] }]"
            placeholder="请输入topic"
          />
        </a-form-item>
        <!-- <a-form-item label="权限">
          <a-select
            v-decorator="[
              'access',
              {
                rules: [{ required: true, message: '请选择权限' }]
              }
            ]"
            placeholder="请选择权限"
          >
            <a-select-option value=1> 订阅 </a-select-option>
            <a-select-option value=2> 发布 </a-select-option>
            <a-select-option value=3> 订阅与发布 </a-select-option>
          </a-select>
        </a-form-item> -->
      </a-form>
      <template #footer>
        <a-button key="back" @click="handleCancel">取消</a-button>
        <a-button key="submit" type="primary" @click="handleOk">确认</a-button>
      </template>
    </a-modal>
  </a-drawer>
</template>

<script>
import { STable, Ellipsis } from '@/components'
import { getAction, postAction, putAction, deleteAction } from '@/api/manage'
const columns = [
  {
    title: '#',
    scopedSlots: { customRender: 'serial' }
  },
  {
    title: '是否允许',
    dataIndex: 'allow'
  },
  // {
  //   title: 'ip地址',
  //   dataIndex: 'ipaddr'
  // },
  // {
  //   title: '用户名',
  //   dataIndex: 'username'
  // },
  {
    title: '客户端ID',
    dataIndex: 'clientId'
  },
  // {
  //   title: '权限',
  //   dataIndex: 'access'
  // },
  {
    title: 'topic',
    dataIndex: 'topic'
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '150px',
    scopedSlots: { customRender: 'action' }
  }
]

export default {
  name: 'Acl',
  components: {
    STable,
    Ellipsis
  },
  data() {
    this.columns = columns
    return {
      // 高级搜索 展开/关闭
      advanced: false,
      confirmLoading: false,
      visible: false,
      formVisible: false,
      current: null,
      dataSource: [],
      list: [],
      form: this.$form.createForm(this),
      // 查询参数
      queryParam: {},
      // 加载数据方法 必须为 Promise 对象
      loadData: parameter => {
        const requestParameters = Object.assign({}, parameter, this.queryParam)
        console.log('loadData request parameters:', requestParameters)
        return getAction('/v1/api/acl/pageAcl', requestParameters).then(res => {
          this.dataSource = res.data.data
          return res.data
        })
      },
      selectedRowKeys: [],
      selectedRows: []
    }
  },
  created() {
  },
  computed: {
    rowSelection() {
      return {
        selectedRowKeys: this.selectedRowKeys,
        onChange: this.onSelectChange
      }
    }
  },
  methods: {
    update(record) {
      console.log(record)
      this.list = record.configs.data
      this.visible = true
    },
    onClose() {
      this.visible = false
    },
    onSelectChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    },
    toggleAdvanced() {
      this.advanced = !this.advanced
    },
    resetSearchForm() {
      this.queryParam = {}
    },
    handleDelete(record) {
      deleteAction('/v1/api/acl', { id: record.id }).then(res => {
        this.$message.success(res.message)
        this.$refs.table.refresh()
      })
    },
    handleSave(record) {
      this.formVisible = true
      this.current = record
      this.form.resetFields()
      if (record != null) {
        this.$nextTick(() => {
          this.form.setFieldsValue({
            id: record.id,
            allow: record.allow,
            ipaddr: record.ipaddr,
            username: record.username,
            clientId: record.clientId,
            access: record.access,
            topic: record.topic
          })
        })
      }
    },
    handleOk() {
      const that = this
      this.formVisible = false
      // 触发表单验证
      this.form.validateFields((err, values) => {
        console.log(err)
        if (!err) {
          const formData = Object.assign({}, values)
          let obj
          if (this.current.id) {
            formData.id = this.current.id
            obj = putAction('/v1/api/acl', formData)
          } else {
            obj = postAction('/v1/api/acl', formData)
          }
          obj
            .then(res => {
              if (res.code === 200) {
                that.$message.success(res.message)
                that.$emit('ok')
                // this.loadData()
                this.$refs.table.refresh()
              } else {
                that.$message.warning(res.message)
              }
            })
            .finally(() => {
              that.confirmLoading = false
              that.formVisible = false
            })
        }
      })
    },
    handleCancel() {
      this.formVisible = false
    },
    uuid() {
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = Math.random() * 16 | 0
        const v = c === 'x' ? r : (r & 0x3 | 0x8)
        return v.toString(16)
      })
    },
    filterOption(input, option) {
      if (!option.componentOptions.children[0].text) {
        return false
      }
      return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    }
  }
}
</script>

<style scoped>
</style>
