package com.stephen.answer.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.answer.annotation.AuthCheck;
import com.stephen.answer.common.BaseResponse;
import com.stephen.answer.common.DeleteRequest;
import com.stephen.answer.common.ErrorCode;
import com.stephen.answer.constants.UserConstant;
import com.stephen.answer.exception.BusinessException;
import com.stephen.answer.model.dto.userAnswer.UserAnswerAddRequest;
import com.stephen.answer.model.dto.userAnswer.UserAnswerEditRequest;
import com.stephen.answer.model.dto.userAnswer.UserAnswerQueryRequest;
import com.stephen.answer.model.dto.userAnswer.UserAnswerUpdateRequest;
import com.stephen.answer.model.entity.App;
import com.stephen.answer.model.entity.User;
import com.stephen.answer.model.entity.UserAnswer;
import com.stephen.answer.model.vo.UserAnswerVO;
import com.stephen.answer.scoring.ScoringStrategyExecutor;
import com.stephen.answer.service.AppService;
import com.stephen.answer.service.UserAnswerService;
import com.stephen.answer.service.UserService;
import com.stephen.answer.utils.ResultUtils;
import com.stephen.answer.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户答案接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/userAnswer")
@Slf4j
public class UserAnswerController {
	
	@Resource
	private UserAnswerService userAnswerService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private AppService appService;
	
	@Resource
	private ScoringStrategyExecutor scoringStrategyExecutor;
	
	// region 增删改查
	
	/**
	 * 创建用户答案
	 *
	 * @param userAnswerAddRequest userAnswerAddRequset
	 * @param request              request
	 * @return BaseResponse<Long>
	 */
	@PostMapping("/add")
	@Transactional(rollbackFor = Exception.class)
	public BaseResponse<Long> addUserAnswer(@RequestBody UserAnswerAddRequest userAnswerAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(userAnswerAddRequest == null, ErrorCode.PARAMS_ERROR);
		// todo 在此处将实体类和 DTO 进行转换
		UserAnswer userAnswer = new UserAnswer();
		BeanUtils.copyProperties(userAnswerAddRequest, userAnswer);
		List<String> choices = userAnswerAddRequest.getChoices();
		userAnswer.setChoices(JSONUtil.toJsonStr(choices));
		// 数据校验
		userAnswerService.validUserAnswer(userAnswer, true);
		// todo 填充默认值
		User loginUser = userService.getLoginUser(request);
		userAnswer.setUserId(loginUser.getId());
		// 写入数据库
		boolean result = userAnswerService.save(userAnswer);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		// 返回新写入的数据 id
		long newUserAnswerId = userAnswer.getId();
		// 调用评分模块
		Long appId = userAnswerAddRequest.getAppId();
		App app = appService.getById(appId);
		try {
			UserAnswer userAnswerWithResult = scoringStrategyExecutor.doScore(choices, app);
			userAnswerWithResult.setId(newUserAnswerId);
			userAnswerService.updateById(userAnswerWithResult);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "评分错误");
		}
		return ResultUtils.success(newUserAnswerId);
	}
	
	/**
	 * 删除用户答案
	 *
	 * @param deleteRequest deleteRequest
	 * @param request       request
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteUserAnswer(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		UserAnswer oldUserAnswer = userAnswerService.getById(id);
		ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (!oldUserAnswer.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = userAnswerService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 更新用户答案（仅管理员可用）
	 *
	 * @param userAnswerUpdateRequest userAnswerUpdateRequest
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateUserAnswer(@RequestBody UserAnswerUpdateRequest userAnswerUpdateRequest) {
		if (userAnswerUpdateRequest == null || userAnswerUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		UserAnswer userAnswer = new UserAnswer();
		BeanUtils.copyProperties(userAnswerUpdateRequest, userAnswer);
		userAnswer.setChoices(JSONUtil.toJsonStr(userAnswerUpdateRequest.getChoices()));
		// 数据校验
		userAnswerService.validUserAnswer(userAnswer, false);
		// 判断是否存在
		long id = userAnswerUpdateRequest.getId();
		UserAnswer oldUserAnswer = userAnswerService.getById(id);
		ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = userAnswerService.updateById(userAnswer);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 根据 id 获取用户答案（封装类）
	 *
	 * @param id id
	 * @return BaseResponse<UserAnswerVO>
	 */
	@GetMapping("/get/vo")
	public BaseResponse<UserAnswerVO> getUserAnswerVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		UserAnswer userAnswer = userAnswerService.getById(id);
		ThrowUtils.throwIf(userAnswer == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(userAnswerService.getUserAnswerVO(userAnswer, request));
	}
	
	/**
	 * 分页获取用户答案列表（仅管理员可用）
	 *
	 * @param userAnswerQueryRequest
	 * @return
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<UserAnswer>> listUserAnswerByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest) {
		long current = userAnswerQueryRequest.getCurrent();
		long size = userAnswerQueryRequest.getPageSize();
		// 查询数据库
		Page<UserAnswer> userAnswerPage = userAnswerService.page(new Page<>(current, size),
				userAnswerService.getQueryWrapper(userAnswerQueryRequest));
		return ResultUtils.success(userAnswerPage);
	}
	
	/**
	 * 分页获取用户答案列表（封装类）
	 *
	 * @param userAnswerQueryRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<UserAnswerVO>> listUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest,
	                                                               HttpServletRequest request) {
		long current = userAnswerQueryRequest.getCurrent();
		long size = userAnswerQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<UserAnswer> userAnswerPage = userAnswerService.page(new Page<>(current, size),
				userAnswerService.getQueryWrapper(userAnswerQueryRequest));
		// 获取封装类
		return ResultUtils.success(userAnswerService.getUserAnswerVOPage(userAnswerPage, request));
	}
	
	/**
	 * 分页获取当前登录用户创建的用户答案列表
	 *
	 * @param userAnswerQueryRequest userAnswerQueryRequest
	 * @param request                request
	 * @return BaseResponse<Page < UserAnswerVO>>
	 */
	@PostMapping("/my/list/page/vo")
	public BaseResponse<Page<UserAnswerVO>> listMyUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest,
	                                                                 HttpServletRequest request) {
		ThrowUtils.throwIf(userAnswerQueryRequest == null, ErrorCode.PARAMS_ERROR);
		// 补充查询条件，只查询当前登录用户的数据
		User loginUser = userService.getLoginUser(request);
		userAnswerQueryRequest.setUserId(loginUser.getId());
		long current = userAnswerQueryRequest.getCurrent();
		long size = userAnswerQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<UserAnswer> userAnswerPage = userAnswerService.page(new Page<>(current, size),
				userAnswerService.getQueryWrapper(userAnswerQueryRequest));
		// 获取封装类
		return ResultUtils.success(userAnswerService.getUserAnswerVOPage(userAnswerPage, request));
	}
	
	/**
	 * 编辑用户答案（给用户使用）
	 *
	 * @param userAnswerEditRequest userAnswerEditRequest
	 * @param request               request
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editUserAnswer(@RequestBody UserAnswerEditRequest userAnswerEditRequest, HttpServletRequest request) {
		if (userAnswerEditRequest == null || userAnswerEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		UserAnswer userAnswer = new UserAnswer();
		BeanUtils.copyProperties(userAnswerEditRequest, userAnswer);
		userAnswer.setChoices(JSONUtil.toJsonStr(userAnswerEditRequest.getChoices()));
		
		// 数据校验
		userAnswerService.validUserAnswer(userAnswer, false);
		User loginUser = userService.getLoginUser(request);
		// 判断是否存在
		long id = userAnswerEditRequest.getId();
		UserAnswer oldUserAnswer = userAnswerService.getById(id);
		ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可编辑
		if (!oldUserAnswer.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = userAnswerService.updateById(userAnswer);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	// endregion
}
