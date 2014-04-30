package eu.spitfire.ssp.backends.generic.registration;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import eu.spitfire.ssp.backends.generic.BackendComponentFactory;
import eu.spitfire.ssp.backends.generic.DataOrigin;
import eu.spitfire.ssp.server.http.webservices.HttpSemanticProxyWebservice;
import eu.spitfire.ssp.backends.generic.observation.DataOriginObserver;
import eu.spitfire.ssp.server.common.messages.DataOriginRegistrationMessage;
import eu.spitfire.ssp.server.common.messages.DataOriginRemovalMessage;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.local.LocalServerChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * A {@link DataOriginRegistry} is the component to register new data origins, i.e. the resources from data origins.
 * A data origin could e.g. be a Webservice whose response contains the status of at least one semantic resource. In
 * that example the generic type T would be an {@link URI}.
 *
 * @author Oliver Kleine
 */
public abstract class DataOriginRegistry<T> {

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    protected BackendComponentFactory<T> componentFactory;

    protected DataOriginRegistry(BackendComponentFactory<T> componentFactory) {
        this.componentFactory = componentFactory;
    }


    /**
     * This method is to be called by implementing classes, i.e. registries for particular data origins,
     * to register the model from that data origin at the SSP.
     *
     * @param dataOrigin the data origin to be registered
     *
     * @return a {@link ListenableFuture} where {@link ListenableFuture#get()} returns the list of resource proxy URIs
     * for all resources from the given data origin / model.
     */
    protected final ListenableFuture<Void> registerDataOrigin(final DataOrigin<T> dataOrigin){

        final SettableFuture<Void> registrationFuture = SettableFuture.create();

        HttpSemanticProxyWebservice httpProxyWebservice = componentFactory.getSemanticProxyWebservice(dataOrigin);
        T identifier = dataOrigin.getIdentifier();

        try{
            //Register resource
            final DataOriginRegistrationMessage<T> registerResourceMessage =
                    new DataOriginRegistrationMessage<>(dataOrigin, httpProxyWebservice);

            log.info("Try to register data origin with identifier \"{}\".", identifier);

            LocalServerChannel localChannel = componentFactory.getLocalChannel();
            ChannelFuture channelFuture = Channels.write(localChannel, registerResourceMessage);

            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        registrationFuture.set(null);

                        if(dataOrigin.isObservable()){
                            DataOriginObserver<T> observer = componentFactory.getDataOriginObserver(dataOrigin);

                            if(observer != null){
                                log.info("Start observation of data origin \"{}\".", dataOrigin);
                                observer.startObservation(dataOrigin);
                            }
                            else{
                                log.warn("Backend component factory did not return a data origin observer for \"{}\"",
                                        dataOrigin.getIdentifier());
                            }
                        }

                    }
                    else
                        registrationFuture.setException(future.getCause());
                }
            });

            return registrationFuture;
        }

        catch (Exception ex) {
            log.error("Registration of data origin failed!", ex);
            registrationFuture.setException(ex);

            return registrationFuture;
        }
    }



    protected ListenableFuture<Void> removeDataOrigin(DataOrigin<T> dataOrigin){
        final SettableFuture<Void> removalFuture = SettableFuture.create();

        log.info("Try to remove data origin: \"{}\".", dataOrigin);
        DataOriginRemovalMessage<T> removalMessage = new DataOriginRemovalMessage<>(dataOrigin);

        Channels.write(componentFactory.getLocalChannel(), removalMessage)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if(future.isSuccess())
                            removalFuture.set(null);
                        else
                            removalFuture.setException(future.getCause());
                    }
                });

        return removalFuture;
    }

    /**
     * This method is automatically invoked by the framework. Implementing classes are supposed to do everything that
     * is necessary to enable new data origins to register at this
     * {@link eu.spitfire.ssp.backends.generic.registration.DataOriginRegistry} instance.
     */
    public abstract void startRegistry() throws Exception;
}
