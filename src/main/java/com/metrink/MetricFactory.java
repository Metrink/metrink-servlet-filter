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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class MetricFactory {
    //private static final Logger LOG = LoggerFactory.getLogger(MetricFactory.class);

    private final String device;
    private final String applicationName;
    private final URL apiUrl;

    public MetricFactory(final String device, final String applicationName, final String apiUrl) throws MalformedURLException {
        this.device = device;
        this.applicationName = applicationName;
        this.apiUrl = new URL(apiUrl);
    }

    public Metric generateMetric(final String groupName,
                                 final String name,
                                 final String value,
                                 final String units) {
        return generateMetric(groupName, name, Double.parseDouble(value), units);
    }

    public Metric generateMetric(final String groupName,
                                 final String name,
                                 final double value,
                                 final String units) {

        final String metricName = applicationName == null ? name : applicationName + " - " + name;

        return new Metric(device, groupName, metricName, System.currentTimeMillis(), value, units);
    }

    public URL getUrl() {
        return apiUrl;
    }

    public String toJsonString(final List<Metric> metrics) {
        final JSONObject ret = new JSONObject();
        final JSONArray metricArray = new JSONArray();

        for(Metric metric:metrics) {
            final JSONObject obj = new JSONObject();

            try {
                obj.put("g", metric.getGroup());
                obj.put("n", metric.getName());
                obj.put("v", metric.getValue());
                obj.put("u", metric.getUnits());
                obj.put("t", metric.getTimestamp());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            metricArray.put(obj);
        }

        try {
            ret.put("d", device);
            ret.put("m", metricArray);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        return ret.toString();
    }

    public static void printMetric(final Metric metric) {
        System.out.println(metric.toString());
    }

    public static void printMetrics(final List<Metric> metrics) {
        for(Metric metric:metrics) {
            printMetric(metric);
        }
    }
}
