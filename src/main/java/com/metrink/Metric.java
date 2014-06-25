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

import java.io.Serializable;

/**
 * Immutable object that represents a metric.
 */
class Metric implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String device;
    private final String group;
    private final String name;
    private final long timestamp;
    private final double value;
    private final String units;

    public Metric(final String device,
                  final String group,
                  final String name,
                  final long timestamp,
                  final double value,
                  final String units) {
        this.device = device;
        this.group = group;
        this.name = name;
        this.timestamp = timestamp;
        this.value = value;
        this.units = units;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(timestamp + "\t");

        sb.append(getDevice());
        sb.append(":");
        sb.append(getGroup());
        sb.append(":");
        sb.append(getName());
        sb.append(" ");
        sb.append(getValue());

        if(getUnits() != null) {
            sb.append(getUnits());
        }

        return sb.toString();
    }

    /*
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final Metric rhs = (Metric) obj;

        return new EqualsBuilder()
                      .append(id, rhs.id)
                      .append(value, rhs.getMetricValue())
                      .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(id)
          .append(value)
          .toHashCode();
    }
*/

    public String getDevice() {
        return device;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }

    public String getUnits() {
        return units;
    }

}
