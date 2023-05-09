package com.dhl.fin.api.common.util;

import com.github.tuupertunut.powershelllibjava.PowerShell;
import com.github.tuupertunut.powershelllibjava.PowerShellExecutionException;

import java.io.IOException;
import java.util.List;

public class WindowsUtil {


    /**
     * 获取AD账号的过期时间
     *
     * @param account
     * @return
     * @throws PowerShellExecutionException
     * @throws IOException
     */
    public static String getADUserExpireDate(String account) throws PowerShellExecutionException, IOException {
        if (StringUtil.isNotEmpty(account)) {
            PowerShell psSession = PowerShell.open();
            String script = String.format("((Get-ADUser -Identity %s -server kul-dc.dhl.com -Properties msDS-UserPasswordExpiryTimeComputed).'msDS-UserPasswordExpiryTimeComputed' | ForEach-Object -Process {[datetime]::FromFileTime($_)}).tostring('yyyy-MM-dd HH:mm:ss') ", account);
            return psSession.executeCommands(script);
        }
        return null;
    }

    public static String getADUserLastLoginDate(String account) throws PowerShellExecutionException, IOException {
        if (StringUtil.isNotEmpty(account)) {
            PowerShell psSession = PowerShell.open();
            String script = String.format("((Get-ADUser -Identity  %s -Properties lastLogon).'lastLogon' | ForEach-Object -Process {[datetime]::FromFileTime($_)}).tostring('yyyy-MM-dd HH:mm:ss') ", account);
            return psSession.executeCommands(script);
        }
        return null;
    }


    /**
     * 设置AD账号的新密码
     *
     * @param account
     * @param oldPassword
     * @param newPassword
     * @return
     * @throws PowerShellExecutionException
     * @throws IOException
     */
    public static String setADUserNewPWD(String account, String oldPassword, String newPassword) throws PowerShellExecutionException, IOException {
        if (StringUtil.isNotEmpty(account) || StringUtil.isNotEmpty(oldPassword) || StringUtil.isNotEmpty(newPassword)) {
            PowerShell psSession = PowerShell.open();
            String script = String.format("Set-ADAccountPassword -Identity %s -OldPassword (ConvertTo-SecureString -AsPlainText \"%s\" -Force) -NewPassword (ConvertTo-SecureString -AsPlainText \"%s\" -Force)", account, oldPassword, newPassword);
            return psSession.executeCommands(script);
        }
        return null;
    }


    /**
     * 获取Group 的成员列表
     *
     * @return
     * @throws PowerShellExecutionException
     * @throws IOException
     */
    public static List<String> getGroupMember(String groupName) throws PowerShellExecutionException, IOException {
        if (StringUtil.isNotEmpty(groupName)) {
            PowerShell psSession = PowerShell.open();
            String script = String.format("(Get-ADGroupMember  -Identity '" + groupName + "' | Where-Object {$_.distinguishedName -like '*OU=CN*'} | ForEach-Object { $_.name } ) -join ';'");
            String result = psSession.executeCommands(script);

            if (StringUtil.isEmpty(result)) {
                return null;
            } else {
                return ArrayUtil.arrayToList(result.split(";"));
            }
        }
        return null;
    }


}






