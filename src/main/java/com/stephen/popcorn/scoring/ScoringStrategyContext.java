package com.stephen.popcorn.scoring;

import com.stephen.popcorn.common.ErrorCode;
import com.stephen.popcorn.exception.BusinessException;
import com.stephen.popcorn.model.entity.App;
import com.stephen.popcorn.model.entity.UserAnswer;
import com.stephen.popcorn.model.enums.AppTypeEnum;
import com.stephen.popcorn.model.enums.ScoringStrategyEnum;
import com.stephen.popcorn.utils.ThrowUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: stephen qiu
 * @create: 2024-08-09 16:01
 **/
@Service
@Deprecated
public class ScoringStrategyContext {
	@Resource
	private CustomScoreScoringStrategy customScoreScoringStrategy;
	
	@Resource
	private CustomTestScoringStrategy customTestScoringStrategy;
	
	/**
	 *
	 * @param choiceList
	 * @param app
	 * @return
	 * @throws Exception
	 */
	public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
		AppTypeEnum appTypeEnum = AppTypeEnum.getEnumByValue(app.getAppType());
		ScoringStrategyEnum scoringStrategyEnum = ScoringStrategyEnum.getEnumByValue(app.getScoringStrategy());
		ThrowUtils.throwIf(appTypeEnum == null || scoringStrategyEnum == null, ErrorCode.PARAMS_ERROR);
		// 根据不同的应用类型和评分策略，选择对应的策略执行
		switch (appTypeEnum) {
			case TEST:
				switch (scoringStrategyEnum) {
					case CUSTOM:
						return customTestScoringStrategy.doScore(choiceList, app);
					case AI:
						break;
				}
				break;
			case SCORE:
				switch (scoringStrategyEnum) {
					case CUSTOM:
						return customScoreScoringStrategy.doScore(choiceList, app);
					case AI:
						break;
				}
				break;
		}
		throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
	}
}
