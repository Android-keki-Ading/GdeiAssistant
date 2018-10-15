package com.gdeiassistant.gdeiassistant.Pojo.UserLogin;

import com.gdeiassistant.gdeiassistant.Enum.Base.LoginResultEnum;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.User;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/10/15
 */

@Component
@Scope("prototype")
public class UserLoginResult {

    private LoginResultEnum loginResultEnum;

    private User user;

    private Long timestamp;

    public LoginResultEnum getLoginResultEnum() {
        return loginResultEnum;
    }

    public void setLoginResultEnum(LoginResultEnum loginResultEnum) {
        this.loginResultEnum = loginResultEnum;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
