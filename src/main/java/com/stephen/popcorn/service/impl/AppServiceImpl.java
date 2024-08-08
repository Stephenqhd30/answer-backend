package com.stephen.popcorn.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.popcorn.model.entity.App;
import com.stephen.popcorn.service.AppService;
import com.stephen.popcorn.mapper.AppMapper;
import org.springframework.stereotype.Service;

/**
* @author stephen qiu
* @description 针对表【app(应用)】的数据库操作Service实现
* @createDate 2024-08-08 10:16:25
*/
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
    implements AppService{

}




