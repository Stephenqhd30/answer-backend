package com.stephen.answer.model.dto.scoringResult;

import com.stephen.answer.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询评分结果请求
 *
 * @author stephen qiu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScoringResultQueryRequest extends PageRequest implements Serializable {
	
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * 结果名称，如物流师
	 */
	private String resultName;
	
	/**
	 * 结果描述
	 */
	private String resultDesc;
	
	/**
	 * 结果图片
	 */
	private String resultPicture;
	
	/**
	 * 结果属性集合 JSON，如 [I,S,T,J]
	 */
	private String resultProp;
	
	/**
	 * 结果得分范围，如 80，表示 80及以上的分数命中此结果
	 */
	private Integer resultScoreRange;
	
	/**
	 * 创建用户 id
	 */
	private Long userId;
	
	/**
	 * 搜索关键词
	 */
	private String searchText;
	
	private static final long serialVersionUID = 1L;
}