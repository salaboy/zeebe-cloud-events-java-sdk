package io.zeebe.cloudevents;

import io.cloudevents.extensions.ExtensionFormat;
import io.cloudevents.extensions.InMemoryFormat;

import java.util.*;

// @TODO: can this live in a library?
public class ZeebeCloudEventExtension {
    private String correlationKey;
    private String bpmnActivityName;
    private String bpmnActivityId;
    private String workflowKey;
    private String workflowInstanceKey;
    private String jobKey;

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public String getBpmnActivityName() {
        return bpmnActivityName;
    }

    public void setBpmnActivityName(String bpmnActivityName) {
        this.bpmnActivityName = bpmnActivityName;
    }

    public String getBpmnActivityId() {
        return bpmnActivityId;
    }

    public void setBpmnActivityId(String bpmnActivityId) {
        this.bpmnActivityId = bpmnActivityId;
    }

    public String getWorkflowKey() {
        return workflowKey;
    }

    public void setWorkflowKey(String workflowKey) {
        this.workflowKey = workflowKey;
    }

    public String getWorkflowInstanceKey() {
        return workflowInstanceKey;
    }

    public void setWorkflowInstanceKey(String workflowInstanceKey) {
        this.workflowInstanceKey = workflowInstanceKey;
    }

    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZeebeCloudEventExtension that = (ZeebeCloudEventExtension) o;
        return Objects.equals(correlationKey, that.correlationKey) &&
                Objects.equals(bpmnActivityName, that.bpmnActivityName) &&
                Objects.equals(bpmnActivityId, that.bpmnActivityId) &&
                Objects.equals(workflowKey, that.workflowKey) &&
                Objects.equals(workflowInstanceKey, that.workflowInstanceKey) &&
                Objects.equals(jobKey, that.jobKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationKey, bpmnActivityName, bpmnActivityId, workflowKey, workflowInstanceKey, jobKey);
    }

    public static class Format implements ExtensionFormat {
        public static final String IN_MEMORY_KEY = "zeebe";
        public static final String CORRELATION_KEY = "correlationKey";
        public static final String BPMN_ACTIVITY_ID = "bpmnActivityId";
        public static final String BPMN_ACTIVITY_NAME = "bpmnActivityName";
        public static final String WORKFLOW_KEY = "workflowKey";
        public static final String WORKFLOW_INSTANCE_KEY = "workflowInstanceKey";
        public static final String JOB_KEY = "jobKey";

        private final InMemoryFormat memory;
        private final Map<String, String> transport = new HashMap<>();

        public Format(ZeebeCloudEventExtension extension) {
            Objects.requireNonNull(extension);

            memory = InMemoryFormat.of(IN_MEMORY_KEY, extension,
                    ZeebeCloudEventExtension.class);
            transport.put(CORRELATION_KEY, extension.getCorrelationKey());
            transport.put(BPMN_ACTIVITY_ID, extension.getBpmnActivityId());
            transport.put(BPMN_ACTIVITY_NAME, extension.getBpmnActivityName());
            transport.put(WORKFLOW_KEY, extension.getWorkflowKey());
            transport.put(WORKFLOW_INSTANCE_KEY, extension.getWorkflowInstanceKey());
            transport.put(JOB_KEY, extension.getJobKey());

        }

        @Override
        public InMemoryFormat memory() {
            return memory;
        }

        @Override
        public Map<String, String> transport() {
            return transport;
        }

        public static Optional<ExtensionFormat> unmarshall(
                Map<String, String> exts) {
            String correlationKey = exts.get(Format.CORRELATION_KEY);
            String bpmnActivityId = exts.get(Format.BPMN_ACTIVITY_ID);
            String bpmnActivityName = exts.get(Format.BPMN_ACTIVITY_NAME);
            String workflowKey = exts.get(Format.WORKFLOW_KEY);
            String workflowInstanceKey = exts.get(Format.WORKFLOW_INSTANCE_KEY);
            String jobKey = exts.get(Format.JOB_KEY);


            ZeebeCloudEventExtension zcee = new ZeebeCloudEventExtension();
            zcee.setCorrelationKey(correlationKey);


            InMemoryFormat inMemory =
                    InMemoryFormat.of(Format.IN_MEMORY_KEY, zcee,
                            ZeebeCloudEventExtension.class);

            return Optional.of(
                    ExtensionFormat.of(inMemory,
                            new AbstractMap.SimpleEntry<>(Format.CORRELATION_KEY, correlationKey),
                            new AbstractMap.SimpleEntry<>(Format.BPMN_ACTIVITY_ID, bpmnActivityId),
                            new AbstractMap.SimpleEntry<>(Format.BPMN_ACTIVITY_NAME, bpmnActivityName),
                            new AbstractMap.SimpleEntry<>(Format.WORKFLOW_KEY, workflowKey),
                            new AbstractMap.SimpleEntry<>(Format.WORKFLOW_INSTANCE_KEY, workflowInstanceKey),
                            new AbstractMap.SimpleEntry<>(Format.JOB_KEY, jobKey)
                    )
            );

        }
    }
}
