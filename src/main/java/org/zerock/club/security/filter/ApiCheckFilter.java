package org.zerock.club.security.filter;

import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.zerock.club.security.util.JWTUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static jdk.nashorn.internal.objects.NativeString.match;

@Log4j2
public class ApiCheckFilter extends OncePerRequestFilter {

    private AntPathMatcher antPathMatcher;
    private String pattern;
    private JWTUtil jwtUtil;

    public ApiCheckFilter(String pattern, JWTUtil jwtUtil){
        this.antPathMatcher = new AntPathMatcher();
        this.pattern = pattern;
        this.jwtUtil = jwtUtil;
        //class의 jwtUtil = 매개변수의 jwtUtil
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("REQUEST URI: " + request.getRequestURI());
        boolean  result = antPathMatcher.match(pattern, request.getRequestURI());
        log.info("antPathMatcher.match(): " + result);
        if (result) {
            log.info("ApiCheckFilter.................................................");
            log.info("ApiCheckFilter.................................................");
            log.info("ApiCheckFilter.................................................");
            boolean checkHeader = checkAuthHeader(request);

            if (checkHeader) {
                filterChain.doFilter(request,response);
                return;
            } else{
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); //거부된 것을 response에 알림
                // json 리턴 및 한글깨짐 수정.
                response.setContentType("application/json;charset=utf-8");
                JSONObject json = new JSONObject();
                String message = "FAIL CHECK API TOKEN";
                json.put("code", "403");
                json.put("message", message);

                PrintWriter out = response.getWriter(); //클라이언트에게 입출력
                out.print(json);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean checkAuthHeader(HttpServletRequest request) {
        boolean checkResult = false;
        String authHeader = request.getHeader("Authorization");
        if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer")){
            log.info("Authorization exist: " + authHeader);
            /*
            if(authHeader.equals("12345678")){
                checkResult = true;
            }*/
            try { //값이 추출되면 email에 담김
                String email = jwtUtil.validateAndExtract(authHeader.substring(7));
                log.info("validate result: " + email);
                checkResult = email.length() > 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return checkResult;
    }
}
