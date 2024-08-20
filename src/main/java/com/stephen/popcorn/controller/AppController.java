package com.stephen.popcorn.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.popcorn.annotation.AuthCheck;
import com.stephen.popcorn.common.BaseResponse;
import com.stephen.popcorn.common.DeleteRequest;
import com.stephen.popcorn.common.ErrorCode;
import com.stephen.popcorn.common.ReviewRequest;
import com.stephen.popcorn.constant.UserConstant;
import com.stephen.popcorn.exception.BusinessException;
import com.stephen.popcorn.model.dto.app.AppAddRequest;
import com.stephen.popcorn.model.dto.app.AppEditRequest;
import com.stephen.popcorn.model.dto.app.AppQueryRequest;
import com.stephen.popcorn.model.dto.app.AppUpdateRequest;
import com.stephen.popcorn.model.entity.App;
import com.stephen.popcorn.model.entity.User;
import com.stephen.popcorn.model.enums.ReviewStatusEnum;
import com.stephen.popcorn.model.vo.AppVO;
import com.stephen.popcorn.service.AppService;
import com.stephen.popcorn.service.UserService;
import com.stephen.popcorn.utils.ResultUtils;
import com.stephen.popcorn.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 应用接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/app")
@Slf4j
public class AppController {
	
	@Resource
	private AppService appService;
	
	@Resource
	private UserService userService;
	
	// region 增删改查
	
	/**
	 * 创建应用
	 *
	 * @param appAddRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
		// todo  在此处将实体类和 DTO 进行转换
		App app = new App();
		BeanUtils.copyProperties(appAddRequest, app);
		
		// 数据校验
		appService.validApp(app, true);
		// todo 填充默认值
		User loginUser = userService.getLoginUser(request);
		app.setUserId(loginUser.getId());
		app.setReviewStatus(ReviewStatusEnum.REVIEWING.getValue());
		// 写入数据库
		boolean result = appService.save(app);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		// 返回新写入的数据 id
		long newAppId = app.getId();
		return ResultUtils.success(newAppId);
	}
	
	/**
	 * 删除应用
	 *
	 * @param deleteRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		App oldApp = appService.getById(id);
		ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (!oldApp.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = appService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 更新应用（仅管理员可用）
	 *
	 * @param appUpdateRequest
	 * @return
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest) {
		if (appUpdateRequest == null || appUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		App app = new App();
		BeanUtils.copyProperties(appUpdateRequest, app);
		// 数据校验
		appService.validApp(app, false);
		// 判断是否存在
		long id = appUpdateRequest.getId();
		App oldApp = appService.getById(id);
		ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = appService.updateById(app);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 根据 id 获取应用（封装类）
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/get/vo")
	public BaseResponse<AppVO> getAppVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		App app = appService.getById(id);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(appService.getAppVO(app, request));
	}
	
	/**
	 * 分页获取应用列表（仅管理员可用）
	 *
	 * @param appQueryRequest
	 * @return
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<App>> listAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
		long current = appQueryRequest.getCurrent();
		long size = appQueryRequest.getPageSize();
		// 查询数据库
		Page<App> appPage = appService.page(new Page<>(current, size),
				appService.getQueryWrapper(appQueryRequest));
		return ResultUtils.success(appPage);
	}
	
	/**
	 * 分页获取应用列表（封装类）
	 *
	 * @param appQueryRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<AppVO>> listAppVOByPage(@RequestBody AppQueryRequest appQueryRequest,
	                                                 HttpServletRequest request) {
		long current = appQueryRequest.getCurrent();
		long size = appQueryRequest.getPageSize();
		appQueryRequest.setReviewStatus(ReviewStatusEnum.PASS.getValue());
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<App> appPage = appService.page(new Page<>(current, size),
				appService.getQueryWrapper(appQueryRequest));
		// 获取封装类
		return ResultUtils.success(appService.getAppVOPage(appPage, request));
	}
	
	/**
	 * 分页获取当前登录用户创建的应用列表
	 *
	 * @param appQueryRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/my/list/page/vo")
	public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest,
	                                                   HttpServletRequest request) {
		ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
		// todo 补充查询条件，只查询当前登录用户的数据
		User loginUser = userService.getLoginUser(request);
		appQueryRequest.setUserId(loginUser.getId());
		long current = appQueryRequest.getCurrent();
		long size = appQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<App> appPage = appService.page(new Page<>(current, size),
				appService.getQueryWrapper(appQueryRequest));
		// 获取封装类
		return ResultUtils.success(appService.getAppVOPage(appPage, request));
	}
	
	/**
	 * 编辑应用（给用户使用）
	 *
	 * @param appEditRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editApp(@RequestBody AppEditRequest appEditRequest, HttpServletRequest request) {
		if (appEditRequest == null || appEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		App app = new App();
		BeanUtils.copyProperties(appEditRequest, app);
		// 数据校验
		appService.validApp(app, false);
		// todo 填充默认值
		User loginUser = userService.getLoginUser(request);
		// 重置审核状态
		app.setReviewStatus(ReviewStatusEnum.REVIEWING.getValue());
		// 判断是否存在
		long id = appEditRequest.getId();
		App oldApp = appService.getById(id);
		ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可编辑
		if (!oldApp.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = appService.updateById(app);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	// endregion
	
	/**
	 * 应用审核
	 *
	 * @param reviewRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/review")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> doAppReview(@RequestBody ReviewRequest reviewRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(reviewRequest == null, ErrorCode.PARAMS_ERROR);
		// 取出来请求中需要的属性
		Long id = reviewRequest.getId();
		Integer reviewStatus = reviewRequest.getReviewStatus();
		String reviewMessage = reviewRequest.getReviewMessage();
		ReviewStatusEnum reviewStatusEnum = ReviewStatusEnum.getEnumByValue(reviewStatus);
		
		// 校验
		ThrowUtils.throwIf(id == null || reviewStatusEnum == null, ErrorCode.PARAMS_ERROR);
		// 判断app是否存在
		App odaApp = appService.getById(id);
		ThrowUtils.throwIf(odaApp == null, ErrorCode.NOT_FOUND_ERROR);
		// 判断是否已经审核
		ThrowUtils.throwIf(odaApp.getReviewStatus().equals(reviewStatus), ErrorCode.PARAMS_ERROR, "请勿重复审核");
		// 更新审核状态
		User loginUser = userService.getLoginUser(request);
		App app = new App();
		app.setId(id);
		app.setReviewStatus(reviewStatus);
		app.setReviewMessage(reviewMessage);
		app.setReviewerId(loginUser.getId());
		app.setReviewTime(new Date());
		boolean result = appService.updateById(app);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
}