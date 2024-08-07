package com.stephen.popcorn.model.dto.file;

import java.io.Serializable;

import lombok.Data;

/**
 * 文件上传请求
 *
 * @author stephen qiu
 */
@Data
public class UploadFileRequest implements Serializable {
	
	private static final long serialVersionUID = 6149704783947487687L;
	/**
	 * 业务
	 */
	private String biz;
}