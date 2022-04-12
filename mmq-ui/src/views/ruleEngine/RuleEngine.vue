<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="规则ID">
                <a-input v-model="queryParam.ruleId" placeholder="" />
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
        :alert="true"
        :rowSelection="rowSelection"
        showPagination="auto"
      >
        <span slot="serial" slot-scope="text, record, index">
          {{ index + 1 }}
        </span>
        <span slot="enable" slot-scope="record">
          <a-tag v-show="record.enable" color="red"> 停止 </a-tag>
          <a-tag v-show="!record.enable" color="green"> 启用 </a-tag>
        </span>
        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="handleEnable(record)">{{ record.enable ? '停止' : '启用' }}</a>
            <a-divider type="vertical" />
            <a @click="handleSave(record)">编辑</a>
            <a-divider type="vertical" />
            <a-popconfirm v-if="dataSource.length" title="Sure to delete?" @confirm="() => handleDelete(record)">
              <a href="javascript:;">删除</a>
            </a-popconfirm>
          </template>
        </span>
      </s-table>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import { STable, Ellipsis } from '@/components'
import { getAction, postAction, deleteAction } from '@/api/manage'

const columns = [
  {
    title: '#',
    scopedSlots: { customRender: 'serial' }
  },
  {
    title: '规则Id',
    dataIndex: 'ruleId'
  },
  {
    title: '说明',
    dataIndex: 'description',
    sorter: true
  },
  //   {
  //     title: '是否启用',
  //     dataIndex: 'enable',
  //     scopedSlots: { customRender: 'enable' }
  //   },
  {
    title: '操作',
    dataIndex: 'action',
    width: '150px',
    scopedSlots: { customRender: 'action' }
  }
]

export default {
  name: 'RuleEngine',
  components: {
    STable,
    Ellipsis
  },
  data() {
    this.columns = columns
    return {
      // 高级搜索 展开/关闭
      advanced: false,
      dataSource: [],
      loading: true,
      // 查询参数
      queryParam: {},
      // 加载数据方法 必须为 Promise 对象
      loadData: parameter => {
        this.loading = true
        const requestParameters = Object.assign({}, parameter, this.queryParam)
        console.log('loadData request parameters:', requestParameters)
        return getAction('/v1/ruleEngine/ruleEngines', requestParameters).then(res => {
          this.dataSource = res.data.data
          this.loading = false
          return res.data
        })
      },
      selectedRowKeys: [],
      selectedRows: []
    }
  },
  created() {},
  computed: {
    rowSelection() {
      return {
        selectedRowKeys: this.selectedRowKeys,
        onChange: this.onSelectChange
      }
    }
  },
  methods: {
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
    handleEnable(record) {
      record.enable = !record.enable
      postAction('/v1/ruleEngine', record).then(res => {
        if (res.code === 200) {
          this.$message.info('启动成功！')
          this.$refs.table.refresh(true)
        } else {
          this.$message.info(res.message)
        }
      })
    },
    handleDelete(record) {
      console.log(record)
      deleteAction('/v1/ruleEngine', { ruleId: record.ruleId }).then(res => {
        if (res.code === 200) {
          this.$message.info(res.message)
          this.$refs.table.refresh(true)
        } else {
          this.$message.info(res.message)
        }
      })
    },
    handleSave(record) {
      this.$router.push({ name: 'RuleEngineModel', params: { id: record.ruleId } })
    }
  }
}
</script>
