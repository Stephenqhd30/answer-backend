package com.stephen.popcorn.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: stephen qiu
 * @create: 2024-08-08 14:17
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionContentDTO {
	
	/**
	 * 题目标题
	 */
	private String title;
	
	/**
	 * 题目选项列表
	 */
	private List<QuestionOption> questionOptionList;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class QuestionOption {
		
		/**
		 * 结果
		 */
		private String result;
		
		/**
		 * 得分
		 */
		private int score;
		
		/**
		 * 用户选项
		 */
		private String value;
		
		/**
		 * 正确选项
		 */
		private String key;
	}
}

