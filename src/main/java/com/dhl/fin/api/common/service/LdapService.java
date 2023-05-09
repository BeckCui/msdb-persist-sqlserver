package com.dhl.fin.api.common.service;

import com.dhl.fin.api.common.exception.LoginException;
import com.dhl.fin.api.common.util.SpringContextUtil;
import com.dhl.fin.api.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author becui
 * @date 8/31/2020
 */
@Slf4j
@Service
public class LdapService {

    private Hashtable getLdapConfig() {
        Hashtable hashEnv = new Hashtable<>();
        String ldapHost = SpringContextUtil.getPropertiesValue("local.ldap.host");
        String ldapPost = SpringContextUtil.getPropertiesValue("local.ldap.post");

        ldapHost = StringUtil.isEmpty(ldapHost) ? "ads.cn.dhl.com" : ldapHost;
        ldapPost = StringUtil.isEmpty(ldapPost) ? "389" : ldapPost;
        hashEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
        hashEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        hashEnv.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":" + ldapPost + "/");
        hashEnv.put("com.sun.jndi.ldap.connect.timeout", "500");
        return hashEnv;
    }

    /**
     * 查询账号信息（邮箱，工号，名字）
     *
     * @return
     * @throws NamingException
     */
    public Map<String, String> queryAccountByUUID(String loginUuid, String loginPwd, String queryUuid) throws NamingException {
        return queryAccount(loginUuid, loginPwd, "cn", queryUuid);
    }


    public Map<String, String> queryAccount(String loginUuid, String loginPwd, String field, String value) throws NamingException {
        Map<String, String> result = new HashMap<>();
        SearchControls constraints = new SearchControls();
        Hashtable hashEnv = getLdapConfig();
        hashEnv.put(Context.SECURITY_PRINCIPAL, "kul-dc\\" + loginUuid);
        hashEnv.put(Context.SECURITY_CREDENTIALS, loginPwd);
        DirContext ctx = new InitialDirContext(hashEnv);
        String[] attributeToSearch = {
                "displayName", "mail", "info", "company", "cn",
                "title", "company", "manager", "description", "telephoneNumber", "department", "userAccountControl",
                "pwdLastSet", "streetaddress", "employeeid", "whencreated", "lastlogon","accountexpires","lastlogontimestamp","lastlogon",
                "employeetype", "dpwncrestcode", "c", "dpwndivision", "dpwnorgtitle", "msDS-UserPasswordExpired", "passwordControl"};
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(null);
        String filter = field + "=" + value;
        String userBase = "DC=kul-dc,DC=dhl,DC=com";

        NamingEnumeration<SearchResult> results;

        results = ctx.search(userBase, filter, constraints);
        while (results != null) {
            SearchResult entry;
            try {
                entry = results.nextElement();
            } catch (Exception e) {
                break;
            }
            if (entry == null) {
                continue;
            }
            String[] line = new String[attributeToSearch.length + 1];
            line[0] = entry.getName();
            for (int i = 1; i <= attributeToSearch.length; i++) {
                NamingEnumeration<?> attr;
                try {
                    attr = entry.getAttributes().get(attributeToSearch[i - 1]).getAll();
                } catch (Exception e) {
                    continue;
                }
                while (attr.hasMore()) {
                    if (line[i] != null) {
                        line[i] = line[i] + ";" + attr.next().toString();
                    } else {
                        line[i] = attr.next().toString();
                    }
                }
                line[i] = entry.getAttributes().get(attributeToSearch[i - 1]).getAll().next().toString();
                result.put(attributeToSearch[i - 1], line[i]);
            }
        }
        return result;
    }


    /**
     * 验证账号密码
     *
     * @param uuid
     * @param password
     * @return
     * @throws Exception
     */
    public Map auth(String uuid, String password) {

        try {
            return queryAccountByUUID(uuid, password, uuid);
        } catch (AuthenticationException e) {
            throw new LoginException("账号或密码错误");
        } catch (javax.naming.CommunicationException e) {
            throw new LoginException("AD域连接失败");
        } catch (Exception e) {
            throw new LoginException("账号或密码错误");
        }
    }

}

