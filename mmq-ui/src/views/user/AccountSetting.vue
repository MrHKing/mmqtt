<template>
  <!-- hidden PageHeaderWrapper title demo -->

  <a-card :loading="confirmLoading" :bordered="false">
    <a-form :form="form">
      <a-form-item label="旧密码">
        <a-input-password
          v-decorator="['oldPassword', { rules: [{ required: true, message: '请输入旧密码' }] }]"
          placeholder="请输入旧密码"
        />
      </a-form-item>
      <a-form-item label="新密码">
        <a-input-password
          v-decorator="['newPassword', { rules: [{ required: true, message: '请输入新密码' }] }]"
          placeholder="请输入新密码"
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
      <a-button type="primary" @click="updatePassword"> Submit </a-button>
    </div>
  </a-card>
</template>

<script>
import { updatePassword } from '@/api/manage'

export default {
  name: 'RuleEngineForm',
  props: {
    currentUser: {
      type: Object,
      default: () => null
    }
  },
  data() {
    return {
      visible: false,
      confirmLoading: false,
      form: this.$form.createForm(this)
    }
  },
  mounted() {
    this.form.resetFields()
  },
  methods: {
    updatePassword() {
      const that = this
      this.form.validateFields((err, values) => {
        if (!err) {
          const formData = Object.assign({}, values)
          const obj = updatePassword(formData)
          obj
            .then(res => {
              if (res.code === 200) {
                that.$message.success(res.message)
                that.$emit('password change success!')
              } else {
                that.$message.warning(res.message)
              }
            })
            .finally(() => {
              that.onClose()
            })
        }
      })
    },
    onClose() {
      this.$router.push({ name: 'dashboard' })
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
