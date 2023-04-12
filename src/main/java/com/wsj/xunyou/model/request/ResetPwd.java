package com.wsj.xunyou.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author yupi
 */
@Data
public class ResetPwd implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String checkCode;
}
