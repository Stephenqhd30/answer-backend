package com.stephen.popcorn.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

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
	
	private final int value;
	
	ReviewStatusEnum(String text, int value) {
		this.text = text;
		this.value = value;
	}
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value
	 * @return
	 */
	public static ReviewStatusEnum getEnumByValue(int value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (ReviewStatusEnum reviewStatusEnum : ReviewStatusEnum.values()) {
			if (reviewStatusEnum.value == value) {
				return reviewStatusEnum;
			}
		}
		return null;
	}
}
