package com.gdeiassistant.gdeiassistant.Controller.Wechat;

import com.gdeiassistant.gdeiassistant.Pojo.Entity.User;
import com.gdeiassistant.gdeiassistant.Pojo.Result.JsonResult;
import com.gdeiassistant.gdeiassistant.Pojo.UserLogin.UserCertificate;
import com.gdeiassistant.gdeiassistant.Service.UserData.UserDataService;
import com.gdeiassistant.gdeiassistant.Service.UserLogin.UserLoginService;
import com.gdeiassistant.gdeiassistant.Service.Wechat.WechatUserDataService;
import com.gdeiassistant.gdeiassistant.Tools.HttpClientUtils;
import com.gdeiassistant.gdeiassistant.Tools.StringUtils;
import com.gdeiassistant.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/11/18
 */
@RestController
public class WechatAttachRestController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserDataService userDataService;

    @Autowired
    private WechatUserDataService wechatUserDataService;

    @RequestMapping(value = "/wechat/userattach", method = RequestMethod.POST)
    public JsonResult WechatUserAttach(HttpServletRequest request, @Validated(value = UserLoginValidGroup.class) User user) throws Exception {
        JsonResult result = new JsonResult();
        String wechatUserID = (String) request.getSession().getAttribute("wechatUserID");
        if (StringUtils.isBlank(wechatUserID)) {
            result.setSuccess(false);
            result.setMessage("用户授权已过期，请重新登录并授权");
            return result;
        }
        //清除已登录用户的用户凭证记录
        HttpClientUtils.ClearHttpClientCookieStore(request.getSession().getId());
        UserCertificate userCertificate = userLoginService.UserLogin(request.getSession().getId(), user, true);
        //同步用户教务系统账号信息到数据库
        userDataService.SyncUserData(userCertificate.getUser());
        //同步微信数据
        wechatUserDataService.SyncWechatUserData(userCertificate.getUser().getUsername(), wechatUserID);
        //将用户信息数据写入Session
        request.getSession().setAttribute("username", userCertificate.getUser().getUsername());
        request.getSession().setAttribute("password", userCertificate.getUser().getPassword());
        //异步同步教务系统会话
        userLoginService.AsyncUpdateSession(request);
        return new JsonResult(true);
    }
}
