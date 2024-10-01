package com.stephen.answer.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: stephen qiu
 * @create: 2024-08-09 14:44
 **/
@Data
public class ReviewRequest implements Serializable {
	
	private static final long serialVersionUID = 1973973718590531783L;
	/**
	 * id
	 */
	private Long id;
	
	
	/**
	 * 审核状态 0-待审核 1-通过 2-拒绝
	 */
	private Integer reviewStatus;
	
	
	/**
	 * 审核信息
	 */
	private String reviewMessage;
	
	
	/**
	 * 使用JSON字符串类型来接收批量审核的 id 列表
	 */
	private String idList;
}
