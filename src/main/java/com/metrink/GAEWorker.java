package com.metrink;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class GAEWorker extends CallableWorker {
    private static final Logger LOG = LoggerFactory.getLogger(GAEWorker.class);

    private URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();

    public GAEWorker(MetricFactory metricFactory, Map<String, Long> serverTime, Map<String, Long> pageCount) {
        super(metricFactory, serverTime, pageCount);
    }

    @Override
    protected int doPost(final URL url, final byte[] jsonBytes) {
        final HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST, FetchOptions.Builder.doNotValidateCertificate());

        request.setPayload(jsonBytes);
        request.setHeader(new HTTPHeader("Content-Type", "application/json"));
        request.setHeader(new HTTPHeader("Content-Encoding", "gzip"));

        try {
            final int respCode = fetchService.fetch(request).getResponseCode();

            LOG.debug("Response code: {}", respCode);

            return respCode;
        } catch (IOException e) {
            LOG.error("Error making fetch: {}", e.getMessage(), e);
            return 500;
        }
    }
}
