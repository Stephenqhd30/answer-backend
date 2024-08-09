package com.stephen.popcorn.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.stephen.popcorn.annotation.ScoringStrategyConfig;
import com.stephen.popcorn.model.dto.question.QuestionContentDTO;
import com.stephen.popcorn.model.entity.App;
import com.stephen.popcorn.model.entity.Question;
import com.stephen.popcorn.model.entity.ScoringResult;
import com.stephen.popcorn.model.entity.UserAnswer;
import com.stephen.popcorn.model.vo.QuestionVO;
import com.stephen.popcorn.service.QuestionService;
import com.stephen.popcorn.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 自定义测评类应用评分策略
 *
 * @author: stephen qiu
 * @create: 2024-08-09 15:12
 **/
@ScoringStrategyConfig(appType = 1, scoringStrategy = 0)
public class CustomTestScoringStrategy implements ScoringStrategy {
	
	@Resource
	private QuestionService questionService;
	
	@Resource
	private ScoringResultService scoringResultService;
	
	/**
	 * @param choices 选项
	 * @param app     应用实体类
	 * @return
	 * @throws Exception 抛异常
	 */
	@Override
	public UserAnswer doScore(List<String> choices, App app) throws Exception {
		// 1. 根据 id 查询到题目和题目结果信息
		Long appId = app.getId();
		Question question = questionService.getOne(
				// 使用lambda查询通过question表种的 appId 查询得到题目信息
				Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
		);
		List<ScoringResult> scoringResultList = scoringResultService.list(
				Wrappers.lambdaQuery(ScoringResult.class).eq(ScoringResult::getAppId, app.getId())
		);
		// 2. 统计用户每个选择对应的属性个数， 如果 "I":5, "S":10
		// 初始化一个 map 用于存放选项和个数
		HashMap<String, Integer> optionCount = new HashMap<>();
		
		QuestionVO questionVO = QuestionVO.objToVo(question);
		List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
		// 遍历题目列表
		for (QuestionContentDTO questionContentDTO : questionContent) {
			// 遍历答案列表
			for (String answer : choices) {
				// 遍历题目中的选项
				for (QuestionContentDTO.QuestionOption questionOption : questionContentDTO.getQuestionOptionList()) {
					// 如果答案和选项相匹配
					if (questionOption.getKey().equals(answer)) {
						// 获取选项的 result
						String result = questionOption.getResult();
						
						// 如果 result 属性不在 optionCount 中 初始化为0
						if (!optionCount.containsKey(result)) {
							optionCount.put(result, 0);
						}
						
						// 在 optionCount 中增加计数
						optionCount.put(result, optionCount.get(result) + 1);
					}
				}
			}
		}
		// 3. 遍历每种评分结果， 计算那个结果的得分更高
		// 初始化最高分和最高分对应的评分结果
		int maxScore = 0;
		ScoringResult maxScoringResult = scoringResultList.get(0);
		for (ScoringResult scoringResult : scoringResultList) {
			List<String> resultProps = JSONUtil.toList(scoringResult.getResultProp(), String.class);
			// 计算当前评分结果的分数
			int score = resultProps.stream().mapToInt(props ->
					optionCount.getOrDefault(props, 0)
			).sum();
			
			// 如果分数高于当前最高分数，更新最高分数和对应的评分策略
			if (score > maxScore) {
				maxScore = score;
				maxScoringResult = scoringResult;
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
		userAnswer.setResultPicture(maxScoringResult.getResultPicture());
		
		return userAnswer;
	}
}
