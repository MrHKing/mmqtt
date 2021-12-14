<template>
  <a-card :bordered="false">
    <!-- <a-button type="primary" @click="handleSave({})">添加</a-button> -->
    <div class="table-operator"></div>
    <a-list :grid="{ gutter: 24, lg: 3, md: 2, sm: 1, xs: 1 }" :loading="loading" :data-source="data">
      <a-list-item slot="renderItem" slot-scope="item">
        <template v-if="!item || item.resourceID === undefined">
          <a-button @click="handleSave({})" class="new-btn" type="dashed">
            <a-icon type="plus" />
            新增资源
          </a-button>
        </template>
        <template v-else>
          <a-card :title="item.title">
            <div slot="title">{{ item.resourceID }}</div>
            <a slot="actions" @click="handleSave(item)">编辑</a>
            <a-popconfirm slot="actions" title="Sure to delete?" @confirm="() => handleDelete(item)">
              <a href="javascript:;">删除</a>
            </a-popconfirm>
            {{ getDescription(item) }}
          </a-card>
        </template>
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
  data() {
    return {
      loading: true,
      data: []
    }
  },
  filters: {},
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      setTimeout(() => {
        return getAction('/v1/resources/resources', {}).then(res => {
          this.data = res.data
          this.data.unshift({})
          this.loading = false
          //   res.data.forEach(resource => {
          //     this.data.push(resource)
          //   })
          console.log(this.data)
        })
      }, 1000)
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
          return ' Kafka服务:' + resource.server
        case 'MQTT_BROKER':
          return ' MQTT BROKER服务:' + resource.server
        default:
          return ''
      }
    },
    handleSave(record) {
      this.$refs.ResourceModel.save(record)
    },
    handleDelete(record) {
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
