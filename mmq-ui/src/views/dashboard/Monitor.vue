<template>
  <page-header-wrapper>
    <div style="background: #ececec; padding: 30px">
      <a-row :gutter="24">
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="系统名称"
              :value="SystemInfoMateData.systemName"
              :precision="2"
              :value-style="{ color: '#3f8600' }"
              style="margin-right: 50px"
            >
              <template #prefix>
                <a-icon type="border-bottom" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="版本"
              :value="SystemInfoMateData.version"
              class="demo-class"
              :value-style="{ color: '#3f8600' }"
            >
              <template #prefix>
                <a-icon type="border-horizontal" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="运行时间"
              :value="SystemInfoMateData.systemRunTime + 'ms'"
              class="demo-class"
              :value-style="{ color: '#3f8600' }"
            >
              <template #prefix>
                <a-icon type="border-outer" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic title="系统时间" :value="dateTime" class="demo-class" :value-style="{ color: '#3f8600' }">
              <template #prefix>
                <a-icon type="border-inner" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
      <a-row :gutter="24">
        <a-col :span="12">
          <a-card>
            <a-statistic
              title="连接数"
              :value="SystemInfoMateData.clientCount"
              class="demo-class"
              :value-style="{ color: '#3f8600' }"
            >
              <template #prefix>
                <a-icon type="border-inner" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card>
            <a-statistic
              title="订阅数"
              :value="SystemInfoMateData.subscribeCount"
              class="demo-class"
              :value-style="{ color: '#3f8600' }"
            >
              <template #prefix>
                <a-icon type="border-inner" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </page-header-wrapper>
</template>

<script>
import moment from 'moment'
import { getSystemInfo } from '@/api/system'

export default {
  name: 'Monitor',
  data () {
    return {
      dateTime: moment(new Date()).format('YYYY-MM-DD'),
      SystemInfoMateData: {
        clientCount: 0,
        systemRunTime: 0,
        version: '',
        systemName: '',
        subscribeCount: 0
      }
    }
  },
  created () {

  },
  mounted () {
    const _this = this // 声明一个变量指向Vue实例this，保证作用域一致
    this.timer = setInterval(() => {
      _this.dateTime = moment(new Date()).format('YYYY-MM-DD HH:mm:ss') // 修改数据date
    }, 1000)

    getSystemInfo().then(res => {
      console.log(res)
      this.SystemInfoMateData = res.data
    })
  },
  methods: {

  }
}
</script>

<style scoped>
</style>
