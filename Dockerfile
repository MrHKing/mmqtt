# 基础镜像
FROM  openjdk:8-jre
# author
MAINTAINER solley
# 挂载目录
VOLUME /home/mmq
# 创建目录
RUN mkdir -p /home/mmq
# 指定路径
WORKDIR /home/mmq
# 复制jar文件到路径
COPY ./distribution/target/mmq-server-1.1.5.tar.gz /home/mmq/mmq-server-1.1.5.tar.gz
# 解压缩
RUN tar -zxvf /home/mmq/mmq-server-1.1.5.tar.gz
# 启动
ENTRYPOINT java -jar '-Duser.timezone=GMT+08' '-Dmmq.standalone=true' mmq/target/mmq-server.jar --spring.config.additional-location=mmq/conf/