package com.stephen.answer.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

/**
 * 评分策略枚举类
 * 评分策略（0-自定义，1-AI）
 *
 * @author stephen qiu
 */
@Getter
public enum ScoringStrategyEnum {
	
	CUSTOM("自定义", 0),
	AI("AI", 1);
	private final String text;
	
	private final Integer value;
	
	ScoringStrategyEnum(String text, Integer value) {
		this.text = text;
		this.value = value;
	}
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value
	 * @return
	 */
	public static ScoringStrategyEnum getEnumByValue(Integer value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (ScoringStrategyEnum scoringStrategyEnum : ScoringStrategyEnum.values()) {
			if (Objects.equals(scoringStrategyEnum.value, value)) {
				return scoringStrategyEnum;
			}
		}
		return null;
	}
}
