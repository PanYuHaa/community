package com.nowcoder.community.util;


import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            // 读每一行查找keyword
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            // 获取当前c这样的子节点
            TrieNode subNode = tempNode.getSubNode(c);

            // 如果c这样的子节点不存在
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 指向子节点,进入下一轮循环
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     *
     * 只需要多加一个判断就可以解决了，不需要修改循环的指针； 即当postion到达了末尾，按照课程讲解是退出了循环，并且直接把剩余部分加入到结果中；
     * 但是我们没法确定这剩余部分的中间是否存在敏感词，那么我们应该是继续对这部分进行判断；
     * 1. 此时我们知道begin指向的字符是合法的，所以我们把它加入到结果中，sb.append(test.charAt(begin));
     * 2. 那么剩下的部分就是 begin+1 - postion 这一部分了，我们要继续判断的话，则是从begin+1 位置继续循环 ，所以设置 position=++begin;
     * 3. 前缀树也是重新开始判断   =>  tempNode=rootNodel
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 不为空的情况，开始对文本进行过滤处理；用三个指针，分别指向的是前缀树、文本；
        // 在文本中，相当于用双指针，找到前缀树中出现的路径，然后对双指针指向的区域进行文本替换
        TrieNode tempNode= rootNode; // 指针 1
        int begin=0,position=0;  // 指针2 3
        // 结果
        StringBuilder sb=new StringBuilder();

        while(position < text.length()){

            Character c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                if (tempNode == rootNode) {
                    begin++;
                    sb.append(c);
                }
                position++;
                continue;
            }
            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            }
            // 发现敏感词
            else if (tempNode.isKeywordEnd()) {
                sb.append(REPLACEMENT);
                begin = ++position;
            }
            // 检查下一个字符
            else {
                position++;
            }

            // 提前判断postion是不是到达结尾，要跳出while,如果是，则说明begin-position这个区间不是敏感词，但是里面不一定没有
            if (position==text.length() && begin!=position){
                // 说明还剩下一段需要判断，则把position==++begin
                // 并且当前的区间的开头字符是合法的
                sb.append(text.charAt(begin));
                position=++begin;
                tempNode=rootNode;  // 前缀表从头开始了
            }
        }
        return sb.toString();
    }


    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点（key是下级字符，value是下级节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
