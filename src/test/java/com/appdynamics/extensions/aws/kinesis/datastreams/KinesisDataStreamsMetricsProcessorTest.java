/*
 * Copyright (c) 2020 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.aws.kinesis.datastreams;

import com.appdynamics.extensions.aws.config.Dimension;
import com.appdynamics.extensions.aws.config.IncludeMetric;
import com.appdynamics.extensions.aws.dto.AWSMetric;
import com.appdynamics.extensions.aws.metric.AccountMetricStatistics;
import com.appdynamics.extensions.aws.metric.MetricStatistic;
import com.appdynamics.extensions.aws.metric.NamespaceMetricStatistics;
import com.appdynamics.extensions.aws.metric.RegionMetricStatistics;
import com.appdynamics.extensions.metrics.Metric;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by pradeep.nair on 8/21/18.
 */
public class KinesisDataStreamsMetricsProcessorTest {
    private final NamespaceMetricStatistics namespaceMetricStats = new NamespaceMetricStatistics();
    private KinesisDataStreamsMetricsProcessor kinesisDataStreamsMetricsProcessor;

    @Before
    public void setup() {
        // populate dimension with display name
        List<Dimension> dimensions = new ArrayList<>();
        Dimension dimension = new Dimension();
        dimension.setName("StreamName");
        dimension.setDisplayName("Stream Name");
        dimensions.add(dimension);

        kinesisDataStreamsMetricsProcessor = new KinesisDataStreamsMetricsProcessor(new ArrayList<>(), dimensions);
        createNamespaceMetricsStatsForTest();
    }

    private void createNamespaceMetricsStatsForTest() {
        AccountMetricStatistics accountStats = new AccountMetricStatistics();
        accountStats.setAccountName("Appd");
        RegionMetricStatistics regionStats = new RegionMetricStatistics();
        regionStats.setRegion("us-west-2");
        IncludeMetric includeMetric = new IncludeMetric();
        includeMetric.setName("testMetric");

        List<com.amazonaws.services.cloudwatch.model.Dimension> dimensions = new ArrayList<>();
        com.amazonaws.services.cloudwatch.model.Dimension dimension = new com.amazonaws.services.cloudwatch.model.Dimension();
        dimension.withName("StreamName").withValue("Sample");
        dimensions.add(dimension);

        com.amazonaws.services.cloudwatch.model.Metric metric = new com.amazonaws.services.cloudwatch.model.Metric();
        metric.setDimensions(dimensions);

        AWSMetric awsMetric = new AWSMetric();
        awsMetric.setIncludeMetric(includeMetric);
        awsMetric.setMetric(metric);

        MetricStatistic metricStatistic = new MetricStatistic();
        metricStatistic.setMetric(awsMetric);
        metricStatistic.setValue(new Random().nextDouble());
        metricStatistic.setUnit("testUnit");
        metricStatistic.setMetricPrefix("Custom Metrics|AWS Kinesis Data Streams|");

        regionStats.addMetricStatistic(metricStatistic);
        accountStats.add(regionStats);
        namespaceMetricStats.add(accountStats);
    }

    @Test
    public void createMetricStatsMapAndCheckMetricPathHierarchyWithDimensionTest() {
        List<Metric> stats = kinesisDataStreamsMetricsProcessor
                .createMetricStatsMapForUpload(namespaceMetricStats);
        Metric metric = stats.get(0);
        String expectedMetricName = "Custom Metrics|AWS Kinesis Data Streams|Appd|us-west-2|Stream Name|Sample|testMetric";
        assertNotNull(metric);
        assertEquals(expectedMetricName, metric.getMetricPath());
    }
}
