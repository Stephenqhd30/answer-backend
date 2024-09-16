package com.stephen.answer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.answer.annotation.AuthCheck;
import com.stephen.answer.common.BaseResponse;
import com.stephen.answer.common.DeleteRequest;
import com.stephen.answer.common.ErrorCode;
import com.stephen.answer.constant.UserConstant;
import com.stephen.answer.exception.BusinessException;
import com.stephen.answer.model.dto.user.*;
import com.stephen.answer.model.entity.User;
import com.stephen.answer.model.vo.LoginUserVO;
import com.stephen.answer.model.vo.UserVO;
import com.stephen.answer.service.UserService;
import com.stephen.answer.utils.ResultUtils;
import com.stephen.answer.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.stephen.answer.constant.SaltConstant.SALT;
import static com.stephen.answer.constant.UserConstant.DEFAULT_PASSWORD;
import static com.stephen.answer.constant.UserConstant.USER_AVATAR;


/**
 * 用户接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
	
	@Resource
	private UserService userService;
	
	
	// region 登录相关
	
	/**
	 * 用户注册
	 *
	 * @param userRegisterRequest 用户注册请求
	 * @return 注册是否成功
	 */
	@PostMapping("/register")
	public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
		if (userRegisterRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String userAccount = userRegisterRequest.getUserAccount();
		String userPassword = userRegisterRequest.getUserPassword();
		String checkPassword = userRegisterRequest.getCheckPassword();
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
			return null;
		}
		long result = userService.userRegister(userAccount, userPassword, checkPassword);
		return ResultUtils.success(result);
	}
	
	/**
	 * 用户登录
	 *
	 * @param userLoginRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/login")
	public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
		if (userLoginRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String userAccount = userLoginRequest.getUserAccount();
		String userPassword = userLoginRequest.getUserPassword();
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
		return ResultUtils.success(loginUserVO);
	}
	
	/**
	 * 用户注销
	 *
	 * @param request
	 * @return
	 */
	@PostMapping("/logout")
	public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
		if (request == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean result = userService.userLogout(request);
		return ResultUtils.success(result);
	}
	
	/**
	 * 获取当前登录用户
	 *
	 * @param request
	 * @return
	 */
	@GetMapping("/get/login")
	public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
		User user = userService.getLoginUser(request);
		return ResultUtils.success(userService.getLoginUserVO(user));
	}
	
	// endregion
	
	// region 增删改查
	
	/**
	 * 创建用户
	 *
	 * @param userAddRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/add")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
		if (userAddRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = new User();
		BeanUtils.copyProperties(userAddRequest, user);
		// 默认密码 12345678
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT + DEFAULT_PASSWORD).getBytes());
		// 设置一个默认的头像
		user.setUserPassword(encryptPassword);
		user.setUserAvatar(USER_AVATAR);
		boolean result = userService.save(user);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(user.getId());
	}
	
	/**
	 * 删除用户
	 *
	 * @param deleteRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean b = userService.removeById(deleteRequest.getId());
		return ResultUtils.success(b);
	}
	
	/**
	 * 更新用户
	 *
	 * @param userUpdateRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/update")
	public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
	                                        HttpServletRequest request) {
		if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = new User();
		BeanUtils.copyProperties(userUpdateRequest, user);
		boolean result = userService.updateById(user);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 根据 id 获取用户（仅管理员）
	 *
	 * @param id 用户id
	 * @param request request
	 * @return
	 */
	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getById(id);
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
		return ResultUtils.success(user);
	}
	
	/**
	 * 根据 id 获取包装类
	 *
	 * @param id 用户id
	 * @param request request
	 * @return 查询得到的用户包装类
	 */
	@GetMapping("/get/vo")
	public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
		BaseResponse<User> response = getUserById(id, request);
		User user = response.getData();
		return ResultUtils.success(userService.getUserVO(user));
	}
	
	/**
	 * 分页获取用户列表（仅管理员）
	 *
	 * @param userQueryRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
	                                               HttpServletRequest request) {
		long current = userQueryRequest.getCurrent();
		long size = userQueryRequest.getPageSize();
		Page<User> userPage = userService.page(new Page<>(current, size),
				userService.getQueryWrapper(userQueryRequest));
		return ResultUtils.success(userPage);
	}
	
	/**
	 * 分页获取用户封装列表
	 *
	 * @param userQueryRequest 用户查询请求
	 * @param request request
	 * @return
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
	                                                   HttpServletRequest request) {
		if (userQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long current = userQueryRequest.getCurrent();
		long size = userQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		Page<User> userPage = userService.page(new Page<>(current, size),
				userService.getQueryWrapper(userQueryRequest));
		Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
		List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
		userVOPage.setRecords(userVO);
		return ResultUtils.success(userVOPage);
	}
	
	// endregion
	
	/**
	 * 更新个人信息
	 *
	 * @param userUpdateMyRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/update/my")
	public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
	                                          HttpServletRequest request) {
		if (userUpdateMyRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		User user = new User();
		BeanUtils.copyProperties(userUpdateMyRequest, user);
		user.setId(loginUser.getId());
		boolean result = userService.updateById(user);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
}
