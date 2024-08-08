package com.stephen.popcorn.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.popcorn.common.ErrorCode;
import com.stephen.popcorn.constant.CommonConstant;
import com.stephen.popcorn.mapper.QuestionMapper;
import com.stephen.popcorn.model.dto.question.QuestionQueryRequest;
import com.stephen.popcorn.model.entity.App;
import com.stephen.popcorn.model.entity.Question;
import com.stephen.popcorn.model.entity.User;
import com.stephen.popcorn.model.vo.QuestionVO;
import com.stephen.popcorn.model.vo.UserVO;
import com.stephen.popcorn.service.AppService;
import com.stephen.popcorn.service.QuestionService;
import com.stephen.popcorn.service.UserService;
import com.stephen.popcorn.utils.SqlUtils;
import com.stephen.popcorn.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 题目服务实现
 *
 * @author stephen qiu
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
	
	@Resource
	private UserService userService;
	
	@Resource
	private AppService appService;
	
	/**
	 * 校验数据
	 *
	 * @param question
	 * @param add      对创建的数据进行校验
	 */
	@Override
	public void validQuestion(Question question, boolean add) {
		ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
		// todo 从对象中取值
		String questionContent = question.getQuestionContent();
		Long appId = question.getAppId();
		// 创建数据时，参数不能为空
		if (add) {
			// todo 补充校验规则
			ThrowUtils.throwIf(StringUtils.isBlank(questionContent), ErrorCode.PARAMS_ERROR, "题目不能为空");
			ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId非法");
		}
		// 修改数据时，有参数则校验
		// todo 补充校验规则
		if (StringUtils.isNotBlank(questionContent)) {
			ThrowUtils.throwIf(questionContent.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
		}
		if (appId != null) {
			App app = appService.getById(appId);
			ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
		}
	}
	
	/**
	 * 获取查询条件
	 *
	 * @param questionQueryRequest
	 * @return
	 */
	@Override
	public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
		QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
		if (questionQueryRequest == null) {
			return queryWrapper;
		}
		// todo 从对象中取值
		Long id = questionQueryRequest.getId();
		String questionContent = questionQueryRequest.getQuestionContent();
		Long appId = questionQueryRequest.getAppId();
		Long userId = questionQueryRequest.getUserId();
		String sortField = questionQueryRequest.getSortField();
		String sortOrder = questionQueryRequest.getSortOrder();
		
		
		// todo 补充需要的查询条件
		// 模糊查询
		queryWrapper.like(StringUtils.isNotBlank(questionContent), "questionContent", questionContent);
		
		// 精确查询
		queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
		// 排序规则
		queryWrapper.orderBy(SqlUtils.validSortField(sortField),
				sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return queryWrapper;
	}
	
	/**
	 * 获取题目封装
	 *
	 * @param question
	 * @param request
	 * @return
	 */
	@Override
	public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
		// 对象转封装类
		QuestionVO questionVO = QuestionVO.objToVo(question);
		
		// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Long userId = question.getUserId();
		User user = null;
		if (userId != null && userId > 0) {
			user = userService.getById(userId);
		}
		UserVO userVO = userService.getUserVO(user);
		questionVO.setUserVO(userVO);
		// endregion
		
		return questionVO;
	}
	
	/**
	 * 分页获取题目封装
	 *
	 * @param questionPage
	 * @param request
	 * @return
	 */
	@Override
	public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
		List<Question> questionList = questionPage.getRecords();
		Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
		if (CollUtil.isEmpty(questionList)) {
			return questionVOPage;
		}
		// 对象列表 => 封装对象列表
		List<QuestionVO> questionVOList = questionList.stream().map(QuestionVO::objToVo).collect(Collectors.toList());
		
		// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
		Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		// 填充信息
		questionVOList.forEach(questionVO -> {
			Long userId = questionVO.getUserId();
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			questionVO.setUserVO(userService.getUserVO(user));
		});
		// endregion
		
		questionVOPage.setRecords(questionVOList);
		return questionVOPage;
	}
	
}
