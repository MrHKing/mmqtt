# MMQ broker
*MMQ broker* 是一款完全开源，高度可伸缩，高可用的分布式 MQTT 消息服务器，适用于 IoT、M2M 和移动应用程序。

*MMQ broker* 完整支持MQTT V3.1 和 V3.1.1。

## 安装

*MMQ broker* 是跨平台的，支持 Linux、Unix、macOS 以及 Windows。这意味着 *MMQ broker* 可以部署在 x86_64 架构的服务器上。由于使用raft一致性算法，集群部署建议奇数个。

#### 直接安装

#### Docker安装

#### Kubernetes安装

# 快速入门
### 单机版启动

```bash
cd .\bin
#windows start
startup.cmd -m standalone
```

```bash
cd ./bin
#linux start
sh startup.sh -m standalone
#linux shutdown
sh shutdown.sh
```

### 集群版启动

```bash
cd ./config
#配置集群文件
cp cluster.conf.example cluster.conf
```

```bash
cd .\bin
#windows start
startup.cmd
```

```bash
cd ./bin
#linux start
sh startup.sh
#linux shutdown
sh shutdown.sh
```


## MQTT 规范

你可以通过以下链接了解与查阅 MQTT 协议:

[MQTT Version 3.1.1](https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html)

## 开源许可

Apache License 2.0, 详见 [LICENSE](./LICENSE)。

# 开发计划
##规则引擎
###桥接kafka
###桥接MQTT
###桥接Tdengine
###桥接influxdb
##模块
###Coap协议网关
###TCP协议网关
###JT/T808网关
