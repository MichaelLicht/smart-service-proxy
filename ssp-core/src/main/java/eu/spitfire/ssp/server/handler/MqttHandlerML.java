package eu.spitfire.ssp.server.handler;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import eu.spitfire.ssp.server.internal.message.InternalCacheUpdateRequest;
import eu.spitfire.ssp.server.internal.utils.HttpResponseFactory;
import eu.spitfire.ssp.server.internal.utils.Language;
import eu.spitfire.ssp.server.internal.wrapper.ExpiringGraph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.nio.charset.Charset;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;


/**
 * Created by 614991 on 30.10.15.
 */
public class MqttHandlerML extends SimpleChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(MqttHandlerML.class.getName());
    public static final Language language = Language.RDF_TURTLE;

    private HttpRequest httpRequest;
    private HttpResponse httpResponse;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent me) throws Exception {
        log.info("Dies ist eine Instanz von {}", me.getMessage().getClass().getName());
        System.out.println(me.getMessage().getClass().getName());

        ctx.sendUpstream(me);
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent me) throws Exception {
        System.out.println(me.getMessage().getClass().getName());

        // Check for Update
        if(me.getMessage() instanceof InternalCacheUpdateRequest){
            InternalCacheUpdateRequest request = (InternalCacheUpdateRequest) me.getMessage();
            // Test, nicht sicher was benötigt wird
            Model model = request.getExpiringNamedGraph().getModel();
            StringWriter writer = new StringWriter();

            RDFDataMgr.write(writer, model, Lang.TTL);

            log.info("Model to be serialized{}", model);

            byte[] content = writer.toString().getBytes(Charset.forName("UTF-8"));
            // Ausgabe vom Inhalt
            //System.out.println(new String(content, Charset.forName("UTF-8")));

            // Setting expiringNamedGraph as Topic
            String topic        = request.getExpiringNamedGraph().getGraphName().toString();
            System.out.println(topic);

            String mqttContent      = (new String(content, Charset.forName("UTF-8")));
            int qos             = 2;
            String broker       = "tcp://localhost:1883";
            String clientId     = "MqttHandlerML";

            try {
                MqttClient mqttClient = new MqttClient(broker, clientId);
                System.out.println("Connecting to broker:"+broker);
                mqttClient.connect();
                System.out.println("Connected");
                System.out.println("Publishing update");
                MqttMessage message = new MqttMessage(mqttContent.getBytes());
                message.setQos(qos);
                message.setRetained(true);
                mqttClient.publish(topic, message);
                System.out.println("Update published");
                mqttClient.disconnect();
            } catch(MqttException mq) {
                System.out.println("reason "+mq.getReasonCode());
                System.out.println("msg "+mq.getMessage());
                System.out.println("loc "+mq.getLocalizedMessage());
                System.out.println("cause "+mq.getCause());
                System.out.println("excep "+mq);
                mq.printStackTrace();
            }
        }
        ctx.sendDownstream(me);
    }
}