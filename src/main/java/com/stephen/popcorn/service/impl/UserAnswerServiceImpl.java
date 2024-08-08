package com.stephen.popcorn.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.popcorn.model.entity.UserAnswer;
import com.stephen.popcorn.service.UserAnswerService;
import com.stephen.popcorn.mapper.UserAnswerMapper;
import org.springframework.stereotype.Service;

/**
* @author stephen qiu
* @description 针对表【user_answer(用户答题记录)】的数据库操作Service实现
* @createDate 2024-08-08 10:16:45
*/
@Service
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer>
    implements UserAnswerService{

}




