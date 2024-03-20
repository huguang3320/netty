package cn.itcast.nettystart.protocol;

/**
 * 自定义协议要素：
 * 1、魔数：用来第一时间判断是否是有效包
 * 2、版本号：可以支持协议的升级
 * 3、序列化算法：消息正文采用那种序列化方式，例如 json、jdk、protobuf、hessian等
 * 4、指令类型：登录、注册、单聊、群聊
 * 5、请求序号：为了双工通信，提供异步能力
 * 6、正文长度
 * 7、消息正文
 */
public class MessageCodec {

}
