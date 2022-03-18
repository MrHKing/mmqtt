English| [简体中文](./README.md)

# MMQ broker
*MMQ broker* It is a fully open source, highly scalable and highly available distributed mqtt message server, which is suitable for IOT, M2M and mobile applications

*MMQ broker* Fully support mqtt v3 1 and v3 1.1。

## Install

*MMQ broker* It is cross platform and supports Linux, UNIX, MacOS and windows. This means that * MMQ broker * can be deployed in x86_ On a 64 architecture server. Due to the use of raft consistency algorithm, the cluster deploys more than 3 nodes.

#### Download the source code from GitHub
```bash
git clone https://github.com/MrHKing/mmq.git
cd mmq
mvn -Prelease-mmq -Dmaven.test.skip=true clean install -U
```

#### Clone install
You can download the mmq-server-$version.zip package from the latest stable version。

```bash
unzip mmq-server-$version.zip 或者 tar -xvf mmq-server-$version.tar.gz
cd mmq/bin
```

#### Docker install
```bash
docker run -d --name mmq -p 2883:2883 -p 1883:3883 -p 8888:8888  paperman/mmq:v1.0.8
```
#### Kubernetes install

# Quick Start

### Standalone startup

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

### Cluster startup

```bash
cd mmq/config
# Configure cluster files
cp cluster.conf.example cluster.conf
```

```bash
#Each node needs to configure the addresses of other nodes, as shown below：
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

### Configuration file
mqtt tcp port：3883

mqtt websocket port：2883
```bash
#*************** Spring Boot Related Configurations ***************#
### Default web context path:
server.servlet.contextPath=/
### Default web server port:
server.port=8888

#*************** mqtt broker Configurations ***************#
mmq.broker.websocketPort=2883
mmq.broker.port=3883
mmq.broker.ssl.password=mmq
mmq.broker.ssl.certPath=cert/mmq.pfx
mmq.broker.ssl.port=17733
mmq.broker.ssl.websocketPort=36633
mmq.broker.default.user=admin
mmq.broker.default.password=admin@mmq
mmq.broker.default.anonymous=true
```
### Dashboard -- Standalone demonstration
Address http://101.43.4.211:8888/

Default account：mmq

Default password：aaaaaa

mqtt tcp port：1883

mqtt websocket port：2883

## Document
See for details
[wiki](https://github.com/MrHKing/mmq/wiki)

## MQTT Standard

You can learn about and consult the mqtt protocol through the following links:

[MQTT Version 3.1.1](https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html)

## Open source license

Apache License 2.0, See details [LICENSE](./LICENSE)。

## QQ Group number
1016132679