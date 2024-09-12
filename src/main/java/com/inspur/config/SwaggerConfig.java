package com.inspur.config;

import com.inspur.code.RuntimeEnvironmentStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

/**
 * swagger_ui配置
 * @author: kliu
 * @date: 2022/4/18 20:13
 */
@Configuration
public class SwaggerConfig {
    /**
     * swaggui运行环境及通用token配置
     * @param environment
     * @return springfox.documentation.spring.web.plugins.Docket
     * @author kliu
     * @date 2022/5/24 19:37
     */
    @Bean
    public Docket docket(Environment environment) {
        // 设置要显示的swagger 环境
        Profiles p = Profiles.of(RuntimeEnvironmentStatus.DEV, RuntimeEnvironmentStatus.TEST);
        // 通过environment.acceptsProfiles 判断是否处在自己设定的环境中
        boolean b = environment.acceptsProfiles(p);

        ParameterBuilder ticketPar = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<Parameter>();
        ticketPar.name("token").description("user token")
                .modelRef(new ModelRef("string")).parameterType("header")
                .required(false).build();
        pars.add(ticketPar.build());

        return new Docket(DocumentationType.SWAGGER_2)
                .enable(b)
                .select()
                .apis(RequestHandlerSelectors.any())
                .build()
                .globalOperationParameters(pars)
                .apiInfo(apiInfo());
    }

    /**
     * swaggui-页面显示信息
     * @return ApiInfo
     * @author kliu
     * @date 2022/5/24 19:38
     */
    private ApiInfo apiInfo() {
        //返回一个apiinfo
        return new ApiInfoBuilder()
                .title("工业巡检机器人api接口文档")
                .contact(new Contact("kliu","http://174z011a01.iok.la","kliu@inspur.com"))
                .description("工业巡检机器人")
                .version("1.2.0")
                .build();
    }
}
