<template>
  <a-card :bordered="false">
    <!-- <a-button type="primary" @click="handleSave({})">添加</a-button> -->
    <div class="table-operator"></div>
    <a-list :grid="{ gutter: 24, lg: 3, md: 2, sm: 1, xs: 1 }" :loading="loading" :data-source="data">
      <a-list-item slot="renderItem" slot-scope="item">
        <a-card :title="item.moduleName">
          <div slot="title">{{ item.moduleName }}</div>
          <img slot="cover" alt="example" src="https://gw.alipayobjects.com/zos/rmsportal/JiqGstEfoWAOHiTxclqi.png" />
          <template slot="actions" class="ant-card-actions">
            <a slot="actions" @click="handleUpdate(item)">编辑</a>
            <a-switch :checked="item.enable" @change="checked => handleCheck(checked, item)">
              <a-icon slot="checkedChildren" type="check" />
              <a-icon slot="unCheckedChildren" type="close" />
            </a-switch>
          </template>
          {{ item.description }}
        </a-card>
      </a-list-item>
    </a-list>
    <AuthModuleModel ref="AuthModuleModel" @ok="loadData"></AuthModuleModel>
    <ApiModuleModel ref="ApiModuleModel" @ok="loadData"></ApiModuleModel>
  </a-card>
</template>

<script>
import { getAction, putAction } from '@/api/manage'
import AuthModuleModel from './modules/AuthModuleModel'
import ApiModuleModel from './modules/ApiModuleModel'
export default {
  name: 'Subscribes',
  components: {
    AuthModuleModel,
    ApiModuleModel
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
        return getAction('/v1/modules/modules', {}).then(res => {
          this.data = res.data
          this.loading = false
          //   res.data.forEach(resource => {
          //     this.data.push(resource)
          //   })
          console.log(this.data)
        })
      }, 1000)
    },
    handleCheck(check, item) {
      item.enable = check
      const obj = putAction('/v1/modules', item)
      obj.then(res => {
        if (res.code === 200) {
          this.$message.success(res.message)
        } else {
          this.$message.warning(res.message)
        }
      })
    },
    handleUpdate(record) {
      switch (record.key) {
        case 'AUTH-MODULE':
          this.$refs.AuthModuleModel.update(record)
          break
        case 'API-MODULE':
          this.$refs.ApiModuleModel.update(record)
          break
      }
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
