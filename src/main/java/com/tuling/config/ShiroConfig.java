package com.tuling.config;

import com.tuling.shiro.MyCredentialsMatcher;
import com.tuling.shiro.MyShiroRealm;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.LinkedHashMap;

/**
 * Created by Administrator on 2017/8/2.
 */
@Configuration
public class ShiroConfig {


    /**
     * 配置自定义的权限登录器
     * @param matcher
     * @return
     */
    @Bean(name="authRealm")
    public MyShiroRealm myShiroRealm(@Qualifier("credentialsMatcher") CredentialsMatcher matcher) {
        MyShiroRealm myShiroRealm = new MyShiroRealm();
        myShiroRealm.setCredentialsMatcher(matcher);

        //用户授权/认证信息Cache, 采用EhCache 缓存
        myShiroRealm.setCacheManager(getEhCacheManager());
        return myShiroRealm;
    }

    /**
     * 配置自定义的密码比较器
     * @return
     */
    @Bean(name="credentialsMatcher")
    public CredentialsMatcher credentialsMatcher() {
        return new MyCredentialsMatcher();
    }

    /**
     * 配置核心安全事务管理器
     * @param authRealm
     * 不指定名字的话，自动创建一个方法名第一个字母小写的bean * @Bean(name = "securityManager") * @return
     * @return
     */
    @Bean(name="securityManager")
    public SecurityManager securityManager(@Qualifier("authRealm") MyShiroRealm authRealm){
        DefaultWebSecurityManager manager=new DefaultWebSecurityManager();
        manager.setRealm(authRealm);
        return manager;
    }

    /**
     * 配置shiro的过滤器工厂类
     * @param manager
     * @return
     */
    @Bean(name="shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(@Qualifier("securityManager") SecurityManager manager) {
        ShiroFilterFactoryBean bean=new ShiroFilterFactoryBean();
        bean.setSecurityManager(manager);
        //配置登录的url和登录成功的url和无权限
        bean.setLoginUrl("api/user/login");
        bean.setSuccessUrl("/index");
        bean.setUnauthorizedUrl("/403");
        System.out.println("shiro:过滤");
        //配置访问权限
        LinkedHashMap<String, String> filterChainDefinitionMap=new LinkedHashMap<>();
        filterChainDefinitionMap.put("/api/user/logout", "anon"); //表示可以匿名访问
        filterChainDefinitionMap.put("/druid*//*","anon");
        filterChainDefinitionMap.put("/fs*//*","anon");
        filterChainDefinitionMap.put("/*", "authc");//表示需要认证才可以访问

        bean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return bean;
    }


    /**
     * Shiro生命周期处理器 * @return
     * 保证实现了Shiro内部lifecycle函数的bean执行
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 配置shiro缓存
     * @return
     */
    @Bean
    public EhCacheManager getEhCacheManager() {
        EhCacheManager em = new EhCacheManager();
        em.setCacheManagerConfigFile("classpath:ehcache/ehcache-shiro.xml");
        return em;
    }

    /**
     * 开启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,
     * 并在必要时进行安全逻辑验证 * 配置以下两个bean(DefaultAdvisorAutoProxyCreator(可选)和AuthorizationAttributeSourceAdvisor)即可实现此功能 * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor aasa = new AuthorizationAttributeSourceAdvisor();
        aasa.setSecurityManager(securityManager);
        return aasa;
    }

    @Bean
    @DependsOn({"lifecycleBeanPostProcessor"})
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
        proxyCreator.setProxyTargetClass(true);
        return proxyCreator;
    }

}