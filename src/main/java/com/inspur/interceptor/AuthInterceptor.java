package com.inspur.interceptor;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.inspur.industrialinspection.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author kliu
 * @description 权限控制逻辑
 * @date 2022/4/18 20:30A
 */
@Slf4j
public class AuthInterceptor implements HandlerInterceptor{
	
	@Autowired
	JwtService jwtService;

	/**
	 * 执行业务逻辑时的预处理
	 * @param request
	 * @param response
	 * @param handler
	 * @return boolean
	 * @author kliu
	 * @date 2022/5/24 17:57
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
		String token = request.getHeader("token");
		if (token==null){
			throw new RuntimeException("token不能为空");
		}
		if (!jwtService.isTokenValid(token)){
			throw new TokenExpiredException("用户登录超时，请重新登录");
		}
		return true;
	}
}
