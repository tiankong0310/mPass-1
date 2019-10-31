package com.ibyte.framework.discovery.handler;

import com.alibaba.fastjson.JSONObject;
import com.ibyte.common.exception.KmssServiceException;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @Description: <RestTemplate ResponseErrorHandler>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-31
 */
public class RestResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        int rawStatusCode = response.getRawStatusCode();
        HttpStatus statusCode = HttpStatus.resolve(rawStatusCode);
        return (statusCode != null ? hasError(statusCode) : hasError(rawStatusCode));
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = HttpStatus.resolve(response.getRawStatusCode());
        if (statusCode == null) {
            throw new KmssServiceException("errors.unkown", new UnknownHttpStatusCodeException(response.getRawStatusCode(), response.getStatusText(),response.getHeaders(), getResponseBody(response), getCharset(response)));
        }
        handleError(response, statusCode);
    }

    protected boolean hasError(HttpStatus statusCode) {
        return statusCode.isError();
    }

    protected boolean hasError(int unknownStatusCode) {
        HttpStatus.Series series = HttpStatus.Series.resolve(unknownStatusCode);
        return (series == HttpStatus.Series.CLIENT_ERROR || series == HttpStatus.Series.SERVER_ERROR);
    }

    protected byte[] getResponseBody(ClientHttpResponse response) {
        try {
            return FileCopyUtils.copyToByteArray(response.getBody());
        }
        catch (IOException ex) {
            throw new KmssServiceException("errors.unkown", ex);
        }
    }

    protected Charset getCharset(ClientHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        MediaType contentType = headers.getContentType();
        return (contentType != null ? contentType.getCharset() : null);
    }

    protected void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
        Charset charset = getCharset(response);
        String body = IOUtils.toString(response.getBody(), charset);
        JSONObject jo = JSONObject.parseObject(body);
        if(jo.containsKey("code") && jo.containsKey("msg")) {
            throw new KmssServiceException(jo.getString("code"), jo.getString("msg"));
        }
    }
}
