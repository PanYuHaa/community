package com.nowcoder.community.controller.Interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断这个Handler是不是一个方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            // 获取该Handler也就是标注过@RequestMapping的头上的@LoginRequired注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            // 如果@LoginRequired非空，那么则说明他被注解了，需要登陆才可以
            if (loginRequired != null && hostHolder.getUser() == null) {
                // 上述条件为如果它需要登陆，但是没有获取到登陆的当前用户时，则错误。进行重定向到登陆界面
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
