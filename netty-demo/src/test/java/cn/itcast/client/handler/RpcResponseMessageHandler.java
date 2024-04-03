package cn.itcast.client.handler;

import cn.itcast.message.RpcRequestMessage;
import cn.itcast.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对消息进行处理，并返回给客户端
 */

@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    //                       序号      用来接收结果的 promise 对象
    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();

    @Override

    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        log.info("===================={}", msg);
        // 根据 sequenceId 拿到空的 promise
        Promise<Object> promise = PROMISES.remove(msg.getSequenceId());
        if (promise != null) {
            // 定义消息的返回结果
            Object returnValue = msg.getReturnValue();
            // 定义消息的异常结果
            Exception exceptionValue = msg.getExceptionValue();
            if(exceptionValue != null) {    // exceptionValue 是空的说明无异常 否则消息报异常
                promise.setFailure(exceptionValue); // 设置异常
            } else {
                promise.setSuccess(returnValue);    // 设置返回结果
            }
        }
    }
}
