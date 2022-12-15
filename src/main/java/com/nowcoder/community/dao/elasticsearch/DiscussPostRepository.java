package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
// ElasticsearchRepository自带增删改查
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}
