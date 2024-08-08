package com.stephen.popcorn.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新个人信息请求
 *
 * @author stephen qiu
 */
@Data
public class UserUpdateMyRequest implements Serializable {
	
	private static final long serialVersionUID = 402901746420005392L;
	/**
	 * 用户昵称
	 */
	private String userName;
	
	/**
	 * 用户头像
	 */
	private String userAvatar;
	
	/**
	 * 简介
	 */
	private String userProfile;
	
	/**
	 * 用户邮箱
	 */
	private String userEmail;
	
	/**
	 * 手机号码
	 */
	private String userPhone;

}