<template>
  <a-card :bordered="false">
    <a-button type="primary" @click="handleSave({})">添加</a-button>
    <div class="table-operator"></div>
    <a-list :grid="{ gutter: 16, column: 4 }" :data-source="data">
      <a-list-item slot="renderItem" slot-scope="item">
        <a-card :title="item.title">
          <div slot="title">{{ item.resourceID }}</div>
          <a slot="actions" @click="handleSave(item)">编辑</a>
          <a slot="actions" @click="hanldeDelete(item)">删除</a>
          {{ getDescription(item) }}
        </a-card>
      </a-list-item>
    </a-list>
    <ResourceModel ref="ResourceModel" @ok="loadData"></ResourceModel>
  </a-card>
</template>

<script>
import { getAction, deleteAction } from '@/api/manage'
import ResourceModel from './modules/ResourceModel'
export default {
  name: 'Subscribes',
  components: {
    ResourceModel
  },
  data () {
    return {
      loading: true,
      data: []
    }
  },
  filters: {
  },
  created () {
    this.loadData()
  },
  methods: {
    loadData () {
      return getAction('/v1/resources/resources', {})
        .then(res => {
          this.data = res.data
          console.log(this.data)
        })
    },
    getDescription (item) {
      return '资源ID:' + item.resourceID + ' 资源类型:' + item.type + this.getResourceContentByType(item.resource, item.type)
    },
    getResourceContentByType (resource, type) {
      switch (type) {
        case 'MYSQL':
          return ' ip:' + resource.ip + ' port:' + resource.port + ' 数据库名称:' + resource.databaseName
        default:
          return ''
      }
    },
    handleSave (record) {
      this.$refs.ResourceModel.save(record)
    },
    hanldeDelete (record) {
      console.log(record)
      deleteAction('/v1/resources', { resourceID: record.resourceID }).then(res => {
        if (res.code === 200) {
          this.$message.info(res.message)
          this.loadData()
        } else {
          this.$message.info(res.message)
        }
      })
    }
  }
}
</script>
<style>
.resource-loadmore-list {
  min-height: 350px;
}
</style>
