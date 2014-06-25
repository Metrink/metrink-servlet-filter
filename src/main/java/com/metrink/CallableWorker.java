/*
 * This file is part of Metrink-Servlet-Filter.
 *
 *  Metrink-Servlet-Filter is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Metrink-Servlet-Filter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE
 *  along with Metrink-Servlet-Filter.  If not, see <http://www.gnu.org/licenses/agpl.html>.
 */
package com.metrink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CallableWorker implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(CallableWorker.class);

    private static final String SERVER_TIME = "server time";
    //private static final String PAGE_LOAD = "page load";
    private static final String PAGE_COUNT = "page count";

    private final MetricFactory metricFactory;
    private final Map<String, Long> serverTime;
    private final Map<String, Long> pageCount;

    CallableWorker(MetricFactory metricFactory, Map<String, Long> serverTime, Map<String, Long> pageCount) {
        this.metricFactory = metricFactory;
        this.serverTime = serverTime;
        this.pageCount = pageCount;
    }

    @Override
    public Integer call() throws Exception {
        final List<Metric> metrics = new ArrayList<Metric>();

        //
        // Process server times
        //
        final Set<Entry<String, Long>> serverTimes = serverTime.entrySet();

        synchronized(serverTime) {
            final Iterator<Entry<String, Long>> it = serverTimes.iterator();

            while(it.hasNext()) {
                final Entry<String, Long> entry = it.next();
                metrics.add(metricFactory.generateMetric(SERVER_TIME, entry.getKey(), entry.getValue(), "ms"));
            }

            serverTime.clear();
        }

        //
        // process page counts
        //
        final Set<Entry<String, Long>> pageCounts = pageCount.entrySet();

        synchronized(pageCount) {
            final Iterator<Entry<String, Long>> it = pageCounts.iterator();

            while(it.hasNext()) {
                final Entry<String, Long> entry = it.next();
                metrics.add(metricFactory.generateMetric(PAGE_COUNT, entry.getKey(), entry.getValue(), "count"));
            }

            pageCount.clear();
        }

        if(!metrics.isEmpty()) {
            return sendMetrics(metrics);
        }

        return Integer.valueOf(200); // just say it's all good
    }

    private Integer sendMetrics(final List<Metric> metrics) {
        // convert to JSON and compress
        final String json = metricFactory.toJsonString(metrics);

        LOG.debug(json);

        // compress the JSON
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            final GZIPOutputStream compressor = new GZIPOutputStream(bos);

            compressor.write(json.getBytes());
            compressor.close();
        } catch (final IOException e) {
            LOG.error("IOException when processing metrics: {}", e.getMessage(), e);
        }

        final byte[] jsonBytes = bos.toByteArray();

        try {
            //final URL url = new URL("http://api.metrink.com");
            final URL url = metricFactory.getUrl();
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Encoding", "gzip");

            // Get output stream before input stream
            final OutputStream out = connection.getOutputStream();

            out.write(jsonBytes);
            out.close();

            final Integer respCode = Integer.valueOf(connection.getResponseCode());

            LOG.debug("Got response code: {}", connection.getResponseCode());

            return respCode;
        } catch (final MalformedURLException e) {
            LOG.error("Malformed URL: {}", e.getMessage(), e);
        } catch (final IOException e) {
            LOG.error("IOException while sending metrics: {}", e.getMessage(), e);
        }

        return Integer.valueOf(500); // just call this a 500
    }

}
