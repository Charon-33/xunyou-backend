package com.wsj.xunyou.utils;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import java.util.List;

public class AlgorithmMetric {
    public static double compareUseM(List<String> list1, List<String> list2) {
        // 创建一个余弦相似度对象
        StringMetric metric = StringMetrics.cosineSimilarity();
        // 定义一个变量，用于存放总的相似度分数
        double totalSimilarity = 0.0;
        // 遍历两个list中的每个字符串
        for (String s1 : list1) {
            for (String s2 : list2) {
                // 计算两个字符串的相似度
                float similarity = metric.compare(s1, s2);
                // 累加到总的相似度分数
                totalSimilarity += similarity;
            }
        }
        // 计算两个list的平均相似度分数
        return totalSimilarity / (list1.size() * list2.size());
    }

    public static double compareUseM(String s1, String s2) {
        StringMetric metric = StringMetrics.cosineSimilarity();
        return metric.compare(s1, s2);
    }
}
