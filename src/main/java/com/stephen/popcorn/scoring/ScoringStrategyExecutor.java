package com.stephen.popcorn.scoring;

import com.stephen.popcorn.annotation.ScoringStrategyConfig;
import com.stephen.popcorn.common.ErrorCode;
import com.stephen.popcorn.exception.BusinessException;
import com.stephen.popcorn.model.entity.App;
import com.stephen.popcorn.model.entity.UserAnswer;
import com.stephen.popcorn.utils.ThrowUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评分策略执行器
 *
 * @author: stephen qiu
 * @create: 2024-08-20 14:10
 **/
@Service
public class ScoringStrategyExecutor {
	
	// 策略列表
	@Resource
	private List<ScoringStrategy> scoringStrategyList;
	
	public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
		Integer appType = app.getAppType();
		Integer scoringStrategy = app.getScoringStrategy();
		ThrowUtils.throwIf(appType == null || scoringStrategy == null, ErrorCode.SYSTEM_ERROR, "应用配置有误");
		// 根据注解获取评分策略
		for (ScoringStrategy strategy : scoringStrategyList) {
			if (strategy.getClass().isAnnotationPresent(ScoringStrategyConfig.class)) {
				ScoringStrategyConfig scoringStrategyConfig = strategy.getClass().getAnnotation(ScoringStrategyConfig.class);
				if (scoringStrategyConfig.appType() == appType && scoringStrategyConfig.scoringStrategy() == scoringStrategy) {
					return strategy.doScore(choiceList, app);
				}
			}
		}
		throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
	}
	
}
