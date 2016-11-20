package org.bumishi.admin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * spring security默认的url授权方式需要预先硬编码在配置中，这里改写默认方式
 * @author qiang.xie
 * @date 2016/9/23
 */

public class UrlSecurityInterceptor extends FilterSecurityInterceptor {

    @Autowired
    @Override
    public void setAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
        super.setAccessDecisionManager(accessDecisionManager);
    }

    @Autowired
    @Override
    public void setAuthenticationManager(AuthenticationManager newManager) {
        super.setAuthenticationManager(newManager);
    }


    @Autowired
    @Override
    public void setSecurityMetadataSource(FilterInvocationSecurityMetadataSource newSource) {
        super.setSecurityMetadataSource(newSource);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        FilterInvocation fi = new FilterInvocation(request, response, chain);
        if (((HttpServletRequest) request).getServletPath().equals("/to-login")) {
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
            return;
        }

        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null || authentication instanceof AnonymousAuthenticationToken){
            //没有认证的，直接就结束
            throw new AuthenticationCredentialsNotFoundException("please login");
        }
        String url=((HttpServletRequest) request).getServletPath();
        request.setAttribute("currentMenu","system");
        if(url.length()>2) {
            url=url.substring(1);
            String currentMenu =url;
            int index=url.indexOf("/");
            if(index>0) {
                currentMenu = url.substring(0, index);
            }
            request.setAttribute("currentMenu",currentMenu);
        }
        String currentUser = authentication.getName();
        if ("root".equalsIgnoreCase(currentUser)) {//不处理root账户的授权
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
        }else{
            InterceptorStatusToken token = super.beforeInvocation(fi);
            //这里对于未配置的资源均通过
            try {
                fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
            } finally {
                super.finallyInvocation(token);
            }

            super.afterInvocation(token, null);
        }
    }

    @Override
    public void destroy() {

    }
}
