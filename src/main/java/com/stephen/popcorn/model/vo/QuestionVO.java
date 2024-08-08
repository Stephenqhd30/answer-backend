package com.stephen.popcorn.model.vo;

import cn.hutool.json.JSONUtil;
import com.stephen.popcorn.model.dto.question.QuestionContentDTO;
import com.stephen.popcorn.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目视图
 *
 * @author stephen
 */
@Data
public class QuestionVO implements Serializable {
	
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * 题目内容（json格式）
	 */
	private List<QuestionContentDTO> questionContent;
	
	/**
	 * 应用 id
	 */
	private Long appId;
	
	/**
	 * 创建用户 id
	 */
	private Long userId;
	
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	/**
	 * 更新时间
	 */
	private Date updateTime;
	
	
	/**
	 * 创建人信息视图
	 */
	private UserVO userVO;
	
	
	/**
	 * 封装类转对象
	 *
	 * @param questionVO
	 * @return
	 */
	public static Question voToObj(QuestionVO questionVO) {
		if (questionVO == null) {
			return null;
		}
		Question question = new Question();
		BeanUtils.copyProperties(questionVO, question);
		List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
		question.setQuestionContent(JSONUtil.toJsonStr(questionContent));
		return question;
	}
	
	/**
	 * 对象转封装类
	 *
	 * @param question
	 * @return
	 */
	public static QuestionVO objToVo(Question question) {
		if (question == null) {
			return null;
		}
		QuestionVO questionVO = new QuestionVO();
		BeanUtils.copyProperties(question, questionVO);
		String questionContent = question.getQuestionContent();
		if (questionContent != null) {
			questionVO.setQuestionContent(JSONUtil.toList(questionContent, QuestionContentDTO.class));
			
		}
		return questionVO;
	}
}