package com.stephen.popcorn.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 评分策略枚举类
 * 评分策略（0-自定义，1-AI）
 *
 * @author stephen qiu
 */
@Getter
public enum ScoringStrategyEnum {
	
	REVIEWING("自定义", 0),
	PASS("AI", 1);
	private final String text;
	
	private final int value;
	
	ScoringStrategyEnum(String text, int value) {
		this.text = text;
		this.value = value;
	}
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value
	 * @return
	 */
	public static ScoringStrategyEnum getEnumByValue(int value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (ScoringStrategyEnum scoringStrategyEnum : ScoringStrategyEnum.values()) {
			if (scoringStrategyEnum.value == value) {
				return scoringStrategyEnum;
			}
		}
		return null;
	}
}
