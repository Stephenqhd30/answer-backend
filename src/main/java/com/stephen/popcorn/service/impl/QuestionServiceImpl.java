package com.stephen.popcorn.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.popcorn.model.entity.Question;
import com.stephen.popcorn.service.QuestionService;
import com.stephen.popcorn.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

/**
* @author stephen qiu
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2024-08-08 10:16:31
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

}




