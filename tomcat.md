# 配置 Tomcat 服务

+ 新建服务脚本
`[root@localhost ~]# vim /etc/init.d/tomcat`

+ 添加脚本内容
```
#!/bin/bash
# description: Tomcat7 Start Stop Restart
# processname: tomcat7
# chkconfig: 234 20 80

CATALINA_HOME=/usr/local/tomcat/apache-tomcat-7.0.77

case $1 in
        start)
                sh $CATALINA_HOME/bin/startup.sh
                ;;
        stop)
                sh $CATALINA_HOME/bin/shutdown.sh
                ;;
        restart)
                sh $CATALINA_HOME/bin/shutdown.sh
                sh $CATALINA_HOME/bin/startup.sh
                ;;
        *)
                echo 'please use : tomcat {start | stop | restart}'
        ;;
esac
exit 0
```

# 执行脚本，启动、停止 和 重启服务。
+ 启动：service tomcat start
+ 停止：service tomcat stop
+ 重启：service tomcat restart

# Tomcat 配置开机自启动

+ 向chkconfig添加 tomcat 服务的管理
`[root@localhost ~]# chkconfig --add tomcat`

+ 设置tomcat服务自启动
`[root@localhost ~]# chkconfig tomcat on`

+ 查看tomcat的启动状态
`[root@localhost ~]# chkconfig --list | grep tomcat`

+ 状态如下：
`[root@localhost ~]# chkconfig –list | grep tomcat`
`tomcat 0:off 1:off 2:on 3:on 4:on 5:on 6:off`

+ 关闭tomcat服务自启动：
`chkconfig tomcat off`

+ 删除tomcat服务在chkconfig上的管理：
`chkconfig –del tomcat`
