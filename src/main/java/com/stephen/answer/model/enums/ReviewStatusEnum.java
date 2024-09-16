package com.stephen.answer.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

/**
 * 审核状态枚举类
 * 审核状态：0-待审核, 1-通过, 2-拒绝
 *
 * @author stephen qiu
 */
@Getter
public enum ReviewStatusEnum {
	
	REVIEWING("待审核", 0),
	PASS("通过", 1),
	REJECT("拒绝", 2);
	private final String text;
	
	private final Integer value;
	
	ReviewStatusEnum(String text, Integer value) {
		this.text = text;
		this.value = value;
	}
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value
	 * @return
	 */
	public static ReviewStatusEnum getEnumByValue(Integer value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (ReviewStatusEnum reviewStatusEnum : ReviewStatusEnum.values()) {
			if (Objects.equals(reviewStatusEnum.value, value)) {
				return reviewStatusEnum;
			}
		}
		return null;
	}
}
