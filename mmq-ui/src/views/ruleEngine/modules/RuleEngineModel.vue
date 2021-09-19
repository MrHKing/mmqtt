<template>
  <!-- hidden PageHeaderWrapper title demo -->

  <a-card :bordered="false">
    <a-row :gutter="48">
      <a-col :md="12" :sm="24">
        <a-form :form="form">
          <a-form-item label="规则名称">
            <a-input
              v-decorator="['name', { rules: [{ required: true, message: '请输入规则名称' }] }]"
              name="name"
              :placeholder="$t('form.basic-form.title.placeholder')"
            />
          </a-form-item>

          <a-form-item label="SQL">
            <codemirror
              v-decorator="['ruleSql', { rules: [{ required: true, message: '请输入SQL' }] }]"
              :options="options"
            ></codemirror>
          </a-form-item>
          <a-form-item label="备注">
            <a-textarea
              v-decorator="[
                'description',
                {
                  rules: [{ required: true, message: '请输入备注' }],
                },
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
            SELECT payload.value, payload.deviceName FROM "topic/#"
          </p>
          <p>3.条件查询SQL:</p>
          <p style="background: rgba(55, 54, 61, 0.2); padding: 10px 5px">
            SELECT payload.value, payload.deviceName FROM "topic/#" WHERE payload.deviceName like 'abc%'
          </p>
        </a-card>
      </a-col>
    </a-row>
    <a-divider />
    <a-row :gutter="48">
      <a-col :md="24" :sm="24">
        <a-list :grid="{ gutter: 24, lg: 3, md: 2, sm: 1, xs: 1 }" :data-source="resources">
          <a-list-item slot="renderItem" slot-scope="item">
            <template v-if="!item || item.resourceID === undefined">
              <a-button @click="handleSave({})" class="new-btn" type="dashed">
                <a-icon type="plus" />
                目标资源
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
      </a-col>
    </a-row>
    <a-divider />
    <a-button htmlType="submit" type="primary">保存</a-button>
    <a-button style="margin-left: 8px">取消</a-button>
  </a-card>
</template>

<script>
import { codemirror } from 'vue-codemirror-lite'
require('codemirror/mode/sql/sql.js')
require('codemirror/mode/vue/vue')
require('codemirror/addon/hint/show-hint.js')
require('codemirror/addon/hint/show-hint.css')
require('codemirror/theme/base16-light.css')
require('codemirror/addon/selection/active-line')

export default {
  name: 'RuleEngineForm',
  components: { codemirror },
  data () {
    return {
      form: this.$form.createForm(this),
      resources: [{}],
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
      record: {}
    }
  },
  mounted () {
    console.log(this.record)
    this.record = this.$route.params.record
    console.log(this.record)
  },
  methods: {
    // handler
    handleSubmit (e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          console.log('Received values of form: ', values)
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
