package io.zeebe.cloudevents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaboy.cloudevents.helper.CloudEventsHelper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.zeebe.client.api.response.ActivatedJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class ZeebeCloudEventsHelper {

    private static ObjectMapper mapper = new ObjectMapper();

    /*
     * This method will parse an HTTP request (headers and body) and it will create a Zeebe Cloud Event, that means
     * a Cloud Event From cloudevents.io with a Zeebe Extension
     * If the Zeebe Extension is not present in the headers, it will return a base Cloud Event.
     */
    public static CloudEvent  parseZeebeCloudEventFromRequest(HttpHeaders headers, Object body){
        ZeebeCloudEventExtension zeebeCloudEventExtension =  createZeebeCloudEventExtension(headers);
        return internalParseCloudEventWithExtensionOrDefault(body, headers.toSingleValueMap(), zeebeCloudEventExtension);
    }

    private static ZeebeCloudEventExtension createZeebeCloudEventExtension(HttpHeaders headers) {
        ZeebeCloudEventExtension zeebeCloudEventExtension =  new ZeebeCloudEventExtension();
        zeebeCloudEventExtension.setCorrelationKey(headers.getFirst(ZeebeCloudEventExtension.CORRELATION_KEY));
        zeebeCloudEventExtension.setBpmnActivityId(headers.getFirst(ZeebeCloudEventExtension.BPMN_ACTIVITY_ID));
        zeebeCloudEventExtension.setBpmnActivityName(headers.getFirst(ZeebeCloudEventExtension.BPMN_ACTIVITY_NAME));
        zeebeCloudEventExtension.setWorkflowKey(headers.getFirst(ZeebeCloudEventExtension.WORKFLOW_KEY));
        zeebeCloudEventExtension.setWorkflowInstanceKey(headers.getFirst(ZeebeCloudEventExtension.WORKFLOW_INSTANCE_KEY));
        zeebeCloudEventExtension.setJobKey(headers.getFirst(ZeebeCloudEventExtension.JOB_KEY));
        return zeebeCloudEventExtension;
    }

    private static CloudEvent internalParseCloudEventWithExtensionOrDefault(Object body,  Map<String, String> headers, ZeebeCloudEventExtension extension) {
        if (extension != null) {
            return CloudEventsHelper.parseFromRequestWithExtension(headers, body, extension);
        } else {
            return CloudEventsHelper.parseFromRequest(headers, body);
        }
    }


    /*
     * This method will create a Zeebe Cloud Event from an ActivatedJob inside a worker, this allow other systems to consume
     * this Cloud Event and
     */
    public static CloudEvent createZeebeCloudEventFromJob(ActivatedJob job) throws JsonProcessingException {


        final ZeebeCloudEventExtension zeebeCloudEventExtension = new ZeebeCloudEventExtension();

        // I need to do the HTTP to Cloud Events mapping here, that means picking up the CorrelationKey header and add it to the Cloud Event
        zeebeCloudEventExtension.setBpmnActivityId(String.valueOf(job.getElementInstanceKey()));
        zeebeCloudEventExtension.setBpmnActivityName(job.getElementId());
        zeebeCloudEventExtension.setJobKey(String.valueOf(job.getKey()));
        zeebeCloudEventExtension.setWorkflowKey(String.valueOf(job.getWorkflowKey()));
        zeebeCloudEventExtension.setWorkflowInstanceKey(String.valueOf(job.getWorkflowInstanceKey()));
        ObjectMapper objectMapper = new ObjectMapper();
        log.info(">>>>> Job Variables: " + objectMapper.writeValueAsString(job.getVariables()));

        final CloudEvent zeebeCloudEvent = CloudEventBuilder.v03()
                .withId(UUID.randomUUID().toString())
                .withTime(ZonedDateTime.now())
                .withType(job.getCustomHeaders().get(Headers.CLOUD_EVENT_TYPE)) // from headers
                .withSource(URI.create("zeebe.default.svc.cluster.local"))
                .withData(objectMapper.writeValueAsString(job.getVariables()).getBytes())
                .withDataContentType(Headers.CONTENT_TYPE)
                .withSubject("Zeebe Job")
                .withExtension(zeebeCloudEventExtension)
                .build();

        return zeebeCloudEvent;
    }


    /*
     * Using a CloudEventsBuilder we can create a ZeebeCloudEventBuilder where we can add the Zeebe Extension parameters
     * and then build a ZeebeCloudEvent.
     */
    public static ZeebeCloudEventBuilder buildZeebeCloudEvent(CloudEventBuilder cloudEventBuilder){
        return new ZeebeCloudEventBuilder(cloudEventBuilder);
    }

    public static void emitZeebeCloudEventHTTPFromJob(ActivatedJob job, String host) throws JsonProcessingException {

        final CloudEvent myCloudEvent = ZeebeCloudEventsHelper.createZeebeCloudEventFromJob(job);

      //  log.info(Json.encode(myCloudEvent));

        WebClient webClient = WebClient.builder().baseUrl(host).filter(logRequest()).build();

        WebClient.ResponseSpec postCloudEvent = CloudEventsHelper.createPostCloudEvent(webClient, "/", myCloudEvent);

        postCloudEvent.bodyToMono(String.class).doOnError(t -> t.printStackTrace())
                .doOnSuccess(s -> log.info("Result -> " + s)).subscribe();
    }

    //@TODO: refactor to helper class
    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: " + clientRequest.method() + " - " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info(name + "=" + value)));
            return Mono.just(clientRequest);
        });
    }



}
