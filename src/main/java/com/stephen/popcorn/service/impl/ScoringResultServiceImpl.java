package com.stephen.popcorn.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.popcorn.model.entity.ScoringResult;
import com.stephen.popcorn.service.ScoringResultService;
import com.stephen.popcorn.mapper.ScoringResultMapper;
import org.springframework.stereotype.Service;

/**
* @author stephen qiu
* @description 针对表【scoring_result(评分结果)】的数据库操作Service实现
* @createDate 2024-08-08 10:16:37
*/
@Service
public class ScoringResultServiceImpl extends ServiceImpl<ScoringResultMapper, ScoringResult>
    implements ScoringResultService{

}




