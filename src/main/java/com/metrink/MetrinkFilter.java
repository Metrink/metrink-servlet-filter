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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic filter to be used with any servlet container.
 */
public class MetrinkFilter extends MetrinkFilterBase {
    private static final Logger LOG = LoggerFactory.getLogger(MetrinkFilter.class);

    private ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

    @Override
    protected void initHook(final FilterConfig config) {
        // report metrics once-a-minute (we cannot do this because GAE sucks)
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    callableWorker.call();
                } catch(Exception e) {
                    LOG.error("Error POSTing metrics: {}", e.getMessage());
                }
            }

        }, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }

}
