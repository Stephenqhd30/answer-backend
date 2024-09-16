package com.stephen.answer.model.dto.question;

import com.stephen.answer.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询题目请求
 *
 * @author stephen qiu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionQueryRequest extends PageRequest implements Serializable {
	
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * 题目内容（json格式）
	 */
	private String questionContent;
	
	/**
	 * 应用 id
	 */
	private Long appId;
	
	/**
	 * 创建用户 id
	 */
	private Long userId;
	
	
	private static final long serialVersionUID = 1L;
}