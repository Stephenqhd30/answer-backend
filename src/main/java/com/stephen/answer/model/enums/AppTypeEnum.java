package com.stephen.answer.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

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
	
	private final Integer value;
	
	AppTypeEnum(String text, Integer value) {
		this.text = text;
		this.value = value;
	}
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value
	 * @return
	 */
	public static AppTypeEnum getEnumByValue(Integer value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (AppTypeEnum appTypeEnum : AppTypeEnum.values()) {
			if (Objects.equals(appTypeEnum.value, value)) {
				return appTypeEnum;
			}
		}
		return null;
	}
}
