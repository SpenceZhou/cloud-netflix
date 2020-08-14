# 微服务实践

## 1. 引言

随着网站应用的不断发展，往往会遇到以下两方面的挑战：

1. 业务复杂度增加
2. 访问数据量增加

在上述两个或者一个挑战下，首先也必须要做的事情：

1. 扩大研发团队规模；
2. 增加硬件资源规模；

那在扩大团队与增加硬件的同时如何能保证投入与产出的比例衰减的不是那么离谱呢？也就是怎样保证单位人员与单位资源使用的效率呢？其实计算机软硬件的发展史已经有经得起时间考验的答案了。

- 硬件CPU的发展在单核公益与性能达到极限的时候采用了多核方式；

- 磁盘在容量大小与读写的瓶颈下采用了RAID以及网络分布式存储方案；

- 数据计算在在单台无法满足的情况下催生出Hadoop、Spark、Strom、Flink等分布式计算框架；

- 系统软件在复杂度越来越高的情况下，采用包、模块、子系统等方式进行封装；

- ...

上述这些方法其实都围绕着一个核心的思想：分治。其实道家的老祖宗早就对此有所解释：一生二、二生三、三生万物。

微服务也是秉承分治的思想，将大的服务进行拆分，我认为这也就是微服务的名称的来源。微服务的核心就是：

将服务进行拆分，服务之间利用RPC调用共同协作完成业务逻辑。

上述的思想以及实践已经由来已久，但是并未系统的解决有服务拆分和远程调用带来的问题，微服务从下面的这些问题提出了一套完整的解决方案。

1. 服务之间的远程调用需要足够的便捷、高效、安全，且需要统一的规范，这也就微服务的核心：RPC
2. 服务见的远程RPC调用，随着部署环境的变化需要不断的进行配置调整或者编码，需要规范、通用的组建来完成，也就是现在的注册中心与服务发现
3. 微服务与单体架构相比会分布式集群部署，如何发挥集群分布式部署的能力，管理内部RPC与外部接口访问，微服务中提供的解决方式有：负载均衡、智能路由与统一网关
4. 各个服务所承受的访问压力是不同的，为了防止大规模访问导致某个服务访问失效，进而导致大量的依赖服务不可用，产生雪崩效应，微服务在这个问题上的解决方式是：限流熔断
5. 服务间的调用从单体的方法调用到微服务的RPC调用，各个微服务之间的依赖关系也会越来越复杂，定位排查问题的难度也会越来越大微服务统一的解决方式是：服务追踪
6. 随着服务数量与服务器规模的增加，配置管理的复杂度也越来越高，微服务的解决方式是：配置中心
7. 随着微服务架构的普及，越来越多的问题被纳入到微服务需要解决的范畴，微服务的疆域也越来越大：分布式事务、事件总线...

需要永远铭记“软件开发没有银弹”，微服务是一种架构，只能在架构层面给出文章首部提出的问题的一种解决方案，整个网站应用面临问题仍需从：团队建设，产品方向，开发流程（需求、设计、开发、测试、运维）的各个环节进行优化，共同解决问题。

早期的Dubbo就是实现了上述的RPC、服务发现、注册中心与负载均衡的功能，目前大家熟知的微服务Spring Cloud是上面解决方案标准化组件的合集，比较常用的解决方案组件集合有：Spring Cloud Netflix 、Spring Cloud Alibaba 等，下面就先体检下目前应用广泛的Spring Cloud Netflix。

## 2. Spring Cloud Netflix实践

基础环境与工具：Maven、Idea、JDK8

### 2.1 创建基础工程

1. 创建示例Maven工程，命名cloud-netflix，删除src文件；

2. 管理pom.xml文件，添加一下内容：

```
	<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.2.RELEASE</version>
        <relativePath /> <!-- lookup parent from repository -->
    </parent>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
        <spring-cloud.version>Hoxton.SR7</spring-cloud.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```



### 2.2 Eureka Server搭建 

1. 创建eureka-server服务模块，项目名称右键 -> New -> Module，命名为eureka-server

2. 在eureka-server模块的pom.xml文件中添加以下依赖代码：

```
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    </dependencies>
```

3. 分别在eureka-server的src/main/java与src/main/resources文件夹下创建EurekaServerApplication类与application.yml配置文件

```
### EurekaServerApplication 类

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

```
### application.yml配置文件

spring:
  application:
    name: eureka-server

server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

4. 启动EurekaServerApplication类中的main方法，完成服务注册中心单节点的启动

5. 浏览器访问：http://localhost:8761 查看Eureka服务监控页面，如果正常登录则说明单节点服务注册中心搭建完成

### 2.3 利用Eureka Client进行服务注册

1. 创建server-producer服务模块，项目名称右键 -> New -> Module，命名为server-producer ，作为服务的被调用模块
2. 在server-producer 模块的pom.xml文件中添加以下依赖代码：

```
	<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
```

3. 分别在server-producer 的src/main/java与src/main/resources文件夹下创建ServerProducerApplication类与application.yml配置文件

```
### ServerProducerApplication 类

@SpringBootApplication
@EnableEurekaClient
@RestController
public class ServerProducerApplication {

    @Value("${server.port}")
    private String port;

    @RequestMapping("/produce")
    public String produce(@RequestParam(value = "name", defaultValue = "anonymous") String name) {
        return "Hello " + name + " ,this is a message from port:" + port;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerProducerApplication.class, args);
    }
}
```

```
### application.yml配置文件
server:
  port: 8762

spring:
  application:
    name: service-producer

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

4. 启动ServerProducerApplication服务，浏览访问 http://localhost:8762/produce?name=spence，查看返回结果
5. 再次访问 http://localhost:8761 查看Eureka中注册的服务，查看 “**SERVICE-PRODUCER**”是否存在
6. 修改Idea的运行配置“Run/Debug Configurations”，在ServerProducerApplication运行服务类中勾选 “Allow paraller run”，允许并行运行
7. 修改 application.yml 服务中的端口为 8763，再次启动ServerProducerApplication，访问http://localhost:8763/produce?name=spence
8. 再次访问 http://localhost:8761 查看Eureka中注册的服务，查看 “**SERVICE-PRODUCER**”的“**Availability Zones**”是否变为2

### 2.3 利用Feign远程调用server-producer服务

1. 创建server-consumer服务模块，项目名称右键 -> New -> Module，命名为server-consumer ，作为服务的被调用模块
2. 在server-consumer模块的pom.xml文件中添加以下依赖代码：

```
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
    </dependencies>
```

3. 分别在server-consumer的src/main/java与src/main/resources文件夹下创建ServerConsumererApplication，类与application.yml配置文件

```
# ServerConsumererApplication

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableFeignClients
public class ServerConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerConsumerApplication.class, args);
    }
}
```

```
### application.yml配置文件

server:
  port: 8770

spring:
  application:
    name: server-consumer

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

4. 创建Consumer接口与ConsumerController类

```
# Consumer
@FeignClient(value = "service-producer")
public interface Consumer {

    @RequestMapping(value = "/produce",method = RequestMethod.GET)
    String consume(@RequestParam(value = "name") String name);

}
```

```
# ConsumerController
@RestController
public class ConsumerController {

    @Autowired
    private Consumer consumer;

    @GetMapping(value = "/consume")
    public String consume(@RequestParam String name) {
        String content = consumer.consume(name);
        return "echo { " + content + " }";
    }
}
```

5. 启动 并访问：http://localhost:8770/consume?name=spencez，根据返回结果可知，feign已经通过rpc调用producer服务，多次访问会发现端口号会随机在8762与8763之间变化，说明已经实现负载均衡；

### 2.4 利用Hystrix实现熔断限流

1. 在server-producer模块pom.xml文件中添加hystrix依赖

```
		<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
```

2. 在server-producer模块添加FailConsumer类实现Consumer接口

```
@Component
public class FailConsumer implements Consumer {
    @Override
    public String consume(String name) {
        return "sorry, " + name;
    }
}
```

3. 在server-producer模块的的Consumer接口注解增加fallback = FailConsumer.class

```
@FeignClient(value = "service-producer", fallback = FailConsumer.class)
public interface Consumer {

    @RequestMapping(value = "/produce",method = RequestMethod.GET)
    String consume(@RequestParam(value = "name") String name);

}
```

4. 重启ServerConsumerApplication，同时停止已经启动的 server-producer 服务，再次访问 http://localhost:8770/consume?name=spencez，会发现使用FailConsumer提供的异常处理服务。



## 参考

- [SpringCloudNetflix](https://spring.io/projects/spring-cloud-netflix)
- [SpringCloudLearning](https://github.com/forezp/SpringCloudLearning)

