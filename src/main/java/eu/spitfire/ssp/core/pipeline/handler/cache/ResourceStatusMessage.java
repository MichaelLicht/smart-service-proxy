package eu.spitfire.ssp.core.pipeline.handler.cache;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.options.OptionRegistry;
import eu.spitfire.ssp.core.payloadserialization.Language;
import eu.spitfire.ssp.core.payloadserialization.ShdtDeserializer;
import eu.spitfire.ssp.gateway.ProxyServiceException;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Date;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 09.08.13
 * Time: 16:42
 * To change this template use File | Settings | File Templates.
 */
public class ResourceStatusMessage {

    private static Logger log = LoggerFactory.getLogger(ResourceStatusMessage.class.getName());
    private URI resourceUri;
    private final Model resourceStatus;
    private final Date expiry;

    public ResourceStatusMessage(URI resourceUri, Model resourceStatus, Date expiry){
        this.resourceUri = resourceUri;

        this.resourceStatus = resourceStatus;
        this.expiry = expiry;
    }

    public Model getResourceStatus() {
        return resourceStatus;
    }

    public Date getExpiry() {
        return expiry;
    }

    public URI getResourceUri() {
        return resourceUri;
    }

    public static ResourceStatusMessage create(CoapResponse coapResponse, URI resourceUri) throws Exception{

        Model resourceStatus = ModelFactory.createDefaultModel();;

        //read payload from CoAP response
        byte[] coapPayload = new byte[coapResponse.getPayload().readableBytes()];
        coapResponse.getPayload().getBytes(0, coapPayload);

        if(coapResponse.getContentType() == OptionRegistry.MediaType.APP_SHDT){
            log.debug("SHDT payload in CoAP response.");
            (new ShdtDeserializer(64)).read_buffer(resourceStatus, coapPayload);
        }
        else{
            Language language = Language.getByCoapMediaType(coapResponse.getContentType());
            if(language == null){
                throw new ProxyServiceException(INTERNAL_SERVER_ERROR, "CoAP response had no semantic content type");
            }

            try{
                resourceStatus.read(new ByteArrayInputStream(coapPayload), null, language.lang);
            }
            catch(Exception e){
                log.error("Error while reading resource status from CoAP response!", e);
                throw new ProxyServiceException(INTERNAL_SERVER_ERROR,
                        "Error while reading resource status from CoAP response!", e);
            }
        }

        //Get expiry of resource
        Long maxAge = (Long) coapResponse.getOption(OptionRegistry.OptionName.MAX_AGE)
                .get(0).getDecodedValue();

        log.debug("Max-Age option of CoAP response: {}", maxAge);

        return new ResourceStatusMessage(resourceUri, resourceStatus, getExpiryDate(maxAge));
    }

    private static Date getExpiryDate(Long secondsFromNow){
        return new Date(System.currentTimeMillis() + 1000 * secondsFromNow);
    }
}
