package com.stephen.popcorn.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.stephen.popcorn.annotation.ScoringStrategyConfig;
import com.stephen.popcorn.model.dto.question.QuestionContentDTO;
import com.stephen.popcorn.model.entity.App;
import com.stephen.popcorn.model.entity.Question;
import com.stephen.popcorn.model.entity.ScoringResult;
import com.stephen.popcorn.model.entity.UserAnswer;
import com.stephen.popcorn.model.enums.AppTypeEnum;
import com.stephen.popcorn.model.vo.QuestionVO;
import com.stephen.popcorn.service.QuestionService;
import com.stephen.popcorn.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 自定义评分类应用评分策略
 *
 * @author: stephen qiu
 * @create: 2024-08-09 15:12
 **/
@ScoringStrategyConfig(appType = 0, scoringStrategy = 0)
public class CustomScoreScoringStrategy implements ScoringStrategy {
	@Resource
	private QuestionService questionService;
	
	@Resource
	private ScoringResultService scoringResultService;
	
	@Override
	public UserAnswer doScore(List<String> choices, App app) throws Exception {
		Long appId = app.getId();
		// 1. 根据 id 查询到题目和题目结果信息（按照分数降序排序）
		Question question = questionService.getOne(
				// 使用lambda查询通过question表种的 appId 查询得到题目信息
				Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
		);
		List<ScoringResult> scoringResultList = scoringResultService.list(
				Wrappers.lambdaQuery(ScoringResult.class).eq(ScoringResult::getAppId, app.getId()).orderByDesc(ScoringResult::getResultScoreRange)
		);
		// 2. 统计用户总得分
		// 初始化一个 map 用于存放选项和个数
		int totalScore = 0;
		QuestionVO questionVO = QuestionVO.objToVo(question);
		List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
		// 遍历题目列表
		for (QuestionContentDTO questionContentDTO : questionContent) {
			// 如果选项列表为空，跳过当前循环
			if (questionContentDTO.getQuestionOptionList() == null) {
				continue;
			}
			// 遍历答案列表
			for (String answer : choices) {
				// 遍历题目中的选项
				for (QuestionContentDTO.QuestionOption questionOption : questionContentDTO.getQuestionOptionList()) {
					// 如果答案和选项相匹配
					if (questionOption.getKey().equals(answer)) {
						// 填充默认值
						int score = Optional.of(questionOption.getScore()).orElse(0);
						totalScore += score;
					}
				}
			}
		}
		// 3. 遍历得分结果，找到第一个用户分数大于得分高的结果，作为最总结果
		ScoringResult maxScoringResult = scoringResultList.get(0);
		for (ScoringResult scoringResult : scoringResultList) {
			if (totalScore >= scoringResult.getResultScoreRange()) {
				maxScoringResult = scoringResult;
				break;
			}
		}
		
		// 4. 构建返回值
		UserAnswer userAnswer = new UserAnswer();
		userAnswer.setAppId(app.getId());
		userAnswer.setAppType(app.getAppType());
		userAnswer.setScoringStrategy(app.getScoringStrategy());
		userAnswer.setChoices(JSONUtil.toJsonStr(choices));
		userAnswer.setResultId(maxScoringResult.getId());
		userAnswer.setResultName(maxScoringResult.getResultName());
		userAnswer.setResultDesc(maxScoringResult.getResultDesc());
		userAnswer.setResultScore(totalScore);
		userAnswer.setResultPicture(maxScoringResult.getResultPicture());
		
		return userAnswer;
	}
}
