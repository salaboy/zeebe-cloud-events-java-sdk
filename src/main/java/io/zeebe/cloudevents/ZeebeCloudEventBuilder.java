package io.zeebe.cloudevents;

import io.cloudevents.CloudEvent;
import io.cloudevents.extensions.ExtensionFormat;
import io.cloudevents.v03.AttributesImpl;
import io.cloudevents.v03.CloudEventBuilder;

public class ZeebeCloudEventBuilder {
    private CloudEventBuilder<String> cloudEventBuilder;
    private final ZeebeCloudEventExtension zeebeCloudEventExtension;

    public ZeebeCloudEventBuilder(CloudEventBuilder<String> cloudEventBuilder) {
        this.cloudEventBuilder = cloudEventBuilder;
        zeebeCloudEventExtension = new ZeebeCloudEventExtension();
    }

    public ZeebeCloudEventBuilder withCorrelationKey(String correlationKey) {
        zeebeCloudEventExtension.setCorrelationKey(correlationKey);
        return this;
    }

    public ZeebeCloudEventBuilder withJobKey(String jobKey) {
        zeebeCloudEventExtension.setJobKey(jobKey);
        return this;
    }

    public ZeebeCloudEventBuilder withWorkflowKey(String workflowKey) {
        zeebeCloudEventExtension.setWorkflowKey(workflowKey);
        return this;
    }

    public ZeebeCloudEventBuilder withWorkflowInstanceKey(String workflowInstanceKey) {
        zeebeCloudEventExtension.setWorkflowInstanceKey(workflowInstanceKey);
        return this;
    }

    public ZeebeCloudEventBuilder withBpmnActivityId(String bpmnActivityId) {
        zeebeCloudEventExtension.setBpmnActivityId(bpmnActivityId);
        return this;
    }

    public ZeebeCloudEventBuilder withBpmnActivityName(String bpmnActivityName) {
        zeebeCloudEventExtension.setBpmnActivityName(bpmnActivityName);
        return this;
    }

    public CloudEvent<AttributesImpl, String> build() {
        ExtensionFormat zeebe = new ZeebeCloudEventExtension.Format(zeebeCloudEventExtension);
        return cloudEventBuilder
                .withExtension(zeebe)
                .build();
    }
}
