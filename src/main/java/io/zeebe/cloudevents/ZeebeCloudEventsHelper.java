package io.zeebe.cloudevents;

import com.salaboy.cloudevents.helper.CloudEventsHelper;
import io.cloudevents.CloudEvent;
import io.cloudevents.extensions.ExtensionFormat;
import io.cloudevents.json.Json;
import io.cloudevents.v03.AttributesImpl;
import io.cloudevents.v03.CloudEventBuilder;
import io.zeebe.client.api.response.ActivatedJob;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

public class ZeebeCloudEventsHelper {

    /*
     * This method will parse an HTTP request (headers and body) and it will create a Zeebe Cloud Event, that means
     * a Cloud Event From cloudevents.io with a Zeebe Extension
     */
    public static CloudEvent<AttributesImpl, String>  parseZeebeCloudEventFromRequest(Map<String, String> headers, Object body){
        String extension = headers.get(Headers.ZEEBE_CLOUD_EVENTS_EXTENSION);
        ZeebeCloudEventExtension zeebeCloudEventExtension = Json.decodeValue(extension, ZeebeCloudEventExtension.class);
        final ExtensionFormat zeebe = new ZeebeCloudEventExtension.Format(zeebeCloudEventExtension);
        return CloudEventsHelper.parseFromRequestWithExtension(headers, body, zeebe);
    }

    /*
     * This method will create a Zeebe Cloud Event from an ActivatedJob inside a worker, this allow other systems to consume
     * this Cloud Event and
     */
    public static CloudEvent<AttributesImpl, String> createZeebeCloudEventFromJob(ActivatedJob job){
        ZeebeCloudEventExtension zeebeCloudEventExtension = new ZeebeCloudEventExtension();
        // I need to do the HTTP to Cloud Events mapping here, that means picking up the CorrelationKey header and add it to the Cloud Event
        zeebeCloudEventExtension.setBpmnActivityId(String.valueOf(job.getElementInstanceKey()));
        zeebeCloudEventExtension.setBpmnActivityName(job.getElementId());
        zeebeCloudEventExtension.setJobKey(String.valueOf(job.getKey()));
        zeebeCloudEventExtension.setWorkflowKey(String.valueOf(job.getWorkflowKey()));
        zeebeCloudEventExtension.setWorkflowInstanceKey(String.valueOf(job.getWorkflowInstanceKey()));

        final ExtensionFormat zeebe = new ZeebeCloudEventExtension.Format(zeebeCloudEventExtension);

        final CloudEvent<AttributesImpl, String> zeebeCloudEvent = CloudEventBuilder.<String>builder()
                .withId(UUID.randomUUID().toString())
                .withTime(ZonedDateTime.now())
                .withType(job.getCustomHeaders().get(Headers.CLOUD_EVENT_TYPE)) // from headers
                .withSource(URI.create("zeebe.default.svc.cluster.local"))
                .withData(job.getVariables())
                .withDatacontenttype(Headers.CONTENT_TYPE)
                .withSubject("Zeebe Job")
                .withExtension(zeebe)
                .build();

        return zeebeCloudEvent;
    }


    /*
     * Using a CloudEventsBuilder we can create a ZeebeCloudEvent with the following builder
     */
    public static ZeebeCloudEventBuilder buildZeebeCloudEvent(CloudEventBuilder<String> cloudEventBuilder){

        cloudEventBuilder.withExtension(zeebe);
    }



}
