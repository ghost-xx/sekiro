package cn.iinti.sekiro3.open;

import cn.iinti.sekiro3.business.netty.bootstrap.ServerBootstrap;
import cn.iinti.sekiro3.business.netty.buffer.Unpooled;
import cn.iinti.sekiro3.business.netty.channel.ChannelFutureListener;
import cn.iinti.sekiro3.business.netty.channel.ChannelInitializer;
import cn.iinti.sekiro3.business.netty.channel.nio.NioEventLoopGroup;
import cn.iinti.sekiro3.business.netty.channel.socket.SocketChannel;
import cn.iinti.sekiro3.business.netty.channel.socket.nio.NioServerSocketChannel;
import cn.iinti.sekiro3.business.netty.util.concurrent.DefaultThreadFactory;
import cn.iinti.sekiro3.open.core.Session;
import cn.iinti.sekiro3.open.detector.HttpMatcher;
import cn.iinti.sekiro3.open.detector.ProtocolDetector;
import cn.iinti.sekiro3.open.detector.ProtocolMatcher;
import cn.iinti.sekiro3.open.detector.SekiroMatcher;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
public class Bootstrap {
    private static boolean started = false;
    private static final Properties properties = new Properties();
    private static final byte[] UN_SUPPORT_PROTOCOL_MSG = "sekiro unknown protocol".getBytes();
    @Getter
    public static Integer listenPort;

    public static boolean isLocalDebug;

    public static void main(String[] args) throws Exception {
        if (started) {
            return;
        }
        InputStream resourceAsStream = Bootstrap.class.getClassLoader().getResourceAsStream("config.properties");

        properties.load(resourceAsStream);

        isLocalDebug = BooleanUtils.toBoolean(properties.getProperty("sekiro.localDebug"));
        System.out.println("isLocalDebug:" + isLocalDebug);
        parseNamedArgs(args);
        startUp();
        started = true;
    }
    

    
    private static void parseNamedArgs(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String paramValue = arg.substring(2); // 移除前导的--
                int equalsIndex = paramValue.indexOf('=');
                
                if (equalsIndex > 0) {
                    String paramName = paramValue.substring(0, equalsIndex);
                    String value = paramValue.substring(equalsIndex + 1);
                    
                    if ("sekiro.port".equals(paramName)) {
                        try {
                            int port = Integer.parseInt(value);
                            if (port > 0 && port < 65536) {
                                properties.setProperty("sekiro.port", value);

                                log.info("参数设置端口: {}", port);
                            } else {
                                log.warn("命名参数端口号无效: {}，将使用配置文件中的默认值", value);
                            }
                        } catch (NumberFormatException e) {
                            log.warn("解析命名参数端口号失败: {}，将使用配置文件中的默认值", value);
                        }
                    }
                    
                    if ("sekiro.strict.bindClientCheck".equals(paramName)) {
                        String lowercaseValue = value.toLowerCase();
                        if ("true".equals(lowercaseValue) || "false".equals(lowercaseValue)) {
                            properties.setProperty("sekiro.strict.bindClientCheck", lowercaseValue);
                            log.info("严格绑定客户端检查: {}", lowercaseValue);
                        } else {
                            log.warn("命名参数严格绑定客户端检查值无效: {}，将使用配置文件中的默认值", value);
                        }
                    }
                }
            }
        }
    }

    private static void startUp() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup serverBossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new DefaultThreadFactory("sekiro-boss"));
        NioEventLoopGroup serverWorkerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 6, new DefaultThreadFactory("sekiro-worker"));
        serverBootstrap.group(serverBossGroup, serverWorkerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                Session.newSession(socketChannel);
                List<ProtocolMatcher> matchers = Arrays.asList(
                        // sekiro底层协议
                        new SekiroMatcher(), new HttpMatcher());

                ProtocolDetector protocolDetector = new ProtocolDetector((ctx, buf) -> {
                    Session.get(ctx.channel()).getRecorder().recordEvent("unsupported protocol");
                    buf.release();
                    ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(UN_SUPPORT_PROTOCOL_MSG)).addListener(ChannelFutureListener.CLOSE);
                }, matchers.toArray(new ProtocolMatcher[]{}));
                socketChannel.pipeline().addLast(protocolDetector);
            }
        });

        listenPort = NumberUtils.toInt(properties.getProperty("sekiro.port", "5612"));
        String startMsg = "启动sekiro网络服务器,端口:" + listenPort;
        log.info(startMsg);
        String bindCheckMsg = "自动匹配客户端:" + isStrictBindClientCheck();
        log.info(bindCheckMsg);
        serverBootstrap.bind(listenPort).addListener(future -> {
            if (future.isSuccess()) {
                String successMsg = "sekiro网络服务器启动成功";
                log.info(successMsg);

            } else {
                String failMsg = "sekiro网络服务器启动失败";
                log.info(failMsg);

            }
        });
    }

    public static boolean isStrictBindClientCheck() {
        return Boolean.parseBoolean(properties.getProperty("sekiro.strict.bindClientCheck", "false"));
    }

}
