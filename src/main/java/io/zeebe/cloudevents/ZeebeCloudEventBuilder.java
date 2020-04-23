package io.zeebe.cloudevents;

import io.cloudevents.CloudEvent;
import io.cloudevents.extensions.ExtensionFormat;
import io.cloudevents.v03.AttributesImpl;
import io.cloudevents.v03.CloudEventBuilder;

public class ZeebeCloudEventBuilder {
    private CloudEventBuilder<String> cloudEventBuilder;
    private static final ZeebeCloudEventBuilder zeebeCloudEventBuilder = new ZeebeCloudEventBuilder();
    private final ZeebeCloudEventExtension zeebeCloudEventExtension;

    public ZeebeCloudEventBuilder() {
        zeebeCloudEventExtension = new ZeebeCloudEventExtension();
    }


    public ZeebeCloudEventBuilder withCorrelationKey(String correlationKey){
        zeebeCloudEventExtension.setCorrelationKey(correlationKey);
        return zeebeCloudEventBuilder;
    }


    public CloudEvent<AttributesImpl, String> build(){
        ExtensionFormat zeebe = new ZeebeCloudEventExtension.Format(zeebeCloudEventExtension);
        return cloudEventBuilder
                .withExtension(zeebe)
                .build();
    }
}
