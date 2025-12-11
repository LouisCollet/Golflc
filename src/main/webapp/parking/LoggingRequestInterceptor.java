package listeners;
import static interfaces.Log.LOG;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LoggingRequestInterceptor implements HttpRequestInterceptor {
/*
    @Override
    public void process(HttpRequest request, HttpContext context) throws IOException {
        String message = buildRequestEntry(request, context) 
                    + buildHeadersEntry(request.getAllHeaders())
                    + buildEntityEntry(request);
        LOG.info(message);
    }


    private String buildRequestEntry(HttpRequest request, HttpContext context) {
        return "\nRequest - "
                + request.getRequestLine().getMethod + " " 
                + context.getAttribute("http.target_host") 
                + request.getRequestLine().getUri();
    }

    private String buildHeadersEntry(Header[] headers) {
        return "\nHeaders: ["
                + Arrays.asList(headers).stream()
                        .map(header -> header.getName() + ": " + header.getValue())
                        .collect(Collectors.joining(", "))
                + "]";
    }

    private String buildEntityEntry(HttpRequest request) throws IOException {
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            entity.writeTo(bs);
            return "\nPayload:\n" + new String(bs.toByteArray());
        }
    }
*/
}