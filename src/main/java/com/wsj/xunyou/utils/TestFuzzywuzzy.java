package com.wsj.xunyou.utils;

import me.xdrop.diffutils.DiffUtils;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.Arrays;

public class TestFuzzywuzzy {
    public static void main(String[] args) {
        /*FuzzySearch.ratio(String s1, String s2)
        全匹配，对顺序敏感

        FuzzySearch.partialRatio(String s1, String s2)
        搜索匹配(部分匹配)，对顺序敏感

        FuzzySearch.tokenSortRatio(String s1, String s2)
        首先做排序，然后全匹配，对顺序不敏感(也就是更换单词位置之后，相似度依然会很高)

        FuzzySearch.tokenSortPartialRatio(String s1, String s2)
        首先做排序，然后搜索匹配(部分匹配)，对顺序不敏感

        FuzzySearch.tokenSetRatio(String s1, String s2)
        首先取集合(去掉重复词)，然后全匹配，对顺序不敏感，第二个字符串包含第一个字符串就100

        FuzzySearch.tokenSetPartialRatio(String s1, String s2)
        首先取集合，然后搜索匹配(部分匹配)，对顺序不敏感

        FuzzySearch.weightedRatio(String s1, String s2)
        对顺序敏感，算法不同
        */
        System.out.println("1 " + FuzzySearch.ratio("admin", "admin"));
        System.out.println("2 " + FuzzySearch.partialRatio("ADMIN", "admin"));
        System.out.println("3 " + FuzzySearch.tokenSetPartialRatio("test", "test1"));
        System.out.println("4 " + FuzzySearch.weightedRatio("你是", "你是我"));
        System.out.println("5 " + FuzzySearch.tokenSortRatio("你是", "你是W"));
        System.out.println("6 " + FuzzySearch.tokenSetRatio("你是", "你是o"));
        System.out.println(DiffUtils.getRatio("你是", "你是我"));
        System.out.println(DiffUtils.levEditDistance("你是", "你是我", 1));
        System.out.println(Arrays.toString(DiffUtils.getMatchingBlocks("你是", "你是我")));
        System.out.println(Arrays.toString(DiffUtils.getEditOps("你是", "你是我")));

    }
}
