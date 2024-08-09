package com.stephen.popcorn.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 应用类型枚举类
 * 应用类型（0-得分类，1-测评类）
 *
 * @author stephen qiu
 */
@Getter
public enum AppTypeEnum {
	
	
	SCORE("得分类", 0),
	TEST("测评类", 1);
	
	private final String text;
	
	private final int value;
	
	AppTypeEnum(String text, int value) {
		this.text = text;
		this.value = value;
	}
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value
	 * @return
	 */
	public static AppTypeEnum getEnumByValue(int value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (AppTypeEnum appTypeEnum : AppTypeEnum.values()) {
			if (appTypeEnum.value == value) {
				return appTypeEnum;
			}
		}
		return null;
	}
}
