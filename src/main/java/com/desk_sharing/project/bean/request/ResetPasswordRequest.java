package com.desk_sharing.project.bean.request;

import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    private String oldPassword;
    private String newPassword;
}
