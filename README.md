# MMQ broker
*MMQ broker* 是一款完全开源，高度可伸缩，高可用的分布式 MQTT 消息服务器，适用于 IoT、M2M 和移动应用程序。

*MMQ broker* 完整支持MQTT V3.1 和 V3.1.1。

## 安装

*MMQ broker* 是跨平台的，支持 Linux、Unix、macOS 以及 Windows。这意味着 *MMQ broker* 可以部署在 x86_64 架构的服务器上。由于使用raft一致性算法，集群部署三个节点以上。

#### 从 Github 上下载源码方式
```bash
git clone https://github.com/MrHKing/mmq.git
cd mmq
mvn -Prelease-mmq -Dmaven.test.skip=true clean install -U
```

#### 直接安装
您可以从 最新稳定版本 下载 mmq-server-$version.zip 包。

```bash
unzip mmq-server-$version.zip 或者 tar -xvf mmq-server-$version.tar.gz
cd mmq/bin
```

#### Docker安装
```bash
docker run -d --name mmq -p 2883:2883 -p 1883:3883 -p 8888:8888  paperman/mmq:v1.0.8
```
#### Kubernetes安装

# 快速入门

### 单机版启动

```bash
cd mmq\bin
#windows start
startup.cmd -m standalone
```

```bash
cd mmq/bin
#linux start
sh startup.sh -m standalone
#linux shutdown
sh shutdown.sh
```

### 集群版启动

```bash
cd mmq/config
#配置集群文件
cp cluster.conf.example cluster.conf
```

```bash
#每个节点都需要配置其他节点的地址,如下：
#example
192.168.31.9:7777
192.168.31.9:8848
192.168.31.9:8888
```

```bash
cd mmq\bin
#windows start
startup.cmd
```

```bash
cd mmq/bin
#linux start
sh startup.sh
#linux shutdown
sh shutdown.sh
```

### 配置文件
mqtt tcp端口默认：3883

mqtt websocket端口：2883
```bash
#*************** Spring Boot Related Configurations ***************#
### Default web context path:
server.servlet.contextPath=/
### Default web server port:
server.port=8888

#*************** mqtt broker Configurations ***************#
mmq.broker.websocketPort=2883
mmq.broker.port=3883
mmq.broker.ssl.enabled=false
mmq.broker.ssl.password=true
mmq.broker.default.user=admin
mmq.broker.default.password=admin@mmq
mmq.broker.default.anonymous=true
```
### Dashboard --单机演示
启动后访问 http://101.43.4.211:8888/

默认账户：mmq

默认密码：aaaaaa

mqtt tcp端口默认：1883

mqtt websocket端口：2883

## 测试及规则引擎
详细参见
[wiki](https://github.com/MrHKing/mmq/wiki)

## MQTT 规范

你可以通过以下链接了解与查阅 MQTT 协议:

[MQTT Version 3.1.1](https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html)

## 开源许可

Apache License 2.0, 详见 [LICENSE](./LICENSE)。

## 群号
QQ群: 1016132679
