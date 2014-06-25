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

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A version of the MetrinkFilter that works with Google App Engine.
 */
public class MetrinkGAEFilter extends MetrinkFilterBase {
    private static final Logger LOG = LoggerFactory.getLogger(MetrinkGAEFilter.class);

    @Override
    protected boolean beforeDoFilter(final ServletRequest request, final ServletResponse response)
            throws IOException, ServletException {

        if(request instanceof HttpServletRequest) {
            final String page = ((HttpServletRequest) request).getRequestURI();

            // this is our crappy hook to send metrics
            if(page.equals("/send-metrics")) {
                try {
                    final HttpServletResponse httpResponse = (HttpServletResponse) response;
                    final Integer respCode = callableWorker.call();


                    httpResponse.setStatus(respCode);
                    httpResponse.setContentLength(0);

                } catch (Exception e) {
                    LOG.error("Exception when sending metrics: {}", e.getMessage());
                }

                // we don't want to continue
                return true;
            }
        }

        return false;
    }
}
