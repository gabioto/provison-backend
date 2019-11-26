package pe.telefonica.provision.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response of service
 */
public class ResponseBucket {

    @JsonProperty("header")
    private BucketHeaderResponse header;
    @JsonProperty("body")
    private BucketBodyResponse body;

    public BucketHeaderResponse getHeader() {
        return header;
    }

    public void setHeader(BucketHeaderResponse header) {
        this.header = header;
    }

    public BucketBodyResponse getBody() {
        return body;
    }

    public void setBody(BucketBodyResponse body) {
        this.body = body;
    }

}
