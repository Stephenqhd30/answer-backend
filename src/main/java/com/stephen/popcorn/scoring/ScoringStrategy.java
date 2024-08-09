package com.stephen.popcorn.scoring;

import com.stephen.popcorn.model.entity.App;
import com.stephen.popcorn.model.entity.UserAnswer;

import java.util.List;

/**
 * 策略模式
 * @author: stephen qiu
 * @create: 2024-08-09 15:09
 **/
public interface ScoringStrategy {
	/**
	 * 评分策略
	 * @param choices
	 * @param app
	 * @return
	 * @throws Exception
	 */
	UserAnswer doScore(List<String> choices, App app) throws Exception;
}
