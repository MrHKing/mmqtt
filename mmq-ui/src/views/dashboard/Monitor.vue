<template>
  <page-header-wrapper>
    <div style="background: #ececec; padding: 10px">
      <a-row :gutter="24">
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="总CPU使用情况"
              :value="Number(useCPU / totalCPU).toFixed(2) + '%'"
              class="demo-class"
              :value-style="{ color: '#3f8600', fontSize: '32px' }"
            >
              <template #prefix>
                <a-icon type="smile" theme="twoTone" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="总内存使用情况"
              :value="Number(useMem).toFixed(2) + 'M'"
              class="demo-class"
              :value-style="{ color: '#3f8600', fontSize: '28px' }"
            >
              <template #prefix>
                <a-icon type="smile" theme="twoTone" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="连接数"
              :value="SystemInfoMateData.clientCount"
              class="demo-class"
              :value-style="{ color: 'hotpink', fontSize: '32px' }"
            >
              <template #prefix>
                <a-icon type="smile" theme="twoTone" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="订阅数"
              :value="SystemInfoMateData.subscribeCount"
              class="demo-class"
              :value-style="{ color: '#52c41a', fontSize: '32px' }"
            >
              <template #prefix>
                <a-icon type="smile" theme="twoTone" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>
    <a-card style="ma" :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="节点">
                <a-select size="large" placeholder="请选择节点" v-model="node" @change="nodeSelectChange">
                  <a-select-option v-for="item in nodeList" :key="item.address" :value="item.address">{{
                    item.address
                  }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <a-tabs default-active-key="1" v-model="activeKey">
        <a-tab-pane key="1" tab="基本信息"> <SystemInfo ref="systemInfo" :nodeUrl="node"></SystemInfo> </a-tab-pane>
        <a-tab-pane key="2" tab="Tomact信息"> <TomcatInfo ref="tomcatInfo" :nodeUrl="node"></TomcatInfo> </a-tab-pane>
        <a-tab-pane key="3" tab="JVM信息"> <JvmInfo ref="jvmInfo" :nodeUrl="node"></JvmInfo> </a-tab-pane>
        <a-tab-pane key="4" tab="HTTP追踪">
          <HttpTrace ref="httpTrace" :nodeUrl="node"></HttpTrace>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import moment from 'moment'
import { getSystemInfo, getNodes } from '@/api/system'
import { getAction } from '@/api/manage'
import HttpTrace from './module/HttpTrace'
import JvmInfo from './module/JvmInfo'
import SystemInfo from './module/SystemInfo'
import TomcatInfo from './module/TomcatInfo'

export default {
  name: 'Monitor',
  components: {
    HttpTrace,
    JvmInfo,
    SystemInfo,
    TomcatInfo
  },
  data() {
    return {
      dateTime: moment(new Date()).format('YYYY-MM-DD'),
      node: '',
      nodeList: [],
      totalCPU: 1,
      useCPU: 1,
      totalJVMMem: 1,
      useMem: 1,
      activeKey: '1',
      SystemInfoMateData: {
        clientCount: 0,
        systemRunTime: 0,
        version: '',
        systemName: '',
        subscribeCount: 0
      }
    }
  },
  created() {},
  mounted() {
    const _this = this // 声明一个变量指向Vue实例this，保证作用域一致
    this.getTotalSystemInfo()
    this.timer = setInterval(() => {
      getSystemInfo().then(res => {
        _this.SystemInfoMateData = res.data
      })
    }, 5000)

    getSystemInfo().then(res => {
      _this.SystemInfoMateData = res.data
    })
    getNodes().then(res => {
      _this.nodeList = res.data
      _this.node = res.data[0].address
    })
  },
  methods: {
    nodeSelectChange(node) {
      if (this.activeKey === '1') {
        this.$refs.systemInfo.loadTomcatInfo()
      } else if (this.activeKey === '2') {
        this.$refs.tomcatInfo.loadTomcatInfo()
      } else if (this.activeKey === '3') {
        this.$refs.jvmInfo.loadTomcatInfo()
      } else {
        this.$refs.httpTrace.fetch()
      }
    },
    getTotalSystemInfo() {
      this.totalCPU = 1
      this.useCPU = 0
      this.totalJVMMem = 1
      this.useMem = 0
      this.nodeList.forEach(node => {
        const url = this.nodeUrl ? 'http://' + this.nodeUrl : ''
        getAction(url + '/actuator/metrics/system.cpu.count').then(value => {
          const val = value.measurements[0].value
          this.totalCPU += val
          getAction(url + '/actuator/metrics/process.cpu.usage').then(value => {
            let usage = value.measurements[0].value
            usage = this.convert(usage, Number)
            this.useCPU += val * usage
          })
        })
        getAction(url + '/actuator/metrics/jvm.memory.max').then(value => {
          let val = value.measurements[0].value
          val = this.convertMem(val, Number)
          this.totalJVMMem += val
        })
        getAction(url + '/actuator/metrics/jvm.memory.used').then(value => {
          let used = value.measurements[0].value
          used = this.convertMem(used, Number)
          this.useMem = Number(this.useMem) + Number(used)
          console.log(this.useMem + ':' + used)
        })
      })
    },
    convert(value, type) {
      if (type === Number) {
        return Number(value * 100).toFixed(2)
      } else if (type === Date) {
        return moment(value * 1000).format('YYYY-MM-DD HH:mm:ss')
      }
      return value
    },
    convertMem(value, type) {
      if (type === Number) {
        return Number(value / 1048576).toFixed(2)
      }
      return value
    }
  }
}
</script>

<style scoped>
</style>
