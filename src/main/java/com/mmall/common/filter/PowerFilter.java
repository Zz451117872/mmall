package com.mmall.common.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by aa on 2017/7/2.
 */
public class PowerFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
       HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.addHeader("Access-Control-Allow-Origin","http://127.0.0.1:8020");
        response.addHeader("Access-Control-Allow-Credentials","true");
        response.addHeader("Access-Control-Allow-Headers","x-requested-with,content-type,Access-Control-Allow-Credentials");
        System.out.println("cros过滤器");
        filterChain.doFilter(servletRequest,response);
    }

    @Override
    public void destroy() {

    }
}
