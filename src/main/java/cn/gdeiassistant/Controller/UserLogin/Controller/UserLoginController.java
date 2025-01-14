package cn.gdeiassistant.Controller.UserLogin.Controller;

import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.Redirect.RedirectInfo;
import cn.gdeiassistant.Service.UserLogin.AutoLoginService;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import cn.gdeiassistant.Service.UserLogin.UserLoginService;
import cn.gdeiassistant.Tools.Utils.HttpClientUtils;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class UserLoginController {

    private final int AUTOLOGIN_NOT = 0;

    private final int AUTOLOGIN_SESSION = 1;

    private final int AUTOLOGIN_COOKIE = 2;

    @Autowired
    private AutoLoginService autoLoginService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserCertificateService userCertificateService;

    /**
     * 用户登录，若自动登录失败，则进入登录界面
     *
     * @param request
     * @param response
     * @param redirectInfo
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/login")
    public ModelAndView DoDispatch(HttpServletRequest request, HttpServletResponse response
            , RedirectInfo redirectInfo) throws Exception {
        //检查自动登录状态
        ModelAndView modelAndView = new ModelAndView();
        int autoLoginState = autoLoginService.CheckAutoLogin(request);
        if (autoLoginState == AUTOLOGIN_NOT) {
            //不自动登录,返回登录主页
            if (redirectInfo.needToRedirect()) {
                modelAndView.addObject("RedirectURL", redirectInfo.getRedirect_url());
            }
            modelAndView.setViewName("Login/login");
        } else if (autoLoginState == AUTOLOGIN_COOKIE) {
            //获取Cookie中的用户信息自动登录
            Cookie[] cookies = request.getCookies();
            String cookieUsername = "";
            String cookiePassword = "";
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("username")) {
                    cookieUsername = cookie.getValue();
                }
                if (cookie.getName().equals("password")) {
                    cookiePassword = cookie.getValue();
                }
            }
            //将用户信息进行解密
            cookieUsername = StringEncryptUtils.decryptString(cookieUsername);
            cookiePassword = StringEncryptUtils.decryptString(cookiePassword);
            //清除已登录用户的用户凭证记录
            HttpClientUtils.ClearHttpClientCookieStore(request.getSession().getId());
            //进行用户登录
            userLoginService.UserLogin(request.getSession().getId(), cookieUsername, cookiePassword);
            //异步地与教务系统会话进行同步
            userCertificateService.AsyncUpdateSessionCertificate(request.getSession().getId()
                    , new User(cookieUsername, cookiePassword));
            //将加密的用户信息保存到Cookie中
            Cookie usernameCookie = new Cookie("username", StringEncryptUtils
                    .encryptString(cookieUsername));
            Cookie passwordCookie = new Cookie("password", StringEncryptUtils
                    .encryptString(cookiePassword));
            //设置Cookie最大有效时间为3个月
            usernameCookie.setMaxAge(7776000);
            usernameCookie.setPath("/");
            usernameCookie.setHttpOnly(true);
            passwordCookie.setMaxAge(7776000);
            passwordCookie.setPath("/");
            passwordCookie.setHttpOnly(true);
            response.addCookie(usernameCookie);
            response.addCookie(passwordCookie);
            if (redirectInfo.needToRedirect()) {
                modelAndView.setViewName("redirect:" + redirectInfo.getRedirect_url());
            } else {
                modelAndView.setViewName("redirect:/index");
            }
        } else {
            if (redirectInfo.needToRedirect()) {
                modelAndView.addObject("RedirectURL", redirectInfo.getRedirect_url());
                modelAndView.setViewName("Login/login");
            } else {
                modelAndView.setViewName("redirect:/index");
            }
        }
        return modelAndView;
    }

}
