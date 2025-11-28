package uz.zero.shopapp

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean

import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource

import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import org.springframework.web.servlet.support.RequestContextUtils
import java.util.Locale


@Configuration
class WebMvcConfig : WebMvcConfigurer {

    @Bean
    fun localeResolver() = SessionLocaleResolver().apply {
        setDefaultLocale(Locale("uz"))
    }
    @Bean
    fun errorMessageSource() = ResourceBundleMessageSource().apply {
        setDefaultEncoding(Charsets.UTF_8.name())
        setBasename("error")
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(object : AsyncHandlerInterceptor {
            override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

                request.getHeader("hl")?.let {
                    RequestContextUtils.getLocaleResolver(request)
                        ?.setLocale(request, response, Locale(it))
                }
                return true
            }
        })
    }
}
