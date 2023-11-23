package com.castle.publicremark.dto;

import lombok.Data;

/**
 * @author YuLong
 * Date: 2022/11/16 16:39
 */
@Data
public class LoginFormDTO {
    private String phone;
    private String code;
    private String password;
}
