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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class MetrinkFilterBase implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(MetrinkFilterBase.class);

    private String applicationName;
    private String apiUrl;

    private MetricFactory metricFactory;
    private final Map<String, Long> serverTimes;
    private final Map<String, Long> pageCounts;
    protected CallableWorker callableWorker;

    public MetrinkFilterBase() {
        serverTimes = Collections.synchronizedMap(new HashMap<String, Long>());
        pageCounts = Collections.synchronizedMap(new HashMap<String, Long>());
    }

    @Override
    public void init(final FilterConfig config) throws ServletException {
        applicationName = config.getInitParameter("app-name"); // allowed to be null
        apiUrl = config.getInitParameter("api-url");

        if(apiUrl == null) {
            throw new ServletException("Must set api-url for MetrinkFilter");
        }

        String device = config.getInitParameter("device");

        if(device == null) {
            try {
                // try to get the hostname
                device = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                try {
                    // try to get the IPv4 address
                    device = Inet4Address.getLocalHost().toString();
                } catch (UnknownHostException e1) {
                    // if we cannot get any of those, just set to unknown
                    LOG.error("Error getting hostname: {}, try setting it manually" + e.getMessage());
                    device = "unknown";
                }
            }
        }

        try {
            metricFactory = new MetricFactory(device, applicationName, apiUrl);
        } catch (MalformedURLException e) {
            throw new ServletException(e);
        }

        callableWorker = createWorker(metricFactory, serverTimes, pageCounts);

        // call our hook for any other setup
        initHook(config);

        LOG.info("Initialized MetrinkFilter");
    }

    protected abstract CallableWorker createWorker(MetricFactory metricFactory, Map<String, Long> serverTime, Map<String, Long> pageCount);

    protected void initHook(final FilterConfig config) {
    }

    @Override
    public void destroy() {
        LOG.info("Destroyed MetrinkFilter");
    }

    /**
     * Called before the doFilter call on the chain.
     * @param request the request
     * @param response the response
     * @return true if the call should return, false to continue.
     * @throws IOException
     * @throws ServletException
     */
    protected boolean beforeDoFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
        return false;
    }

    /**
     * Called after the doFilter call on the chain.
     * @param request the request.
     * @param response the response.
     * @return true if the call should return, false to continue and record the request.
     * @throws IOException
     * @throws ServletException
     */
    protected boolean afterDoFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {

        if(beforeDoFilter(request, response)) {
            return;
        }

        final long start = System.currentTimeMillis();

        // call everyone else
        chain.doFilter(request, response);

        final long end = System.currentTimeMillis();

        if(afterDoFilter(request, response)) {
            return;
        }

        if(request instanceof HttpServletRequest) {
            final String page = ((HttpServletRequest) request).getRequestURI();
            final Long time = end-start;

            // filter out css & js
            if(page.endsWith(".css") || page.endsWith(".js")) {
                return;
            }

            synchronized(serverTimes) {
                serverTimes.put(page, time);
            }

            synchronized(pageCounts) {
                Long count = pageCounts.get(page);

                if(count == null) {
                    count = 0l;
                }

                count++;

                pageCounts.put(page, count);

                LOG.debug("{} ({}): {}", time, count, ((HttpServletRequest) request).getRequestURI());
            }
        }
    }
}
