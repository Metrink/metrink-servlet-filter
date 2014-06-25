# metrink-servlet-filter

This [Servlet Filter](http://docs.oracle.com/javaee/6/api/javax/servlet/Filter.html) captures timing information
about pages loaded for the installed application. The metrics are sent to [Metrink](https://www.metrink.com) (you must have an account, [signup here](https://www.metrink.com/signup)) and recorded.

## Configuring Your Servlet Container

You must "install" the Metrink Servlet Filter to be able to use it. This is usually done by adding the following lines to your web.xml file:

```
  <filter>
    <filter-name>MetrinkFilter</filter-name>
    <filter-class>com.metrink.MetrinkFilter</filter-class>
    <init-param>
      <param-name>app-name</param-name>
      <param-value>metrink</param-value>
    </init-param>
    <init-param>
      <param-name>api-url</param-name>
      <param-value>https://user:pass@metrink-gae.appspot.com/api</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>MetrinkFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
```

You **must** replace ``api-url`` in the above configuration with the value you find here: https://www.metrink.com/setup

### Use with Google App Engine

Because you cannot create background threads in Google App Engine, a special filter has been constructed: ``MetrinkGAEFilter``. This filter has a special hook that sends the metrics when a request is made to ``/send-metrics``. To enable this to happen on a regular basis, simply install the following in your ``cron.xml`` file:

```
  <cron>
    <url>/send-metrics</url>
    <description>Send metrics from metrink-servlet-filter</description>
    <schedule>every 1 minutes</schedule>
    <target>default</target>
  </cron>
```

