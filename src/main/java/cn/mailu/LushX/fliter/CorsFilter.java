package cn.mailu.LushX.fliter;

import cn.mailu.LushX.common.ResponseCode;
import cn.mailu.LushX.common.ServerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.deploy.net.HttpUtils;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: NULL
 * @Description:解决跨域请求
 * @Date: Create in 2017/11/4 11:47
 */
@Component
public class CorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request= (HttpServletRequest) servletRequest;
        HttpServletResponse response= (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin",request.getHeader("origin"));
        response.setHeader("Access-Control-Allow-Origin","*");  //允许跨域访问的域
        response.setHeader("Access-Control-Allow-Methods","POST,GET,OPTIONS,DELETE");  //允许使用的请求方法
        response.setHeader("Access-Control-Expose-Headers","*");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with,Cache-Control,Pragma,Content-Type,access-token");  //允许使用的请求方法
        response.setHeader("Access-Control-Allow-Credentials","true");//是否允许请求带有验证信息
        if(request.getMethod().equals("OPTIONS")){
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println();
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
