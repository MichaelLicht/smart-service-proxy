package eu.spitfire.ssp.server.handler;

//import org.apache.log4j.spi.LoggerFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Created by 614991 on 30.10.15.
 */
public class MqttHandlerML extends SimpleChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(MqttHandlerML.class.getName());

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent me) throws Exception {
        //if (me.getMessage() instanceof HttpRequest) {
            log.info("Dies ist eine Instanz von {}", me.getMessage().getClass().getName());
        //}
        System.out.println(me.getMessage().getClass().getName());
        ctx.sendUpstream(me);
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent me) throws Exception {
        System.out.println("Bye");
        System.out.println(me.getMessage().getClass().getName());
        ctx.sendDownstream(me);
    }

}