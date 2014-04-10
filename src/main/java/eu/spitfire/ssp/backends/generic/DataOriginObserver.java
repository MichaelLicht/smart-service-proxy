package eu.spitfire.ssp.backends.generic;

import org.jboss.netty.channel.local.LocalServerChannel;

import java.util.concurrent.ScheduledExecutorService;

public abstract class DataOriginObserver<T> extends DataOriginAccessor<T>{

    private ScheduledExecutorService executorService;


    protected DataOriginObserver(LocalServerChannel localChannel, ScheduledExecutorService executorService){
        super(localChannel);
        this.executorService = executorService;
    }


    /**
     * Starts the observation of the given {@link eu.spitfire.ssp.backends.generic.DataOrigin}. Whenever the status
     * of the observed {@link eu.spitfire.ssp.backends.generic.DataOrigin} changes, implementing classes are supposed
     * to invoke {@link #updateCache(WrappedDataOriginStatus)}.
     *
     * @param dataOrigin the {@link eu.spitfire.ssp.backends.generic.DataOrigin} to be observed.
     */
    public abstract void startObservation(DataOrigin<T> dataOrigin);


    /**
     * Returns the {@link java.util.concurrent.ScheduledExecutorService} to be used to schedule or submit observation
     * specific tasks.
     *
     * @return the {@link java.util.concurrent.ScheduledExecutorService} to be used to schedule or submit observation
     * specific tasks.
     */
    protected ScheduledExecutorService getExecutorService(){
        return this.executorService;
    }


//    /**
//     * This method is to be invoked by extending classes if there was an update at the data origin.
//     *
//     * @param model the {@link Model} containing the new status of the resource(s) hosted at the observed
//     *              data origin
//     * @param expiry the {@link Date} indicating the expiry of the new status
//     */
//    protected final void cacheResourcesStates(Model model, final Date expiry){
//        final Map<URI, Model> models = ResourceToolbox.getModelsPerSubject(model);
//        for(final URI resourceUri : models.keySet()){
//            scheduledExecutorService.submit(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        cacheResourceStatus(models.get(resourceUri), expiry);
//                    }
//                    catch (MultipleSubjectsInModelException | URISyntaxException e) {
//                        log.error("This should never happen.", e);
//                    }
//                }
//            });
//        }
//    }
//
//
//    public ChannelFuture cacheResourceStatus(final Model model, Date expiry) throws MultipleSubjectsInModelException, URISyntaxException {
//
//        InternalResourceStatusMessage internalResourceStatusMessage = new InternalResourceStatusMessage(model, expiry);
//        return Channels.write(localChannel, internalResourceStatusMessage);
//
//    }

//
//    protected ChannelFuture deleteResource(URI resourceUri){
//        InternalRemoveResourcesMessage message = new InternalRemoveResourcesMessage(resourceUri);
//        return Channels.write(localChannel, message);
//    }
//
//
//    protected final void updateResourceStatus(Statement statement, Date expiry){
//        InternalUpdateResourceStatusMessage message = new InternalUpdateResourceStatusMessage(statement, expiry);
//        Channels.write(localChannel, message);
//    }



}

