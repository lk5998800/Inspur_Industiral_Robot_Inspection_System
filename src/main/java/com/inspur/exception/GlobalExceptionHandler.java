package com.inspur.exception;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.inspur.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.bind.ValidationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Collectors;

/**
 * 异常处理通用返回
 *
 * @author: kliu
 * @date: 2022/4/18 20:16
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * token过期异常处理
     *
     * @param e
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/5/24 17:56
     */
    @ExceptionHandler(value = TokenExpiredException.class)
    @ResponseBody
    public Result exceptionHandler(TokenExpiredException e) {
        return Result.tokenInvalid();
    }

    /**
     * 空指针异常处理
     *
     * @param e
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/5/24 17:56
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    public Result exceptionHandler(NullPointerException e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log.error(sw.toString());
        return Result.fail("后端处理错误，请联系后端人员解决");
    }

    /**
     * 运行时异常处理
     *
     * @param e
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/5/24 17:56
     */
    @ExceptionHandler(value = RuntimeException.class)
    @ResponseBody
    public Result exceptionHandler(RuntimeException e) {
        return Result.fail(e.getMessage());
    }

    /**
     * 其他异常处理
     *
     * @param e
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/5/24 17:56
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result exceptionHandler(Exception e) {
        if (e instanceof MethodArgumentNotValidException) {
            // BeanValidation exception
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            return Result.fail(ex.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining("; ")));
        } else if (e instanceof BindException) {
            // BeanValidation GET object param
            BindException ex = (BindException) e;
            return Result.fail(ex.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining("; ")));
        } else {
            return Result.fail(e.getMessage());
        }
    }
}
