package com.stephen.answer.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.answer.common.ErrorCode;
import com.stephen.answer.constant.CommonConstant;
import com.stephen.answer.mapper.AppMapper;
import com.stephen.answer.model.dto.app.AppQueryRequest;
import com.stephen.answer.model.entity.App;
import com.stephen.answer.model.entity.User;
import com.stephen.answer.model.enums.AppTypeEnum;
import com.stephen.answer.model.enums.ReviewStatusEnum;
import com.stephen.answer.model.enums.ScoringStrategyEnum;
import com.stephen.answer.model.vo.AppVO;
import com.stephen.answer.model.vo.UserVO;
import com.stephen.answer.service.AppService;
import com.stephen.answer.service.UserService;
import com.stephen.answer.utils.SqlUtils;
import com.stephen.answer.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用服务实现
 *
 * @author stephen qiu
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {
	
	@Resource
	private UserService userService;
	
	/**
	 * 校验数据
	 *
	 * @param app
	 * @param add 对创建的数据进行校验
	 */
	@Override
	public void validApp(App app, boolean add) {
		ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
		// 从对象中取值
		String appName = app.getAppName();
		String appDesc = app.getAppDesc();
		Integer appType = app.getAppType();
		Integer scoringStrategy = app.getScoringStrategy();
		Integer reviewStatus = app.getReviewStatus();
		
		
		// 创建数据时，参数不能为空
		if (add) {
			// todo 补充校验规则
			ThrowUtils.throwIf(StringUtils.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名不能为空");
			ThrowUtils.throwIf(StringUtils.isBlank(appDesc), ErrorCode.PARAMS_ERROR, "应用描述不能为空");
			ThrowUtils.throwIf(AppTypeEnum.getEnumByValue(appType) == null, ErrorCode.PARAMS_ERROR, "应用类型错误");
			ThrowUtils.throwIf(ScoringStrategyEnum.getEnumByValue(scoringStrategy) == null, ErrorCode.PARAMS_ERROR, "评分类型错误");
		}
		// 修改数据时，有参数则校验
		// todo 补充校验规则
		if (StringUtils.isNotBlank(appName)) {
			ThrowUtils.throwIf(appName.length() > 80, ErrorCode.PARAMS_ERROR, "应用名过长");
		}
		if (StringUtils.isNotBlank(appDesc)) {
			ThrowUtils.throwIf(appName.length() > 512, ErrorCode.PARAMS_ERROR, "应用名过长");
		}
		if (reviewStatus != null) {
			ThrowUtils.throwIf(ReviewStatusEnum.getEnumByValue(reviewStatus) == null, ErrorCode.PARAMS_ERROR, "审核类型错误");
		}
	}
	
	/**
	 * 获取查询条件
	 *
	 * @param appQueryRequest
	 * @return
	 */
	@Override
	public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
		QueryWrapper<App> queryWrapper = new QueryWrapper<>();
		if (appQueryRequest == null) {
			return queryWrapper;
		}
		// todo 从对象中取值
		Integer notId = appQueryRequest.getNotId();
		Long id = appQueryRequest.getId();
		String appName = appQueryRequest.getAppName();
		String appDesc = appQueryRequest.getAppDesc();
		String appIcon = appQueryRequest.getAppIcon();
		Integer appType = appQueryRequest.getAppType();
		Integer scoringStrategy = appQueryRequest.getScoringStrategy();
		Integer reviewStatus = appQueryRequest.getReviewStatus();
		String reviewMessage = appQueryRequest.getReviewMessage();
		Long reviewerId = appQueryRequest.getReviewerId();
		Long userId = appQueryRequest.getUserId();
		String searchText = appQueryRequest.getSearchText();
		String sortField = appQueryRequest.getSortField();
		String sortOrder = appQueryRequest.getSortOrder();
		
		// todo 补充需要的查询条件
		// 从多字段中搜索
		if (StringUtils.isNotBlank(searchText)) {
			// 需要拼接查询条件
			queryWrapper.and(qw -> qw.like("appName", searchText).or().like("appDesc", searchText));
		}
		// 模糊查询
		queryWrapper.like(StringUtils.isNotBlank(appName), "appName", appName);
		queryWrapper.like(StringUtils.isNotBlank(appDesc), "appDesc", appDesc);
		queryWrapper.like(StringUtils.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
		// 精确查询
		queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "reviewStatus", notId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(reviewerId), "reviewerId", reviewerId);
		queryWrapper.eq(StringUtils.isNotEmpty(appIcon), "appIcon", appIcon);
		queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
		queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
		queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
		
		// 排序规则
		queryWrapper.orderBy(SqlUtils.validSortField(sortField),
				sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return queryWrapper;
	}
	
	/**
	 * 获取应用封装
	 *
	 * @param app     app
	 * @param request request
	 * @return {@link AppVO}
	 */
	@Override
	public AppVO getAppVO(App app, HttpServletRequest request) {
		// 对象转封装类
		AppVO appVO = AppVO.objToVo(app);
		
		// 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Long userId = app.getUserId();
		User user = null;
		if (userId != null && userId > 0) {
			user = userService.getById(userId);
		}
		UserVO userVO = userService.getUserVO(user);
		appVO.setUserVO(userVO);
		// endregion
		return appVO;
	}
	
	/**
	 * 分页获取应用封装
	 *
	 * @param appPage appPage
	 * @param request request
	 * @return {@link Page}<{@link AppVO}>
	 */
	@Override
	public Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request) {
		List<App> appList = appPage.getRecords();
		Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
		if (CollUtil.isEmpty(appList)) {
			return appVOPage;
		}
		// 对象列表 => 封装对象列表
		List<AppVO> appVOList = appList.stream().map(AppVO::objToVo).collect(Collectors.toList());
		
		// 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Set<Long> userIdSet = appList.stream().map(App::getUserId).collect(Collectors.toSet());
		Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		// 填充信息
		appVOList.forEach(appVO -> {
			Long userId = appVO.getUserId();
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			appVO.setUserVO(userService.getUserVO(user));
		});
		// endregion
		
		appVOPage.setRecords(appVOList);
		return appVOPage;
	}
	
}