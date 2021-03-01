# netty-demo
开发速度快，文档全，性能高

Reactor模型
翻译：反应器模式，分发者模式，通知者模式
原理：
1）通过一个或多个输入同时请求服务处理器(基于事件驱动)；
2）服务处理器接收到请求，同步分发到相应的处理线程；
3）使用IO复用监听事件，分发事件到线程(进程)，这点就是网络高并发的关键。

组成
1）Reactor：一个单独的线程
2）Handlers：事件处理器，可能是开启单独的线程，也可能开启个进程处理事件

单Reactor单线程模型
1）简单的nio群聊就是这种模式。
2）缺点：不能支持高并发。不能发挥cpu性能，有一个handler在读写时会阻塞整个系统。

单Reactor多线程模型
1）Handler加入了线程池，去处理耗时的业务逻辑。
2）需要注意的是：线程池处理完，需要将结果返回给handler，再返回给客户端。
3）优点：充分发挥多核cpu的优势；
4）缺点：多线程数据共享和访问比较复杂，Reactor处理所有事件的监听和响应，是单线程运行，高并发场景容易出现性能瓶颈



主从Reactor多线程模型
图解
1）reactor主线程MainReactor对象通过select监听事件，收到事件后转发给acceptor
2） acceptor处理连接事件后，将连接分配给subReactor
3） subReactor将连接加入到连接队列进行监听并创建handler进行各种事件处理
4）当有新事件发生数时，subReactor就会调用对应的handler处理
5）handler通过read读取数据，分发给后面的worker线程处理
6）worker线程池分配独立的线程进行业务处理，并返回结果
7）handler收到返回结果后，通过send将结果返回客户端
8）Reactor主线程可以对应多个Reactor子线程
优点：父线程和子线程职责明确，父线程只负责接收连接，子线程负责业务处理。父子线程交互也简单，父线程只需要把连接传给子线程，子线程无需返回数据
缺点：编程复杂度高
实例：nginx主从Reactor多线程模型，memcached主从多线程，netty主从多线程模型



Netty模型
image.png
Netty抽象出两组线程池，BossGroup专门负责接收客户端的连接，WorkerGroup专门负责网络读写
BossGroup和WorkerGroup类型都是NioEventLoopGroup
NioEventLoopGroup相当于一个事件循环组，这个组中含有多个事件循环，每一个事件循环是NioEventLoop
NioEventLoop表示一个不断循环的执行处理任务的线程，每个NioEventLoop都有一个selector，用于监听绑定在其上的socket的网络通讯
NioEventLoopGroup可以有多个线程，即可以含有多个NioEventLoop
每个Boss NioEventLoop 循环执行的步骤有3步
1）轮询accept事件
2）处理accept事件，与client建立连接，生成NioSocketChannel，并将其注册到某个worker NIOEventLoop上的selector
3）处理任务队列的任务，即runAllTasks
每个worker NIOEventLoop循环执行的步骤
1）轮询read，write事件
2）处理IO事件，即read,write，在对应NioSocketchannel处理
3）处理任务队列中的任务，即runAllTasks
每个Worker NIOEventLoop处理业务时，会使用pipeline(管道)，pipeline中包含了channel，即通过pipeline可以获取到对应通道，管道中维护了很多的处理器
实战
https://github.com/lanqqiao/net-demo.git

最简单Netty服务
服务：NettyServer
服务handler：NettyServerHandler extends ChannelInboundHandlerAdapter
客户端：NettyClient
客户端handler：NettyClientHandler extends ChannelInboundHandlerAdapter
hander任务异步实现
ctx.channel().eventLoop().execute(new Runnable() {});
ctx.channel().eventLoop().schedule(new Runnable() {}, 5, TimeUnit.SECONDS);
ChannelFuture 监听机制
ChannelFuture cf = bootstrap.bind(6668).sync();
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (cf.isSuccess()) {
                        System.out.println("监听端口6668成功");
                    }
                }
            });
http服务
主要是使用netty已经存在的handler，比如http编解码器：HttpServerCodec
ChannelInitializer初始化里面加内置http处理器

pipeline.addLast(new HttpServerCodec());
handler实现时继承内置channelInboundHandler
 extends SimpleChannelInboundHandler<HttpObject> 


链接：https://www.jianshu.com/p/d95638039a66

